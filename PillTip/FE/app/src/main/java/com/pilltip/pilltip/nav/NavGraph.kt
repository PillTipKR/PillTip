package com.pilltip.pilltip.nav

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pilltip.pilltip.composable.MainComposable.BottomTab
import com.pilltip.pilltip.model.search.LogViewModel
import com.pilltip.pilltip.model.search.QuestionnaireViewModel
import com.pilltip.pilltip.model.search.ReviewViewModel
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
import com.pilltip.pilltip.view.main.DURLoadingPage
import com.pilltip.pilltip.view.main.DURPage
import com.pilltip.pilltip.view.main.DURResultPage
import com.pilltip.pilltip.view.main.DURSearchPage
import com.pilltip.pilltip.view.main.EssentialInfoPage
import com.pilltip.pilltip.view.main.MyDataPage
import com.pilltip.pilltip.view.main.MyDrugInfoPage
import com.pilltip.pilltip.view.main.MyDrugManagementPage
import com.pilltip.pilltip.view.main.MyHealthDetailPage
import com.pilltip.pilltip.view.main.MyHealthPage
import com.pilltip.pilltip.view.main.NotificationPage
import com.pilltip.pilltip.view.main.PillMainPage
import com.pilltip.pilltip.view.questionnaire.AreYouPage
import com.pilltip.pilltip.view.questionnaire.EssentialPage
import com.pilltip.pilltip.view.questionnaire.QuestionnaireCheckPage
import com.pilltip.pilltip.view.questionnaire.QuestionnairePage
import com.pilltip.pilltip.view.questionnaire.QuestionnaireSearchPage
import com.pilltip.pilltip.view.questionnaire.WritePage
import com.pilltip.pilltip.view.search.DetailPage
import com.pilltip.pilltip.view.search.DosageAlarmPage
import com.pilltip.pilltip.view.search.DosagePage
import com.pilltip.pilltip.view.search.SearchPage
import com.pilltip.pilltip.view.search.SearchResultsPage

