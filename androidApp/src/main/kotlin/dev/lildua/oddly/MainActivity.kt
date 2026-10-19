package dev.lildua.oddly

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must precede super.onCreate. Dark bars match the dark splash and window
        // background; once Compose is up, SystemBarsEffect re-applies them to
        // follow the theme the user picked.
        installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        setContent {
            OddlyApp()
        }
    }
}

@Preview
@Composable
private fun OddlyAppPreview() {
    OddlyApp()
}
