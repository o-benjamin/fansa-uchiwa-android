package com.example.fansauchiwa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fansauchiwa.ui.theme.FansaUchiwaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FansaUchiwaTheme {
                FansaUchiwaNavGraph()
            }
        }
    }
}