import SwiftUI

/// Small VoiceOver helpers shared by every screen, mirroring the Android
/// `a11yHeading` / `a11yHidden` / `selectableNoRipple` modifiers so both
/// platforms announce the same structure.
extension View {
    /// Marks a screen title or section label so the Headings rotor lists it.
    func accessibilityHeading() -> some View {
        accessibilityAddTraits(.isHeader)
    }

    /// iOS has no radio/checkbox role; "selected" is the shared way to expose a chosen option.
    func accessibilitySelected(_ on: Bool) -> some View {
        accessibilityAddTraits(on ? .isSelected : [])
    }

    /// Grows the tap area to the 44pt minimum without changing what is drawn:
    /// apply to the label's content *after* the visible background.
    func minimumTapTarget() -> some View {
        frame(minWidth: 44, minHeight: 44).contentShape(Rectangle())
    }
}

/// Speaks `text` through VoiceOver without moving focus; a no-op when VoiceOver is off.
/// Used where Android uses a polite live region.
func announce(_ text: String) {
    AccessibilityNotification.Announcement(text).post()
}
