package dev.lildua.oddly.notifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Runtime state of `POST_NOTIFICATIONS`, and the two things a screen wants to
 * do with it: ask for it, or send the user to system settings once they have
 * refused and the OS will no longer show the dialog.
 */
@Stable
class NotificationPermissionState internal constructor(
    val granted: Boolean,
    private val onRequest: () -> Unit,
    private val onOpenSettings: () -> Unit,
) {
    fun request() = onRequest()
    fun openSystemSettings() = onOpenSettings()
}

@Composable
fun rememberNotificationPermission(): NotificationPermissionState {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(isGranted(context)) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { result -> granted = result }

    return NotificationPermissionState(
        granted = granted,
        onRequest = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Nothing to ask for below API 33 — notifications are granted
                // at install time.
                granted = true
            }
        },
        onOpenSettings = {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(intent) }.onFailure {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null),
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        },
    )
}

private fun isGranted(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

/** Re-reads the permission after returning from system settings. */
internal fun Activity.notificationsAllowed(): Boolean = isGranted(this)
