package dev.lildua.oddly.ui.feedback

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import dev.lildua.oddly.domain.usecase.FeedbackCue
import dev.lildua.oddly.domain.usecase.HapticCue

/**
 * Plays the haptic and short system sound that [dev.lildua.oddly.domain.usecase.FeedbackRules]
 * picked. The caller has already applied the settings; this only maps cues to the platform.
 *
 * Haptics go through [View.performHapticFeedback], which needs no VIBRATE permission and
 * follows the system "touch feedback" setting. The tone uses the notification stream so it
 * follows the phone's volume, and is skipped unless the ringer is normal so Silent and
 * Vibrate stay quiet.
 */
class OddlyFeedback(private val view: View) {

    fun play(cue: FeedbackCue) {
        when (cue.haptic) {
            HapticCue.SELECTION -> view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            HapticCue.SUCCESS -> view.performHapticFeedback(successConstant())
            HapticCue.CELEBRATION -> {
                // Two taps read as stronger than one on every device, whatever the motor.
                view.performHapticFeedback(successConstant())
                view.postDelayed({ view.performHapticFeedback(successConstant()) }, CELEBRATION_GAP_MS)
            }
            null -> Unit
        }
        if (cue.playSound) playTone(view.context)
    }

    // CONFIRM exists from API 30; LONG_PRESS is the closest firm tap before that.
    private fun successConstant(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }

    private fun playTone(context: Context) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        // The constructor can throw on some devices; a missing sound is not worth a crash.
        val tone = runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, TONE_VOLUME) }
            .getOrNull() ?: return
        tone.startTone(ToneGenerator.TONE_PROP_ACK, TONE_DURATION_MS)
        Handler(Looper.getMainLooper()).postDelayed({ tone.release() }, RELEASE_DELAY_MS)
    }

    private companion object {
        const val CELEBRATION_GAP_MS = 150L
        const val TONE_VOLUME = 70
        const val TONE_DURATION_MS = 180
        const val RELEASE_DELAY_MS = 400L
    }
}

@Composable
fun rememberOddlyFeedback(): OddlyFeedback {
    val view = LocalView.current
    return remember(view) { OddlyFeedback(view) }
}
