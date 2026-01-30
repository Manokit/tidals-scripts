---
# tidals-scripts-khmf
title: Document OSMB feedback patterns
status: completed
type: task
created_at: 2026-01-28T07:57:14Z
updated_at: 2026-01-28T07:57:14Z
---

## Summary
Document all patterns from OSMB framework developer feedback to prevent future issues.

## Checklist
- [x] Add section 19: Dialogue/UIResult never null (common-mistakes.md)
- [x] Add section 20: Interface interaction timing - wait before clicking (common-mistakes.md)
- [x] Update Best Practices Summary with new items
- [x] Enhance Walker.md Pattern 1 with query-based note
- [x] Enhance Walker.md Pattern 2 with existing-reference note  
- [x] Add Interface Operation Verification section (interaction-patterns.md)
- [x] Add 'verify before closing' to Summary table

## Key Patterns Documented
1. Dialogue and UIResult are guaranteed non-null (check .isVisible() / .isFound())
2. Wait 300-600ms after interface opens before interacting
3. Always verify operation succeeded before closing interface
4. Query-based vs existing-reference walker breakConditions