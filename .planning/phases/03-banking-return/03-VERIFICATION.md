---
phase: 03-banking-return
verified: 2026-01-16T05:35:00Z
status: passed
score: 5/5 must-haves verified
must_haves:
  truths:
    - "Script uses fairy ring at (3469, 3431, 0) to teleport to Zanaris"
    - "Script walks to Zanaris bank chest and deposits fungus"
    - "Script uses ardy cloak -> monastery -> walks to fairy ring -> Last-destination (BKR)"
    - "Script validates BKR is configured before using Last-destination"
    - "Script terminates safely if BKR not configured"
  artifacts:
    - path: "TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java"
      provides: "Mode-aware banking and return with BKR validation"
  key_links:
    - from: "teleportToBank()"
      to: "useZanarisBanking()"
      via: "isFairyRingMode() conditional at line 908"
    - from: "useZanarisBanking()"
      to: "fairy ring at 3469, 3431"
      via: "RetryUtils.objectInteract(fairyRing, 'Zanaris') at line 1065"
    - from: "returnToArea()"
      to: "useFairyRingReturn()"
      via: "isFairyRingMode() conditional at line 1300"
    - from: "useFairyRingReturn()"
      to: "BKR validation"
      via: "tapGetResponse menu check at lines 1428-1449"
---

# Phase 3: Banking & Return Verification Report

**Phase Goal:** Bank via Zanaris, return via monastery fairy ring
**Verified:** 2026-01-16T05:35:00Z
**Status:** passed
**Re-verification:** No - initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Script uses fairy ring at (3469, 3431, 0) to teleport to Zanaris | VERIFIED | `MORT_MYRE_FAIRY_RING = new WorldPosition(3469, 3431, 0)` at line 120; `useZanarisBanking()` walks to ring and uses "Zanaris" action at line 1065 |
| 2 | Script walks to Zanaris bank chest and deposits fungus | VERIFIED | `ZANARIS_BANK_PATH` with 8 waypoints at lines 126-135; `walkToZanarisBank()` uses path at line 1100; opens bank and calls `handleBankInterface()` |
| 3 | Script uses ardy cloak -> monastery -> walks to fairy ring -> "Last-destination (BKR)" | VERIFIED | `useFairyRingReturn()` at lines 1380-1486 implements full flow: `tryArdougneCloakTeleport()` -> `MONASTERY_TO_FAIRY_PATH` -> tap fairy ring with "last-destination" action |
| 4 | Script validates BKR is configured before using "Last-destination" | VERIFIED | `tapGetResponse(true, ringPoly)` at line 1428 checks menu; validates "last-destination" at line 1439 and "bkr" at line 1446 |
| 5 | Script terminates safely if BKR not configured | VERIFIED | `script.stop()` called at lines 1442 and 1449 with clear error messages |

**Score:** 5/5 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `TidalsSecondaryCollector/src/main/java/strategies/MortMyreFungusCollector.java` | Mode-aware banking and return | VERIFIED | 1513 lines, exports `MortMyreFungusCollector` class, contains `useZanarisBanking()` and `useFairyRingReturn()` |

### Key Link Verification

| From | To | Via | Status | Details |
|------|-----|-----|--------|---------|
| `teleportToBank()` | `useZanarisBanking()` | `isFairyRingMode()` conditional | WIRED | Line 908: `if (isFairyRingMode()) { return useZanarisBanking(); }` |
| `useZanarisBanking()` | Fairy ring at (3469, 3431, 0) | `RetryUtils.objectInteract()` | WIRED | Line 1054: walks to `MORT_MYRE_FAIRY_RING`; Line 1065: `objectInteract(fairyRing, "Zanaris")` |
| `walkToZanarisBank()` | Bank chest | `ZANARIS_BANK_PATH` + `walkPath()` | WIRED | Line 1100: `walkPath(ZANARIS_BANK_PATH)`; Line 1110: `openBank(bankChest)` |
| `returnToArea()` | `useFairyRingReturn()` | `isFairyRingMode()` conditional | WIRED | Line 1300: `if (isFairyRingMode())` -> Line 1306: `return useFairyRingReturn()` |
| `useFairyRingReturn()` | Monastery teleport | `tryArdougneCloakTeleport()` | WIRED | Line 1391: calls `tryArdougneCloakTeleport()` |
| `useFairyRingReturn()` | Fairy ring walk | `MONASTERY_TO_FAIRY_PATH` | WIRED | Line 1409: `walkPath(MONASTERY_TO_FAIRY_PATH)` |
| `useFairyRingReturn()` | BKR validation | `tapGetResponse` + action check | WIRED | Line 1428: `tapGetResponse(true, ringPoly)`; Lines 1439, 1446: validates action contains "last-destination" and "bkr" |
| BKR validation failure | Safe termination | `script.stop()` | WIRED | Lines 1442, 1449: `script.stop()` with error messages |

### Requirements Coverage

| Requirement | Status | Blocking Issue |
|-------------|--------|----------------|
| BANK-01: Zanaris fairy ring teleport | SATISFIED | - |
| BANK-02: Walk to Zanaris bank | SATISFIED | - |
| RETN-01: Ardy cloak to monastery | SATISFIED | - |
| RETN-02: Walk to monastery fairy ring | SATISFIED | - |
| RETN-03: BKR validation and safe termination | SATISFIED | - |

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| - | - | - | - | No anti-patterns found |

Code scan results:
- No TODO/FIXME comments in banking/return methods
- No placeholder returns (all methods have real implementations)
- No console.log-only handlers
- All constants properly defined with real coordinate values

### Build Verification

```
BUILD SUCCESSFUL in 722ms
4 actionable tasks: 4 executed
JAR location: TidalsSecondaryCollector/jar/TidalsSecondaryCollector.jar (156K)
```

### Human Verification Required

#### 1. Zanaris Teleport Flow
**Test:** Start script in fairy ring mode with full inventory at 3-log tile
**Expected:** Script walks to fairy ring at (3469, 3431), uses "Zanaris" action, arrives in Zanaris
**Why human:** Visual verification of teleport animation and destination

#### 2. Zanaris Bank Pathing
**Test:** After teleport to Zanaris, observe walking
**Expected:** Script walks smooth 8-waypoint path to bank chest, opens bank, deposits fungus
**Why human:** Verify pathing doesn't get stuck on obstacles

#### 3. Return via Monastery Fairy Ring
**Test:** After banking, let script return
**Expected:** Ardy cloak teleport -> monastery -> walk to fairy ring -> "Last-destination (BKR)" -> arrive at 3-log tile
**Why human:** Full flow verification, timing, and animation handling

#### 4. BKR Validation Error
**Test:** Unset BKR (set different destination), run script in fairy ring mode
**Expected:** Script detects non-BKR destination, logs clear error, stops safely
**Why human:** Verify error message clarity and safe termination behavior

---

*Verified: 2026-01-16T05:35:00Z*
*Verifier: Claude (gsd-verifier)*
