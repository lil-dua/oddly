import Foundation

/// Marketing version shown in Settings. The value comes from `MARKETING_VERSION` in
/// `Configuration/Version.xcconfig` via the generated Info.plist.
enum AppVersion {
    static var marketing: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "—"
    }
}
