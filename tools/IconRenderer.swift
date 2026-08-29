#!/usr/bin/env swift
//
// Generates every app-icon asset from one description of the mark.
//
//   swift tools/IconRenderer.swift <repo-root>
//
// The mark is a geometric "1%" — the app's wordmark — drawn as paths rather
// than typeset. Two reasons: the whole app draws its artwork from hand-authored
// path data already, and outlines taken from a system font could not be shipped
// inside an Android APK without a licence problem.
//
// One path definition feeds all three outputs, so the platforms cannot drift:
//   • PNGs for the iOS app icon and the Android legacy launcher icons
//   • Android `pathData` strings, printed for pasting into the vector drawables
//

import Foundation
import CoreGraphics
import ImageIO
import UniformTypeIdentifiers

// MARK: - Palette (mirrors OddlyColors)

let background = CGColor(red: 0x07 / 255, green: 0x07 / 255, blue: 0x0C / 255, alpha: 1)
let surface = CGColor(red: 0x16 / 255, green: 0x14 / 255, blue: 0x24 / 255, alpha: 1)
let brandStops: [CGColor] = [
    CGColor(red: 0xFF / 255, green: 0x7E / 255, blue: 0xB3 / 255, alpha: 1), // pink
    CGColor(red: 0xE8 / 255, green: 0x79 / 255, blue: 0xF9 / 255, alpha: 1), // magenta
    CGColor(red: 0xA7 / 255, green: 0x8B / 255, blue: 0xFA / 255, alpha: 1), // purple
    CGColor(red: 0x60 / 255, green: 0xA5 / 255, blue: 0xFA / 255, alpha: 1), // blue
]
let purpleGlow = CGColor(red: 0xA7 / 255, green: 0x8B / 255, blue: 0xFA / 255, alpha: 0.30)

// MARK: - The mark, authored in a 108×108 space

/// The "1", as one closed outline: stem, head, and the angled flag.
func oneGlyph() -> CGPath {
    let path = CGMutablePath()
    path.move(to: CGPoint(x: 44, y: 36))
    path.addLine(to: CGPoint(x: 44, y: 72))
    path.addLine(to: CGPoint(x: 34, y: 72))
    path.addLine(to: CGPoint(x: 34, y: 47.5))
    path.addLine(to: CGPoint(x: 26, y: 52))
    path.addLine(to: CGPoint(x: 26, y: 43))
    path.addLine(to: CGPoint(x: 36, y: 36))
    path.closeSubpath()
    return path
}

/// The "%": two rings and a slash. Filled even-odd so the rings keep their holes.
func percentGlyph() -> CGPath {
    let path = CGMutablePath()

    func ring(centre: CGPoint, outer: CGFloat, inner: CGFloat) {
        path.addEllipse(in: CGRect(x: centre.x - outer, y: centre.y - outer,
                                   width: outer * 2, height: outer * 2))
        path.addEllipse(in: CGRect(x: centre.x - inner, y: centre.y - inner,
                                   width: inner * 2, height: inner * 2))
    }

    ring(centre: CGPoint(x: 58, y: 44), outer: 8, inner: 3.6)
    ring(centre: CGPoint(x: 76, y: 64), outer: 8, inner: 3.6)

    // The slash, as a filled quad so its ends stay square against the rings.
    let from = CGPoint(x: 55, y: 71)
    let to = CGPoint(x: 79, y: 37)
    let along = CGVector(dx: to.x - from.x, dy: to.y - from.y)
    let length = (along.dx * along.dx + along.dy * along.dy).squareRoot()
    let half: CGFloat = 3.6
    let offset = CGVector(dx: -along.dy / length * half, dy: along.dx / length * half)

    path.move(to: CGPoint(x: from.x - offset.dx, y: from.y - offset.dy))
    path.addLine(to: CGPoint(x: from.x + offset.dx, y: from.y + offset.dy))
    path.addLine(to: CGPoint(x: to.x + offset.dx, y: to.y + offset.dy))
    path.addLine(to: CGPoint(x: to.x - offset.dx, y: to.y - offset.dy))
    path.closeSubpath()

    return path
}

/// The whole mark, scaled to sit inside the adaptive icon's safe circle.
///
/// Android crops an adaptive icon to any shape it likes within a 66/108 circle,
/// so the mark is fitted to that rather than to the full canvas.
func markPath(canvas: CGFloat, safeFraction: CGFloat = 66.0 / 108.0) -> CGPath {
    let combined = CGMutablePath()
    combined.addPath(oneGlyph())
    combined.addPath(percentGlyph())

    let bounds = combined.boundingBox
    // Largest box that fits in the safe circle with this aspect ratio.
    let safeRadius = canvas * safeFraction / 2
    let diagonal = (bounds.width * bounds.width + bounds.height * bounds.height).squareRoot()
    let scale = (safeRadius * 2) / diagonal

    var transform = CGAffineTransform.identity
        .translatedBy(x: canvas / 2, y: canvas / 2)
        .scaledBy(x: scale, y: scale)
        .translatedBy(x: -bounds.midX, y: -bounds.midY)

    return combined.copy(using: &transform) ?? combined
}

