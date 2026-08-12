package com.ivor.scale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ivor.scale.ui.ScaleApp
import com.ivor.scale.ui.theme.ScaleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScaleTheme {
                ScaleApp()
            }
        }
    }
}
