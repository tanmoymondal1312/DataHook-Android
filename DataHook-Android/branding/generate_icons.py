#!/usr/bin/env python3
"""Regenerate every app icon from branding/logo-source.webp.

Run from the Android project root:  python branding/generate_icons.py
Requires Pillow. See branding/README.md for what each output is used for.
"""

from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "branding/logo-source.webp"
RES = ROOT / "app/src/main/res"

# Adaptive icons mask away the outer third, so the artwork lives in the middle.
ADAPTIVE_SAFE_FRACTION = 0.66
LEGACY_LOGO_FRACTION = 0.72
NOTIFICATION_FRACTION = 0.92

ADAPTIVE_PX = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}
LEGACY_PX = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
NOTIFICATION_PX = {"mdpi": 24, "hdpi": 36, "xhdpi": 48, "xxhdpi": 72, "xxxhdpi": 96}


def save(image, path, lossless=False):
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path, "WEBP", quality=90, lossless=lossless, method=6)
    return path.stat().st_size


def centred(source, canvas_px, fraction):
    """The logo centred on a transparent square canvas."""
    canvas = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    target = max(1, int(canvas_px * fraction))
    logo = source.resize((target, target), Image.LANCZOS)
    offset = (canvas_px - target) // 2
    canvas.paste(logo, (offset, offset), logo)
    return canvas


def circular_mask(size):
    # Drawn oversized and downsampled so the edge is antialiased.
    mask = Image.new("L", (size * 4, size * 4), 0)
    ImageDraw.Draw(mask).ellipse((0, 0, size * 4 - 1, size * 4 - 1), fill=255)
    return mask.resize((size, size), Image.LANCZOS)


def main():
    source = Image.open(SOURCE).convert("RGBA")
    total = 0

    total += save(source.resize((512, 512), Image.LANCZOS), RES / "drawable-nodpi/dh_logo.webp")

    for dpi, px in ADAPTIVE_PX.items():
        total += save(
            centred(source, px, ADAPTIVE_SAFE_FRACTION),
            RES / f"mipmap-{dpi}/ic_launcher_foreground.webp",
        )

    for dpi, px in LEGACY_PX.items():
        base = Image.new("RGBA", (px, px), (255, 255, 255, 255))
        logo = source.resize((int(px * LEGACY_LOGO_FRACTION),) * 2, Image.LANCZOS)
        offset = (px - logo.width) // 2
        base.paste(logo, (offset, offset), logo)
        total += save(base, RES / f"mipmap-{dpi}/ic_launcher.webp")

        rounded = base.copy()
        rounded.putalpha(circular_mask(px))
        total += save(rounded, RES / f"mipmap-{dpi}/ic_launcher_round.webp")

    for dpi, px in NOTIFICATION_PX.items():
        shaped = centred(source, px, NOTIFICATION_FRACTION)
        # Android ignores colour here and renders the alpha as a flat silhouette.
        white = Image.new("RGBA", shaped.size, (255, 255, 255, 255))
        white.putalpha(shaped.getchannel("A"))
        total += save(white, RES / f"drawable-{dpi}/ic_notification.webp", lossless=True)

    print(f"Regenerated all icons — {total / 1024:.1f} KB total")


if __name__ == "__main__":
    main()
