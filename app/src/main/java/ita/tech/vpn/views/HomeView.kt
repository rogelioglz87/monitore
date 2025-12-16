package ita.tech.vpn.views

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import ita.tech.vpn.R
import ita.tech.vpn.dataStore.StoreVPN
import ita.tech.vpn.viewModels.VPNViewModel

@Composable
fun HomeView(
    navController: NavController,
    viewModel: VPNViewModel
){
    ContentHomeView(navController, viewModel)
}

@Composable
fun ContentHomeView(navController: NavController, viewModel: VPNViewModel) {
    val context = LocalContext.current

    // DataStore
    val dataStore = StoreVPN(context)

    val bandCreacionKeys = dataStore.getBandCreacionKeys.collectAsState(initial = false)
    val bandEnvioDatos = dataStore.getBandEnvioDatos.collectAsState(initial = false)
    val bandConfiguracion = dataStore.getBandConfiguracion.collectAsState(initial = null) // Se deja en NULL para que tengamos una referencia de cuando se recuperan los datos del DataStore

    val idDispositivo = dataStore.getIdDispositivo.collectAsState(initial = "")
    val privateKey = dataStore.getPrivateKey.collectAsState(initial = "")
    val publicKey = dataStore.getPublicKey.collectAsState(initial = "")

    val interfaceAddress = dataStore.getInterfaceAddress.collectAsState(initial = "")
    val interfaceDns = dataStore.getInterfaceDns.collectAsState(initial = "")
    val peerPublicKey = dataStore.getPeerPublicKey.collectAsState(initial = "")
    val peerPresharedKey = dataStore.getPeerPresharedKey.collectAsState(initial = "")
    val peerAllowedIPs = dataStore.getPeerAllowedIPs.collectAsState(initial = "")
    val peerEndpoint = dataStore.getPeerEndpoint.collectAsState(initial = "")
    val peerPersistentKeepalive = dataStore.getPeerPersistentKeepalive.collectAsState(initial = "")

    // Status VPN
    val statusVPN by viewModel.vpnState.collectAsState()

    // Datos VPN
    val stateVPN = viewModel.stateVPN

    // Observa el estado del Modal VPN
    val mostrarModalVPN by viewModel.mostrarModalVPN.collectAsState()

    LaunchedEffect(bandConfiguracion.value) {
        if(bandConfiguracion.value != null){
            // Recuperamos datos del DataStore

            viewModel.setBandCreacionKeys(bandCreacionKeys.value)
            viewModel.setBandEnvioDatos(bandEnvioDatos.value)
            viewModel.setBandConfiguracion(bandConfiguracion.value)

            viewModel.setIdDispositivo(idDispositivo.value)
            viewModel.setPrivateKey(privateKey.value)
            viewModel.setPublicKey(publicKey.value)

            viewModel.setInterfaceAddress(interfaceAddress.value)
            viewModel.setInterfaceDns(interfaceDns.value)
            viewModel.setPeerPublicKey(peerPublicKey.value)
            viewModel.setPeerPresharedKey(peerPresharedKey.value)
            viewModel.setPeerAllowedIPs(peerAllowedIPs.value)
            viewModel.setPeerEndpoint(peerEndpoint.value)
            viewModel.setPeerPersistentKeepalive(peerPersistentKeepalive.value)

            // Inicializamos el proceso de configuración de la VPN
            // viewModel.initVPN()
            viewModel.inicializaProcesoVPN()
        }

    }
    if( mostrarModalVPN ){
        AlertDialog(
            onDismissRequest = { viewModel.cerrarModalVPN() },
            title = { Text("Error de Configuración", fontWeight = FontWeight.Bold) },
            text = { Text("No se puede iniciar la VPN. Asegúrate de que todos los parámetros de configuración necesarios estén definidos.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Notificar al ViewModel que la UI ha cerrado el diálogo
                        viewModel.cerrarModalVPN()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(10.dp),
                ) {
                    AsyncImage(
                        model = R.drawable.logo_itatech_blanco,
                        contentDescription = "ITA TECH",
                        modifier = Modifier
                            .width(130.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "M O N I T O R E O",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,      // Color de la sombra
                                offset = Offset(3f, 3f),  // Desplazamiento en X y Y (píxeles)
                                blurRadius = 6f          // QUÉ TAN DIFUMINADA ES LA SOMBRA
                            )
                        )
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxWidth()
                    .background(color = Color.White),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.7f)
                        .fillMaxWidth(0.6f)
                        .background(color = Color(0xFFE7E7E7), shape = RoundedCornerShape(12.dp)),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "ESTATUS: ${statusVPN.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Concepto("Permiso asignado", stateVPN.bandPermiso)
                    Concepto("Generación de claves", stateVPN.bandCreacionKeys)
                    Concepto("Envío de datos", stateVPN.bandEnvioDatos)
                    Concepto("Configuración", stateVPN.bandConfiguracion)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ElevatedButton(
                            enabled = !stateVPN.bandConfiguracion,
                            onClick = {
                                viewModel.conectarVPN()
                            }
                        ) {
                            Text("Conectar")
                        }
                        ElevatedButton(
                            onClick = {}
                        ) {
                            Text("Borrar configuración")
                        }
                        ElevatedButton(
                            onClick = {}
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
        AsyncImage(
            model = R.drawable.candado,
            contentDescription = "ITA TECH",
            modifier = Modifier
                .size(80.dp)
                .background(color = Color.White, CircleShape)
                .clip(CircleShape)
                .padding(20.dp)

        )
    }


}
@Composable
fun Concepto( nombre:String, valor:Boolean = false ){
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(end = 5.dp)
                .weight(0.5f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                nombre,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .padding(2.dp)
                .weight(0.5f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .wrapContentSize(Alignment.CenterStart) // Ajusta el tamaño del Box al contenido real del Switch escalado
                    .height(20.dp)
            ) {
                Switch(
                    checked = valor,
                    onCheckedChange = {},
                    modifier = Modifier.scale(0.5f)
                )
            }
        }
    }
}
