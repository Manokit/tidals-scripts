# Requirements: TidalsSecondaryCollector v1.1

**Defined:** 2026-01-16
**Core Value:** Auto-detect equipment to seamlessly support both Ver Sinhaza and Fairy Ring modes without user configuration

## v1 Requirements

Requirements for v1.1 Flexible Validation milestone.

### Validation

- [ ] **VALID-01**: Script accepts bloom items (silver sickle/flails) in inventory without requiring them equipped
- [ ] **VALID-02**: Script accepts ardy cloak in inventory without requiring it equipped
- [ ] **VALID-03**: Script accepts quest cape in inventory without requiring it equipped
- [ ] **VALID-04**: Script auto-equips dramen staff from inventory at startup if not already equipped

### Performance

- [ ] **PERF-01**: Script prioritizes region 13877 (Mort Myre fairy ring) for faster startup in fairy ring mode

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Navigation

- **NAV-01**: Quest cape support for return teleport via Legends Guild fairy ring

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| UI selection for mode | Auto-detection is simpler and less error-prone |
| Other fairy ring destinations | Only BKR (Mort Myre) needed |
| House fairy ring support | Adds complexity without significant benefit |

## Traceability

Which phases cover which requirements. Updated by create-roadmap.

| Requirement | Phase | Status |
|-------------|-------|--------|
| VALID-01 | TBD | Pending |
| VALID-02 | TBD | Pending |
| VALID-03 | TBD | Pending |
| VALID-04 | TBD | Pending |
| PERF-01 | TBD | Pending |

**Coverage:**
- v1 requirements: 5 total
- Mapped to phases: 0
- Unmapped: 5 ⚠️

---
*Requirements defined: 2026-01-16*
*Last updated: 2026-01-16 after initial definition*
