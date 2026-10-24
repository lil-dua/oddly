import XCTest

/// Launch + navigation helpers for the Debug-only hooks in `UITestHooks.swift`.
///
/// Elements are found by `accessibilityIdentifier` across all element types: SwiftUI
/// reports a tagged Button, a combined container or a Text under different types,
/// and the id is what the tests care about.
final class OddlyAppLauncher {
    let app = XCUIApplication()
    private(set) var launchedAt = Date()

    /// Generous: the first wait after launch covers the 1.9 s splash.
    static let timeout: TimeInterval = 20

    /// - Parameter now: pinned local time, ISO `yyyy-MM-ddTHH:mm:ss` (the clock keeps running from there).
    func launch(now: String, keepData: Bool = false) {
        app.launchEnvironment["ODDLY_UI_TEST"] = "1"
        app.launchEnvironment["ODDLY_UI_TEST_NOW"] = now
        app.launchEnvironment["ODDLY_UI_TEST_KEEP_DATA"] = keepData ? "1" : nil
        app.launch()
        // Stamped AFTER launch(): it returns only once iOSApp.init() has pinned the clock, so
        // launchedAt >= pin time and any lag becomes extra waiting, never too little.
        launchedAt = Date()
    }

    func element(_ id: String) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: id).firstMatch
    }

    @discardableResult
    func waitFor(_ id: String, timeout: TimeInterval = OddlyAppLauncher.timeout) -> XCUIElement {
        let e = element(id)
        if !e.waitForExistence(timeout: timeout) {
            // The element tree is the fastest way to see which screen the app was really on.
            XCTContext.runActivity(named: "UI tree when '\(id)' was missing") { activity in
                let tree = XCTAttachment(string: app.debugDescription)
                tree.lifetime = .keepAlways
                activity.add(tree)
            }
            XCTFail("'\(id)' did not appear")
        }
        return e
    }

    func tap(_ id: String) {
        waitFor(id).tap()
    }

    /// Taps `id`, then confirms `next` appeared; retries (bounded) if the tap evidently did not land.
    ///
    /// Needed for the splash -> onboarding cross-fade: the onboarding button is already in the
    /// accessibility tree while the fading splash still sits on top and swallows the touch. The
    /// retry only fires when `id` is still there and `next` is not, so a tap that worked is never repeated.
    func tap(_ id: String, expecting next: String, attempts: Int = 3, settle: TimeInterval = 3) {
        for attempt in 1...attempts {
            let e = waitFor(id)
            let hittable = NSPredicate(format: "exists == true AND isHittable == true")
            let exp = XCTNSPredicateExpectation(predicate: hittable, object: e)
            _ = XCTWaiter.wait(for: [exp], timeout: 5)
            if e.exists { e.tap() }
            // Last attempt waits the full default timeout so a real failure reports like before.
            let wait = attempt == attempts ? OddlyAppLauncher.timeout : settle
            if element(next).waitForExistence(timeout: wait) { return }
            if !element(id).exists { break }  // screen moved on; let waitFor report what is missing
        }
        waitFor(next)
    }

    func exists(_ id: String) -> Bool { element(id).exists }

    /// Blocks until `seconds` of real time have passed since `launch` returned (which is never earlier
    /// than the clock pin), so the app's clock is at least `seconds` past the pinned time.
    func wait(untilSinceLaunch seconds: TimeInterval) {
        let remaining = seconds - Date().timeIntervalSince(launchedAt)
        if remaining > 0 { Thread.sleep(forTimeInterval: remaining) }
    }

    func onboardWithInterest(_ categoryId: String = "health") {
        tap(UITestIds.onboardingStart, expecting: UITestIds.interest(categoryId))
        tap(UITestIds.interest(categoryId), expecting: UITestIds.interestsContinue)
        tap(UITestIds.interestsContinue, expecting: UITestIds.permissionLater)
        tap(UITestIds.permissionLater, expecting: UITestIds.homeTodayTitle)
    }

    func skipOnboarding() {
        tap(UITestIds.onboardingSkip, expecting: UITestIds.homeTodayTitle)
    }

    /// From Home: open today's challenge, begin it, wait for the reward screen.
    func completeTodaysChallenge() {
        tap(UITestIds.homeTodayStart)
        tap(UITestIds.detailBegin)
        waitFor(UITestIds.completeDone)
    }

    func finishTodayAndReturnHome(now: String = "2026-03-10T09:00:00") {
        launch(now: now)
        skipOnboarding()
        waitFor(UITestIds.homeTodayTitle)
        completeTodaysChallenge()
        tap(UITestIds.completeDone)
        waitFor(UITestIds.homeTodayDone)
    }
}