@Composable
fun NavGraph(
    startPage: String,
    signUpViewModel: SignUpViewModel,
    searchHiltViewModel: SearchHiltViewModel,
    logViewModel: LogViewModel = viewModel(),
    questionnaireViewModel: QuestionnaireViewModel,
    reviewViewModel: ReviewViewModel
) {
    val navController = rememberNavController()
    val myPageNavController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startPage
    ) {
        /*SignIn Flow*/
        composable("SplashPage") {
            SplashPage(navController)
        }
        composable("SelectPage") {
            SelectPage(navController, signUpViewModel)
        }
        composable("KakaoAuthPage") {
            KakaoAuthPage(navController, signUpViewModel)
        }
        composable("LoginPage") {
            LoginPage(navController, signUpViewModel)
        }
        composable("FindMyInfoPage/{mode}") { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "FIND_ID"
            FindMyInfoPage(navController = navController, mode = mode)
        }
        composable("IDPage") {
            IdPage(navController = navController, signUpViewModel)
        }
        composable("PasswordPage") {
            PasswordPage(navController = navController, signUpViewModel)
        }
        composable("PhoneAuthPage") {
            PhoneAuthPage(navController = navController, signUpViewModel)
        }
        composable("ProfilePage") {
            ProfilePage(navController, signUpViewModel)
        }
        composable("InterestPage") {
            InterestPage(navController = navController, signUpViewModel)
        }

        /* Main */
        composable("PillMainPage") {
            PillMainPage(navController, searchHiltViewModel, questionnaireViewModel)
        }

        composable("PillMainPage/{tab}") { backStackEntry ->
            val tabName = backStackEntry.arguments?.getString("tab")
            val selected = BottomTab.entries.firstOrNull { it.name == tabName } ?: BottomTab.Home

            PillMainPage(
                navController = navController,
                searchHiltViewModel = hiltViewModel(),
                questionnaireViewModel = hiltViewModel(),
                initialTab = selected
            )
        }

        composable("NotificationPage") {
            NotificationPage(navController, searchHiltViewModel)
        }

        /* Search */
        composable("SearchPage") {
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
        composable("DetailPage") {
            DetailPage(
                navController,
                searchHiltViewModel,
                reviewViewModel
            )
        }
        composable(
            route = "DosagePage/{drugId}/{drugName}",
            arguments = listOf(
                navArgument("drugId") { type = NavType.LongType },
                navArgument("drugName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val drugId = backStackEntry.arguments?.getLong("drugId") ?: 0L
            val drugName = backStackEntry.arguments?.getString("drugName") ?: ""
            DosagePage(navController, searchHiltViewModel, drugId, drugName)
        }

        composable("DosageAlarmPage/{mode}") { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode")?.toBoolean() ?: false
            DosageAlarmPage(
                navController = navController,
                searchViewModel = searchHiltViewModel,
                isEditMode = mode
            )
        }

        /* questionnaire */
        composable("QuestionnairePage") {
            QuestionnairePage(navController)
        }
        composable("EssentialPage") {
            EssentialPage(navController, questionnaireViewModel)
        }
        composable("AreYouPage/{query}") { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query") ?: ""

            val (mode, title) = when (query) {
                "약" -> "drug" to "복용 중인 약이\n있으신가요?"
                "알러지" -> "allergy" to "알러지가\n있으신가요?"
                "기저질환" -> "etc" to "혹시 기저질환이\n있으신가요?"
                else -> "surgery" to "최근 받은 수술이\n있으신가요?"
            }

            val (onYesClicked, onNoClicked) = when (query) {
                "약" -> ({
                    navController.navigate("QuestionnaireSearchPage")
                }) to {
                    navController.navigate("AreYouPage/알러지")
                }

                "알러지" -> ({
                    navController.navigate("WritePage/$mode")
                }) to {
                    navController.navigate("AreYouPage/기저질환")
                }

                "기저질환" -> ({
                    navController.navigate("WritePage/$mode")
                }) to {
                    navController.navigate("AreYouPage/수술")
                }

                else -> ({
                    navController.navigate("WritePage/$mode")
                }) to {
                    navController.navigate("FinalPage")
                }
            }

            AreYouPage(
                navController = navController,
                query = query,
                title = title,
                onYesClicked = onYesClicked,
                onNoClicked = onNoClicked
            )
        }
        composable("QuestionnaireSearchPage") {
            QuestionnaireSearchPage(
                navController,
                logViewModel,
                searchHiltViewModel,
                questionnaireViewModel
            )
        }

        composable("WritePage/{mode}") { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: ""

            val (title, description, placeholder) = when (mode) {
                "allergy" -> Triple(
                    "알러지명을 입력해주세요",
                    "예: 특정 약에 알러지가 있다면 알려주세요",
                    "예: 페니실린, 복숭아"
                )

                "etc" -> Triple(
                    "기저질환을 입력해주세요",
                    "당뇨, 고혈압 등 의료진께 알려야 할 질환을 적어주세요",
                    "고혈압,당뇨,"
                )

                else -> Triple(
                    "최근 받은 수술명을 입력해주세요",
                    "과거 수술 이력도 포함해 입력해주시면 좋아요",
                    "예: 충수절제술, 백내장 수술"
                )

            }

            WritePage(
                navController = navController,
                title = title,
                description = description,
                placeholder = placeholder,
                mode = mode,
                questionnaireViewModel
            )
        }

        composable("QuestionnaireCheckPage") {
            QuestionnaireCheckPage(navController, questionnaireViewModel)
        }

        composable("questionnaire_check/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            QuestionnaireCheckPage(
                navController = navController,
                viewModel = hiltViewModel(),
                fromDetail = true,
                questionnaireId = id
            )
        }

        /* mypage */
        composable("MyDrugInfoPage") { MyDrugInfoPage(navController, searchHiltViewModel) }
        composable("EssentialInfoPage") { EssentialInfoPage(navController, searchHiltViewModel) }
        composable("DURPage") { DURPage(navController) }
        composable("DURSearchPage") {
            DURSearchPage(navController, searchHiltViewModel)
        }
        composable("DURLoadingPage/{firstDrug}/{secondDrug}") { backStackEntry ->
            val firstDrug = backStackEntry.arguments?.getString("firstDrug") ?: ""
            val secondDrug = backStackEntry.arguments?.getString("secondDrug") ?: ""
            Log.d("selectedDrugs: ", "$firstDrug, $secondDrug")
            DURLoadingPage(
                navController,
                searchHiltViewModel,
                firstDrug = firstDrug.toLong(),
                secondDrug = secondDrug.toLong(),
            )
        }
        composable("MyHealthPage") {
            MyHealthPage(navController, searchHiltViewModel)
        }

        composable("MyHealthDetailPage/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            MyHealthDetailPage(
                type = type,
                navController = navController,
                searchHiltViewModel = searchHiltViewModel
            )
        }
        composable("MyDataPage") {
            MyDataPage(navController, searchHiltViewModel)
        }
        composable("MyDrugManagementPage/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            MyDrugManagementPage(navController, searchHiltViewModel, id.toLong())
        }

    }
}