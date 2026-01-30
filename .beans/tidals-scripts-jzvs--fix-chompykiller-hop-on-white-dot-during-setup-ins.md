---
# tidals-scripts-jzvs
title: 'Fix ChompyKiller: hop on white dot during Setup instead of dropping toads'
status: completed
type: bug
priority: normal
created_at: 2026-01-30T10:55:10Z
updated_at: 2026-01-30T10:56:46Z
---

During Setup, if DetectPlayers finds a white dot (another player) on the minimap, the script should world hop immediately and NOT proceed to drop toads. Currently Setup completes and DropToads activates even when another player is nearby.