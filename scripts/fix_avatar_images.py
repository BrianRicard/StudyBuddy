#!/usr/bin/env python3
"""
Fix avatar PNG images: remove white backgrounds using flood-fill from corners,
then resize back to canonical dimensions.

Usage:
    python3 scripts/fix_avatar_images.py
    python3 scripts/fix_avatar_images.py --dry-run
"""

import os
import sys
from collections import deque
from PIL import Image

DRAWABLE_BASE = "core/core-ui/src/main/res"
DENSITIES = {
    "drawable-mdpi": 200,
    "drawable-hdpi": 300,
    "drawable-xhdpi": 400,
    "drawable-xxhdpi": 600,
    "drawable-xxxhdpi": 800,
}

# Threshold: pixels with R,G,B all above this are considered "white-ish"
WHITE_THRESHOLD = 240
# Anti-alias: pixels near the fill boundary get partial transparency
AA_THRESHOLD = 220


def has_white_background(img):
    """Check if image has significant opaque white regions near edges."""
    if img.mode != "RGBA":
        return True
    pixels = img.load()
    w, h = img.size

    # Sample a band 3-30 pixels inside each edge
    white_count = 0
    total = 0
    for offset in range(3, min(31, w // 4)):
        for x in range(0, w, max(1, w // 20)):
            for y_pos in [offset, h - 1 - offset]:
                r, g, b, a = pixels[x, y_pos]
                total += 1
                if r > WHITE_THRESHOLD and g > WHITE_THRESHOLD and b > WHITE_THRESHOLD and a > 200:
                    white_count += 1
        for y in range(0, h, max(1, h // 20)):
            for x_pos in [offset, w - 1 - offset]:
                r, g, b, a = pixels[x_pos, y]
                total += 1
                if r > WHITE_THRESHOLD and g > WHITE_THRESHOLD and b > WHITE_THRESHOLD and a > 200:
                    white_count += 1

    return total > 0 and (white_count / total) > 0.15


def flood_fill_remove_background(img):
    """
    Flood-fill from all 4 corners to remove white/near-white background.
    Only removes pixels connected to the outer edge.
    """
    img = img.convert("RGBA")
    pixels = img.load()
    w, h = img.size

    visited = [[False] * h for _ in range(w)]
    to_clear = set()
    queue = deque()

    def is_background(r, g, b, a):
        """Pixel is white-ish and opaque, or fully transparent."""
        if a == 0:
            return True
        return r > WHITE_THRESHOLD and g > WHITE_THRESHOLD and b > WHITE_THRESHOLD and a > 200

    # Seed from all edge pixels
    for x in range(w):
        for y in [0, h - 1]:
            if not visited[x][y]:
                visited[x][y] = True
                queue.append((x, y))
    for y in range(h):
        for x in [0, w - 1]:
            if not visited[x][y]:
                visited[x][y] = True
                queue.append((x, y))

    # BFS: flood through transparent + white pixels; only clear white ones
    while queue:
        cx, cy = queue.popleft()
        r, g, b, a = pixels[cx, cy]
        if r > WHITE_THRESHOLD and g > WHITE_THRESHOLD and b > WHITE_THRESHOLD and a > 0:
            to_clear.add((cx, cy))

        for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
            nx, ny = cx + dx, cy + dy
            if 0 <= nx < w and 0 <= ny < h and not visited[nx][ny]:
                nr, ng, nb, na = pixels[nx, ny]
                if is_background(nr, ng, nb, na):
                    visited[nx][ny] = True
                    queue.append((nx, ny))

    # Clear background pixels
    for x, y in to_clear:
        pixels[x, y] = (0, 0, 0, 0)

    # Anti-alias: soften edges adjacent to cleared pixels
    for x, y in to_clear:
        for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1),
                        (-1, -1), (1, -1), (-1, 1), (1, 1)]:
            nx, ny = x + dx, y + dy
            if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in to_clear and pixels[nx, ny][3] > 0:
                r, g, b, a = pixels[nx, ny]
                if r > AA_THRESHOLD and g > AA_THRESHOLD and b > AA_THRESHOLD and a > 0:
                    # Make semi-transparent based on how white it is
                    whiteness = min(r, g, b)
                    new_alpha = max(0, int(a * (1.0 - (whiteness - AA_THRESHOLD) / (256 - AA_THRESHOLD))))
                    pixels[nx, ny] = (r, g, b, new_alpha)

    return img


def process_image(path, target_size, dry_run=False):
    """Process a single avatar image. Returns True if changes were made."""
    img = Image.open(path)

    if not has_white_background(img):
        return False

    if dry_run:
        print(f"  WOULD FIX: {os.path.basename(path)} ({img.size[0]}x{img.size[1]})")
        return True

    # Remove white background
    fixed = flood_fill_remove_background(img)

    # Resize to target if needed (should already be correct, but ensure)
    if fixed.size != (target_size, target_size):
        fixed = fixed.resize((target_size, target_size), Image.LANCZOS)

    fixed.save(path, optimize=True)
    print(f"  FIXED: {os.path.basename(path)} ({img.size[0]}x{img.size[1]})")
    return True


def main():
    dry_run = "--dry-run" in sys.argv
    base = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), DRAWABLE_BASE)

    total_fixed = 0

    for density, target_size in DENSITIES.items():
        density_dir = os.path.join(base, density)
        if not os.path.isdir(density_dir):
            continue

        print(f"\n{density} ({target_size}x{target_size}):")
        fixed_count = 0

        for fname in sorted(os.listdir(density_dir)):
            if not fname.startswith("avatar_") or not fname.endswith(".png"):
                continue

            path = os.path.join(density_dir, fname)
            if process_image(path, target_size, dry_run):
                fixed_count += 1

        if fixed_count == 0:
            print("  All images OK")
        total_fixed += fixed_count

    mode = "Would fix" if dry_run else "Fixed"
    print(f"\n{mode} {total_fixed} images total.")


if __name__ == "__main__":
    main()
