# Requirements: TidalsSecondaryCollector - Fairy Ring Mode

**Defined:** 2026-01-16
**Core Value:** Auto-detect equipment to seamlessly support both Ver Sinhaza (Drakan's Medallion) and Fairy Ring modes without requiring user configuration.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Mode Detection

- [x] **MODE-01**: Script auto-detects Fairy Ring mode when Dramen staff equipped + bloom tool in inventory + ardy cloak equipped
- [x] **MODE-02**: Script auto-detects Ver Sinhaza mode when Drakan's Medallion equipped + bloom tool equipped (existing behavior)

### Collection

- [ ] **COLL-01**: Fairy Ring mode uses 3-log tile at position 3474, 3419, 0
- [ ] **COLL-02**: Fairy Ring mode casts bloom from inventory (not equipment slot)

### Banking

- [ ] **BANK-01**: Fairy Ring mode teleports to Zanaris via fairy ring at 3469, 3431, 0
- [ ] **BANK-02**: Fairy Ring mode walks to Zanaris bank chest after arrival

### Return

- [ ] **RETN-01**: Fairy Ring mode uses ardy cloak → monastery → walks to fairy ring → "Last-destination (BKR)"
- [ ] **RETN-02**: Script validates BKR is configured as last destination before interacting
- [ ] **RETN-03**: Script terminates with error if BKR not configured (safety check)

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Return (Alternate)

- **RETN-04**: Quest cape support for return teleport via Legends Guild fairy ring

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| UI mode selection | Auto-detection is simpler and less error-prone |
| Other fairy ring destinations | Only BKR (Mort Myre) needed |
| House fairy ring support | Adds complexity without significant benefit |

## Traceability

Which phases cover which requirements. Updated by create-roadmap.

| Requirement | Phase | Status |
|-------------|-------|--------|
| MODE-01 | Phase 1 | Complete |
| MODE-02 | Phase 1 | Complete |
| COLL-01 | Phase 2 | Pending |
| COLL-02 | Phase 2 | Pending |
| BANK-01 | Phase 3 | Pending |
| BANK-02 | Phase 3 | Pending |
| RETN-01 | Phase 3 | Pending |
| RETN-02 | Phase 3 | Pending |
| RETN-03 | Phase 3 | Pending |

**Coverage:**
- v1 requirements: 9 total
- Mapped to phases: 9 ✓
- Unmapped: 0

---
*Requirements defined: 2026-01-16*
*Last updated: 2026-01-16 after initial definition*
