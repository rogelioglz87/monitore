package ita.tech.vpn.views

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import ita.tech.vpn.R
import ita.tech.vpn.viewModels.VPNViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreenView(
    navController: NavController,
    viewModel: VPNViewModel
){
    ContentSplashScreenView(navController, viewModel)
}

@Composable
fun ContentSplashScreenView(navController: NavController, viewModel: VPNViewModel){

    LaunchedEffect(Unit) {
        delay(1000)
        navController.navigate("Home"){
            popUpTo(0)
        }
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth()
            .fillMaxHeight()
            .focusGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AsyncImage(
            model = R.drawable.logo_itatech_blanco,
            contentDescription = "ITA TECH",
            modifier = Modifier
                .width(250.dp)
                .padding(bottom = 100.dp)
        )
        Text(
            text = "w w w . i t a . t e c h",
            color = Color.White,
            fontSize = 20.sp
        )
    }
}
