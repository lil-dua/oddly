import XCTest

/// T-02. XCUITest runs in another process, so the app's pinned clock cannot be moved:
/// the tests start it seconds before midnight and let real time carry it across.
final class DayChangeTests: XCTestCase {
    private let ui = OddlyAppLauncher()

    override func setUp() {
        continueAfterFailure = false
    }

    func testNewDayShowsBannerAndNewChallenge() {
        // Midnight is 60 s after the pin; launch lag eats into that, so the first completion has plenty of room.
        ui.launch(now: "2026-03-10T23:59:00")
        ui.skipOnboarding()
        ui.waitFor(UITestIds.homeTodayTitle)
        ui.completeTodaysChallenge()
        ui.tap(UITestIds.completeDone)
        ui.waitFor(UITestIds.homeTodayDone)
        let yesterdayTitle = ui.element(UITestIds.homeTodayTitle).label

        ui.wait(untilSinceLaunch: 70)
        // Background + foreground is what makes the app re-read the day (scenePhase .active).
        XCUIDevice.shared.press(.home)
        ui.app.activate()

        ui.waitFor(UITestIds.homeDayBanner, timeout: 5)
        ui.waitFor(UITestIds.homeTodayStart, timeout: 5)
        XCTAssertFalse(ui.exists(UITestIds.homeTodayDone))
        XCTAssertNotEqual(ui.element(UITestIds.homeTodayTitle).label, yesterdayTitle)
    }

    func testCompletionAcrossMidnightIsRejected() {
        // Midnight is 35 s after the pin; the detail screen must be reached inside that.
        ui.launch(now: "2026-03-10T23:59:25")
        ui.skipOnboarding()
        ui.waitFor(UITestIds.homeTodayTitle)
        ui.tap(UITestIds.homeTodayStart)
        let begin = ui.waitFor(UITestIds.detailBegin)

        ui.wait(untilSinceLaunch: 45)
        begin.tap()

        ui.waitFor(UITestIds.homeDayBanner, timeout: 5)
        XCTAssertFalse(ui.exists(UITestIds.completeDone), "completion must be refused after midnight")

        ui.tap(UITestIds.tabJourney)
        ui.waitFor(UITestIds.journeyEmpty)
    }
}
