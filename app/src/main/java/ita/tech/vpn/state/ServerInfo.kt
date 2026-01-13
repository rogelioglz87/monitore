package ita.tech.vpn.state

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServerInfo(
    // Interface details
    @SerializedName("address") val interfaceAddress: String?,
    @SerializedName("dns") val interfaceDns: String?,
    @SerializedName("private_key") val interfacePrivateKey: String?,

    // Peer details
    @SerializedName("public_key") val peerPublicKey: String?,
    @SerializedName("preshared_key") val peerPresharedKey: String?,
    @SerializedName("allowed_ips") val peerAllowedIPs: String?,
    @SerializedName("endpoint") val peerEndpoint: String?,
    @SerializedName("persistent_keep_alive") val peerPersistentKeepalive: String?
): Parcelable


