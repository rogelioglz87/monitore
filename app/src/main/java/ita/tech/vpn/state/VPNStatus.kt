package ita.tech.vpn.state

enum class VPNStatus {
    PREPARE,        // VPN is getting ready
    CONNECTING,     // Establishing a connection
    CONNECTED,      // VPN is active and running
    DISCONNECTING,  // Disconnecting from the VPN
    DISCONNECTED,   // VPN is not connected
    NO_CONNECTION,  // No available VPN connection
    REFRESHING,      // Refreshing VPN status
}