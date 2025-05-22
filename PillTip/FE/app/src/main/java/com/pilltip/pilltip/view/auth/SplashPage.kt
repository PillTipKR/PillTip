package com.pilltip.pilltip.view.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pilltip.pilltip.composable.NextButton
import com.pilltip.pilltip.composable.WhiteScreenModifier

@Composable
fun SplashPage(navController: NavController){
    Column(
        modifier = WhiteScreenModifier
    ){
        Spacer(modifier = Modifier.weight(1f))
        NextButton(
            text = "ID로 회원가입",
            onClick = {
                navController.navigate("IDPage")
            }
        )
        NextButton(
            text = "카카오로 회원가입",
            onClick = {
                navController.navigate("KakaoAuthPage")
            }
        )

    }
}

@Preview
@Composable
fun SplashPagePreview(){
    SplashPage(
        navController = rememberNavController()
    )
}