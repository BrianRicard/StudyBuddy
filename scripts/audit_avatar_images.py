#!/usr/bin/env python3
"""Audit avatar PNG images for white backgrounds and sizing issues."""

import os
import sys
from PIL import Image

DRAWABLE_BASE = "core/core-ui/src/main/res"
DENSITIES = ["drawable-mdpi", "drawable-hdpi", "drawable-xhdpi", "drawable-xxhdpi", "drawable-xxxhdpi"]

# Expected sizes per density bucket
EXPECTED_SIZES = {
    "drawable-mdpi": 200,
    "drawable-hdpi": 300,
    "drawable-xhdpi": 400,
    "drawable-xxhdpi": 600,
    "drawable-xxxhdpi": 800,
}


def has_white_background(img):
    """Check if the image has significant white/near-white regions at corners."""
    if img.mode != "RGBA":
        # No alpha channel = likely has opaque background
        return True, "no alpha channel"

    pixels = img.load()
    w, h = img.size

    # Check corners (5x5 pixel samples)
    corners = []
    for cx, cy in [(0, 0), (w - 1, 0), (0, h - 1), (w - 1, h - 1)]:
        for dx in range(min(5, w)):
            for dy in range(min(5, h)):
                x = min(cx + dx, w - 1) if cx == 0 else max(cx - dx, 0)
                y = min(cy + dy, h - 1) if cy == 0 else max(cy - dy, 0)
                corners.append(pixels[x, y])

    white_count = sum(1 for r, g, b, a in corners if r > 240 and g > 240 and b > 240 and a > 240)
    total = len(corners)
    ratio = white_count / total if total > 0 else 0

    if ratio > 0.5:
        return True, f"{ratio:.0%} corner pixels are white"
    return False, "transparent"


def audit_density(density_dir, density_name):
    """Audit all avatar PNGs in a density directory."""
    results = []
    expected = EXPECTED_SIZES.get(density_name, 0)

    for fname in sorted(os.listdir(density_dir)):
        if not fname.startswith("avatar_") or not fname.endswith(".png"):
            continue

        path = os.path.join(density_dir, fname)
        img = Image.open(path)
        w, h = img.size
        has_alpha = img.mode == "RGBA"
        is_white, reason = has_white_background(img)
        size_ok = (w == expected and h == expected) if expected else True

        status = "OK"
        issues = []
        if is_white:
            issues.append(f"WHITE BG ({reason})")
        if not size_ok:
            issues.append(f"SIZE {w}x{h} (expected {expected}x{expected})")
        if not has_alpha:
            issues.append("NO ALPHA")

        if issues:
            status = "NEEDS FIX"

        results.append({
            "name": fname,
            "width": w,
            "height": h,
            "alpha": has_alpha,
            "white_bg": is_white,
            "reason": reason,
            "size_ok": size_ok,
            "status": status,
            "issues": ", ".join(issues),
        })

    return results


def main():
    base = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), DRAWABLE_BASE)

    for density in DENSITIES:
        density_dir = os.path.join(base, density)
        if not os.path.isdir(density_dir):
            continue

        results = audit_density(density_dir, density)
        if not results:
            continue

        expected = EXPECTED_SIZES.get(density, "?")
        print(f"\n{'=' * 80}")
        print(f"  {density} (expected: {expected}x{expected})")
        print(f"{'=' * 80}")

        ok = [r for r in results if r["status"] == "OK"]
        fix = [r for r in results if r["status"] == "NEEDS FIX"]

        if fix:
            print(f"\n  NEEDS FIX ({len(fix)}):")
            for r in fix:
                print(f"    {r['name']:40s} {r['width']:4d}x{r['height']:<4d}  {r['issues']}")

        if ok:
            print(f"\n  OK ({len(ok)}):")
            for r in ok:
                print(f"    {r['name']:40s} {r['width']:4d}x{r['height']:<4d}")

        print(f"\n  Summary: {len(ok)} OK, {len(fix)} need fix out of {len(results)} total")


if __name__ == "__main__":
    main()
