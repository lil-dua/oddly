import SwiftUI
import SharedLogic

private struct StringsKey: EnvironmentKey {
    static let defaultValue: any Strings = VietnameseStrings.shared
}

extension EnvironmentValues {
    /// The UI string table for the language the user picked in Settings.
    ///
    /// Read from the environment rather than the app state so a view that only
    /// shows text does not have to take the whole state object as a dependency.
    var strings: any Strings {
        get { self[StringsKey.self] }
        set { self[StringsKey.self] = newValue }
    }
}

extension View {
    func oddlyStrings(_ language: AppLanguage) -> some View {
        environment(\.strings, StringsCompanion.shared.of(language: language))
    }
}

extension LocalizedText {
    /// The side of this text matching the language currently on screen.
    func of(_ strings: any Strings) -> String {
        of(language: strings.language)
    }
}

extension Challenge {
    /// Resolves a challenge's content into the language currently on screen.
    func localized(_ strings: any Strings) -> LocalizedChallenge {
        localized(language: strings.language)
    }
}

extension Quote {
    /// Resolves a quote's content into the language currently on screen.
    func localized(_ strings: any Strings) -> LocalizedQuote {
        localized(language: strings.language)
    }
}
