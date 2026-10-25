import XCTest

/// Produces the store screenshots (T-19). Skipped unless `tools/store-shots.sh` passes
/// `ODDLY_SHOTS_DIR` (as `TEST_RUNNER_ODDLY_SHOTS_DIR`), so the regular UI suite is unaffected.
/// Files are numbered in store order though they are taken in the order of the flow.
final class StoreScreenshotTests: XCTestCase {
    private let ui = OddlyAppLauncher()
    private let now = "2026-11-19T09:41:00"
    private var shotsDir = ""

    override func setUpWithError() throws {
        continueAfterFailure = false
        guard let dir = ProcessInfo.processInfo.environment["ODDLY_SHOTS_DIR"], !dir.isEmpty else {
            throw XCTSkip("ODDLY_SHOTS_DIR not set; run tools/store-shots.sh")
        }
        shotsDir = dir
    }

    func testCaptureVietnamese() throws { try captureAll(language: "vi") }
    func testCaptureEnglish() throws { try captureAll(language: "en") }

    private func captureAll(language: String) throws {
        ui.launch(now: now, scenario: "storeShots", language: language)
        ui.waitFor(UITestIds.homeTodayTitle)
        shoot("01-home", language)

        ui.tap(UITestIds.homeTodayStart)
        ui.tap(UITestIds.detailBegin)
        ui.waitFor(UITestIds.completeDone)
        shoot("02-reward", language)

        ui.tap(UITestIds.completeShare, expecting: UITestIds.shareCard)
        // The 9:16 card is taller than the sheet and pushes "Share now" off the bottom; the square one fits the
        // whole sheet (title, switch, card, button), so the store image shows the complete screen.
        ui.tap(UITestIds.shareLayoutSquare)
        shoot("05-share", language)
        // The share screen is a sheet over the reward cover; its close button is found by id
        // (a swipe-down on the scrolling card never settled under XCUITest).
        ui.tap(UITestIds.shareClose)
        ui.tap(UITestIds.completeDone)

        ui.tap(UITestIds.tabJourney)
        scrollToEnd(UITestIds.journeyCalendar)
        ui.tap(UITestIds.journeyCalendar, expecting: UITestIds.calendarScreen)
        shoot("03-calendar", language)
        ui.tap(UITestIds.navBack, expecting: UITestIds.tabStatistics)

        ui.tap(UITestIds.tabStatistics, expecting: UITestIds.statisticsScreen)
        shoot("04-stats", language)

        ui.tap(UITestIds.tabSettings)
        ui.waitFor(UITestIds.settingsReminder)
        // The simulator starts without notification permission, so the red notice shows. Turning the switch off
        // and on again asks the system for it; accept, and the notice goes (later runs already have it).
        if ui.exists(UITestIds.settingsNotificationsNotice) {
            ui.tap(UITestIds.settingsReminder)
            ui.tap(UITestIds.settingsReminder)
            let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
            let allow = springboard.alerts.buttons.element(boundBy: 1)  // [Don't Allow, Allow]
            if allow.waitForExistence(timeout: 5) { allow.tap() }
            _ = XCTWaiter.wait(for: [XCTNSPredicateExpectation(
                predicate: NSPredicate(format: "exists == false"), object: ui.element(UITestIds.settingsNotificationsNotice))], timeout: 5)
        }
        XCTAssertFalse(ui.exists(UITestIds.settingsNotificationsNotice), "permission notice must not show")
        shoot("06-settings", language)
    }

    /// The Journey rows sit below the fold on this profile, under the tab bar, and `isHittable` still says yes for
    /// them; so always drag the list up first (twice reaches the end of it) before tapping.
    private func scrollToEnd(_ id: String) {
        ui.waitFor(id)
        for _ in 0..<2 {
            let from = ui.app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.7))
            let to = ui.app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.2))
            from.press(forDuration: 0.1, thenDragTo: to)
        }
    }

    /// Writes the PNG to the host directory, and attaches it to the result bundle as a fallback.
    private func shoot(_ name: String, _ language: String) {
        // Let the last frame settle: XCUITest idle does not cover a finishing transition.
        Thread.sleep(forTimeInterval: 0.8)
        let shot = XCUIScreen.main.screenshot()
        let dir = (shotsDir as NSString).appendingPathComponent(language)
        try? FileManager.default.createDirectory(atPath: dir, withIntermediateDirectories: true)
        let path = (dir as NSString).appendingPathComponent("\(name).png")
        try? shot.pngRepresentation.write(to: URL(fileURLWithPath: path))
        let attachment = XCTAttachment(screenshot: shot)
        attachment.name = "\(language)-\(name)"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
