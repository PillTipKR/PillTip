package com.pilltip.pilltip.nav

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.view.auth.AgePage
import com.pilltip.pilltip.view.auth.BodyStatPage
import com.pilltip.pilltip.view.auth.GenderPage
import com.pilltip.pilltip.view.auth.IdPage
import com.pilltip.pilltip.view.auth.InterestPage
import com.pilltip.pilltip.view.auth.KakaoAuthPage
import com.pilltip.pilltip.view.auth.NicknamePage
import com.pilltip.pilltip.view.auth.PasswordPage
import com.pilltip.pilltip.view.auth.PhoneAuthPage
import com.pilltip.pilltip.view.auth.ProfilePage
import com.pilltip.pilltip.view.auth.SelectPage
import com.pilltip.pilltip.view.auth.SplashPage
import com.pilltip.pilltip.view.main.PillMainPage

@Composable
fun NavGraph(startPage: String) {
    val navController = rememberNavController()
    val signUpViewModel: SignUpViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = startPage
    ) {
        /*SignIn Flow*/
        composable("SplashPage"){
            SplashPage(navController)
        }
        composable("SelectPage"){
            SelectPage(navController)
        }
        composable("KakaoAuthPage") {
            KakaoAuthPage(navController, signUpViewModel)
        }
        composable("IDPage"){
            IdPage(navController = navController, signUpViewModel)
        }
        composable("PasswordPage"){
            PasswordPage(navController = navController, signUpViewModel)
        }
        composable("PhoneAuthPage"){
            PhoneAuthPage(navController = navController, signUpViewModel)
        }
        composable("ProfilePage"){
            ProfilePage(navController, signUpViewModel)
        }
        composable("NicknamePage"){
            NicknamePage(navController = navController, signUpViewModel)
        }
        composable("GenderPage"){
            GenderPage(navController = navController, signUpViewModel)
        }
        composable("AgePage"){
            AgePage(navController = navController, signUpViewModel)
        }
        composable("BodyStatPage"){
            BodyStatPage(navController = navController, signUpViewModel)
        }
        composable("InterestPage"){
            InterestPage(navController = navController, signUpViewModel)
        }

        composable("PillMainPage"){
            PillMainPage()
        }

    }
}