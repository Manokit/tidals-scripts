# Technology Stack

**Analysis Date:** 2026-01-16

## Languages

**Primary:**
- Java 17 - All script source code, utility libraries, and task implementations

**Secondary:**
- Groovy - Gradle build scripts (`settings.gradle`)

## Runtime

**Environment:**
- Java 17 (required by OSMB platform)
- OSMB Client runtime (color bot framework)

**Package Manager:**
- Gradle (wrapper included: `gradlew`, `gradlew.bat`)
- Lockfile: Not present (Gradle does not use lockfiles by default)

**Build Tool:**
- Custom `osmb build` CLI command wraps Gradle
- Gradle wrapper handles dependency resolution

## Frameworks

**Core:**
- OSMB API (`API/API.jar`, ~9.5MB) - Color bot scripting framework
  - Provides: Script base class, widget managers, visual detection, walking, input simulation
  - Package: `com.osmb.api.*`

**UI:**
- JavaFX - Script configuration dialogs (ScriptUI classes)
  - `javafx.scene.*` for layout and controls
  - Built-in OSMB JavaFX utilities (`com.osmb.api.javafx.JavaFXUtils`)

**Graphics:**
- Java AWT/Swing - Paint overlay rendering (`java.awt.*`, `javax.imageio.ImageIO`)
  - Custom `Canvas` abstraction from OSMB API (`com.osmb.api.visual.drawing.Canvas`)

## Key Dependencies

**Critical (External API):**
- OSMB API (`API/API.jar`) - The entire scripting framework
  - Provides all game interaction: `Script`, `WidgetManager`, `ObjectManager`, `Walker`, `Finger`, etc.

**Shared Utilities (Internal):**
- TidalsUtilities.jar (`utilities/jar/TidalsUtilities.jar`, ~114KB)
  - `RetryUtils` - Menu interaction retries with logging
  - `BankingUtils` - Bank discovery and operations
  - `BankSearchUtils` - Bank search box interactions
  - `DialogueUtils` - Dialogue handling
  - `TabUtils` - Tab opening with verification
  - `LoadoutManager` - Equipment/inventory loadout management

**Standard Library:**
- `java.net.HttpURLConnection` - HTTP requests for stats reporting
- `java.util.prefs.Preferences` - User settings persistence
- `java.util.UUID` - Session ID generation
- `javax.imageio.ImageIO` - Logo image loading

## Configuration

**Environment:**
- Java 17 must be available (scripts check via OSMB)
- No `.env` files - secrets stored in gitignored `obf/Secrets.java`

**Build:**
- `settings.gradle` - Root build settings, auto-discovers script subprojects
- Each script directory contains source in `src/main/java/`
- Build output: `<script>/jar/<ScriptName>.jar`

**User Preferences:**
- Stored via `java.util.prefs.Preferences` (per-script settings)
- Key format: `<scriptname>_<setting>` (e.g., `tgemcutter_selected_gem`)

## Project Structure

**Script Projects (buildable):**
- `TidalsSecondaryCollector/` - Herblore secondary collector
- `TidalsGemCutter/` - Gem cutting script
- `TidalsGoldSuperheater/` - Gold bar superheating
- `TidalsCannonballThiever/` - Port Roberts stall thieving
- `TidalsBankTester/` - Bank utility testing
- `TidalsLoadoutTester/` - Loadout system testing

**Shared Code:**
- `utilities/` - TidalsUtilities library (shared across scripts)
- `API/` - OSMB API jar (provided by platform)

**Example Scripts:**
- `examples/dAmethystMiner/`
- `examples/dLooter/`
- `examples/dSunbleakWCer/`
- `examples/BlisterwoodChopper.java`

## Platform Requirements

**Development:**
- Java 17 JDK
- macOS/Linux/Windows (cross-platform via Gradle wrapper)
- OSMB CLI tool (`osmb build`) for script compilation

**Production:**
- OSMB Client (closed-source, color bot platform)
- Scripts deployed as `.jar` files to OSMB scripts directory

## Build Commands

```bash
# build specific script
osmb build TidalsSecondaryCollector

# build all scripts
osmb build all

# build utilities library
cd tidals-scripts && JAVA_HOME=$(/usr/libexec/java_home -v 17) gradle :utilities:build
```

## Key OSMB API Imports

```java
// Core script
import com.osmb.api.script.Script;
import com.osmb.api.script.ScriptDefinition;
import com.osmb.api.script.SkillCategory;

// UI/Widgets
import com.osmb.api.ui.tabs.Tab;
import com.osmb.api.ui.bank.Bank;
import com.osmb.api.item.ItemID;
import com.osmb.api.item.ItemGroupResult;

// Scene/Objects
import com.osmb.api.scene.RSObject;
import com.osmb.api.location.position.types.WorldPosition;
import com.osmb.api.location.area.impl.RectangleArea;

// Visual/Input
import com.osmb.api.visual.drawing.Canvas;
import com.osmb.api.visual.image.Image;
import com.osmb.api.shape.Polygon;
import com.osmb.api.input.MenuEntry;

// Walking
import com.osmb.api.walker.WalkConfig;

// XP Tracking
import com.osmb.api.trackers.experience.XPTracker;
```

---

*Stack analysis: 2026-01-16*
