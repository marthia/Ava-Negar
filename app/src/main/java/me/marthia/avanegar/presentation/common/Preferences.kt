//package me.marthia.avanegar.presentation.common
//
//import android.content.Context
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.preferencesDataStore
//import dagger.hilt.android.qualifiers.ApplicationContext
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.flow.map
//import java.io.IOException
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class Preferences @Inject constructor(@ApplicationContext context: Context) {
//    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_pref")
//
//    private object PreferencesKey {
//        val currentModel = stringPreferencesKey(name = "access_token")
//    }
//
//    private val dataStore = context.dataStore
//
//    suspend fun setCurrentModel(token: String) {
//        dataStore.edit { preferences ->
//            preferences[PreferencesKey.accessTokenKey] = token
//        }
//    }
//
//    val accessToken: Flow<String> = dataStore.data
//        .catch { exception ->
//            if (exception is IOException) {
//                emit(emptyPreferences())
//            } else {
//                throw exception
//            }
//        }
//        .map { preferences ->
//            preferences[PreferencesKey.currentModel] ?: ""
//        }
//}