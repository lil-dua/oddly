import SwiftUI

/// S01 — branding while the local database initialises. Deliberately brief; the
/// spec calls for a branding moment, not a held splash.
struct SplashScreen: View {
    @Environment(\.palette) private var palette

    let onFinished: () -> Void

    @State private var visible = false
    @State private var dotStep = 0

    private let dotTimer = Timer.publish(every: 0.4, on: .main, in: .common).autoconnect()

    var body: some View {
        ZStack {
            palette.background.ignoresSafeArea()
            StarField(starCount: 80, seed: 5).ignoresSafeArea()

            VStack(spacing: 0) {
                ZStack {
                    BrandRing(size: 190, lineWidth: 7)
                    VStack(spacing: 0) {
                        GradientText("1%", font: OddlyFont.displayMedium, tracking: -1)
                        Text("HUMAN")
                            .font(OddlyFont.labelLarge)
                            .foregroundStyle(palette.textSecondary)
                    }
                }

                VStack(spacing: 0) {
                    Text("Every tiny step")
                    Text("makes a huge change.")
                }
                .font(OddlyFont.bodyLarge)
                .foregroundStyle(palette.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.top, 56)
            }
            .opacity(visible ? 1 : 0)
            .scaleEffect(visible ? 1 : 0.9)
            .animation(.easeOut(duration: 0.7), value: visible)

            VStack {
                Spacer()
                HStack(spacing: 8) {
                    ForEach(0..<3, id: \.self) { index in
                        Circle()
                            .fill(index == dotStep ? palette.textPrimary : palette.textTertiary)
                            .frame(width: 7, height: 7)
                    }
                }
                .padding(.bottom, 72)
            }
        }
        .onReceive(dotTimer) { _ in dotStep = (dotStep + 1) % 3 }
        .task {
            visible = true
            try? await Task.sleep(for: .milliseconds(1900))
            onFinished()
        }
    }
}
