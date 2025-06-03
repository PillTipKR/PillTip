package com.pilltip.pilltip.nav

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.view.auth.IdPage
import com.pilltip.pilltip.view.auth.InterestPage
import com.pilltip.pilltip.view.auth.KakaoAuthPage
import com.pilltip.pilltip.view.auth.PasswordPage
import com.pilltip.pilltip.view.auth.PhoneAuthPage
import com.pilltip.pilltip.view.auth.ProfilePage
import com.pilltip.pilltip.view.auth.SelectPage
import com.pilltip.pilltip.view.auth.SplashPage
import com.pilltip.pilltip.view.main.PillMainPage
import com.pilltip.pilltip.view.search.SearchPage
import com.pilltip.pilltip.view.search.SearchResultsPage

@Composable
fun NavGraph(startPage: String) {
    val navController = rememberNavController()
    val signUpViewModel: SignUpViewModel = hiltViewModel()
    val searchViewModel: LogViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = startPage
    ) {
        /*SignIn Flow*/
        composable("SplashPage"){
            SplashPage(navController)
        }
        composable("SelectPage"){
            SelectPage(navController, signUpViewModel)
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
        composable("InterestPage"){
            InterestPage(navController = navController, signUpViewModel)
        }

        /* Main */

        composable("PillMainPage"){
            PillMainPage(navController)
        }

        /*Search*/

        composable("SearchPage"){
            SearchPage(navController, searchViewModel)
        }
        composable("SearchResultsPage"){
            SearchResultsPage(navController)
        }

    }
}