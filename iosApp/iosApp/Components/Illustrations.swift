import SwiftUI

// Programmatic artwork. Everything here is drawn with SwiftUI primitives rather
// than shipped as an asset, which keeps the bundle small and lets the
// illustrations pick up theme colours automatically.

private struct Star {
    let x: CGFloat
    let y: CGFloat
    let radius: CGFloat
    let opacity: Double
}

/// Faint starfield backdrop. Positions are generated once from a fixed seed so
/// the sky doesn't reshuffle on every redraw.
struct StarField: View {
    private let stars: [Star]

    init(starCount: Int = 60, seed: UInt64 = 7) {
        var generator = SeededGenerator(seed: seed)
        stars = (0..<starCount).map { _ in
            Star(
                x: generator.nextUnit(),
                y: generator.nextUnit(),
                radius: generator.nextUnit() * 1.4 + 0.4,
                opacity: Double(generator.nextUnit()) * 0.5 + 0.15
            )
        }
    }

    var body: some View {
        Canvas { context, size in
            for star in stars {
                let center = CGPoint(x: star.x * size.width, y: star.y * size.height)
                let rect = CGRect(
                    x: center.x - star.radius,
                    y: center.y - star.radius,
                    width: star.radius * 2,
                    height: star.radius * 2
                )
                context.fill(Path(ellipseIn: rect), with: .color(.white.opacity(star.opacity)))
            }
        }
        .allowsHitTesting(false)
    }
}

/// Soft radial bloom placed behind hero content.
struct GlowOrb: View {
    let color: Color
    var alpha: Double = 0.3

    var body: some View {
        GeometryReader { geometry in
            let side = min(geometry.size.width, geometry.size.height)
            Circle()
                .fill(OddlyGradients.glow(color, alpha: alpha, radius: side / 2))
        }
        .allowsHitTesting(false)
    }
}

/// The app mascot. Drawn as a friendly floating astronaut — a helmet with a
/// gradient visor, a rounded suit, and a gentle bob animation.
struct Astronaut: View {
    var size: CGFloat = 160
    var animated: Bool = true

    @State private var bobbing = false

    var body: some View {
        Canvas { context, canvasSize in
            draw(&context, side: min(canvasSize.width, canvasSize.height))
        }
        .frame(width: size, height: size)
        .offset(y: bobbing ? size * 0.02 : -size * 0.02)
        .animation(
            animated ? .linear(duration: 2.6).repeatForever(autoreverses: true) : nil,
            value: bobbing
        )
        .onAppear { if animated { bobbing = true } }
        .allowsHitTesting(false)
    }

    private func draw(_ context: inout GraphicsContext, side s: CGFloat) {
        func p(_ x: CGFloat, _ y: CGFloat) -> CGPoint { CGPoint(x: x * s, y: y * s) }

        let suit = Color(rgb: 0xE9E6F7)
        let suitShadow = Color(rgb: 0xC3BDDE)

        // Backpack, sits behind the torso.
        context.fill(
            Path(roundedRect: CGRect(x: 0.3 * s, y: 0.4 * s, width: 0.4 * s, height: 0.3 * s),
                 cornerRadius: 0.1 * s),
            with: .color(suitShadow)
        )

        // Limbs first so the torso overlaps them cleanly.
        let limbWidth = 0.11 * s
        let limbStyle = StrokeStyle(lineWidth: limbWidth, lineCap: .round)
        for limb in [
            (p(0.36, 0.5), p(0.18, 0.62)),
            (p(0.64, 0.5), p(0.83, 0.58)),
            (p(0.43, 0.7), p(0.38, 0.88)),
            (p(0.57, 0.7), p(0.63, 0.88)),
        ] {
            var path = Path()
            path.move(to: limb.0)
            path.addLine(to: limb.1)
            context.stroke(path, with: .color(suit), style: limbStyle)
        }

        // Torso.
        context.fill(
            Path(roundedRect: CGRect(x: 0.34 * s, y: 0.42 * s, width: 0.32 * s, height: 0.3 * s),
                 cornerRadius: 0.12 * s),
            with: .color(suit)
        )

        // Chest control panel.
        context.fill(
            Path(roundedRect: CGRect(x: 0.43 * s, y: 0.5 * s, width: 0.14 * s, height: 0.09 * s),
                 cornerRadius: 0.03 * s),
            with: .color(OddlyColors.purple.opacity(0.55))
        )

        // Helmet shell and visor.
        let helmetRadius = 0.2 * s
        context.fill(
            Path(ellipseIn: CGRect(x: 0.5 * s - helmetRadius, y: 0.28 * s - helmetRadius,
                                   width: helmetRadius * 2, height: helmetRadius * 2)),
            with: .color(suit)
        )
        let visorRadius = 0.145 * s
        context.fill(
            Path(ellipseIn: CGRect(x: 0.5 * s - visorRadius, y: 0.28 * s - visorRadius,
                                   width: visorRadius * 2, height: visorRadius * 2)),
            with: .linearGradient(
                Gradient(colors: [OddlyColors.purple, OddlyColors.pink, OddlyColors.indigo]),
                startPoint: p(0.36, 0.14),
                endPoint: p(0.64, 0.38)
            )
        )
        // Visor highlight.
        let highlightRadius = 0.035 * s
        context.fill(
            Path(ellipseIn: CGRect(x: 0.44 * s - highlightRadius, y: 0.22 * s - highlightRadius,
                                   width: highlightRadius * 2, height: highlightRadius * 2)),
            with: .color(.white.opacity(0.55))
        )
    }
}

