package duxtami.gramly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import duxtami.gramly.app.ui.ScaleApp
import duxtami.gramly.app.ui.theme.ScaleTheme

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
