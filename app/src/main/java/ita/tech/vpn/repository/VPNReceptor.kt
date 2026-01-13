package ita.tech.vpn.repository

import ita.tech.vpn.state.VPNStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object VPNReceptor {
    private val _vpnState = MutableStateFlow(VPNStatus.NO_CONNECTION)
    val vpnState = _vpnState.asStateFlow()

    private val _isVpnActive = MutableStateFlow(false)
    val isVpnActive = _isVpnActive.asStateFlow()


    fun updateStatus(newStatus: VPNStatus) {
        _vpnState.value = newStatus
    }

    fun updateIsVpnActive(newBand: Boolean) {
        _isVpnActive.value = newBand
    }


}