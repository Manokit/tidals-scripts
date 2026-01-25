# API Reference

Complete reference for core OSMB API methods and imports.

## Core API Access (from Script/ScriptCore)

```java
getWidgetManager()      // UI: Bank, Inventory, Dialogue, Tabs, Minimap
getObjectManager()      // Find RSObjects (trees, rocks, banks, etc.)
getSceneManager()       // NPCs, ground items, tiles
getWalker()             // Pathfinding and walking
getFinger()             // Mouse/touch input
getKeyboard()           // Keyboard input
getPixelAnalyzer()      // Color/pixel detection
getOCR()                // Text recognition
getItemManager()        // Item definitions
getWorldPosition()      // Player's world coordinates
getLocalPosition()      // Player's local/scene coordinates
getScreen()             // Screen capture, dimensions
getSceneProjector()     // 3D to 2D projection (tile cubes, polygons)
```

---

## Essential Imports

```java
// Core
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptManifest;
import com.osmb.api.script.ScriptCore;

// Location
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.location.position.types.LocalPosition;
import com.osmb.api.location.area.impl.RectangleArea;
import com.osmb.api.location.area.impl.PolyArea;

// Scene
import com.osmb.api.scene.RSObject;
import com.osmb.api.scene.RSNPC;
import com.osmb.api.scene.ObjectManager;
import com.osmb.api.scene.SceneManager;

// UI
import com.osmb.api.ui.bank.Bank;
import com.osmb.api.ui.tabs.Inventory;
import com.osmb.api.ui.chatbox.Dialogue;
import com.osmb.api.ui.chatbox.Chatbox;
import com.osmb.api.ui.minimap.Minimap;
import com.osmb.api.ui.widget.WidgetManager;

// Items
import com.osmb.api.item.ItemID;
import com.osmb.api.item.ItemGroupResult;
import com.osmb.api.item.SearchableItem;

// Input
import com.osmb.api.input.Finger;
import com.osmb.api.input.MenuEntry;

// Visual
import com.osmb.api.visual.PixelAnalyzer;
import com.osmb.api.visual.SearchablePixel;
import com.osmb.api.visual.drawing.SceneProjector;
import com.osmb.api.visual.ocr.OCR;

// Walker
import com.osmb.api.walker.Walker;
import com.osmb.api.walker.WalkConfig;

// Shapes
import com.osmb.api.shape.Polygon;
import com.osmb.api.shape.Rectangle;

// Utils
import com.osmb.api.utils.RandomUtils;
import com.osmb.api.utils.UIResult;

// Overlays
import com.osmb.api.ui.overlay.BuffOverlay;
```

---

## Script Structure Methods

### Required Overrides
```java
@Override
public void onStart() {
    // Called once when script starts
    // Initialize variables, load resources
}

@Override  
public int poll() {
    // Main loop - called repeatedly
    // Return sleep time in milliseconds
    return 600;
}

@Override
public void onStop() {
    // Called when script stops
    // Cleanup resources
}
```

### Optional Overrides
```java
@Override
public boolean canAFK() {
    return true; // Control AFK behavior
}

@Override
public boolean canBreak() {
    return true; // Control break behavior  
}

@Override
public boolean canHopWorlds() {
    return true; // Control world hop behavior
}

@Override
public void onNewFrame() {
    // Called each frame
    // READ-ONLY operations only!
    // Use for tracking chat messages, etc.
}

@Override
public ScriptOptions getScriptOptions() {
    return new ScriptOptions() {
        @Override
        public int[] regionsToPrioritise() {
            // Return region IDs for faster map loading
            return new int[]{12850};
        }
    };
}

@Override
public boolean skipYawCheck() {
    return true; // For areas without compass (Temple Trekking)
}
```

---

## Waiting and Timing

### pollFramesUntil - Condition-Based Waiting (BLOCKING)
```java
// Wait for condition with timeout - BLOCKS until condition true or timeout
boolean success = pollFramesUntil(() -> bank.isVisible(), 5000);

// Fixed delay (condition never true, waits full timeout)
pollFramesUntil(() -> false, 2000);  // waits exactly 2000ms
```

### pollFramesHuman - Human-Like Delays (BLOCKING)
```java
// Condition + human delay after (200-400ms added when condition met)
pollFramesHuman(() -> inventory.isFull(), 30000);

// Humanized delay (condition instantly true, then adds human delay)
pollFramesHuman(() -> true, RandomUtils.weightedRandom(200, 400));  // humanized delay
```

**DEPRECATED**: Do NOT use `submitTask` - it may be async. Always use `pollFramesUntil` or `pollFramesHuman`.

### Random Delays
```java
// Use RandomUtils for human-like timing
int delay = RandomUtils.weightedRandom(100, 500); // Weighted toward middle
int gaussian = RandomUtils.gaussianRandom(300, 100); // Mean 300, stddev 100
int uniform = RandomUtils.uniformRandom(400, 800); // Uniform distribution
```

