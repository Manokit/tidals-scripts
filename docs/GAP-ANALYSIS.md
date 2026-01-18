# API Documentation Gap Analysis

Comparison of scraped API docs vs existing documentation.

**Last updated:** Documentation created for high-priority items.

## Legend
- ✅ = Has existing doc
- ❌ = Missing doc (needs creation)
- ⚪ = Low priority (internal/rarely used)

---

## definition Package
| Class | Status | Notes |
|-------|--------|-------|
| ItemDefinition | ✅ | ItemDefinition.md |
| MapDefinition | ⚪ | Internal map data |
| SpriteDefinition | ✅ | Covered in Sprite.md |

---

## input Package
| Class | Status | Notes |
|-------|--------|-------|
| Finger | ✅ | Finger.md exists |
| Keyboard | ✅ | Keyboard.md exists |
| MenuEntry | ❌ | Menu action data structure |
| MenuHook | ⚪ | Internal hook system |
| PhysicalKey | ❌ | Key constants for Keyboard |
| TouchType | ❌ | Touch input types |
| EntityType | ❌ | Entity classification for menus |

---

## item Package
| Class | Status | Notes |
|-------|--------|-------|
| ItemManager | ✅ | ItemManager.md exists |
| ItemGroup | ❌ | Group multiple items for searching |
| ItemGroupResult | ❌ | Result from ItemGroup search |
| ItemSearchResult | ❌ | Individual item search result |
| ItemImage | ❌ | Item sprite handling |
| LinkedItemImage | ⚪ | Internal linking |
| SearchableItem | ❌ | Base interface for searchable items |
| ItemID | ⚪ | Just constants |
| ZoomType | ❌ | Zoom levels for item images |

---

## location Package
| Class | Status | Notes |
|-------|--------|-------|
| Area | ✅ | Areas.md (combined doc) |
| PolyArea | ✅ | Areas.md |
| RectangleArea | ✅ | RectangleArea.md + Areas.md |
| Position | ✅ | Areas.md |
| WorldPosition | ✅ | Areas.md |
| LocalPosition | ⚪ | Scene-local coordinates |
| Location3D | ⚪ | 3D location interface |

---

## scene Package
| Class | Status | Notes |
|-------|--------|-------|
| RSObject | ✅ | RSObject.md exists |
| ObjectManager | ✅ | ObjectManager.md |
| SceneManager | ✅ | SceneManager.md |
| ObjectIdentifier | ✅ | ObjectIdentifier.md exists |
| CollisionMap | ❌ | Collision data access |
| RSTile | ❌ | Tile information |
| ObjectType | ❌ | Object type enum |
| SceneObjectDefinition | ⚪ | Internal definition |
| Drawable | ⚪ | Interface |

---

## ui Package - Core
| Class | Status | Notes |
|-------|--------|-------|
| WidgetManager | ✅ | WidgetManager.md exists |
| Bank | ✅ | Bank.md exists |
| Chatbox | ✅ | Chatbox.md exists |
| Dialogue | ✅ | Dialogue.md exists |
| DepositBox | ✅ | Deposit-box.md exists |
| Equipment | ✅ | Equipment.md exists |
| Minimap | ✅ | Minimap.md exists |
| TabManager | ✅ | TabManager.md exists |
| GameState | ❌ | Game state detection (logged in, etc.) |
| MiniMenu | ❌ | Right-click menu handling |

---

## ui Package - Overlays
| Class | Status | Notes |
|-------|--------|-------|
| BuffOverlay | ✅ | BuffOverlay.md |
| HealthOverlay | ❌ | Health bar overlay detection |
| OverlayBoundary | ⚪ | Internal |
| OverlayPosition | ⚪ | Internal |
| OverlayValueFinder | ⚪ | Internal |

---

## ui Package - Spellbooks
| Class | Status | Notes |
|-------|--------|-------|
| StandardSpellbook | ✅ | StandardSpellbook.md exists |
| AncientSpellbook | ❌ | Ancient magicks spells |
| LunarSpellbook | ❌ | Lunar spells |
| ArceuusSpellbook | ❌ | Arceuus spells |
| Spell | ❌ | Base spell interface |
| SpellbookType | ❌ | Spellbook type enum |

---

