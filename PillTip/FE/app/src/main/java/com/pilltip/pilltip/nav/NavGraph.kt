package com.pilltip.pilltip.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pilltip.pilltip.model.VM
import com.pilltip.pilltip.view.AgePage
import com.pilltip.pilltip.view.GenderPage
import com.pilltip.pilltip.view.IdPage
import com.pilltip.pilltip.view.KakaoAuthPage
import com.pilltip.pilltip.view.PasswordPage
import com.pilltip.pilltip.view.PhoneAuthPage
import com.pilltip.pilltip.view.SplashPage

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "SplashPage"
    ) {
        composable("SplashPage"){
            SplashPage(navController)
        }
        //Main.kt
        composable("KakaoAuthPage") {
            KakaoAuthPage(navController)
        }
        composable("IDPage"){
            IdPage(navController = navController)
        }
        composable("PasswordPage"){
            PasswordPage(navController = navController)
        }
        composable("PhoneAuthenticationPage"){
            PhoneAuthPage(navController = navController)
        }
        composable("GenderPage"){
            GenderPage(navController = navController)
        }
        composable("AgePage"){
            AgePage(navController = navController)
        }
    }
}