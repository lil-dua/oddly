import SwiftUI

/// The app's icon set, drawn with `Canvas` primitives instead of pulling in SF
/// Symbols, so Android and iOS render the identical shapes.
///
/// Decorative icons (categories, celebrations) use emoji — see `Category.emoji`.
/// These are the UI-chrome icons: navigation, chevrons, settings rows.
///
/// All shapes are authored in a normalised 0..1 box and scaled to the requested
/// size, so they stay crisp at any dimension.
enum OddlyIcon {
    case chevronRight
    case chevronLeft
    case check
    case close
    case journey
    case stats
    case settings
    case share
    case bell
    case calendar
    case flame
    case sparkle
    case heart
    case globe
    case download
    case trash
    case info
    case palette
    case volume
    case clock
    case refresh
    case target
    case dice
}

struct OddlyIconView: View {
    @Environment(\.palette) private var palette

    let icon: OddlyIcon
    var size: CGFloat = 20
    var tint: Color?
    var lineWidth: CGFloat = 1.75

    init(_ icon: OddlyIcon, size: CGFloat = 20, tint: Color? = nil, lineWidth: CGFloat = 1.75) {
        self.icon = icon
        self.size = size
        self.tint = tint
        self.lineWidth = lineWidth
    }

    var body: some View {
        let color = tint ?? palette.textPrimary
        Canvas { context, canvasSize in
            draw(icon, in: &context, side: min(canvasSize.width, canvasSize.height), color: color)
        }
        .frame(width: size, height: size)
    }

