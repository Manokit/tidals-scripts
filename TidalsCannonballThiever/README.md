# Tidals Cannonball Thiever

An OSMB script for thieving cannonballs and ores from stalls in Port Roberts. Supports two operating modes for different play styles and XP rates.

## Requirements

- 50 Sailing (To get to Port Roberts)
- 87 Thieving
- Empty inventory (for Two Stall mode)
- Start near the cannonball stall in Port Roberts

## Modes

### Single Stall Mode

Thieves only from the cannonball stall. When the patrolling guard approaches, the script retreats to a nearby safety tile and waits for the guard to pass before resuming.

Less actions, this mode was made first, probably not worth using unless you're ultra paranoid about long term efficient play

### Two Stall Mode

Alternates between the cannonball stall and ore stall following the guard patrol pattern. Uses an XP-based cycle system:
- 4 cannonball thieves
- 2 ore thieves
- Repeat

The script tracks guard movement at the pixel level for instant detection and switches stalls at optimal times. When inventory fills with ores, automatically deposits at the nearby deposit box.

Maximum XP/hr, a lot more actions than the single stall mode

## Features

- Guard patrol tracking with pixel-level movement detection
- XP-based cycle timing for consistent switches
- Automatic jail escape if caught
- Inventory tracking for cannonballs and ores by type
- Deposit box support for ore inventory management
- Break/world hop/AFK support with proper cycle resync
- Paint overlay showing runtime, items stolen, XP rates, and progress

## Setup

1. Travel to Port Roberts
2. Stand near the stalls
3. Empty your inventory (required for Two Stall mode item tracking)
4. Start the script
5. Select your preferred mode in the configuration window
6. Click Start Thieving

## Paint Display

The overlay shows:
- Runtime
- Cannonballs stolen by type (Bronze, Iron, Steel, Mithril, Adamant, Rune)
- Ores stolen by type (Two Stall mode)
- Total items and rates per hour
- XP gained and XP/hr
- Time to level and experience to level
- Current thieving level with levels gained
- Current task and mode

## Notes

- Two Stall mode requires an empty inventory to collect ores
- The script will automatically deposit ores when inventory is full
- After world hops or breaks, the script waits for a fresh guard cycle before resuming
- Guard detection uses both minimap positions and pixel-based highlight tracking for reliability

## Version

1.0
