# XPDropsListener

**Package:** `com.osmb.api.trackers.xpdrops`

**Type:** Class

Listener class for tracking XP drops and managing XP trackers for different skills.

## Constructor

### XPDropsListener(ScriptCore, XPDropsComponent)

```java
public XPDropsListener(ScriptCore core, XPDropsComponent xpDropsComponent)
```

Creates a new XP drops listener.

**Parameters:**
- `core` - The script core instance.
- `xpDropsComponent` - The XP drops component to use for displaying XP drops.

---

## Methods

### getXPTrackers()

```java
public Map<SkillType, XPTracker> getXPTrackers()
```

Gets the map of XP trackers for each skill type.

**Returns:** A map containing XP trackers keyed by skill type.

---

### checkXP()

```java
public void checkXP()
```

Checks for XP changes and updates the XP trackers accordingly.
