package ita.tech.vpn.viewModels

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import ita.tech.vpn.dataStore.StoreVPN
import ita.tech.vpn.state.ServerInfo
import ita.tech.vpn.state.VPNState
import ita.tech.vpn.state.VPNStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.StringReader
import java.net.InetAddress

class WireguardManager(
    private val context: Context,
) {
    private val scope = CoroutineScope(Job() + Dispatchers.Main.immediate)
    private var backend: Backend? = null
    private var tunnelName: String = "wg_default"
    private var config: Config? = null
    private var tunnel: WireGuardTunnel? = null
    private val futureBackend = CompletableDeferred<Backend>()
    private val TAG = "WireguardManager"

    // DataStore
    val dataStore = StoreVPN(context)

    companion object {
        private var state: VPNStatus = VPNStatus.NO_CONNECTION
    }

    init {
        scope.launch(Dispatchers.IO) {
            try {
                // cachedTunnelData = SharedPreferenceHelper.getVpnData()
                backend = GoBackend(context)
                futureBackend.complete(backend!!)
            } catch (e: Throwable) {
                Log.e(TAG, "ERROR: Exception during WireguardManager initialization: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Generates WireGuard configuration from the provided ServerInfo.
     * This creates a wg-quick compatible configuration for the VPN tunnel.
     */
    private fun getConfigData(tunnelData: ServerInfo): Config? {
        val configBuilder = StringBuilder()
        // --- Sección [Interface] ---
        configBuilder.append("[Interface]\n")
        tunnelData.interfacePrivateKey?.let { configBuilder.append("PrivateKey = $it\n") }
        tunnelData.interfaceAddress?.let { configBuilder.append("Address = $it\n") }
        tunnelData.interfaceDns?.let {
            if (it.isNotBlank()) { // Nos aseguramos de que no sea solo una cadena vacía
                configBuilder.append("DNS = $it\n")
            }
        }

        // --- Sección [Peer] ---
        // La sección Peer solo tiene sentido si hay una clave pública
        if (tunnelData.peerPublicKey != null) {
            configBuilder.append("\n[Peer]\n")
            configBuilder.append("PublicKey = ${tunnelData.peerPublicKey}\n")

            // Añadir las otras claves de Peer solo si no son nulas/vacías
            tunnelData.peerPresharedKey?.let {
                if (it.isNotBlank()) configBuilder.append("PresharedKey = $it\n")
            }
            tunnelData.peerAllowedIPs?.let {
                if (it.isNotBlank()) configBuilder.append("AllowedIPs = $it\n")
            }
            tunnelData.peerEndpoint?.let {
                if (it.isNotBlank()) configBuilder.append("Endpoint = $it\n")
            }
            tunnelData.peerPersistentKeepalive?.let {
                if (it.isNotBlank()) configBuilder.append("PersistentKeepalive = $it\n")
            }
        }
        val inputStream = StringReader(configBuilder.toString()).buffered()
        val config = Config.parse(inputStream)
        
        // Podemos agregar mas Apps para excluir
        val appsAExcluir = listOf(
            "ita.tech.eveniment"
        )

        val oldInterface = config.`interface`
        val interfaceBuilder = Interface.Builder()
            .parsePrivateKey(tunnelData.interfacePrivateKey)
        
        for (app in appsAExcluir) {
            interfaceBuilder.excludeApplication(app)
        }

        // Iterate over the addresses and add them one by one
        for (address in oldInterface.addresses) {
            interfaceBuilder.addAddress(address)
        }

        // Iterate over the DNS servers and add them one by one
        for (dns in oldInterface.dnsServers) {
            interfaceBuilder.addDnsServer(dns)
        }

        val newInterface = interfaceBuilder.build()

        val nuevoBuilder = Config.Builder()
            .setInterface(newInterface)

        for( peer in config.peers ){
            nuevoBuilder.addPeer(peer)
        }

        return nuevoBuilder.build()

    }

    /**
     * Checks if any VPN connection is currently active on the device.
     * Returns `true` if a VPN connection is detected, otherwise `false`.
     */
    val isVpnActive: Boolean
        @SuppressLint("ServiceCast")
        get() {
            return try {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                @Suppress("DEPRECATION")
                connectivityManager.allNetworks.any{ network ->
                    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

                    // Verificar si la red actual es el transporte VPN
                    val isVpn = networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

                    // Opcional: Si quieres un nivel extra de seguridad (ignorar VPNs que no dan acceso a Internet)
                    // val hasInternet = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

                    isVpn
                }
            } catch (e: Exception) {
                Log.e(TAG, "isVpnActive - ERROR - ${e.localizedMessage}", e)
                false
            }
        }

    /**
     * Retrieves an existing WireGuard tunnel or creates a new one if none exists.
     * The `callback` function listens for state changes in the tunnel.
     */
    private fun getTunnel(name: String, callback: StateChangeCallback? = null): WireGuardTunnel {
        if (tunnel == null) {
            tunnel = WireGuardTunnel(name, callback)
        }
        return tunnel as WireGuardTunnel
    }

    /**
     * Updates the VPN status based on the tunnel's state.
     * Runs on the main thread to ensure UI updates happen smoothly.
     */
    private fun updateStageFromState(state: Tunnel.State) {
        scope.launch(Dispatchers.Main) {
            when (state) {
                Tunnel.State.UP -> updateStage(VPNStatus.CONNECTED)      // VPN is active
                Tunnel.State.DOWN -> updateStage(VPNStatus.DISCONNECTED) // VPN is disconnected
                else -> updateStage(VPNStatus.NO_CONNECTION)             // No active VPN connection
            }
        }
    }

    /**
     * Sets the VPN status and saves it in shared preferences.
     * Ensures status updates run on the main thread.
     */
    private fun updateStage(stage: VPNStatus?) {
        scope.launch(Dispatchers.Main) {
            val updatedStage = stage ?: VPNStatus.NO_CONNECTION
            state = updatedStage
            // Store VPN status in SharedPreferences if required
            dataStore.saveVPNStatus(state)
        }
    }

    /**
     * Returns the VPN status based on the tunnel's state.
     */
    fun getStatus(): VPNStatus {
        return state
    }

    /**
     * Starts the VPN connection process.
     * Initializes the tunnel and attempts to connect.
     */
    fun start(tunnelData: ServerInfo) {
        Log.i("VPN", "START")
        initialize(tunnelName)
        connect(tunnelData)
    }

    /**
     * Initializes the tunnel with a given name.
     * Ensures the tunnel name is valid before proceeding.
     */
    private fun initialize(localizedDescription: String) {
        if (Tunnel.isNameInvalid(localizedDescription)) {
            Log.e(TAG, "Invalid Tunnel Name: $localizedDescription")
            return
        }
        tunnelName = localizedDescription
    }

    /**
     * Connects to the VPN using the provided tunnel configuration.
     * Updates VPN status at different stages of the connection process.
     */
    private fun connect(tunnelData: ServerInfo) {
        scope.launch(Dispatchers.IO) {
            try {
                updateStage(VPNStatus.PREPARE) // Preparing VPN connection
                // Generate WireGuard configuration
                config = getConfigData(tunnelData)
                updateStage(VPNStatus.CONNECTING) // Attempting to connect
                // Retrieve or create the WireGuard tunnel
                val tunnel = getTunnel(tunnelName) { state ->
                    scope.launch {
                        updateStageFromState(state)
                    }
                }
                // Activate the VPN connection
                futureBackend.await().setState(tunnel, Tunnel.State.UP, config)

                scope.launch(Dispatchers.Main) {
                    updateStage(VPNStatus.CONNECTED) // VPN is successfully connected
                    // Store VPN status in SharedPreferences if required
                }

                Log.i(TAG, "Connect - success!")
            } catch (e: Throwable) {
                updateStage(VPNStatus.NO_CONNECTION) // Failed to establish a connection
                Log.e(TAG, "Connect - ERROR - ${e.message}")
            }
        }
    }

    /**
     * Stops the VPN connection by calling the disconnect method.
     */
    fun stop() {
        disconnect()
    }

    /**
     * Disconnects the active VPN tunnel.
     * - If no tunnel is running, logs an error.
     * - Updates VPN status before and after disconnection.
     * - Handles reconnection if cached tunnel data exists.
     */
    private fun disconnect() {
        scope.launch(Dispatchers.IO) {
            try {
                // Check if any tunnel is currently running
                if (futureBackend.await().runningTunnelNames.isEmpty()) {
                    Log.e(TAG, "Tunnel is not running");
                }
                updateStage(VPNStatus.DISCONNECTING)

                // Retrieve the active tunnel and monitor state changes
                val tunnel = getTunnel(tunnelName) { state ->
                    scope.launch {
                        Log.i(TAG, "onStateChange - $state")
                        updateStageFromState(state)
                    }
                }

                // Set the tunnel state to DOWN to disconnect
                futureBackend.await().setState(tunnel, Tunnel.State.DOWN, config)

                // Update VPN status and shared preferences on the main thread
                scope.launch(Dispatchers.Main) {
                    updateStage(VPNStatus.DISCONNECTED)
                    // Store VPN status in SharedPreferences if required
                }
                Log.i(TAG, "Disconnect - success!")
            } catch (e: Throwable) {
                Log.e(TAG, "Disconnect - ERROR - ${e.message}")
            }
        }
    }
}