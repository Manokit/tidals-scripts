# Common Menu Entries

Quick reference for menu action strings used in OSMB scripts. These are the exact strings needed for `.interact()` calls.

## Banking

| Object | Action |
|--------|--------|
| Bank booth | `"Bank"` |
| Bank chest | `"Use"` |
| Grand Exchange booth | `"Bank"` |
| Deposit box | `"Deposit"` |

## Prayer & Altars

| Object/Item | Action |
|-------------|--------|
| Altar | `"Pray-at"` |
| Ardougne cloak (1-4) | `"Monastery Teleport"` |

## Teleport Items

| Item | Action | Destination |
|------|--------|-------------|
| Crafting cape | `"Teleport"` | Crafting Guild |
| Ring of dueling | `"Ferox Enclave"` | Ferox Enclave bank |
| Drakan's medallion | `"Ver Sinhaza"` | Ver Sinhaza |

## Ground Items & Resources

| Object | Action |
|--------|--------|
| Fungi on log (Mort Myre) | `"Pick"` |

## NPCs

| NPC | Action |
|-----|--------|
| Banker | `"Bank"` |

## Notes

- Menu actions are case-sensitive in some contexts
- Always verify actions using the debug tool or `tapGetResponse()` when unsure
- Some objects have different actions based on state (e.g., doors: "Open" vs "Close")
- Equipment interactions use the action as shown in the right-click menu

## Adding New Entries

When you discover a new menu action that differs from the obvious choice, add it here to save time on future scripts.
