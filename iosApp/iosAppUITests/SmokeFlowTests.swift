import XCTest

final class SmokeFlowTests: XCTestCase {
    private let ui = OddlyAppLauncher()

    override func setUp() {
        continueAfterFailure = false
    }

    func testOnboardingToRewardToJourney() {
        ui.launch(now: "2026-03-10T09:00:00")

        ui.onboardWithInterest("health")
        ui.waitFor(UITestIds.homeTodayTitle)
        ui.completeTodaysChallenge()

        let xp = ui.element(UITestIds.completeXp).label        // "+15 XP"
        XCTAssertTrue(xp.hasPrefix("+") && xp.hasSuffix("XP"), "unexpected XP pill '\(xp)'")
        XCTAssertTrue(ui.element(UITestIds.completeStreak).label.contains("1"), "streak pill should show day 1")
        let earned = xp.dropFirst().split(separator: " ").first.map(String.init) ?? "?"
        XCTAssertNotEqual(earned, "0")

        ui.tap(UITestIds.completeDone)
        ui.tap(UITestIds.tabJourney)
        let level = ui.waitFor(UITestIds.journeyLevel).label
        XCTAssertTrue(level.contains("Level 1"), "level card '\(level)'")
        // Level 1 starts at 0 XP, so the progress numerator is exactly what the reward screen showed.
        XCTAssertTrue(level.contains("\(earned) / "), "level card '\(level)' should show \(earned) XP")
    }
}
