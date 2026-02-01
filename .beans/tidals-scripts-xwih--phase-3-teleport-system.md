---
# tidals-scripts-xwih
title: 'Phase 3: Teleport System'
status: completed
type: feature
priority: normal
created_at: 2026-01-31T02:24:49Z
updated_at: 2026-01-31T03:26:03Z
parent: tidals-scripts-6gj8
blocking:
    - tidals-scripts-dyni
---

Implement teleport registry so the walker can use jewelry, spells, and equipment teleports to shortcut long walks.

## Checklist

- [x] Create `TeleportDefinition` (name, cost, destination, canUse predicate, execute function)
- [x] Create `TeleportRegistry` with static registration
- [x] Register Ring of Dueling teleports (Duel Arena, Castle Wars, Ferox)
- [x] Register Amulet of Glory teleports (Edgeville, Karamja, Draynor, Al Kharid)
- [x] Register Games Necklace teleports (Burthorpe, Wintertodt, etc.)
- [x] Register Skills Necklace teleports (Fishing/Mining/Crafting/Farming/WC Guild)
- [x] Register Combat Bracelet teleports (Warriors', Champions', Monastery, Ranging)
- [x] Register Burning Amulet teleports (Chaos Temple, Bandit Camp, Lava Maze)
- [x] Implement equipped jewelry teleport execution
- [x] Implement inventory jewelry teleport (Rub → dialogue select)
- [x] Register standard spellbook teleports (Varrock, Lumbridge, Falador, Camelot, Ardougne, Watchtower, Trollheim, Kourend)
- [x] Add `findTeleportTo()` — find available teleport near destination

## Implementation Notes
- Item IDs verified via MCP objtypes search — includes 6-charge variants for Skills Necklace and Combat Bracelet
- Equipped jewelry: direct menu action via RetryUtils.equipmentInteract
- Inventory jewelry: Rub → TEXT_OPTION dialogue → selectOption
- Spell teleports: magic level check via getSkillTab(), rune check implicit (SpellNotFoundException if unavailable)
- Equipment teleports: Crafting Cape, Quest Cape