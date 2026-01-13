package ita.tech.vpn.dataStore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ita.tech.vpn.state.VPNStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StoreVPN( private val context: Context) {

    companion object{
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("VPN")
        // Estatus
        val BAND_CREACION_KEYS           = booleanPreferencesKey("band_creacion_keys")
        val BAND_ENVIO_DATOS             = booleanPreferencesKey("band_envio_datos")
        val BAND_RECEPCION_CONFIGURACION = booleanPreferencesKey("band_recepcion_configuracion")
        val BAND_CONFIGURACION           = booleanPreferencesKey("band_configuracion")

        // Datos dispositivo
        val ID_DISPOSITIVO = stringPreferencesKey("id_dispositivo")
        val PRIVATE_KEY    = stringPreferencesKey("private_key")
        val PUBLIC_KEY     = stringPreferencesKey("public_key")

        // Datos Interfaz
        val INTERFACE_ADDRESS         = stringPreferencesKey("interface_address")
        val INTERFACE_DNS             = stringPreferencesKey("interface_dns")
        val PEER_PUBLIC_KEY           = stringPreferencesKey("peer_public_key")
        val PEER_PRESHARED_KEY        = stringPreferencesKey("peer_preshared_key")
        val PEER_ALLOWED_IPS          = stringPreferencesKey("peer_allowed_ips")
        val PEER_ENDPOINT             = stringPreferencesKey("peer_endpoint")
        val PEER_PERSISTENT_KEEPALIVE = stringPreferencesKey("peer_persistent_keepalive")

    }

    val getBandCreacionKeys: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BAND_CREACION_KEYS] ?: false
        }

    suspend fun saveBandCreacionKeys( value: Boolean ){
        context.dataStore.edit { preferences ->
            preferences[BAND_CREACION_KEYS] = value
        }
    }

    val getBandEnvioDatos: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BAND_ENVIO_DATOS] ?: false
        }

    suspend fun saveBandEnvioDatos( value: Boolean ){
        context.dataStore.edit { preferences ->
            preferences[BAND_ENVIO_DATOS] = value
        }
    }

    val getBandRecepcionConfiguracion: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BAND_RECEPCION_CONFIGURACION] ?: false
        }

    suspend fun saveBandRecepcionConfiguracion( value: Boolean ){
        context.dataStore.edit { preferences ->
            preferences[BAND_RECEPCION_CONFIGURACION] = value
        }
    }
    val getBandConfiguracion: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[BAND_CONFIGURACION] ?: false
        }
    suspend fun saveBandConfiguracion( value: Boolean ){
        context.dataStore.edit { preferences ->
            preferences[BAND_CONFIGURACION] = value
        }
    }

    val getIdDispositivo: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[ID_DISPOSITIVO] ?: ""
        }

    suspend fun saveIdDispositivo( value: String ){
        context.dataStore.edit { preferences ->
            preferences[ID_DISPOSITIVO] = value
        }
    }

    val getPrivateKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PRIVATE_KEY] ?: ""
        }

    suspend fun savePrivateKey( value: String ){
        context.dataStore.edit { preferences ->
            preferences[PRIVATE_KEY] = value
        }
    }

    val getPublicKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PUBLIC_KEY] ?: ""
        }

    suspend fun savePublicKey( value: String ){
        context.dataStore.edit { preferences ->
            preferences[PUBLIC_KEY] = value
        }
    }

    val getInterfaceAddress: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[INTERFACE_ADDRESS] ?: ""
        }

    suspend fun saveInterfaceAddress( value: String ){
        context.dataStore.edit { preferences ->
            preferences[INTERFACE_ADDRESS] = value
        }
    }


    val getInterfaceDns: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[INTERFACE_DNS] ?: ""
        }

    suspend fun saveInterfaceDns( value: String ){
        context.dataStore.edit { preferences ->
            preferences[INTERFACE_DNS] = value
        }
    }


    val getPeerPublicKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PEER_PUBLIC_KEY] ?: ""
        }

    suspend fun savePeerPublicKey( value: String ){
        context.dataStore.edit { preferences ->
            preferences[PEER_PUBLIC_KEY] = value
        }
    }

    val getPeerPresharedKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PEER_PRESHARED_KEY] ?: ""
        }

    suspend fun savePeerPresharedKey(value: String?){
        context.dataStore.edit { preferences ->
            preferences[PEER_PRESHARED_KEY] = value as String
        }
    }

    val getPeerAllowedIPs: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PEER_ALLOWED_IPS] ?: ""
        }

    suspend fun savePeerAllowedIPs( value: String ){
        context.dataStore.edit { preferences ->
            preferences[PEER_ALLOWED_IPS] = value
        }
    }


    val getPeerEndpoint: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PEER_ENDPOINT] ?: ""
        }

    suspend fun savePeerEndpoint( value: String ){
        context.dataStore.edit { preferences ->
            preferences[PEER_ENDPOINT] = value
        }
    }


    val getPeerPersistentKeepalive: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PEER_PERSISTENT_KEEPALIVE] ?: ""
        }

    suspend fun savePeerPersistentKeepalive( value: String ){
        context.dataStore.edit { preferences ->
            preferences[PEER_PERSISTENT_KEEPALIVE] = value
        }
    }


}