# OrbManager Class

**Package:** `com.osmb.api.ui.minimap`

**Type:** Class

**Implements:** `MinimapOrbs`

## Overview

The `OrbManager` class provides methods for interacting with the minimap orbs in Old School RuneScape. These orbs display and control the player's hitpoints, prayer points, run energy, special attack, and quick prayers.

## Constructor

### `OrbManager(Map<Class<? extends Component>, MinimapOrb> orbs)`
Creates a new OrbManager instance with the specified orb components.

**Parameters:**
- `orbs` - A map of component classes to minimap orb instances

## Methods

### Status Reading Methods

#### `getHitpoints()`
Gets the player's current hitpoints, reading directly from the health orb.

**Returns:** `Integer` - The current hitpoints value, or `null` if unavailable

---

#### `getHitpointsPercentage()`
Gets the player's current hitpoints percentage, reading directly from the health orb.

**Returns:** `Integer` - The hitpoints percentage (0-100), or `null` if unavailable

---

#### `getPrayerPoints()`
Gets the player's current prayer points, reading directly from the prayer orb.

**Returns:** `Integer` - The current prayer points value, or `null` if unavailable

---

#### `getPrayerPointsPercentage()`
Gets the player's current prayer points percentage, reading directly from the prayer orb.

**Returns:** `Integer` - The prayer points percentage (0-100), or `null` if unavailable

---

#### `getRunEnergy()`
Gets the player's current run energy, reading directly from the run orb.

**Returns:** `Integer` - The run energy percentage (0-100), or `null` if unavailable

---

#### `hasStaminaEffect()`
Checks if the player currently has a stamina effect active, reading directly from the run orb.

**Returns:** `Boolean` - `true` if stamina effect is active, `false` otherwise, or `null` if unavailable

---

#### `isRunEnabled()`
Checks if running is currently enabled, reading directly from the run orb.

**Returns:** `Boolean` - `true` if run is enabled, `false` otherwise, or `null` if unavailable

---

#### `getSpecialAttackPercentage()`
Gets the player's current special attack energy percentage, reading directly from the special attack orb.

**Returns:** `Integer` - The special attack percentage (0-100), or `null` if unavailable

---

#### `isSpecialAttackActivated()`
Checks if the special attack is currently activated, reading directly from the special attack orb.

**Returns:** `Boolean` - `true` if special attack is activated, `false` otherwise, or `null` if unavailable

---

#### `isQuickPrayersActivated()`
Checks if quick prayers are currently activated, reading directly from the prayer orb.

**Returns:** `Boolean` - `true` if quick prayers are active, `false` otherwise, or `null` if unavailable

---

### Action Methods

#### `setQuickPrayers(boolean activated)`
Toggles quick prayers and waits for confirmation.

**Parameters:**
- `activated` - `true` to activate quick prayers, `false` to deactivate

**Returns:** `boolean` - `true` if the operation was successful, `false` otherwise

---

#### `setQuickPrayers(boolean activated, boolean bypassHumanDelay)`
Toggles quick prayers and waits for confirmation, with optional human-like delay before tapping.

**Parameters:**
- `activated` - `true` to activate quick prayers, `false` to deactivate
- `bypassHumanDelay` - `true` to skip the human-like delay, `false` to include it

**Returns:** `boolean` - `true` if the operation was successful, `false` otherwise

---

#### `setSpecialAttack(boolean activated)`
Toggles special attack and waits for confirmation.

**Parameters:**
- `activated` - `true` to activate special attack, `false` to deactivate

**Returns:** `boolean` - `true` if the operation was successful, `false` otherwise

---

#### `setSpecialAttack(boolean activated, boolean bypassHumanDelay)`
Toggles special attack and waits for confirmation, with optional human-like delay before tapping.

**Parameters:**
- `activated` - `true` to activate special attack, `false` to deactivate
- `bypassHumanDelay` - `true` to skip the human-like delay, `false` to include it

**Returns:** `boolean` - `true` if the operation was successful, `false` otherwise

