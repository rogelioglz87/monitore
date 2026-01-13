package ita.tech.vpn.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.hilt.android.EntryPointAccessors
import ita.tech.vpn.dataStore.StoreVPN
import ita.tech.vpn.repository.VPNReceptor
import ita.tech.vpn.repository.VPNReceptorInterface
import ita.tech.vpn.state.ServerInfo
import ita.tech.vpn.state.VPNStatus
import ita.tech.vpn.viewModels.WireguardManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VPNService: Service() {
    private lateinit var storeVPN: StoreVPN
    private var wireguardManager: WireguardManager? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Variable para el ConnectivityManager
    private lateinit var connectivityManager: ConnectivityManager

    // Definición del Callback de Red
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("SERVICIO VPN", "¡Internet detectado! Intentando reconexión...")

            // Cuando hay internet, intentamos conectar si el usuario lo tiene configurado
            intentarConexionAutomatica()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d("SERVICIO VPN", "Se perdió la conexión a internet.")
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Inicializamos el manager usando el servicio como contexto
        wireguardManager = WireguardManager(this)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Registrar el monitor de red
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        serviceScope.launch {
            while (isActive){
                VPNReceptor.updateStatus(wireguardManager?.getStatus() ?: VPNStatus.NO_CONNECTION)
                VPNReceptor.updateIsVpnActive(wireguardManager?.isVpnActive ?: false)
                delay(1000)
            }
        }

        // Recuperamos los datos del DataStore
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                VPNReceptorInterface::class.java
            )
            storeVPN = entryPoint.getStoreVPN()
        }catch (e: Exception){
            Log.e("VPN", "Error al recuperar datos del DataStore")
        }
    }

    // Función centralizada para conectar según persistencia
    private fun intentarConexionAutomatica() {
        serviceScope.launch {
            // Leemos los valores del DataStore
            val estaConfigurado = storeVPN.getBandConfiguracion.first()
            val yaEstaActivo = wireguardManager?.isVpnActive ?: false

            Log.d("SERVICIO VPN", "Auto-check: Configurado=$estaConfigurado, Activo=$yaEstaActivo")

            if (estaConfigurado && !yaEstaActivo) {
                val miServidor = ServerInfo(
                    interfaceAddress        = storeVPN.getInterfaceAddress.first(),
                    interfaceDns            = storeVPN.getInterfaceDns.first(),
                    interfacePrivateKey     = storeVPN.getPrivateKey.first(),
                    peerPublicKey           = storeVPN.getPeerPublicKey.first(),
                    peerPresharedKey        = storeVPN.getPeerPresharedKey.first(),
                    peerAllowedIPs          = storeVPN.getPeerAllowedIPs.first(),
                    peerEndpoint            = storeVPN.getPeerEndpoint.first(),
                    peerPersistentKeepalive = storeVPN.getPeerPersistentKeepalive.first()
                )
                startVPN(miServidor)
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Crear el canal de notificación (Necesario para Android 8.0+)
        createNotificationChannel()

        // 2. Crear la notificación que verá el usuario
        val notification = NotificationCompat.Builder(this, "vpn_channel")
            .setContentTitle("VPN Activa")
            .setContentText("Tu conexión segura está funcionando")
            .setSmallIcon(android.R.drawable.ic_lock_lock) // Usa un icono de tu proyecto
            .build()

        // 3. Convertir el servicio en Foreground
        // startForeground(1, notification)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            // Para versiones anteriores a Android 14
            startForeground(1, notification)
        }

        // 4. Lógica de Conexion a la VPN
        Log.d("SERVICIO VPN", "ACTION ${intent?.action}")

        if( intent?.action == "CONECTAR" ){
            Log.d("SERVICIO VPN", "INICIA SERVICIO DE VPN")

            // Obtenemos datos de configuración
            val configuracion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            {
                intent.getParcelableExtra("CONFIGURACION_VPN", ServerInfo::class.java)
            }
            else
            {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("CONFIGURACION_VPN")
            }
            Log.d("SERVICIO VPN", "${configuracion}")
            // Iniciamos la VPN
            serviceScope.launch {
                if( wireguardManager?.isVpnActive == false ) {
                    startVPN(configuracion!!)
                }
            }
        }
        else if( intent?.action == "DESCONECTAR" ){
            Log.d("SERVICIO VPN", "DESCONECTAR VPN")
            serviceScope.launch {
                if( wireguardManager?.isVpnActive == true ){
                    stopVPN()
                }
            }

        }
        else{
            // En caso de reiniciar el servicio, validamos si ya existe alguna configuracion para volver a conectarnos a la VPN
            serviceScope.launch {
                Log.d("SERVICIO VPN", "CONFIGURAR VPN: ${storeVPN.getBandConfiguracion.first()} - ${ storeVPN.getInterfaceAddress.first() }")

                if( storeVPN.getBandConfiguracion.first() && wireguardManager?.isVpnActive == false ){
                    val miServidor = ServerInfo(
                        interfaceAddress        = storeVPN.getInterfaceAddress.first(),
                        interfaceDns            = storeVPN.getInterfaceDns.first(),
                        interfacePrivateKey     = storeVPN.getPrivateKey.first(),
                        peerPublicKey           = storeVPN.getPeerPublicKey.first(),
                        peerPresharedKey        = storeVPN.getPeerPresharedKey.first(),
                        peerAllowedIPs          = storeVPN.getPeerAllowedIPs.first(),
                        peerEndpoint            = storeVPN.getPeerEndpoint.first(),
                        peerPersistentKeepalive = storeVPN.getPeerPersistentKeepalive.first()
                    )
                    startVPN(miServidor)
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        // 5. Muy importante: Desregistrar el callback para evitar memory leaks
        connectivityManager.unregisterNetworkCallback(networkCallback)

        super.onDestroy()
        serviceScope.launch {
            if( wireguardManager?.isVpnActive == true ){
                stopVPN()
            }
        }
        // Detenemos la VPN
        Log.d("SERVICIO VPN", "DETIENE SERVICIO DE VPN")

    }

    /**
     * Starts the VPN connection with the given tunnel data.
     */
    private suspend fun startVPN(tunnelData: ServerInfo) {
        withContext(Dispatchers.IO) {
            notificaConexionVPN()
            wireguardManager?.start(tunnelData)
        }
    }

    /**
     * Stops the active VPN connection.
     */
    private suspend fun stopVPN() {
        withContext(Dispatchers.IO) {
            notificaConexionVPN()
            wireguardManager?.stop()
        }
    }

    private fun notificaConexionVPN(){
        val ACTION_VPN_READY = "ita.tech.eveniment.VPN_CONECTADA"
        val intent: Intent = Intent(ACTION_VPN_READY).apply {
            setPackage("ita.tech.eveniment")
        }
        sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "vpn_channel", "Servicio de VPN",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(p0: Intent?): IBinder? = null
}