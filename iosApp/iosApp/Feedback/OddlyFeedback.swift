import SwiftUI
import AudioToolbox
import SharedLogic

/// A value a view watches to fire haptics. `FeedbackRules` (shared) has already
/// decided what to play; this only hands it to the platform.
///
/// Equality is the counter alone: the Kotlin enum is an Obj-C class, and every
/// `fire` must count as a change even when the cue repeats.
struct FeedbackTrigger: Equatable {
    private(set) var tick = 0
    private(set) var haptic: HapticCue?

    mutating func fire(_ cue: FeedbackCue) {
        if cue.playSound { OddlyFeedback.playSound() }
        guard cue.haptic != nil else { return }
        haptic = cue.haptic
        tick += 1
    }

    static func == (lhs: FeedbackTrigger, rhs: FeedbackTrigger) -> Bool {
        lhs.tick == rhs.tick
    }
}

enum OddlyFeedback {
    /// A built-in short system sound (no bundled asset). Swap to 1057 if it
    /// proves too long or hard to hear. System sounds follow the silent switch
    /// and the ringer volume, and leave other apps' audio alone.
    static let rewardSound: SystemSoundID = 1025

    static func playSound() {
        AudioServicesPlaySystemSound(rewardSound)
    }
}

extension View {
    /// Fires the haptic whenever `trigger` is fired. A nil haptic plays nothing.
    func oddlyFeedback(_ trigger: FeedbackTrigger) -> some View {
        sensoryFeedback(trigger: trigger) { _, new in
            guard let haptic = new.haptic else { return nil }
            if haptic == HapticCue.selection { return .selection }
            if haptic == HapticCue.celebration { return .impact(weight: .heavy, intensity: 1) }
            return .success
        }
    }
}
