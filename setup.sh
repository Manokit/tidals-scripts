#!/usr/bin/env bash
set -euo pipefail

# Bootstraps a tidals-scripts worktree so Claude can work immediately.
# Run this inside any worktree (or main) to symlink gitignored resources.
#
# Usage: ./setup.sh
#        (or from another worktree: bash /path/to/main/setup.sh)

HERE="$(pwd)"

# find the main worktree (where the real dirs live)
MAIN_DIR="$(git worktree list --porcelain | head -1 | sed 's/^worktree //')"

if [[ "$HERE" == "$MAIN_DIR" ]]; then
    echo "You're in the main worktree — nothing to symlink."
    echo "Verifying dirs exist..."
    missing=0
    for dir in docs examples .claude .beans utilities; do
        if [[ -d "$HERE/$dir" ]]; then
            echo "  ✓ $dir"
        else
            echo "  ✗ $dir missing!"
            missing=1
        fi
    done
    [[ $missing -eq 0 ]] && echo "All good." || echo "Some dirs missing — create them manually."
    exit 0
fi

echo "Setting up worktree: $HERE"
echo "Linking from main:   $MAIN_DIR"
echo ""

# gitignored dirs to symlink from main
DIRS=(docs examples .claude .beans utilities)

for dir in "${DIRS[@]}"; do
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

    # remove empty dir if git created one
    rm -rf "$dst"
    ln -s "$src" "$dst"
    echo "  ✓ $dir -> $src"
done

echo ""
echo "Ready. Run 'claude' to start working."