/// A ringed planet, used on empty states and the share card.
struct Planet: View {
    var size: CGFloat = 120
    var color: Color = OddlyColors.purple

    var body: some View {
        Canvas { context, canvasSize in
            let s = min(canvasSize.width, canvasSize.height)
            let center = CGPoint(x: s / 2, y: s / 2)
            let bodyRadius = 0.3 * s

            context.fill(
                Path(ellipseIn: CGRect(x: center.x - bodyRadius, y: center.y - bodyRadius,
                                       width: bodyRadius * 2, height: bodyRadius * 2)),
                with: .linearGradient(
                    Gradient(colors: [color, color.opacity(0.6), OddlyColors.indigo]),
                    startPoint: .zero,
                    endPoint: CGPoint(x: s, y: s)
                )
            )

            // Surface craters.
            for crater in [(0.42, 0.42, 0.06, 0.13), (0.6, 0.56, 0.04, 0.1)] {
                let r = CGFloat(crater.2) * s
                context.fill(
                    Path(ellipseIn: CGRect(x: CGFloat(crater.0) * s - r, y: CGFloat(crater.1) * s - r,
                                           width: r * 2, height: r * 2)),
                    with: .color(.black.opacity(crater.3))
                )
            }

            // Tilted ring.
            let ring = Path(ellipseIn: CGRect(x: 0.08 * s, y: 0.42 * s, width: 0.84 * s, height: 0.16 * s))
            let tilt = CGAffineTransform(translationX: center.x, y: center.y)
                .rotated(by: -22 * .pi / 180)
                .translatedBy(x: -center.x, y: -center.y)
            context.stroke(
                ring.applying(tilt),
                with: .color(OddlyColors.pink.opacity(0.75)),
                lineWidth: 0.035 * s
            )
        }
        .frame(width: size, height: size)
        .allowsHitTesting(false)
    }
}

/// The splash-screen ring: a sweep-gradient arc that rotates while the app boots.
struct BrandRing: View {
    var size: CGFloat = 180
    var lineWidth: CGFloat = 8

    @State private var spinning = false

    var body: some View {
        ZStack {
            Circle()
                .strokeBorder(OddlyGradients.brandSweep, lineWidth: lineWidth)
            // A bright bead riding the ring.
            Circle()
                .fill(Color.white)
                .frame(width: lineWidth * 1.1, height: lineWidth * 1.1)
                .offset(y: -(size - lineWidth) / 2)
        }
        .frame(width: size, height: size)
        .rotationEffect(.degrees(spinning ? 360 : 0))
        .animation(.linear(duration: 4.2).repeatForever(autoreverses: false), value: spinning)
        .onAppear { spinning = true }
        .allowsHitTesting(false)
    }
}

/// Convenience wrapper: starfield behind arbitrary hero content.
struct StarryBox<Content: View>: View {
    var starCount: Int = 40
    var seed: UInt64 = 11
    @ViewBuilder var content: () -> Content

    var body: some View {
        ZStack {
            StarField(starCount: starCount, seed: seed)
            content()
        }
    }
}

private struct Particle {
    let angle: CGFloat
    let distance: CGFloat
    let color: Color
    let size: CGFloat
    let spin: CGFloat
}

/// A one-shot confetti burst driven by a single 0..1 `progress` value, so the
/// whole effect is one animation rather than dozens.
struct Confetti: View {
    let progress: CGFloat

    private static let particles: [Particle] = {
        let colors: [Color] = [
            OddlyColors.pink,
            OddlyColors.purple,
            OddlyColors.blue,
            OddlyColors.warning,
            OddlyColors.success,
        ]
        var generator = SeededGenerator(seed: 1)
        return (0..<56).map { _ in
            Particle(
                angle: generator.nextUnit() * 360,
                distance: 0.25 + generator.nextUnit() * 0.75,
                color: colors[Int(generator.nextUnit() * CGFloat(colors.count)) % colors.count],
                size: 4 + generator.nextUnit() * 6,
                spin: generator.nextUnit() * 360
            )
        }
    }()

    var body: some View {
        Canvas { context, size in
            guard progress > 0 else { return }
            let origin = CGPoint(x: size.width / 2, y: size.height * 0.38)
            let reach = min(size.width, size.height) * 0.55
            // Ease out, then let gravity pull the pieces down.
            let eased = 1 - (1 - progress) * (1 - progress)
            // Hold full opacity through the spread, then fade over the last
            // third so the burst is actually legible rather than gone on arrival.
            let fade = min(max((1 - progress) / 0.35, 0), 1)

            for particle in Self.particles {
                let radians = particle.angle * .pi / 180
                let travel = particle.distance * reach * eased
                let gravity = size.height * 0.25 * progress * progress
                let position = CGPoint(
                    x: origin.x + cos(radians) * travel,
                    y: origin.y + sin(radians) * travel + gravity
                )
                let rect = CGRect(
                    x: position.x - particle.size / 2,
                    y: position.y - particle.size / 2,
                    width: particle.size,
                    height: particle.size * 1.6
                )
                let rotation = CGAffineTransform(translationX: position.x, y: position.y)
                    .rotated(by: particle.spin * progress * .pi / 180)
                    .translatedBy(x: -position.x, y: -position.y)
                context.fill(
                    Path(rect).applying(rotation),
                    with: .color(particle.color.opacity(Double(fade)))
                )
            }
        }
        .allowsHitTesting(false)
    }
}
