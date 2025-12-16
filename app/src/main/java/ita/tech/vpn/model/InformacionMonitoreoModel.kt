package ita.tech.vpn.model

data class InformacionMonitoreoModel(
    val ip_vpn: String,
    val vpn_interfaceDns: String,
    val vpn_peerPublicKey: String,
    val vpn_peerPresharedKey: String?,
    val vpn_peerAllowedIPs: String,
    val vpn_peerEndpoint: String,
    val vpn_peerPersistentKeepalive: String
)