    private func draw(_ icon: OddlyIcon, in context: inout GraphicsContext, side s: CGFloat, color: Color) {
        let shading = GraphicsContext.Shading.color(color)
        let stroke = StrokeStyle(lineWidth: lineWidth, lineCap: .round, lineJoin: .round)
        let center = CGPoint(x: s / 2, y: s / 2)

        func p(_ x: CGFloat, _ y: CGFloat) -> CGPoint { CGPoint(x: x * s, y: y * s) }

        func line(_ x1: CGFloat, _ y1: CGFloat, _ x2: CGFloat, _ y2: CGFloat) {
            var path = Path()
            path.move(to: p(x1, y1))
            path.addLine(to: p(x2, y2))
            context.stroke(path, with: shading, style: stroke)
        }

        /// An open polyline through normalised points.
        func poly(_ points: [(CGFloat, CGFloat)]) {
            var path = Path()
            path.move(to: p(points[0].0, points[0].1))
            for point in points.dropFirst() { path.addLine(to: p(point.0, point.1)) }
            context.stroke(path, with: shading, style: stroke)
        }

        func circle(_ cx: CGFloat, _ cy: CGFloat, radius: CGFloat, filled: Bool) {
            let r = radius * s
            let rect = CGRect(x: cx * s - r, y: cy * s - r, width: r * 2, height: r * 2)
            let path = Path(ellipseIn: rect)
            if filled {
                context.fill(path, with: shading)
            } else {
                context.stroke(path, with: shading, style: stroke)
            }
        }

        /// An arc inscribed in an arbitrary (possibly non-square) box, matching
        /// Compose's `drawArc(topLeft:size:)`.
        func arc(x: CGFloat, y: CGFloat, width: CGFloat, height: CGFloat, start: Double, sweep: Double) {
            var unit = Path()
            unit.addArc(
                center: .zero,
                radius: 0.5,
                startAngle: .degrees(start),
                endAngle: .degrees(start + sweep),
                clockwise: false
            )
            let box = CGRect(x: x * s, y: y * s, width: width * s, height: height * s)
            let transform = CGAffineTransform(translationX: box.midX, y: box.midY)
                .scaledBy(x: box.width, y: box.height)
            context.stroke(unit.applying(transform), with: shading, style: stroke)
        }

        func curve(_ build: (inout Path) -> Void, filled: Bool) {
            var path = Path()
            build(&path)
            if filled {
                context.fill(path, with: shading)
            } else {
                context.stroke(path, with: shading, style: stroke)
            }
        }

        switch icon {
        case .chevronRight:
            poly([(0.38, 0.24), (0.66, 0.5), (0.38, 0.76)])
        case .chevronLeft:
            poly([(0.62, 0.24), (0.34, 0.5), (0.62, 0.76)])

        case .check:
            poly([(0.22, 0.52), (0.42, 0.72), (0.78, 0.3)])

        case .close:
            line(0.27, 0.27, 0.73, 0.73)
            line(0.73, 0.27, 0.27, 0.73)

        // A winding path with a start dot and a destination pin.
        case .journey:
            curve({ path in
                path.move(to: p(0.24, 0.78))
                path.addCurve(to: p(0.72, 0.42), control1: p(0.24, 0.56), control2: p(0.72, 0.64))
            }, filled: false)
            circle(0.24, 0.8, radius: 0.09, filled: true)
            circle(0.72, 0.28, radius: 0.11, filled: false)

        case .stats:
            line(0.26, 0.76, 0.26, 0.5)
            line(0.5, 0.76, 0.5, 0.26)
            line(0.74, 0.76, 0.74, 0.42)

        // Circle plus radial teeth reads as a gear at small sizes.
        case .settings:
            circle(0.5, 0.5, radius: 0.2, filled: false)
            for i in 0..<8 {
                let angle = Double(i) * 45 * .pi / 180
                var path = Path()
                path.move(to: CGPoint(
                    x: center.x + CGFloat(cos(angle)) * 0.28 * s,
                    y: center.y + CGFloat(sin(angle)) * 0.28 * s
                ))
                path.addLine(to: CGPoint(
                    x: center.x + CGFloat(cos(angle)) * 0.38 * s,
                    y: center.y + CGFloat(sin(angle)) * 0.38 * s
                ))
                context.stroke(path, with: shading, style: stroke)
            }

        case .share:
            circle(0.72, 0.22, radius: 0.1, filled: false)
            circle(0.28, 0.5, radius: 0.1, filled: false)
            circle(0.72, 0.78, radius: 0.1, filled: false)
            line(0.37, 0.44, 0.63, 0.28)
            line(0.37, 0.56, 0.63, 0.72)

        case .bell:
            curve({ path in
                path.move(to: p(0.26, 0.66))
                path.addLine(to: p(0.26, 0.46))
                path.addCurve(to: p(0.74, 0.46), control1: p(0.26, 0.26), control2: p(0.74, 0.26))
                path.addLine(to: p(0.74, 0.66))
                path.closeSubpath()
            }, filled: false)
            line(0.18, 0.66, 0.82, 0.66)
            circle(0.5, 0.8, radius: 0.06, filled: true)

        case .calendar:
            let body = Path(
                roundedRect: CGRect(x: 0.18 * s, y: 0.26 * s, width: 0.64 * s, height: 0.56 * s),
                cornerRadius: 0.1 * s
            )
            context.stroke(body, with: shading, style: stroke)
            line(0.18, 0.44, 0.82, 0.44)
            line(0.34, 0.18, 0.34, 0.3)
            line(0.66, 0.18, 0.66, 0.3)

        case .flame:
            curve({ path in
                path.move(to: p(0.5, 0.16))
                path.addCurve(to: p(0.72, 0.7), control1: p(0.74, 0.38), control2: p(0.82, 0.56))
                path.addCurve(to: p(0.28, 0.7), control1: p(0.64, 0.82), control2: p(0.36, 0.82))
                path.addCurve(to: p(0.42, 0.34), control1: p(0.18, 0.56), control2: p(0.3, 0.42))
                path.addCurve(to: p(0.5, 0.44), control1: p(0.42, 0.46), control2: p(0.48, 0.5))
                path.closeSubpath()
            }, filled: true)

        case .sparkle:
            curve({ path in
                path.move(to: p(0.5, 0.12))
                path.addCurve(to: p(0.88, 0.5), control1: p(0.56, 0.4), control2: p(0.6, 0.44))
                path.addCurve(to: p(0.5, 0.88), control1: p(0.6, 0.56), control2: p(0.56, 0.6))
                path.addCurve(to: p(0.12, 0.5), control1: p(0.44, 0.6), control2: p(0.4, 0.56))
                path.addCurve(to: p(0.5, 0.12), control1: p(0.4, 0.44), control2: p(0.44, 0.4))
                path.closeSubpath()
            }, filled: true)

        case .heart:
            curve({ path in
                path.move(to: p(0.5, 0.8))
                path.addCurve(to: p(0.5, 0.36), control1: p(0.1, 0.55), control2: p(0.18, 0.22))
                path.addCurve(to: p(0.5, 0.8), control1: p(0.82, 0.22), control2: p(0.9, 0.55))
                path.closeSubpath()
            }, filled: true)

        case .globe:
            circle(0.5, 0.5, radius: 0.32, filled: false)
            line(0.18, 0.5, 0.82, 0.5)
            curve({ path in
                path.move(to: p(0.5, 0.18))
                path.addCurve(to: p(0.5, 0.82), control1: p(0.28, 0.34), control2: p(0.28, 0.66))
                path.addCurve(to: p(0.5, 0.18), control1: p(0.72, 0.66), control2: p(0.72, 0.34))
                path.closeSubpath()
            }, filled: false)

        case .download:
            line(0.5, 0.18, 0.5, 0.6)
            poly([(0.32, 0.44), (0.5, 0.62), (0.68, 0.44)])
            line(0.22, 0.78, 0.78, 0.78)

        case .trash:
            line(0.18, 0.3, 0.82, 0.3)
            line(0.4, 0.3, 0.4, 0.2)
            line(0.6, 0.3, 0.6, 0.2)
            line(0.4, 0.2, 0.6, 0.2)
            poly([(0.28, 0.3), (0.33, 0.82), (0.67, 0.82), (0.72, 0.3)])
            line(0.44, 0.42, 0.46, 0.7)
            line(0.56, 0.42, 0.54, 0.7)

        case .info:
            circle(0.5, 0.5, radius: 0.32, filled: false)
            circle(0.5, 0.32, radius: 0.045, filled: true)
            line(0.5, 0.45, 0.5, 0.7)

        case .palette:
            circle(0.5, 0.5, radius: 0.32, filled: false)
            circle(0.38, 0.36, radius: 0.055, filled: true)
            circle(0.62, 0.36, radius: 0.055, filled: true)
            circle(0.34, 0.6, radius: 0.055, filled: true)
            circle(0.58, 0.66, radius: 0.055, filled: true)

        case .volume:
            poly([(0.2, 0.4), (0.34, 0.4), (0.5, 0.24), (0.5, 0.76), (0.34, 0.6), (0.2, 0.6)])
            arc(x: 0.42, y: 0.3, width: 0.34, height: 0.4, start: -50, sweep: 100)
            arc(x: 0.46, y: 0.2, width: 0.5, height: 0.6, start: -50, sweep: 100)

        case .clock:
            circle(0.5, 0.5, radius: 0.32, filled: false)
            line(0.5, 0.5, 0.5, 0.3)
            line(0.5, 0.5, 0.64, 0.58)

        case .refresh:
            arc(x: 0.2, y: 0.2, width: 0.6, height: 0.6, start: 40, sweep: 280)
            poly([(0.66, 0.52), (0.76, 0.72), (0.9, 0.56)])

        case .target:
            circle(0.5, 0.5, radius: 0.32, filled: false)
            circle(0.5, 0.5, radius: 0.18, filled: false)
            circle(0.5, 0.5, radius: 0.06, filled: true)

        case .dice:
            let body = Path(
                roundedRect: CGRect(x: 0.16 * s, y: 0.16 * s, width: 0.68 * s, height: 0.68 * s),
                cornerRadius: 0.16 * s
            )
            context.stroke(body, with: shading, style: stroke)
            for pip in [(0.33, 0.33), (0.67, 0.33), (0.5, 0.5), (0.33, 0.67), (0.67, 0.67)] {
                circle(pip.0, pip.1, radius: 0.055, filled: true)
            }
        }
    }
}
