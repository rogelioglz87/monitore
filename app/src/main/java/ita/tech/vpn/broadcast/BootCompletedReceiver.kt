package ita.tech.vpn.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import ita.tech.vpn.MainActivity
import ita.tech.vpn.services.VPNService

class BootCompletedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if( intent?.action == Intent.ACTION_BOOT_COMPLETED ){
            println("***Inicia app VPN con servicio")
            /*
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context?.startActivity(activityIntent)
            */


            // Dejamos que el servicio valide si existe una configuración para conectarse.
            val serviceIntent = Intent(context, VPNService::class.java).apply {
                action = ""
            }

            // En Android 8.0+ los servicios deben iniciarse como Foreground
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(serviceIntent)
            } else {
                context?.startService(serviceIntent)
            }

        }
    }
}