---

#### `setRun(boolean enabled)`
Toggles run and waits for confirmation.

**Parameters:**
- `enabled` - `true` to enable run, `false` to disable

**Returns:** `boolean` - `true` if the operation was successful, `false` otherwise

---

#### `setRun(boolean enabled, boolean bypassHumanDelay)`
Toggles run and waits for confirmation, with optional human-like delay before tapping.

**Parameters:**
- `enabled` - `true` to enable run, `false` to disable
- `bypassHumanDelay` - `true` to skip the human-like delay, `false` to include it

**Returns:** `boolean` - `true` if the operation was successful, `false` otherwise

---

## Usage Examples

```java
OrbManager orbs = ctx.getOrbManager();

// Check current stats
Integer hp = orbs.getHitpoints();
Integer hpPercent = orbs.getHitpointsPercentage();
Integer prayer = orbs.getPrayerPoints();
Integer runEnergy = orbs.getRunEnergy();
Integer specPercent = orbs.getSpecialAttackPercentage();

// Check status flags
Boolean isRunning = orbs.isRunEnabled();
Boolean hasStamina = orbs.hasStaminaEffect();
Boolean specActive = orbs.isSpecialAttackActivated();
Boolean prayersActive = orbs.isQuickPrayersActivated();

// Enable run if not already enabled
if (orbs.isRunEnabled() != null && !orbs.isRunEnabled()) {
    orbs.setRun(true);
}

// Enable run with bypass delay for instant activation
orbs.setRun(true, true);

// Activate quick prayers
orbs.setQuickPrayers(true);

// Activate special attack with human-like delay
orbs.setSpecialAttack(true, false);

// Deactivate special attack instantly
orbs.setSpecialAttack(false, true);

// Check if we need to restore prayer
if (orbs.getPrayerPointsPercentage() != null && orbs.getPrayerPointsPercentage() < 20) {
    // Drink prayer potion
}

// Enable run only if we have enough energy and stamina effect
if (orbs.getRunEnergy() != null && orbs.getRunEnergy() > 50 && 
    orbs.hasStaminaEffect() != null && orbs.hasStaminaEffect()) {
    orbs.setRun(true);
}
```

## Minimap Orbs Overview

The OrbManager provides access to the following minimap orbs:

1. **Health Orb** - Displays hitpoints (red/green circle)
2. **Prayer Orb** - Displays prayer points and quick prayer status (purple circle)
3. **Run Orb** - Displays run energy and stamina effect (boot icon)
4. **Special Attack Orb** - Displays special attack energy (sword icon)

## Important Notes

- All getter methods may return `null` if the orb is not visible or data is unavailable
- Always null-check return values before using them
- The `bypassHumanDelay` parameter can be used to skip anti-detection delays when speed is critical
- Setting `bypassHumanDelay` to `false` includes human-like delays for more natural interaction
- Action methods wait for confirmation before returning, ensuring the state change has occurred
- The special attack orb is only visible when wielding a weapon with a special attack
- Quick prayers must be configured in the prayer tab before they can be toggled via the orb

## Related Interfaces

- `MinimapOrbs` - The interface implemented by this class
- `MinimapOrb` - Individual orb component
- `Component` - Base component interface

## Best Practices

1. Always check if run is enabled before toggling to avoid unnecessary clicks:
   ```java
   if (orbs.isRunEnabled() != null && !orbs.isRunEnabled()) {
       orbs.setRun(true);
   }
   ```

2. Use percentage methods for threshold checks:
   ```java
   if (orbs.getHitpointsPercentage() != null && orbs.getHitpointsPercentage() < 50) {
       // Eat food
   }
   ```

3. Use `bypassHumanDelay` appropriately:
   - Set to `false` for normal gameplay (more human-like)
   - Set to `true` for combat situations requiring instant activation

4. Handle null returns gracefully:
   ```java
   Integer hp = orbs.getHitpoints();
   if (hp != null && hp < 20) {
       // Emergency eat
   }
   ```
