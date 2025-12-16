package ita.tech.vpn.viewModels

import com.wireguard.android.backend.Tunnel

typealias StateChangeCallback = (Tunnel.State) -> Unit

class WireGuardTunnel(
    private var name: String,
    private val onStateChanged: StateChangeCallback? = null
): Tunnel {
    private var state: Tunnel.State = Tunnel.State.DOWN

    override fun getName() = name

    override fun onStateChange(newState: Tunnel.State) {
        state = newState
        onStateChanged?.invoke(newState)
    }

    // Lo podemos cambiar por MutableState
    fun getState(): Tunnel.State = state
}