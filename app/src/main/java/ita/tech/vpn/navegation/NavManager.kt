package ita.tech.vpn.navegation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ita.tech.vpn.viewModels.VPNViewModel
import ita.tech.vpn.views.HomeView
import ita.tech.vpn.views.SplashScreenView

@Composable
fun NavManager(viewModel: VPNViewModel){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "SplashScreen"){

        composable("Home"){
            HomeView(navController, viewModel)
        }
        composable("SplashScreen"){
            SplashScreenView(navController, viewModel)
        }

    }
}