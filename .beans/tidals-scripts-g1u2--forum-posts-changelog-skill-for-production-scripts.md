---
# tidals-scripts-g1u2
title: Forum posts & changelog skill for production scripts
status: completed
type: feature
priority: normal
created_at: 2026-02-01T12:44:26Z
updated_at: 2026-02-01T12:47:33Z
---

Create Forum-Changelogs/ directory structure for all 4 production scripts (CannonballThiever, GemMiner, GemCutter, GoldSuperheater) with themed forum posts using OSMB forum markdown. Then create a Claude Code skill that can generate new posts or update changelogs from git diffs.

## Checklist
- [ ] Create Forum-Changelogs/ for TidalsCannonballThiever with main-post.md + version files
- [ ] Create Forum-Changelogs/ for TidalsGemMiner with main-post.md + version files
- [ ] Create Forum-Changelogs/ for TidalsGemCutter with main-post.md + version files
- [ ] Create Forum-Changelogs/ for TidalsGoldSuperheater with main-post.md + version files
- [ ] Create the osmb-forum-post skill
- [ ] Build and verify skill works