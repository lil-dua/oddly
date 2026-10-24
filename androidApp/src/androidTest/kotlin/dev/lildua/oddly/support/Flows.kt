package dev.lildua.oddly.support

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import dev.lildua.oddly.core.testing.UiTestTags
import dev.lildua.oddly.domain.model.Category

private const val TIMEOUT_MS = 10_000L

fun ComposeTestRule.waitForTag(tag: String) {
    waitUntil(TIMEOUT_MS) { onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }
}

fun ComposeTestRule.hasTag(tag: String): Boolean =
    onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

fun ComposeTestRule.clickTag(tag: String) {
    waitForTag(tag)
    onNodeWithTag(tag).performClick()
}

/** The first text run of the node, e.g. the Home card title. */
fun ComposeTestRule.textOf(tag: String): String {
    waitForTag(tag)
    return onNodeWithTag(tag).fetchSemanticsNode().config[SemanticsProperties.Text].first().text
}

/** Onboarding with one interest, skipping the notification permission via "Later". */
fun onboardWithInterest(rule: ComposeTestRule, category: Category = Category.HEALTH) {
    rule.clickTag(UiTestTags.ONBOARDING_START)
    rule.clickTag(UiTestTags.interest(category))
    rule.clickTag(UiTestTags.INTERESTS_CONTINUE)
    rule.clickTag(UiTestTags.PERMISSION_LATER)
}

/** Onboarding via the top-right skip, which leaves reminders at their defaults. */
fun skipOnboarding(rule: ComposeTestRule) {
    rule.clickTag(UiTestTags.ONBOARDING_SKIP)
}

/** From Home: open today's challenge, begin it, wait for the reward screen. */
fun completeTodaysChallenge(rule: ComposeTestRule) {
    rule.clickTag(UiTestTags.HOME_TODAY_START)
    rule.clickTag(UiTestTags.DETAIL_BEGIN)
    rule.waitForTag(UiTestTags.COMPLETE_DONE)
}

fun tagStartsWith(prefix: String) = SemanticsMatcher("tag starts with $prefix") {
    (it.config.getOrNull(SemanticsProperties.TestTag) ?: "").startsWith(prefix)
}
