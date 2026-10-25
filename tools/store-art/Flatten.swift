#!/usr/bin/env swift
// Flatten.swift <in.png> <out.png> <width> <height>
// Re-encodes a PNG without an alpha channel (App Store rejects alpha; Play wants 24-bit) and exits 1 if the
// input is not exactly width x height, so a wrong-sized render is caught here rather than at upload.
import Foundation
import CoreGraphics
import ImageIO
import UniformTypeIdentifiers

let args = CommandLine.arguments
guard args.count == 5, let width = Int(args[3]), let height = Int(args[4]) else {
    FileHandle.standardError.write(Data("usage: Flatten.swift <in.png> <out.png> <width> <height>\n".utf8))
    exit(2)
}
guard let source = CGImageSourceCreateWithURL(URL(fileURLWithPath: args[1]) as CFURL, nil),
      let image = CGImageSourceCreateImageAtIndex(source, 0, nil) else {
    FileHandle.standardError.write(Data("cannot read \(args[1])\n".utf8))
    exit(1)
}
guard image.width == width, image.height == height else {
    FileHandle.standardError.write(Data("\(args[1]) is \(image.width)x\(image.height), expected \(width)x\(height)\n".utf8))
    exit(1)
}
// Opaque context in the brand background: anything transparent lands on it, never on black.
guard let context = CGContext(data: nil, width: width, height: height, bitsPerComponent: 8, bytesPerRow: 0,
                              space: CGColorSpace(name: CGColorSpace.sRGB)!,
                              bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue) else { exit(1) }
context.setFillColor(CGColor(srgbRed: 7 / 255, green: 7 / 255, blue: 12 / 255, alpha: 1))
context.fill(CGRect(x: 0, y: 0, width: width, height: height))
context.draw(image, in: CGRect(x: 0, y: 0, width: width, height: height))
guard let flat = context.makeImage(),
      let dest = CGImageDestinationCreateWithURL(URL(fileURLWithPath: args[2]) as CFURL, UTType.png.identifier as CFString, 1, nil)
else { exit(1) }
CGImageDestinationAddImage(dest, flat, nil)
guard CGImageDestinationFinalize(dest) else { exit(1) }
