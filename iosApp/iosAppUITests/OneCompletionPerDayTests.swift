import XCTest

/// T-03: one scoring completion a day.
final class OneCompletionPerDayTests: XCTestCase {
    private let ui = OddlyAppLauncher()

    override func setUp() {
        continueAfterFailure = false
    }

    func testSecondCompletionSameDayIsBlocked() {
        ui.finishTodayAndReturnHome()

        // Today's card is now "done": its detail shows the notice, never Begin.
        ui.tap(UITestIds.homeTodayDone)
        ui.waitFor(UITestIds.detailAlreadyDone)
        XCTAssertFalse(ui.exists(UITestIds.detailBegin))
        ui.tap(UITestIds.navBack)

        // Another challenge from the library: the day is done, so no Begin or Adopt.
        ui.tap(UITestIds.tabJourney)
        // The row sits under the tab bar until the page is scrolled.
        ui.waitFor(UITestIds.journeyLevel)
        ui.app.swipeUp()
        ui.tap(UITestIds.journeyAllChallenges)
        let rows = ui.app.descendants(matching: .any)
            .matching(NSPredicate(format: "identifier BEGINSWITH 'library.'"))
        XCTAssertTrue(rows.firstMatch.waitForExistence(timeout: OddlyAppLauncher.timeout), ui.app.debugDescription)
        // The finished challenge is marked "✓"; any other row will do, take the first one that is not it.
        let other = rows.allElementsBoundByIndex.first { !$0.label.contains("✓") }
        XCTAssertNotNil(other, "library has no unfinished row")
        other?.tap()

        ui.waitFor(UITestIds.detailDayDone)
        XCTAssertFalse(ui.exists(UITestIds.detailBegin))
        XCTAssertFalse(ui.exists(UITestIds.detailAdopt))
    }

    func testRelaunchKeepsDoneState() {
        ui.finishTodayAndReturnHome()
        ui.app.terminate()

        ui.launch(now: "2026-03-10T09:30:00", keepData: true)

        ui.waitFor(UITestIds.homeTodayDone)
        XCTAssertFalse(ui.exists(UITestIds.homeTodayStart))
        ui.tap(UITestIds.homeTodayDone)
        ui.waitFor(UITestIds.detailAlreadyDone)
        XCTAssertFalse(ui.exists(UITestIds.detailBegin))
    }
}
