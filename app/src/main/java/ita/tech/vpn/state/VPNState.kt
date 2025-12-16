package ita.tech.vpn.state

data class VPNState(
    val vpnStatus: VPNStatus = VPNStatus.NO_CONNECTION,

    val bandPermiso: Boolean = false,
    val bandCreacionKeys: Boolean = false,
    val bandEnvioDatos: Boolean = false,
    val bandConfiguracion: Boolean = false,

    // Dispositivo
    val idDispositivo: String = "",
    val privateKey: String = "", // interfacePrivateKey
    val publicKey: String = "",

    // Interfaz
    val interfaceAddress: String = "",
    val interfaceDns: String = "",
    val peerPublicKey: String = "",
    val peerPresharedKey: String? = null,
    val peerAllowedIPs: String = "",
    val peerEndpoint: String = "",
    val peerPersistentKeepalive: String = ""
)
