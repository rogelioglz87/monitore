package ita.tech.vpn.interceptor

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response

class Authentication(user: String, password: String): Interceptor {

    private val credenciales: String = Credentials.basic(user, password)

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        request = request.newBuilder().header("Authorization", credenciales).build()
        return chain.proceed(request)
    }

}