#!/usr/bin/env bash
set -euo pipefail

# Bootstraps a tidals-scripts worktree so Claude can work immediately.
# Symlinks gitignored dirs (docs, examples, .claude) from main.
# Git-tracked dirs (.beans, utilities) are left alone — they come with the branch.
#
# Usage: cd /path/to/worktree && ./setup.sh

HERE="$(pwd)"
MAIN_DIR="$(git worktree list --porcelain | head -1 | sed 's/^worktree //')"

if [[ "$HERE" == "$MAIN_DIR" ]]; then
    echo "You're in the main worktree — nothing to set up."
    exit 0
fi

echo "Setting up worktree: $HERE"
echo "Linking from main:   $MAIN_DIR"
echo ""

# only symlink fully-gitignored dirs (docs/, examples/, .claude/)
# .beans/ and utilities/ are git-tracked — don't touch them
SYMLINK_DIRS=(docs examples .claude)

for dir in "${SYMLINK_DIRS[@]}"; do
    src="$MAIN_DIR/$dir"
    dst="$HERE/$dir"

    if [[ -L "$dst" ]]; then
        echo "  ✓ $dir (already linked)"
        continue
    fi

    if [[ ! -d "$src" ]]; then
        echo "  ⚠ $dir not found in main, skipping"
        continue
    fi

    rm -rf "$dst"
    ln -s "$src" "$dst"
    echo "  ✓ $dir"
done

# restore any git changes the symlinks may have caused
git checkout -- . 2>/dev/null || true

echo ""
echo "Ready. Run 'claude' to start working."
