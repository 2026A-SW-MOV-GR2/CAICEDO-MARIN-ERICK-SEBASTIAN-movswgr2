package com.example.redyseguridad.ui.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Extensión para acceder al DataStore de forma singleton en toda la app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_storage")

/**
 * ViewModel para la pantalla de Almacenamiento Seguro.
 * Gestiona tres tipos de almacenamiento: SharedPreferences, DataStore y EncryptedSharedPreferences.
 * Usa Coroutines para operaciones de I/O en Dispatchers.IO.
 */
class StorageViewModel : ViewModel() {

    // StateFlow que emite el valor recuperado del almacenamiento
    private val _retrievedValue = MutableStateFlow<String?>(null)
    val retrievedValue: StateFlow<String?> = _retrievedValue

    /**
     * SHARED PREFERENCES
     * Guarda un valor en SharedPreferences (almacenamiento no cifrado).
     *
     * @param context Contexto de la aplicación
     * @param key Clave para identificar el valor
     * @param value El valor a guardar
     */
    fun saveToSharedPrefs(context: Context, key: String, value: String) {
        // Obtener la instancia default de SharedPreferences
        val sharedPref = context.getSharedPreferences(
            "com.example.redyseguridad.prefs",
            Context.MODE_PRIVATE
        )
        // Guardar el valor con la clave proporcionada
        sharedPref.edit().putString(key, value).apply()
    }

     /**
      * Recupera un valor de SharedPreferences.
      *
      * @param context Contexto de la aplicación
      * @param key Clave para identificar el valor
      * @return El valor guardado o null si no existe
      */
     fun getFromSharedPrefs(context: Context, key: String): String? {
         val sharedPref = context.getSharedPreferences(
             "com.example.redyseguridad.prefs",
             Context.MODE_PRIVATE
         )
         val value = sharedPref.getString(key, null)
         // También emitir en el StateFlow para mantener consistencia
         _retrievedValue.value = value
         return value
     }

    /**
     * JETPACK DATASTORE
     * Guarda un valor en Jetpack DataStore (almacenamiento con prefs).
     * Usa una coroutine en el viewModelScope que corre en Dispatchers.IO.
     *
     * @param context Contexto de la aplicación
     * @param key Clave para identificar el valor
     * @param value El valor a guardar
     */
    fun saveToDataStore(context: Context, key: String, value: String) {
        // Lanzar coroutine en viewModelScope
        viewModelScope.launch {
            try {
                // Crear una clave de preferencias para almacenar el valor
                val dataStoreKey = stringPreferencesKey(key)

                // Editar el DataStore de forma segura con edit()
                context.dataStore.edit { preferences ->
                    preferences[dataStoreKey] = value
                }
            } catch (e: Exception) {
                // En caso de error, simplemente registrarlo (no se propaga)
                e.printStackTrace()
            }
        }
    }

     /**
      * Recupera un valor de Jetpack DataStore.
      * Lee del Flow y emite el valor en retrievedValue StateFlow una sola vez.
      *
      * @param context Contexto de la aplicación
      * @param key Clave para identificar el valor
      */
     fun getFromDataStore(context: Context, key: String) {
         // Lanzar coroutine en viewModelScope
         viewModelScope.launch {
             try {
                 // Crear una clave de preferencias
                 val dataStoreKey = stringPreferencesKey(key)

                 // Limpiar el valor anterior antes de buscar el nuevo
                 _retrievedValue.value = null

                 // Recopilar el Flow del DataStore, pero solo tomar el primer valor
                 context.dataStore.data.collect { preferences ->
                     val value = preferences[dataStoreKey]
                     // Emitir el valor en el StateFlow
                     _retrievedValue.value = value
                 }
             } catch (e: Exception) {
                 // En caso de error, emitir null
                 _retrievedValue.value = null
                 e.printStackTrace()
             }
         }
     }

    /**
     * ENCRYPTED SHARED PREFERENCES
     * Guarda un valor en EncryptedSharedPreferences (almacenamiento cifrado con AES).
     * Utiliza MasterKey para generar la clave de cifrado.
     *
     * @param context Contexto de la aplicación
     * @param key Clave para identificar el valor
     * @param value El valor a guardar
     */
    fun saveToEncrypted(context: Context, key: String, value: String) {
        try {
            // Crear o recuperar la MasterKey con la clave maestra de la aplicación
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Obtener la instancia de EncryptedSharedPreferences
            val encryptedSharedPref = EncryptedSharedPreferences.create(
                context,
                "com.example.redyseguridad.encrypted_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // Guardar el valor cifrado con la clave proporcionada
            encryptedSharedPref.edit().putString(key, value).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

     /**
      * Recupera un valor de EncryptedSharedPreferences.
      *
      * @param context Contexto de la aplicación
      * @param key Clave para identificar el valor
      * @return El valor descifrado o null si no existe
      */
     fun getFromEncrypted(context: Context, key: String): String? {
         return try {
             // Crear o recuperar la MasterKey
             val masterKey = MasterKey.Builder(context)
                 .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                 .build()

             // Obtener la instancia de EncryptedSharedPreferences
             val encryptedSharedPref = EncryptedSharedPreferences.create(
                 context,
                 "com.example.redyseguridad.encrypted_prefs",
                 masterKey,
                 EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                 EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
             )

             // Recuperar e desencriptar el valor
             val value = encryptedSharedPref.getString(key, null)
             // También emitir en el StateFlow para mantener consistencia
             _retrievedValue.value = value
             value
         } catch (e: Exception) {
             _retrievedValue.value = null
             null
         }
     }

    /**
     * Limpia el StateFlow de valores recuperados.
     */
    fun clearRetrievedValue() {
        _retrievedValue.value = null
    }
}




