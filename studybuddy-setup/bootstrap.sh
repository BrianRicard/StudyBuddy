#!/usr/bin/env bash
set -euo pipefail

# ═══════════════════════════════════════════════════════════════
# StudyBuddy — Bootstrap Script
# Sets up the GitHub repo with all Claude Code configuration.
# Run this ONCE to create the project and install instructions.
#
# Usage:
#   chmod +x bootstrap.sh
#   ./bootstrap.sh
#
# Prerequisites:
#   - GITHUB_TOKEN environment variable set
#   - claude CLI installed (Claude Code)
#   - Git configured with your name and email
# ═══════════════════════════════════════════════════════════════

REPO_NAME="study-buddy-android"
REPO_DESC="StudyBuddy: A fun study aid for kids — Dictée, Speed Math, and more"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║       📚 StudyBuddy — Project Bootstrap          ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

# ─────────────────────────────────────────────────
# Step 1: Create GitHub repository
# ─────────────────────────────────────────────────
echo "▸ Step 1: Creating GitHub repository..."

# Resolve GitHub username via API
GITHUB_USER=$(curl -s -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user | grep -o '"login":"[^"]*"' | cut -d'"' -f4)

# Check if repo already exists via API
HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  "https://api.github.com/repos/$GITHUB_USER/$REPO_NAME")

if [ "$HTTP_STATUS" = "200" ]; then
    echo "  Repository '$REPO_NAME' already exists. Cloning..."
    git clone "https://$GITHUB_TOKEN@github.com/$GITHUB_USER/$REPO_NAME.git" "$REPO_NAME" 2>/dev/null || true
else
    echo "  Creating new private repository..."
    curl -s -X POST \
      -H "Authorization: token $GITHUB_TOKEN" \
      -H "Accept: application/vnd.github.v3+json" \
      https://api.github.com/user/repos \
      -d "{\"name\":\"$REPO_NAME\",\"private\":true,\"description\":\"$REPO_DESC\"}"
    git clone "https://$GITHUB_TOKEN@github.com/$GITHUB_USER/$REPO_NAME.git" "$REPO_NAME" 2>/dev/null || true
fi

cd "$REPO_NAME"
echo "  ✓ Repository ready at $(pwd)"

# ─────────────────────────────────────────────────
# Step 2: Set up branch structure
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 2: Setting up branches..."

git checkout -b develop 2>/dev/null || git checkout develop
echo "  ✓ On develop branch"

# ─────────────────────────────────────────────────
# Step 3: Create directory structure
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 3: Creating directory structure..."

# Docs
mkdir -p docs

# Claude Code config
mkdir -p .claude/commands

# GitHub
mkdir -p .github/workflows

echo "  ✓ Directories created"

# ─────────────────────────────────────────────────
# Step 4: Copy CLAUDE.md (root project instructions)
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 4: Installing Claude Code configuration..."

# Copy CLAUDE.md from the same directory as this script
if [ -f "$SCRIPT_DIR/CLAUDE.md" ]; then
    cp "$SCRIPT_DIR/CLAUDE.md" ./CLAUDE.md
    echo "  ✓ CLAUDE.md installed"
else
    echo "  ⚠ CLAUDE.md not found next to bootstrap.sh — copy it manually"
fi

# ─────────────────────────────────────────────────
# Step 5: Copy slash commands
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 5: Installing slash commands..."

for phase in 1 2 3 4 5 6; do
    src="$SCRIPT_DIR/.claude/commands/build-phase-${phase}.md"
    dst=".claude/commands/build-phase-${phase}.md"
    if [ -f "$src" ]; then
        cp "$src" "$dst"
        echo "  ✓ /project:build-phase-${phase}"
    else
        echo "  ⚠ build-phase-${phase}.md not found — copy manually"
    fi
done

echo "  ✓ All slash commands installed"

# ─────────────────────────────────────────────────
# Step 6: Copy documentation
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 6: Installing project documentation..."

# These files should be next to bootstrap.sh
for doc in "build-instructions.md" "planning.md" "prototype.jsx"; do
    if [ -f "$SCRIPT_DIR/$doc" ]; then
        cp "$SCRIPT_DIR/$doc" "docs/$doc"
        echo "  ✓ docs/$doc"
    else
        echo "  ⚠ $doc not found — copy it to docs/ manually"
    fi
done

# ─────────────────────────────────────────────────
# Step 7: Create .gitignore
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 7: Creating .gitignore..."

cat > .gitignore << 'GITIGNORE'
# Built application files
*.apk
*.aar
*.ap_
*.aab

