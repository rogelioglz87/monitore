package ita.tech.vpn

import android.app.Activity
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
}