---

## Regions and Maps

### Region Prioritization
```java
@Override
public ScriptOptions getScriptOptions() {
    return new ScriptOptions() {
        @Override
        public int[] regionsToPrioritise() {
            return new int[]{12850, 12851, 12594}; // Example: Lumbridge area
        }
    };
}
```

### Custom Maps (for Instanced Areas)
```java
@Override
public void onStart() {
    // Required for: Tempoross, Pest Control, Temple Trekking, etc.
    // Full region is 63x63 tiles (0-indexed)
    addCustomMap(new MapDefinition(
        2624,  // baseX
        2560,  // baseY  
        63,    // width (use 64 if 63 doesn't work)
        63,    // height
        0,     // plane
        0      // flags
    ));
}
```

---

## XP Tracking Utility Pattern

```java
// Create reusable XPTracking utility class
// Place in utils/XPTracking.java

package utils;

import com.osmb.api.ScriptCore;
import com.osmb.api.trackers.experience.XPTracker;
import com.osmb.api.ui.component.tabs.skill.SkillType;
import java.util.Map;

public class XPTracking {
    private final ScriptCore core;

    public XPTracking(ScriptCore core) {
        this.core = core;
    }

    private XPTracker getTracker(SkillType skill) {
        Map<SkillType, XPTracker> trackers = core.getXPTrackers();
        if (trackers == null) return null;
        return trackers.get(skill);
    }

    // Example: Woodcutting methods
    public XPTracker getWoodcuttingTracker() {
        return getTracker(SkillType.WOODCUTTING);
    }

    public double getWoodcuttingXpGained() {
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return 0.0;
        return tracker.getXpGained();
    }

    public int getWoodcuttingXpPerHour() {
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return 0;
        return tracker.getXpPerHour();
    }

    public int getWoodcuttingLevel() {
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return 0;
        return tracker.getLevel();
    }

    public String getWoodcuttingTimeToNextLevel() {
        XPTracker tracker = getWoodcuttingTracker();
        if (tracker == null) return "-";
        return tracker.timeToNextLevelString();
    }
}
```

**Usage:**
```java
public class MyScript extends Script {
    private XPTracking xpTracking;
    
    @Override
    public void onStart() {
        xpTracking = new XPTracking(this);
    }
    
    @Override
    public int poll() {
        log("XP Gained: " + xpTracking.getWoodcuttingXpGained());
        log("XP/hr: " + xpTracking.getWoodcuttingXpPerHour());
        return 600;
    }
}
```

**Adapt for other skills:**
```java
// For crafting:
public XPTracker getCraftingTracker() {
    return getTracker(SkillType.CRAFTING);
}

// For mining:
public XPTracker getMiningTracker() {
    return getTracker(SkillType.MINING);
}
```

**IMPORTANT**: XPTracker does NOT have a `checkXP()` method - trackers update automatically.

---

## Chatbox Message Reading

```java
// In onNewFrame() - only use read-only methods!
@Override
public void onNewFrame() {
    updateChatBoxLines();
}

private List<String> previousLines = new ArrayList<>();

private void updateChatBoxLines() {
    Chatbox chatbox = getWidgetManager().getChatbox();
    if (chatbox.getActiveFilterTab() != ChatboxFilterTab.GAME) return;
    
    List<String> currentLines = chatbox.getLines();
    List<String> newLines = getNewLines(currentLines, previousLines);
    
    for (String line : newLines) {
        if (line.contains("You catch a")) {
            // Handle catch message
        } else if (line.contains("You fail")) {
            // Handle fail message
        }
    }
    
    previousLines = new ArrayList<>(currentLines);
}
```

---

## Respawn Circle Detection

```java
// Find respawn circles in an area (for mining, fishing spots)
List<RespawnCircle> circles = getPixelAnalyzer().findRespawnCircleTypes(searchBounds);

// Or use with objects
List<RSObject> rocks = getObjectManager().getObjects(o -> o.getName().equals("Rocks"));
List<RespawnCircleResult> activeRocks = getPixelAnalyzer().getRespawnCircleObjects(
    rocks,
    RespawnCircleDrawType.DRAW_ON_ACTIVE, // or DRAW_ON_DEPLETED
    zHeight,
    distanceTolerance
);
```

---

## Debug Tool Tips

1. **Find Item ID**: Debug tool > Item > Search by name or ID
2. **Custom Comparator**: If item not detected, adjust HSL tolerance
3. **Respawn Circles**: Adjust z-height and distance tolerance
4. **Create Poly Area**: Use built-in tool to define areas
5. **Scene Viewer**: Inspect objects, tiles, collision data
6. **Check Sprites**: Search all sprites on screen

---

## Resources

- **API Documentation**: https://doc.osmb.co.uk
- **Javadocs**: https://osmb.co.uk/javadocs (for ItemID constants, etc.)