## ui Package - Tabs
| Class | Status | Notes |
|-------|--------|-------|
| EquipmentTabComponent | ✅ | EquipmentTabComponent.md exists |
| SquareTabComponent | ✅ | SquareTabComponent.md exists |
| InventoryTabComponent | ❌ | Inventory tab operations |
| PrayerTabComponent | ❌ | Prayer tab component |
| SkillsTabComponent | ❌ | Skills tab with levels |
| SkillType | ❌ | Skill enum |
| CombatTabComponent | ❌ | Combat options |
| SettingsTabComponent | ❌ | Settings tab |
| SpellbookTabComponent | ❌ | Spellbook tab |
| (other tabs) | ⚪ | Lower priority |

---

## ui Package - Minimap Components
| Class | Status | Notes |
|-------|--------|-------|
| OrbManager | ✅ | OrbManager.md exists |
| EntityMapDot | ✅ | EntityMapDot.md exists |
| Compass | ❌ | Compass direction handling |
| MinimapOrbs | ⚪ | Covered by OrbManager |

---

## visual Package
| Class | Status | Notes |
|-------|--------|-------|
| PixelAnalyzer | ✅ | PixelAnalyzer.md exists |
| PixelCluster | ✅ | PixelCluster.md exists |
| Image | ✅ | Image.md exists |
| ImageSearchResult | ✅ | ImageSearchResult.md exists |
| OCR | ✅ | OCR.md |
| Canvas | ❌ | Drawing on screen |
| SceneProjector | ❌ | World-to-screen projection |
| ColorUtils | ❌ | Color manipulation utilities |
| Font/RSFont | ⚪ | Internal font handling |
| ToleranceComparator | ⚪ | Color comparison internals |
| VisualVerifier | ❌ | Visual verification utilities |

---

## walker Package
| Class | Status | Notes |
|-------|--------|-------|
| Walker | ✅ | Walker.md + Webwalker.md exist |
| WalkConfig | ✅ | WalkConfig.md exists |
| CollisionManager | ❌ | Collision detection |
| CollisionFlags | ❌ | Collision flag constants |
| PathUtils | ⚪ | Internal pathing |
| AStarPathFinder | ⚪ | Internal pathfinder |
| BFSPathFinder | ⚪ | Internal pathfinder |

---

## trackers Package
| Class | Status | Notes |
|-------|--------|-------|
| XPTracker | ✅ | XPTracker.md exists |
| ItemTracker | ❌ | Track item gains/losses |
| ItemListener | ❌ | Item change callbacks |
| TrackedItem | ⚪ | Internal |
| XPDropsListener | ❌ | XP drop callbacks |

---

## utils Package
| Class | Status | Notes |
|-------|--------|-------|
| CachedObject | ✅ | CachedObject.md exists |
| Timer | ✅ | Timing.md |
| Stopwatch | ✅ | Timing.md |
| RandomUtils | ❌ | Random number generation |
| Utils | ❌ | General utilities |
| Result/UIResult | ⚪ | Internal result types |

---

## script Package
| Class | Status | Notes |
|-------|--------|-------|
| Script | ✅ | Script.md exists |
| ScriptCore | ✅ | ScriptCore.md exists |
| ScriptOptions | ✅ | ScriptOptions.md exists |
| ScriptDefinition | ⚪ | Annotation |
| HaltScriptException | ❌ | Exception for stopping scripts |
| TaskInterruptedException | ⚪ | Internal |

---

## Completed Documentation

### HIGH PRIORITY (All Done ✅)
1. ✅ **ObjectManager** - ObjectManager.md
2. ✅ **SceneManager** - SceneManager.md
3. ✅ **OCR** - OCR.md
4. ✅ **BuffOverlay** - BuffOverlay.md
5. ✅ **ItemDefinition** - ItemDefinition.md

### MEDIUM PRIORITY (Partial)
6. ✅ PolyArea - Areas.md
7. ✅ Position/WorldPosition - Areas.md
8. ❌ AncientSpellbook/LunarSpellbook/ArceuusSpellbook
9. ❌ ItemTracker
10. ✅ Timer/Stopwatch - Timing.md
11. ❌ GameState
12. ❌ ColorUtils

---

## Remaining Gaps (Lower Priority)

### Worth Documenting Eventually
- **Spellbooks** - Ancient, Lunar, Arceuus (follow StandardSpellbook pattern)
- **ItemTracker/ItemListener** - For tracking loot/resource gains
- **Canvas/SceneProjector** - For custom paint and world-to-screen conversions
- **ColorUtils** - Color manipulation helpers
- **GameState** - Login state detection

### Low Value (Skip)
- Internal classes (OverlayBoundary, ToleranceComparator, etc.)
- Pathfinder internals (AStarPathFinder, BFSPathFinder)
- Component internals
- Enums that are self-explanatory from usage
