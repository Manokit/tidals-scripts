---
# tidals-scripts-f54t
title: Refactor StartThieving.execute() to use explicit state enum
status: completed
type: task
priority: normal
created_at: 2026-01-30T09:48:59Z
updated_at: 2026-01-30T09:55:05Z
---

StartThieving.java has a long execute() with ~10 sequential condition checks (position, guard sync, stall safety, guard wait, delay, validate position, find stall, danger check, validate geometry, tap). Each has early returns so it works, but should use an explicit state enum like DepositOres/PrepareForBreak for clarity and to ensure only one action per poll.