#!/usr/bin/env python3
"""Generate Rétrocade's Google Play icon and feature graphic.

The artwork is procedural: no external image is read. Only Pillow and a font
available on the build machine are required. The generated PNGs are deterministic
for a given Pillow/font version.
"""

from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

BACKGROUND = (7, 10, 18)
SURFACE = (13, 20, 34)
GRID = (27, 42, 68)
TEXT = (234, 248, 255)
MUTED = (155, 179, 199)
GREEN = (57, 255, 182)
MAGENTA = (255, 63, 164)
AMBER = (255, 200, 87)
CYAN = (70, 230, 255)

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_OUTPUT = REPO_ROOT / "docs" / "release" / "assets"


def load_font(size: int, bold: bool = True) -> ImageFont.FreeTypeFont:
    candidates = [
        Path("/usr/share/fonts/truetype/dejavu/DejaVuSansMono-Bold.ttf" if bold
             else "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf"),
        Path("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold
             else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
    ]
    for candidate in candidates:
        if candidate.is_file():
            return ImageFont.truetype(str(candidate), size=size)
    return ImageFont.load_default()


def draw_grid(draw: ImageDraw.ImageDraw, width: int, height: int, step: int = 28) -> None:
    for x in range(0, width, step):
        draw.line((x, 0, x, height), fill=(*GRID, 90), width=1)
    for y in range(0, height, step):
        draw.line((0, y, width, y), fill=(*GRID, 90), width=1)


def draw_block(draw: ImageDraw.ImageDraw, x: int, y: int, size: int, color: tuple[int, int, int]) -> None:
    inset = max(2, size // 12)
    draw.rounded_rectangle(
        (x + inset, y + inset, x + size - inset, y + size - inset),
        radius=max(2, size // 10),
        fill=color,
    )
    highlight = tuple(min(255, channel + 55) for channel in color)
    draw.rectangle(
        (x + inset + 3, y + inset + 3, x + size - inset - 3, y + inset + max(4, size // 8)),
        fill=highlight,
    )


def draw_tetromino(
    draw: ImageDraw.ImageDraw,
    origin_x: int,
    origin_y: int,
    cell: int,
    cells: list[tuple[int, int]],
    color: tuple[int, int, int],
    glow: bool = True,
) -> None:
    if glow:
        for col, row in cells:
            draw_block(draw, origin_x + col * cell - 3, origin_y + row * cell - 3, cell + 6, color)
    for col, row in cells:
        draw_block(draw, origin_x + col * cell, origin_y + row * cell, cell, color)


def generate_icon(path: Path) -> None:
    size = 512
    image = Image.new("RGB", (size, size), BACKGROUND)
    draw = ImageDraw.Draw(image, "RGBA")
    draw_grid(draw, size, size, 32)

    # Soft neon halo behind the block emblem.
    halo = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    halo_draw = ImageDraw.Draw(halo)
    halo_draw.ellipse((86, 86, 426, 426), fill=(*GREEN, 28))
    halo_draw.ellipse((140, 140, 372, 372), fill=(*GREEN, 24))
    image = Image.alpha_composite(image.convert("RGBA"), halo)
    draw = ImageDraw.Draw(image, "RGBA")

    cell = 72
    origin_x = 112
    origin_y = 152

    # A compact Tetris-like emblem, kept inside the adaptive-icon safe zone.
    draw_tetromino(
        draw,
        origin_x,
        origin_y,
        cell,
        [(0, 0), (1, 0), (0, 1), (0, 2)],
        GREEN,
    )
    draw_tetromino(
        draw,
        origin_x + 2 * cell,
        origin_y,
        cell,
        [(0, 0), (1, 0), (1, 1), (1, 2)],
        CYAN,
        glow=False,
    )
    draw_tetromino(
        draw,
        origin_x + cell,
        origin_y + 2 * cell,
        cell,
        [(0, 0), (1, 0), (2, 0), (3, 0)],
        AMBER,
        glow=False,
    )
    draw.rectangle(
        (origin_x + cell, origin_y + 3 * cell + 24, origin_x + 3 * cell, origin_y + 3 * cell + 36),
        fill=MAGENTA,
    )

    # Subtle scanline pass for the retro CRT feel.
    for y in range(0, size, 4):
        draw.line((0, y, size, y), fill=(0, 0, 0, 18), width=1)

    # Google Play expects a 32-bit PNG with an alpha channel, even if fully opaque.
    image.convert("RGBA").save(path, format="PNG", optimize=True)
    _check_png(path, 512, 512, mode="RGBA")


def generate_feature_graphic(path: Path) -> None:
    width, height = 1024, 500
    image = Image.new("RGB", (width, height), BACKGROUND)
    draw = ImageDraw.Draw(image, "RGBA")
    draw_grid(draw, width, height, 28)

    # Right-side playfield panel.
    panel = (638, 56, 962, 444)
    draw.rounded_rectangle(panel, radius=10, fill=(*SURFACE, 225), outline=(*GREEN, 120), width=2)
    for x in range(panel[0] + 18, panel[2] - 8, 42):
        draw.line((x, panel[1] + 12, x, panel[3] - 12), fill=(*GRID, 130), width=1)
    for y in range(panel[1] + 18, panel[3] - 8, 42):
        draw.line((panel[0] + 12, y, panel[2] - 12, y), fill=(*GRID, 130), width=1)

    cell = 42
    base_x, base_y = 680, 106
    draw_tetromino(
        draw,
        base_x,
        base_y + 3 * cell,
        cell,
        [(0, 0), (1, 0), (2, 0), (3, 0)],
        AMBER,
        glow=False,
    )
    draw_tetromino(
        draw,
        base_x + 3 * cell,
        base_y + 2 * cell,
        cell,
        [(0, 0), (1, 0), (1, 1), (1, 2)],
        CYAN,
        glow=False,
    )
    draw_tetromino(
        draw,
        base_x + 4 * cell,
        base_y + 2 * cell,
        cell,
        [(0, 0), (0, 1), (1, 1), (0, 2)],
        GREEN,
        glow=False,
    )
    draw_tetromino(
        draw,
        base_x + 2 * cell,
        base_y + 4 * cell,
        cell,
        [(0, 0), (1, 0), (0, 1), (0, 2)],
        MAGENTA,
        glow=False,
    )
    draw_tetromino(
        draw,
        base_x + 5 * cell,
        base_y,
        cell,
        [(0, 0), (0, 1), (1, 1), (1, 2)],
        GREEN,
        glow=False,
    )

    # Left-side brand block.
    draw.rectangle((62, 66, 74, 118), fill=GREEN)
    draw.text((92, 62), "ARCADE SYSTEM", font=load_font(20), fill=MUTED)
    draw.text((90, 110), "RÉTROCADE", font=load_font(78), fill=GREEN)
    draw.text((94, 206), "TETRIS RÉTRO", font=load_font(38), fill=TEXT)
    draw.text((96, 262), "SANS ABONNEMENT", font=load_font(26, bold=False), fill=CYAN)
    draw.line((96, 310, 540, 310), fill=(*GRID, 230), width=2)
    draw.text((96, 330), "Achat unique  ·  Scores locaux", font=load_font(20, bold=False), fill=MUTED)
    draw.text((96, 360), "Hors ligne  ·  Français", font=load_font(20, bold=False), fill=MUTED)

    # Keep all text inside Play's feature-graphic safe area.
    for y in range(0, height, 4):
        draw.line((0, y, width, y), fill=(0, 0, 0, 14), width=1)

    image.save(path, format="PNG", optimize=True)
    _check_png(path, width, height, mode="RGB")


def _check_png(path: Path, width: int, height: int, mode: str) -> None:
    with Image.open(path) as image:
        if image.size != (width, height):
            raise RuntimeError(f"{path}: expected {width}x{height}, got {image.size}")
        if image.format != "PNG":
            raise RuntimeError(f"{path}: expected PNG, got {image.format}")
        if image.mode != mode:
            raise RuntimeError(f"{path}: expected {mode}, got {image.mode}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=DEFAULT_OUTPUT,
        help=f"destination directory (default: {DEFAULT_OUTPUT})",
    )
    args = parser.parse_args()
    args.output_dir.mkdir(parents=True, exist_ok=True)

    icon = args.output_dir / "icon-512.png"
    feature = args.output_dir / "feature-graphic-1024x500.png"
    generate_icon(icon)
    generate_feature_graphic(feature)
    print(f"Wrote {icon}")
    print(f"Wrote {feature}")


if __name__ == "__main__":
    main()