// MARK: - Drawing

/// Deterministic starfield, echoing the app's own backdrop.
let stars: [(x: CGFloat, y: CGFloat, r: CGFloat, a: CGFloat)] = [
    (0.11, 0.17, 0.006, 0.55), (0.27, 0.09, 0.004, 0.35), (0.44, 0.20, 0.005, 0.28),
    (0.83, 0.13, 0.006, 0.50), (0.92, 0.31, 0.004, 0.30), (0.07, 0.42, 0.004, 0.32),
    (0.19, 0.71, 0.005, 0.38), (0.36, 0.88, 0.004, 0.26), (0.62, 0.93, 0.005, 0.40),
    (0.88, 0.78, 0.006, 0.45), (0.95, 0.58, 0.004, 0.24), (0.05, 0.86, 0.004, 0.30),
    (0.71, 0.06, 0.004, 0.28), (0.50, 0.04, 0.004, 0.22),
]

func drawBackground(_ context: CGContext, size: CGFloat) {
    context.setFillColor(background)
    context.fill(CGRect(x: 0, y: 0, width: size, height: size))

    // A cool lift from the bottom-left so the tile is not flat black.
    if let gradient = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(),
                                 colors: [surface, background] as CFArray,
                                 locations: [0, 1]) {
        context.saveGState()
        context.drawLinearGradient(
            gradient,
            start: CGPoint(x: 0, y: 0),
            end: CGPoint(x: size, y: size),
            options: []
        )
        context.restoreGState()
    }

    // Purple bloom behind the mark, the same glow the hero screens use.
    if let glow = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(),
                             colors: [purpleGlow, background.copy(alpha: 0)!] as CFArray,
                             locations: [0, 1]) {
        context.saveGState()
        context.drawRadialGradient(
            glow,
            startCenter: CGPoint(x: size * 0.5, y: size * 0.56), startRadius: 0,
            endCenter: CGPoint(x: size * 0.5, y: size * 0.56), endRadius: size * 0.52,
            options: []
        )
        context.restoreGState()
    }

    for star in stars {
        context.setFillColor(CGColor(red: 1, green: 1, blue: 1, alpha: star.a))
        let r = star.r * size
        context.fillEllipse(in: CGRect(x: star.x * size - r, y: star.y * size - r,
                                       width: r * 2, height: r * 2))
    }
}

func drawMark(_ context: CGContext, size: CGFloat, safeFraction: CGFloat, gradient: Bool) {
    // The mark is authored y-down; flip so it lands the right way up.
    context.saveGState()
    context.translateBy(x: 0, y: size)
    context.scaleBy(x: 1, y: -1)

    let mark = markPath(canvas: size, safeFraction: safeFraction)
    context.addPath(mark)

    if gradient {
        context.clip(using: .evenOdd)
        if let brand = CGGradient(colorsSpace: CGColorSpaceCreateDeviceRGB(),
                                  colors: brandStops as CFArray,
                                  locations: [0, 0.35, 0.68, 1]) {
            // Spanning the mark rather than the tile: across the whole canvas
            // the sweep is mostly clipped away and only its middle shows.
            let box = mark.boundingBox
            context.drawLinearGradient(
                brand,
                start: CGPoint(x: box.minX, y: box.minY),
                end: CGPoint(x: box.maxX, y: box.maxY),
                options: [.drawsBeforeStartLocation, .drawsAfterEndLocation]
            )
        }
    } else {
        context.setFillColor(CGColor(red: 1, green: 1, blue: 1, alpha: 1))
        context.fillPath(using: .evenOdd)
    }
    context.restoreGState()
}

enum Variant {
    /// Opaque brand tile: the App Store and home-screen icon.
    case full
    /// The mark alone on transparency: the adaptive icon's foreground layer.
    case markOnly
    /// The backdrop alone: the adaptive icon's background layer.
    case backdrop
    /// Grayscale on transparent, for iOS's tinted appearance.
    case tinted
}

