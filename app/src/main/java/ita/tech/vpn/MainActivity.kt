package ita.tech.vpn

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import ita.tech.vpn.navegation.NavManager
import ita.tech.vpn.services.VPNService
import ita.tech.vpn.state.ServerInfo
import ita.tech.vpn.ui.theme.VpnTheme
import ita.tech.vpn.viewModels.VPNViewModel
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val viewModel: VPNViewModel by viewModels()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            // Validamos si la App fue abrierta por el sistema (Se usa para Android 12)
            // Para las otras versiones se puede dejar el codigo
            val inicioAutomatico = intent.getBooleanExtra("INICIO_DESDE_REINICIO", false)

            // Iniciamos el Servicio
            val intent = Intent(this, VPNService::class.java).apply {
                action = ""
            }
            this.startForegroundService(intent)

            if( inicioAutomatico ){
                minimizarApp()
            }

            // Handling VPN Permission Request
            val vpnPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Indicamos que se dio el permiso
                    viewModel.setBandPermiso(true)
                } else {
                    // Permission denied, show an error or update UI
                    Log.e("VPN", "PERMISO DENEGADO");
                }
            }

            LaunchedEffect(Unit) {
                // viewModel.initVPN()
                val _intent = viewModel.getVpnIntent()
                viewModel.generarClavesLlaves()
                if(_intent != null){
                    vpnPermissionLauncher.launch(_intent)
                }else{
                    // Indicamos que se dio el permiso
                    viewModel.setBandPermiso(true)
                }
            }

            VpnTheme {
                NavManager(viewModel)
            }
        }
    }

    private fun minimizarApp(){
        println("***MainActivity: Iniciando servicio en primer plano de forma segura")
        val minimizadoExitoso = moveTaskToBack(true)

        if (!minimizadoExitoso) {
            // En caso de falla simulamos presionar el botón "Home"
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        }
    }

}