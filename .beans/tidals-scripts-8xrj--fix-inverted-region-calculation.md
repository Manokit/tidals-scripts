---
# tidals-scripts-8xrj
title: Fix inverted region calculation
status: completed
type: bug
priority: critical
created_at: 2026-01-31T02:09:38Z
updated_at: 2026-01-31T02:09:57Z
---

getCurrentRegion() has X and Y swapped. Should be (x >> 6) << 8 | (y >> 6) but is (x >> 6) | ((y >> 6) << 8). Line 1484 of MortMyreFungusCollector.java.