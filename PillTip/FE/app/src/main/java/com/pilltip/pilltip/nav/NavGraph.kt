package com.pilltip.pilltip.nav

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.model.search.SearchHiltViewModel
import com.pilltip.pilltip.model.signUp.SignUpViewModel
import com.pilltip.pilltip.view.auth.FindMyInfoPage
import com.pilltip.pilltip.view.auth.IdPage
import com.pilltip.pilltip.view.auth.InterestPage
import com.pilltip.pilltip.view.auth.KakaoAuthPage
import com.pilltip.pilltip.view.auth.LoginPage
import com.pilltip.pilltip.view.auth.PasswordPage
import com.pilltip.pilltip.view.auth.PhoneAuthPage
import com.pilltip.pilltip.view.auth.ProfilePage
import com.pilltip.pilltip.view.auth.SelectPage
import com.pilltip.pilltip.view.auth.SplashPage
import com.pilltip.pilltip.view.main.PillMainPage
import com.pilltip.pilltip.view.search.DetailPage
import com.pilltip.pilltip.view.search.SearchPage
import com.pilltip.pilltip.view.search.SearchResultsPage

@Composable
fun NavGraph(
    startPage: String,
    signUpViewModel: SignUpViewModel = hiltViewModel(),
    searchHiltViewModel: SearchHiltViewModel = hiltViewModel(),
    logViewModel: LogViewModel = viewModel()
) {
    val navController = rememberNavController()
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
        composable("LoginPage"){
            LoginPage(navController, signUpViewModel)
        }
        composable("FindMyInfoPage/{mode}") { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "FIND_ID"
            FindMyInfoPage(navController = navController, mode = mode)
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
            SearchPage(navController, logViewModel, searchHiltViewModel)
        }
        composable("SearchResultsPage/{query}") { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""
            SearchResultsPage(
                navController = navController,
                logViewModel = logViewModel,
                searchViewModel = searchHiltViewModel,
                initialQuery = query
            )
        }
        composable("DetailPage"){
            DetailPage(
                navController,
                searchHiltViewModel
            )
        }

    }
}