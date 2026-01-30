---
# tidals-scripts-0bz1
title: Replace uniformRandom with gaussianRandom for timing values
status: completed
type: task
priority: normal
created_at: 2026-01-28T21:48:20Z
updated_at: 2026-01-28T21:50:43Z
---

Replace all uniformRandom calls used for timing/delays with gaussianRandom across production scripts. Keep uniformRandom for chance rolls and index selection.