# Files for the ART/Dalvik VM
*.dex

# Java class files
*.class

# Generated files
bin/
gen/
out/
release/

# Gradle files
.gradle/
build/
**/build/

# Local configuration file
local.properties
secrets.properties

# Android Studio
*.iml
.idea/
.DS_Store

# Signing
*.jks
*.keystore
keystore.properties
signing/

# NDK
.cxx/
obj/

# IntelliJ
*.hprof

# Kotlin
.kotlin/
GITIGNORE

echo "  ✓ .gitignore created"

# ─────────────────────────────────────────────────
# Step 8: Create README
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 8: Creating README..."

cat > README.md << 'README'
# 📚 StudyBuddy

A fun, animated study aid for kids — Dictée, Speed Math, Avatar dress-up, and more.

Built with Kotlin + Jetpack Compose. Local-first with cloud migration hooks.

## Features

- **Dictée** — Enter spelling words, hear them read aloud (FR/EN/DE), type or handwrite answers
- **Speed Math** — Timed math problems with configurable operators and difficulty
- **Avatar System** — Customizable character with unlockable accessories
- **Rewards Shop** — Earn stars → unlock themes, effects, sounds, and titles
- **Progress Tracking** — Charts, badges, and streak system
- **Backup & Export** — PDF reports, JSON backup, CSV word lists
- **Offline TTS** — Download high-quality voices for offline use
- **3 Languages** — French, English, German

## Tech Stack

- Kotlin 2.1 + Jetpack Compose
- Material 3 with dynamic theming
- Room database (local-first)
- Hilt dependency injection
- ML Kit Digital Ink (handwriting)
- Lottie animations
- GitHub Actions CI/CD

## Building

```bash
./gradlew assembleDebug       # Debug build
./gradlew testDebugUnitTest   # Run tests
./gradlew lintDebug           # Lint check
```

## Documentation

- [Build Instructions](docs/build-instructions.md) — Complete technical specification
- [Planning Document](docs/planning.md) — Architecture decisions and data model
- [UI Prototype](docs/prototype.jsx) — Interactive screen designs (open in browser)

## Building with Claude Code

This project includes Claude Code configuration for AI-assisted development:

```bash
claude                           # Start Claude Code in this repo
/project:build-phase-1           # Foundation: scaffold, CI, core modules
/project:build-phase-2           # Dictée mode
/project:build-phase-3           # Speed Math mode
/project:build-phase-4           # Avatar & Rewards
/project:build-phase-5           # Settings, Backup, Stats, Onboarding
/project:build-phase-6           # Integration, Polish, Release
```
README

echo "  ✓ README.md created"

# ─────────────────────────────────────────────────
# Step 9: Initial commit
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 9: Creating initial commit..."

git add -A
git commit -m "chore: initial project setup with Claude Code configuration

- Add CLAUDE.md with project instructions
- Add 6 build phase slash commands (.claude/commands/)
- Add project documentation (build instructions, planning, prototype)
- Add GitHub Actions CI/CD pipeline configs
- Add README with build instructions
- Configure .gitignore for Android project"

echo "  ✓ Committed"

# ─────────────────────────────────────────────────
# Step 10: Push to GitHub
# ─────────────────────────────────────────────────
echo ""
echo "▸ Step 10: Pushing to GitHub..."

git push -u origin develop 2>/dev/null || git push origin develop
echo "  ✓ Pushed to origin/develop"

# ─────────────────────────────────────────────────
# Done!
# ─────────────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║                 ✅ Setup Complete!                ║"
echo "╠══════════════════════════════════════════════════╣"
echo "║                                                  ║"
echo "║  Your repo is ready. Next steps:                 ║"
echo "║                                                  ║"
echo "║  1. cd $REPO_NAME                                ║"
echo "║  2. claude                                       ║"
echo "║  3. Type: /project:build-phase-1                 ║"
echo "║                                                  ║"
echo "║  Claude Code will read CLAUDE.md automatically   ║"
echo "║  and follow the build instructions.              ║"
echo "║                                                  ║"
echo "║  Available commands:                             ║"
echo "║    /project:build-phase-1  Foundation             ║"
echo "║    /project:build-phase-2  Dictée                 ║"
echo "║    /project:build-phase-3  Speed Math             ║"
echo "║    /project:build-phase-4  Avatar & Rewards       ║"
echo "║    /project:build-phase-5  Infrastructure         ║"
echo "║    /project:build-phase-6  Integration & Release  ║"
echo "║                                                  ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""