func render(size: Int, variant: Variant, safeFraction: CGFloat, to url: URL) {
    let side = CGFloat(size)
    guard let context = CGContext(
        data: nil,
        width: size, height: size,
        bitsPerComponent: 8, bytesPerRow: 0,
        space: CGColorSpaceCreateDeviceRGB(),
        bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
    ) else { fatalError("could not create context") }

    context.interpolationQuality = .high
    context.setShouldAntialias(true)

    switch variant {
    case .full:
        drawBackground(context, size: side)
        drawMark(context, size: side, safeFraction: safeFraction, gradient: true)
    case .markOnly:
        drawMark(context, size: side, safeFraction: safeFraction, gradient: true)
    case .backdrop:
        drawBackground(context, size: side)
    case .tinted:
        drawMark(context, size: side, safeFraction: safeFraction, gradient: false)
    }

    guard
        let image = context.makeImage(),
        let destination = CGImageDestinationCreateWithURL(
            url as CFURL, UTType.png.identifier as CFString, 1, nil
        )
    else { fatalError("could not write \(url.lastPathComponent)") }

    CGImageDestinationAddImage(destination, image, nil)
    CGImageDestinationFinalize(destination)
    print("  wrote \(url.lastPathComponent) (\(size)px)")
}

// MARK: - Android vector path data

/// Walks the mark and prints it as an Android `pathData` string.
func androidPathData(canvas: CGFloat, safeFraction: CGFloat) -> String {
    var out = ""
    func fmt(_ value: CGFloat) -> String {
        let rounded = (value * 100).rounded() / 100
        return rounded == rounded.rounded()
            ? String(Int(rounded))
            : String(format: "%g", rounded)
    }

    markPath(canvas: canvas, safeFraction: safeFraction).applyWithBlock { element in
        let points = element.pointee.points
        switch element.pointee.type {
        case .moveToPoint:
            out += "M\(fmt(points[0].x)),\(fmt(points[0].y)) "
        case .addLineToPoint:
            out += "L\(fmt(points[0].x)),\(fmt(points[0].y)) "
        case .addQuadCurveToPoint:
            out += "Q\(fmt(points[0].x)),\(fmt(points[0].y)) \(fmt(points[1].x)),\(fmt(points[1].y)) "
        case .addCurveToPoint:
            out += "C\(fmt(points[0].x)),\(fmt(points[0].y)) "
            out += "\(fmt(points[1].x)),\(fmt(points[1].y)) "
            out += "\(fmt(points[2].x)),\(fmt(points[2].y)) "
        case .closeSubpath:
            out += "Z "
        @unknown default:
            break
        }
    }
    return out.trimmingCharacters(in: .whitespaces)
}

// MARK: - Main

let root = URL(fileURLWithPath: CommandLine.arguments.count > 1 ? CommandLine.arguments[1] : ".")
let fm = FileManager.default

func ensure(_ url: URL) -> URL {
    try? fm.createDirectory(at: url, withIntermediateDirectories: true)
    return url
}

// iOS — one 1024 tile per appearance.
print("iOS app icon:")
let appIcon = ensure(root.appendingPathComponent("iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"))
render(size: 1024, variant: .full, safeFraction: 0.82, to: appIcon.appendingPathComponent("icon-light.png"))
render(size: 1024, variant: .full, safeFraction: 0.82, to: appIcon.appendingPathComponent("icon-dark.png"))
render(size: 1024, variant: .tinted, safeFraction: 0.82, to: appIcon.appendingPathComponent("icon-tinted.png"))

// Android.
//
// The adaptive layers are bitmaps rather than vectors because AGP 9's resource
// pipeline silently drops a vector carrying an inline `aapt:attr` gradient —
// the file compiles standalone under aapt2 but never reaches the merged
// resources, and the build then fails with "resource not found". Bitmaps also
// guarantee the tile is pixel-identical to the iOS icon. The monochrome layer
// stays a vector: it is a flat silhouette, so it needs no gradient.
print("Android launcher icons:")
let densities: [(String, Int)] = [
    ("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192),
]
for (density, legacySize) in densities {
    let dir = ensure(root.appendingPathComponent("androidApp/src/main/res/mipmap-\(density)"))

    // Legacy icons (API 24–25) are unmasked, so the mark can fill more of them.
    render(size: legacySize, variant: .full, safeFraction: 0.76,
           to: dir.appendingPathComponent("ic_launcher.png"))
    render(size: legacySize, variant: .full, safeFraction: 0.76,
           to: dir.appendingPathComponent("ic_launcher_round.png"))

    // Adaptive layers are authored on a 108dp canvas, of which only the middle
    // 66dp is guaranteed to survive the launcher's mask.
    let adaptiveSize = legacySize * 108 / 48
    render(size: adaptiveSize, variant: .markOnly, safeFraction: 66.0 / 108.0,
           to: dir.appendingPathComponent("ic_launcher_foreground.png"))
    render(size: adaptiveSize, variant: .backdrop, safeFraction: 1,
           to: dir.appendingPathComponent("ic_launcher_background.png"))
}

print()
print("Android monochrome pathData (108×108 viewport, safe circle):")
print(androidPathData(canvas: 108, safeFraction: 66.0 / 108.0))
print()
print("Android notification pathData (24×24 viewport, full bleed):")
print(androidPathData(canvas: 24, safeFraction: 0.92))
