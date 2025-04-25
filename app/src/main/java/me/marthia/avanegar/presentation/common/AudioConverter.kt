package me.marthia.avanegar.presentation.common

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioConverter {
    companion object {
        private const val TAG = "AudioConverter"
        
        /**
         * Converts an audio file (MP3, AAC, etc.) to a WAV file suitable for vosk
         * @param inputPath Path to input audio file
         * @param outputPath Path where the WAV file should be saved
         * @return true if conversion successful, false otherwise
         */
        fun convertToWav(inputPath: String, outputPath: String): Boolean {
            try {
                val extractor = MediaExtractor()
                extractor.setDataSource(inputPath)
                
                // Find the first audio track
                val audioTrackIndex = selectAudioTrack(extractor)
                if (audioTrackIndex < 0) {
                    Log.e(TAG, "No audio track found in the file")
                    return false
                }
                
                extractor.selectTrack(audioTrackIndex)
                val format = extractor.getTrackFormat(audioTrackIndex)
                
                // Create decoder
                val mime = format.getString(MediaFormat.KEY_MIME) ?: return false
                val decoder = MediaCodec.createDecoderByType(mime)
                decoder.configure(format, null, null, 0)
                decoder.start()
                
                // Create output file and write WAV header
                val outputFile = File(outputPath)
                if (!outputFile.parentFile?.exists()!!) {
                    outputFile.parentFile?.mkdirs()
                }
                
                val outputStream = FileOutputStream(outputFile)
                
                // Default values for WAV with Vosk
                val sampleRate = 16000
                val channels = 1
                val bitsPerSample = 16
                
                // Write placeholder WAV header (will update after decoding)
                writeWavHeader(outputStream, 0, sampleRate, channels, bitsPerSample)
                
                // Process and decode the audio
                val bufferInfo = MediaCodec.BufferInfo()
                val bufferSize = 8192 // Buffer size
                val pcmData = ByteArray(bufferSize)
                var totalBytesWritten = 0
                var sawInputEOS = false
                var sawOutputEOS = false
                
                // Resampling buffer if needed
                val resampler = Resampler()
                
                while (!sawOutputEOS) {
                    // Feed input
                    if (!sawInputEOS) {
                        val inputBufferIndex = decoder.dequeueInputBuffer(10000)
                        if (inputBufferIndex >= 0) {
                            val inputBuffer = decoder.getInputBuffer(inputBufferIndex)
                            inputBuffer?.clear()
                            
                            val sampleSize = if (inputBuffer != null) 
                                extractor.readSampleData(inputBuffer, 0) else -1
                            
                            if (sampleSize < 0) {
                                decoder.queueInputBuffer(
                                    inputBufferIndex, 0, 0, 
                                    0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                                sawInputEOS = true
                            } else {
                                decoder.queueInputBuffer(
                                    inputBufferIndex, 0, sampleSize,
                                    extractor.sampleTime, 0
                                )
                                extractor.advance()
                            }
                        }
                    }
                    
                    // Get output
                    val outputBufferIndex = decoder.dequeueOutputBuffer(bufferInfo, 10000)
                    if (outputBufferIndex >= 0) {
                        val outputBuffer = decoder.getOutputBuffer(outputBufferIndex)
                        if (outputBuffer != null) {
                            val outBuffer = ByteArray(bufferInfo.size)
                            outputBuffer.get(outBuffer)
                            
                            // Convert to 16kHz mono if needed
                            val sourceSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            val sourceChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            
                            if (sourceSampleRate != sampleRate || sourceChannelCount != channels) {
                                val convertedData = resampler.resample(
                                    outBuffer, 
                                    sourceSampleRate, 
                                    sourceChannelCount,
                                    sampleRate, 
                                    channels
                                )
                                outputStream.write(convertedData)
                                totalBytesWritten += convertedData.size
                            } else {
                                outputStream.write(outBuffer)
                                totalBytesWritten += outBuffer.size
                            }
                        }
                        
                        decoder.releaseOutputBuffer(outputBufferIndex, false)
                        
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEOS = true
                        }
                    }
                }
                
                // Update WAV header with final size
                outputStream.close()
                updateWavHeader(outputPath, totalBytesWritten)
                
                // Clean up
                decoder.stop()
                decoder.release()
                extractor.release()
                
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error converting audio: ${e.message}")
                e.printStackTrace()
                return false
            }
        }
        
        /**
         * Select the first audio track from the media file
         */
        private fun selectAudioTrack(extractor: MediaExtractor): Int {
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    return i
                }
            }
            return -1
        }
        
        /**
         * Write WAV header to the output file
         */
        private fun writeWavHeader(
            outputStream: FileOutputStream,
            dataSize: Int,
            sampleRate: Int,
            channels: Int,
            bitsPerSample: Int
        ) {
            val byteRate = sampleRate * channels * (bitsPerSample / 8)
            val blockAlign = channels * (bitsPerSample / 8)
            
            outputStream.apply {
                // RIFF header
                write("RIFF".toByteArray()) // ChunkID
                writeInt(36 + dataSize)     // ChunkSize (placeholder)
                write("WAVE".toByteArray()) // Format
                
                // fmt subchunk
                write("fmt ".toByteArray()) // Subchunk1ID
                writeInt(16)                // Subchunk1Size (16 for PCM)
                writeShort(1)               // AudioFormat (1 for PCM)
                writeShort(channels)        // NumChannels
                writeInt(sampleRate)        // SampleRate
                writeInt(byteRate)          // ByteRate
                writeShort(blockAlign)      // BlockAlign
                writeShort(bitsPerSample)   // BitsPerSample
                
                // data subchunk
                write("data".toByteArray()) // Subchunk2ID
                writeInt(dataSize)          // Subchunk2Size (placeholder)
            }
        }
        
        /**
         * Update the WAV header with the actual data size
         */
        private fun updateWavHeader(wavFilePath: String, dataSize: Int) {
            val file = File(wavFilePath)
            val raf = file.randomAccessFile("rw")
            
            // Update ChunkSize (4 bytes at position 4)
            raf.seek(4)
            raf.writeInt(36 + dataSize)
            
            // Update Subchunk2Size (4 bytes at position 40)
            raf.seek(40)
            raf.writeInt(dataSize)
            
            raf.close()
        }
        
        /**
         * Utility extension to write an int to FileOutputStream
         */
        private fun FileOutputStream.writeInt(value: Int) {
            write(value and 0xFF)
            write((value shr 8) and 0xFF)
            write((value shr 16) and 0xFF)
            write((value shr 24) and 0xFF)
        }
        
        /**
         * Utility extension to write a short to FileOutputStream
         */
        private fun FileOutputStream.writeShort(value: Int) {
            write(value and 0xFF)
            write((value shr 8) and 0xFF)
        }
        
        /**
         * Extension to open RandomAccessFile
         */
        private fun File.randomAccessFile(mode: String) = java.io.RandomAccessFile(this, mode)
        
        /**
         * Get a temporary WAV file path in the app's cache directory
         */
        fun getTempWavFilePath(context: Context, originalFilePath: String): String {
            val cacheDir = context.cacheDir
            val originalFile = File(originalFilePath)
            val baseName = originalFile.nameWithoutExtension
            return File(cacheDir, "$baseName.wav").absolutePath
        }
    }
    
    /**
     * Simple audio resampler for converting between sample rates and channel counts
     */
    private class Resampler {
        fun resample(
            data: ByteArray,
            sourceSampleRate: Int,
            sourceChannels: Int,
            targetSampleRate: Int,
            targetChannels: Int
        ): ByteArray {
            // Convert byte array to short array (assuming 16-bit audio)
            val shortBuffer = ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN)
                .asShortBuffer()
            val shorts = ShortArray(shortBuffer.remaining())
            shortBuffer.get(shorts)
            
            // First handle channel conversion (e.g., stereo to mono)
            val monoShorts = if (sourceChannels > 1 && targetChannels == 1) {
                ShortArray(shorts.size / sourceChannels) { i ->
                    var sum = 0
                    for (ch in 0 until sourceChannels) {
                        sum += shorts[i * sourceChannels + ch]
                    }
                    (sum / sourceChannels).toShort()
                }
            } else {
                shorts
            }
            
            // Then handle sample rate conversion (simple linear interpolation)
            val ratio = sourceSampleRate.toDouble() / targetSampleRate
            val resampledLength = (monoShorts.size / ratio).toInt()
            val resampledShorts = ShortArray(resampledLength)
            
            for (i in resampledShorts.indices) {
                val position = i * ratio
                val index = position.toInt()
                val fraction = position - index
                
                if (index < monoShorts.size - 1) {
                    val value = monoShorts[index] * (1 - fraction) + 
                                monoShorts[index + 1] * fraction
                    resampledShorts[i] = value.toInt().toShort()
                } else if (index < monoShorts.size) {
                    resampledShorts[i] = monoShorts[index]
                }
            }
            
            // Convert shorts back to bytes
            val outputBuffer = ByteBuffer.allocate(resampledShorts.size * 2)
                .order(ByteOrder.LITTLE_ENDIAN)
            val shortOutBuffer = outputBuffer.asShortBuffer()
            shortOutBuffer.put(resampledShorts)
            
            return outputBuffer.array()
        }
    }
}