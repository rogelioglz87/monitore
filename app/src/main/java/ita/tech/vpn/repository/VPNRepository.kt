package ita.tech.vpn.repository

import ita.tech.vpn.data.ApiVPN
import ita.tech.vpn.model.ActualizaClavePublicaModel
import ita.tech.vpn.model.InformacionMonitoreoModel
import javax.inject.Inject

class VPNRepository @Inject constructor( private val apiVPN: ApiVPN ) {

    suspend fun actualizaClavePublica(idPantalla: String, clave: String): ActualizaClavePublicaModel? {
        val response = apiVPN.actualizaClavePublica(idPantalla, clave)
        if( response.isSuccessful ){
            return response.body()!!
        }
        return null
    }

    suspend fun obtenerInformacionMonitoreo(idPantalla: String): InformacionMonitoreoModel? {
        val response = apiVPN.informacionMonitoreo(idPantalla)
        if( response.isSuccessful ){
            return response.body()!!
        }
        return null
    }

}