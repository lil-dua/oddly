package dev.lildua.oddly.ui.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

// Same scrim SystemBarStyle.auto uses. API 24-25 cannot draw dark nav-bar icons,
// so on a light theme enableEdgeToEdge falls back to this scrim there.
private val NavigationBarScrim = 0x801B1B1B.toInt()

/**
 * Keeps system bar icons legible against the theme the app resolved.
 *
 * Follows [isDark] (the app's theme) rather than the device's dark mode, which
 * can differ. Runs in Compose because the activity declares `uiMode` in
 * `configChanges`, so changing theme never recreates it and `onCreate` would not
 * run again. Calling [enableEdgeToEdge] repeatedly is safe.
 */
@Composable
fun SystemBarsEffect(isDark: Boolean) {
    val activity = LocalActivity.current as? ComponentActivity ?: return
    SideEffect {
        if (isDark) {
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            )
        } else {
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, NavigationBarScrim),
            )
        }
    }
}
