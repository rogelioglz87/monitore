package ita.tech.vpn.data

import ita.tech.vpn.model.ActualizaClavePublicaModel
import ita.tech.vpn.model.InformacionMonitoreoModel
import ita.tech.vpn.util.Constants.Companion.ACTUALIZA_CLAVE_PUBLICA
import ita.tech.vpn.util.Constants.Companion.INFORMACION_MONITOREO
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded

import retrofit2.http.POST
import retrofit2.http.Query

interface ApiVPN {

    @FormUrlEncoded
    @POST(ACTUALIZA_CLAVE_PUBLICA)
    suspend fun actualizaClavePublica(
        @Field("idPantalla") idPantalla: String,
        @Field("clave") clave_publica: String
    ): Response<ActualizaClavePublicaModel>

    @FormUrlEncoded
    @POST(INFORMACION_MONITOREO)
    suspend fun informacionMonitoreo(
        @Field("idPantalla") idPantalla: String
    ): Response<InformacionMonitoreoModel>

}