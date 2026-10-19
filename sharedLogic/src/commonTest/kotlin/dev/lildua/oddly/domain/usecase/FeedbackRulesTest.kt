package dev.lildua.oddly.domain.usecase

import dev.lildua.oddly.domain.model.AppSettings
import dev.lildua.oddly.domain.model.Reward
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FeedbackRulesTest {

    private val allOn = AppSettings(soundEnabled = true, hapticsEnabled = true)
    private val allOff = AppSettings(soundEnabled = false, hapticsEnabled = false)
    private val plain = Reward(xp = 10, humanityPercent = 5, leveledUp = false, newLevel = 1)
    private val levelUp = Reward(xp = 10, humanityPercent = 5, leveledUp = true, newLevel = 2)

    @Test
    fun `selection buzzes when haptics are on`() {
        assertEquals(HapticCue.SELECTION, FeedbackRules.selection(allOn).haptic)
    }

    @Test
    fun `selection never plays a sound`() {
        assertEquals(false, FeedbackRules.selection(allOn).playSound)
        assertEquals(false, FeedbackRules.selection(allOn.copy(hapticsEnabled = false)).playSound)
    }

    @Test
    fun `selection is silent when haptics are off`() {
        assertNull(FeedbackRules.selection(allOff).haptic)
        assertNull(FeedbackRules.selection(allOn.copy(hapticsEnabled = false)).haptic)
    }

    @Test
    fun `a plain completion plays success and a sound`() {
        assertEquals(FeedbackCue(HapticCue.SUCCESS, true), FeedbackRules.reward(plain, allOn))
    }

    @Test
    fun `a level up plays the celebration haptic`() {
        assertEquals(FeedbackCue(HapticCue.CELEBRATION, true), FeedbackRules.reward(levelUp, allOn))
    }

    @Test
    fun `a missing reward plays nothing`() {
        assertSame(FeedbackCue.NONE, FeedbackRules.reward(null, allOn))
    }

    @Test
    fun `reward sound follows the sound setting alone`() {
        val cue = FeedbackRules.reward(plain, allOn.copy(soundEnabled = false))
        assertEquals(HapticCue.SUCCESS, cue.haptic)
        assertEquals(false, cue.playSound)
    }

    @Test
    fun `reward haptic follows the haptic setting alone`() {
        val cue = FeedbackRules.reward(levelUp, allOn.copy(hapticsEnabled = false))
        assertNull(cue.haptic)
        assertTrue(cue.playSound)
    }

    @Test
    fun `everything off plays nothing`() {
        assertEquals(FeedbackCue.NONE, FeedbackRules.reward(plain, allOff))
        assertEquals(FeedbackCue.NONE, FeedbackRules.reward(levelUp, allOff))
        assertEquals(FeedbackCue.NONE, FeedbackRules.selection(allOff))
        assertEquals(FeedbackCue.NONE, FeedbackRules.preview(allOff))
    }

    @Test
    fun `preview plays success and a sound when turned on`() {
        assertEquals(FeedbackCue(HapticCue.SUCCESS, true), FeedbackRules.preview(allOn))
    }

    @Test
    fun `preview follows each setting separately`() {
        assertEquals(FeedbackCue(null, true), FeedbackRules.preview(allOn.copy(hapticsEnabled = false)))
        assertEquals(FeedbackCue(HapticCue.SUCCESS, false), FeedbackRules.preview(allOn.copy(soundEnabled = false)))
    }

    @Test
    fun `a level up with the sound off still celebrates`() {
        assertEquals(
            FeedbackCue(HapticCue.CELEBRATION, false),
            FeedbackRules.reward(levelUp, allOn.copy(soundEnabled = false)),
        )
    }
}
