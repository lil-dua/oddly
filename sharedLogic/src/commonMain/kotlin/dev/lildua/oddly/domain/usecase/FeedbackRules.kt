package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.Reward

/** Strength of the haptic tap; each platform maps it to its own system effect. */
enum class HapticCue { SELECTION, SUCCESS, CELEBRATION }

/** What to play for one event: an optional haptic and whether to play the short system sound. */
data class FeedbackCue(val haptic: HapticCue?, val playSound: Boolean) {
    companion object {
        val NONE = FeedbackCue(null, false)
    }
}

/**
 * Which event plays what, shared so both apps feel the same and it can be tested.
 *
 * The sound lives only on the reward moment (and the toggle preview): picking a
 * topic is frequent and a sound each tap would grate. [soundEnabled] and
 * [hapticsEnabled] are read separately; the Settings toggle writes both, so they
 * only differ if an imported file was edited by hand.
 */
object FeedbackRules {

    /** Choosing a topic or interest: a light tick, never a sound. */
    fun selection(settings: AppSettings): FeedbackCue =
        FeedbackCue(
            haptic = if (settings.hapticsEnabled) HapticCue.SELECTION else null,
            playSound = false,
        )

    /**
     * A completed challenge. A null [reward] plays nothing: the reward screen can
     * be rebuilt without one (after it was cleared), and that must stay silent.
     */
    fun reward(reward: Reward?, settings: AppSettings): FeedbackCue {
        if (reward == null) return FeedbackCue.NONE
        val haptic = if (reward.leveledUp) HapticCue.CELEBRATION else HapticCue.SUCCESS
        return FeedbackCue(
            haptic = if (settings.hapticsEnabled) haptic else null,
            playSound = settings.soundEnabled,
        )
    }

    /** Right after the toggle is turned on, so the user feels it works. Pass the new settings. */
    fun preview(settings: AppSettings): FeedbackCue =
        FeedbackCue(
            haptic = if (settings.hapticsEnabled) HapticCue.SUCCESS else null,
            playSound = settings.soundEnabled,
        )
}
