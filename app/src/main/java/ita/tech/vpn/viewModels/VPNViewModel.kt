package ita.tech.vpn.viewModels

import android.app.Application
import android.content.Intent
import android.provider.Settings.Secure
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wireguard.android.backend.GoBackend
import com.wireguard.crypto.KeyPair
import dagger.hilt.android.lifecycle.HiltViewModel
import ita.tech.vpn.dataStore.StoreVPN
import ita.tech.vpn.model.InformacionMonitoreoModel
import ita.tech.vpn.repository.VPNRepository
import ita.tech.vpn.state.ServerInfo
import ita.tech.vpn.state.VPNState
import ita.tech.vpn.state.VPNStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VPNViewModel @Inject constructor (
    application: Application,
    private val repository: VPNRepository
): AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext

    private var wireguardManager: WireguardManager? = null

    // VPN connection status as a StateFlow
    private val _vpnState = MutableStateFlow(VPNStatus.NO_CONNECTION)
    val vpnState: StateFlow<VPNStatus> = _vpnState.asStateFlow()

    // Whether VPN is currently active
    private val _isVpnActive = MutableStateFlow(false)
    val isVpnActive: StateFlow<Boolean> = _isVpnActive.asStateFlow()

    // VPN State
    var stateVPN by mutableStateOf(VPNState())
        private set

    // DataStore
    val dataStore = StoreVPN(context)

    private val _mostrarModalVPN = MutableStateFlow(false)
    val mostrarModalVPN: StateFlow<Boolean> = _mostrarModalVPN

    init {
        initVPN()
    }

    /**
     * Initializes the WireGuard manager and starts monitoring VPN state changes.
     * This method should be called when the ViewModel is created.
     */
    fun initVPN() {
        wireguardManager = WireguardManager(context)

        // Observe VPN state changes in a coroutine
        viewModelScope.launch {
            while (isActive) {
                Log.d("VPN", "ESTATUS: ${_isVpnActive.value}");
                // Restore last known VPN state from SharedPreferences if needed and assign in fallback
                _vpnState.value = wireguardManager?.getStatus() ?: VPNStatus.NO_CONNECTION
                _isVpnActive.value = wireguardManager?.isVpnActive ?: false
                delay(1000) // Check every 500ms (half a second)
            }
        }
    }

    /**
     * Starts the VPN connection with the given tunnel data.
     */
    fun startVPN(tunnelData: ServerInfo) {
        viewModelScope.launch {
            Log.i("VPN", "Connect - success!")
            wireguardManager?.start(tunnelData)
        }
    }

    /**
     * Stops the active VPN connection.
     */
    fun stopVPN() {
        viewModelScope.launch {
            wireguardManager?.stop()
        }
    }

    // Helper para obtener el Intent de permiso de VPN (necesario en el paso 5)
    fun getVpnIntent(): Intent? {
        return GoBackend.VpnService.prepare(getApplication())
    }

    fun setBandPermiso( value: Boolean ) {
        stateVPN = stateVPN.copy(
            bandPermiso = value
        )
    }

    fun setBandCreacionKeys(value: Boolean){
        stateVPN = stateVPN.copy(
            bandCreacionKeys = value
        )
    }

    fun setBandEnvioDatos(value: Boolean){
        stateVPN = stateVPN.copy(
            bandEnvioDatos = value
        )
    }

    fun setBandConfiguracion(value: Boolean?){
        value?.let {
            stateVPN = stateVPN.copy(
                bandConfiguracion = it
            )
        }
    }

    fun setIdDispositivo( value: String ){
        stateVPN = stateVPN.copy(
            idDispositivo = value
        )
    }

    fun setPrivateKey( value: String ){
        stateVPN = stateVPN.copy(
            privateKey = value
        )
    }

    fun setPublicKey( value: String ){
        stateVPN = stateVPN.copy(
            publicKey = value
        )
    }

    fun setInterfaceAddress( value: String ){
        stateVPN = stateVPN.copy(
            interfaceAddress = value
        )
    }
    fun setInterfaceDns( value: String ){
        stateVPN = stateVPN.copy(
            interfaceDns = value
        )
    }
    fun setPeerPublicKey( value: String ){
        stateVPN = stateVPN.copy(
            peerPublicKey = value
        )
    }
    fun setPeerPresharedKey( value: String ){
        stateVPN = stateVPN.copy(
            peerPresharedKey = value
        )
    }
    fun setPeerAllowedIPs( value: String ){
        stateVPN = stateVPN.copy(
            peerAllowedIPs = value
        )
    }
    fun setPeerEndpoint( value: String ){
        stateVPN = stateVPN.copy(
            peerEndpoint = value
        )
    }
    fun setPeerPersistentKeepalive( value: String ){
        stateVPN = stateVPN.copy(
            peerPersistentKeepalive = value
        )
    }


    fun inicializaProcesoVPN(){
        viewModelScope.launch(Dispatchers.IO) {
            delay(500) // Retrasamos un poco para recuperar el status de la VPN
            Log.d("VPN", "Volver a configurar: ${ _isVpnActive.value }")

            // Validamos si existe una conecion activa ya no configuramos las vpn
            if( !_isVpnActive.value ){
                // Valida si existe una configuración
                if( !stateVPN.bandConfiguracion ){

                    // Validamos si existe el ID del dispositivo
                    if( stateVPN.idDispositivo.isEmpty() ){
                        // Obtenemos ID del dispositivo
                        setIdDispositivo(Secure.getString(context.contentResolver, Secure.ANDROID_ID))
                        // Almacenar ID en DataStore
                        dataStore.saveIdDispositivo(stateVPN.idDispositivo)
                    }

                    // Validamos Claves del dispositivo
                    if( !stateVPN.bandCreacionKeys ){
                        generarClavesLlaves()
                        // Almacenar Claves en DataStore
                        dataStore.saveBandCreacionKeys(true)
                        dataStore.savePrivateKey(stateVPN.privateKey)
                        dataStore.savePublicKey(stateVPN.publicKey)
                        setBandEnvioDatos(false) // Aseguramos el envio de Claves al servidor
                    }

                    // Enviamos Llave publica del dispositivo (API)
                    if( !stateVPN.bandEnvioDatos ){
                        try {
                            val response = repository.actualizaClavePublica(stateVPN.idDispositivo, stateVPN.publicKey)
                            if(response?.success == true){
                                // Almacenar estatus en DataStore
                                dataStore.saveBandEnvioDatos(true)
                                setBandEnvioDatos(true)
                            }
                        }catch (e: Exception){
                            Log.e("ViewModel", e.message.toString())
                        }
                    }
                }
                else{
                    // CONECTAR VPN
                    val miServidor = ServerInfo(
                        interfaceAddress        = stateVPN.interfaceAddress,
                        interfaceDns            = stateVPN.interfaceDns,
                        interfacePrivateKey     = stateVPN.privateKey,
                        peerPublicKey           = stateVPN.peerPublicKey,
                        peerPresharedKey        = stateVPN.peerPresharedKey,
                        peerAllowedIPs          = stateVPN.peerAllowedIPs,
                        peerEndpoint            = stateVPN.peerEndpoint,
                        peerPersistentKeepalive = stateVPN.peerPersistentKeepalive
                    )

                    // Conectamos VPN
                    startVPN(miServidor)
                }
            }
        }
    }

    /**
     * Funcion para el Boton Conectar
     */
    fun conectarVPN(){
        viewModelScope.launch(Dispatchers.IO) {

            val idDispositivo = stateVPN.idDispositivo
            var response: InformacionMonitoreoModel? = null

            // Obtenemos datos de monitoreo (API)
            try {
                response = repository.obtenerInformacionMonitoreo(idDispositivo)
                if( response != null ){
                    stateVPN = stateVPN.copy(
                        interfaceAddress        = response?.ip_vpn ?: "",
                        interfaceDns            = response?.vpn_interfaceDns ?: "",
                        peerPublicKey           = response?.vpn_peerPublicKey ?: "",
                        peerPresharedKey        = response?.vpn_peerPresharedKey ?: "",
                        peerAllowedIPs          = response?.vpn_peerAllowedIPs ?: "", // 192.168.136.0/24, 10.0.22.0/22
                        peerEndpoint            = response?.vpn_peerEndpoint ?: "",
                        peerPersistentKeepalive = response?.vpn_peerPersistentKeepalive ?: ""
                    )

                    // Validamos si existe una configuración
                    if( stateVPN.interfaceAddress.isBlank() ||
                        stateVPN.interfaceDns.isBlank() ||
                        stateVPN.peerPublicKey.isBlank() ||
                        stateVPN.peerAllowedIPs.isBlank() ||
                        stateVPN.peerEndpoint.isBlank() ||
                        stateVPN.peerPersistentKeepalive.isBlank()
                        ){
                        _mostrarModalVPN.value = true
                        return@launch
                    }

                    // Almacenamos Configuracion en DataStore
                    dataStore.saveInterfaceAddress(stateVPN.interfaceAddress)
                    dataStore.saveInterfaceDns(stateVPN.interfaceDns)
                    dataStore.savePeerPublicKey(stateVPN.peerPublicKey)
                    dataStore.savePeerPresharedKey(stateVPN.peerPresharedKey)
                    dataStore.savePeerAllowedIPs(stateVPN.peerAllowedIPs)
                    dataStore.savePeerEndpoint(stateVPN.peerEndpoint)
                    dataStore.savePeerPersistentKeepalive(stateVPN.peerPersistentKeepalive)
                }
            }catch (e: Exception){
                Log.e("ViewModel", e.message.toString())
            }

            // Configuramos parametros VPN
            val miServidor = ServerInfo(
                interfaceAddress        = stateVPN.interfaceAddress,
                interfaceDns            = stateVPN.interfaceDns,
                interfacePrivateKey     = stateVPN.privateKey,
                peerPublicKey           = stateVPN.peerPublicKey,
                peerPresharedKey        = stateVPN.peerPresharedKey,
                peerAllowedIPs          = stateVPN.peerAllowedIPs,
                peerEndpoint            = stateVPN.peerEndpoint,
                peerPersistentKeepalive = stateVPN.peerPersistentKeepalive
            )

            // Conectamos VPN
            startVPN(miServidor)

            // Mostramos el estatus de la configuracion
            setBandConfiguracion(true)

            // Almacenamos estatus de configuracion en DataStore
            dataStore.saveBandConfiguracion(true)

        }
    }

    fun cerrarModalVPN(){
        _mostrarModalVPN.value = false
    }

    /**
     * Genera las claves necesarias del dispositivo
     */
    fun generarClavesLlaves(){
        val keyPair = KeyPair()
        setPrivateKey(keyPair.privateKey.toBase64())
        setPublicKey(keyPair.publicKey.toBase64())
        setBandCreacionKeys(true)
    }
}