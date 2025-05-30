package com.pilltip.pilltip.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.pilltip.pilltip.view.auth.SelectPage

@Preview
@Composable
fun SelectPagePreview(){
    SelectPage(navController = rememberNavController())
}