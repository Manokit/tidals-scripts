---
# tidals-scripts-5wj6
title: Add item mapping, osmb_item_lookup, and osmb_itemid_bulk tools to MCP server
status: completed
type: feature
priority: normal
created_at: 2026-01-30T12:12:48Z
updated_at: 2026-01-30T12:15:06Z
---

Enhance MCP server with display name lookups via prices.runescape.wiki mapping API and OSMB ItemID constant generation. Changes: 1) fetchItemMapping() with 24h cache, 2) osmb_item_lookup tool, 3) Enhanced search_objtypes with display names, 4) osmb_itemid_bulk tool.