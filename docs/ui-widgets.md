# UI Widgets and Components

Complete guide to UI interaction: dialogs, equipment, minimap, and more.

## Dialogue Handling

### Basic Dialogue Types
```java
Dialogue dialogue = getWidgetManager().getDialogue();

if (dialogue.isVisible()) {
    DialogueType type = dialogue.getDialogueType();
    
    switch (type) {
        case NPC_DIALOGUE:
        case PLAYER_DIALOGUE:
            dialogue.clickContinue();
            break;
            
        case OPTION:  // Text options
            dialogue.selectOption("Yes"); // or by index
            break;
            
        case ITEM_OPTION:  // Item selection
            dialogue.selectItem(ItemID.COINS);
            break;
            
        case SKILL:  // Quantity input
            dialogue.typeInput("1");
            break;
            
        case TAP_HERE_TO_CONTINUE:  // Level up, etc.
            dialogue.continueChatDialogue();
            break;
    }
}
```

### Complex Multi-Step Dialogue Chains

**Problem**: Some interfaces require TEXT_OPTION → ITEM_OPTION sequences (Construction, Crafting).

```java
private boolean buildItem() {
    RSObject workbench = getObjectManager().getClosestObject(
        getWorldPosition(), "Workbench"
    );

    if (workbench == null) return false;

    // Step 1: Interact with workbench
    if (!workbench.interact("Build")) {
        return false;
    }

    // Step 2: Wait for first dialogue (TEXT_OPTION for category)
    BooleanSupplier waitForTextOption = () -> {
        DialogueType type = getWidgetManager().getDialogue().getDialogueType();
        return type == DialogueType.TEXT_OPTION;
    };

    if (!pollFramesHuman(waitForTextOption, random(4000, 6000))) {
        log(getClass(), "TEXT_OPTION dialogue did not appear.");
        return false;
    }

    // Step 3: Select category
    DialogueType dialogueType = getWidgetManager().getDialogue().getDialogueType();
    if (dialogueType != DialogueType.TEXT_OPTION) {
        log(getClass(), "Expected TEXT_OPTION but got: " + dialogueType);
        return false;
    }

    boolean selectedCategory = getWidgetManager().getDialogue()
        .selectOption("Repair kits");  // Your category

    if (!selectedCategory) {
        log(getClass(), "Failed to select construction category.");
        return false;
    }

    // Step 4: Wait for second dialogue (ITEM_OPTION for specific item)
    BooleanSupplier waitForItemOption = () ->
        getWidgetManager().getDialogue().getDialogueType() == DialogueType.ITEM_OPTION;

    if (!pollFramesHuman(waitForItemOption, random(4000, 6000))) {
        log(getClass(), "ITEM_OPTION dialogue did not appear.");
        return false;
    }

    // Step 5: Select specific item
    DialogueType afterCategory = getWidgetManager().getDialogue().getDialogueType();
    if (afterCategory != DialogueType.ITEM_OPTION) {
        log(getClass(), "Expected ITEM_OPTION, but got: " + afterCategory);
        return false;
    }

    boolean selectedItem = getWidgetManager().getDialogue()
        .selectItem(ItemID.BASIC_REPAIR_KIT);

    if (!selectedItem) {
        log(getClass(), "Failed to select item.");
        return false;
    }

    // Step 6: Wait for crafting to complete
    return waitUntilFinishedCrafting();
}
```

### Waiting for Crafting with Level-Up Detection
```java
private boolean waitUntilFinishedCrafting() {
    Timer amountChangeTimer = new Timer();

    BooleanSupplier condition = () -> {
        // Check for level-up dialogue
        DialogueType type = getWidgetManager().getDialogue().getDialogueType();
        if (type == DialogueType.TAP_HERE_TO_CONTINUE) {
            log(getClass(), "Level up detected!");
            getWidgetManager().getDialogue().continueChatDialogue();
            pollFramesHuman(() -> false, random(1000, 3000));
            return true;
        }

        // Timeout after random duration
        if (amountChangeTimer.timeElapsed() > random(70000, 78000)) {
            return true;
        }

        // Check if we ran out of materials
        ItemGroupResult inv = getWidgetManager().getInventory()
            .search(Set.of(ItemID.MAHOGANY_PLANK));
        if (inv == null) return false;
        return !inv.contains(ItemID.MAHOGANY_PLANK);
    };

    return pollFramesHuman(condition, random(70000, 78000));
}
```

---

## Equipment Tab

```java
EquipmentTab equipment = getWidgetManager().getEquipment();

// Check if item equipped
boolean hasWeapon = equipment.contains(ItemID.RUNE_SCIMITAR);

// Get equipped item
SearchableItem weapon = equipment.getItem(EquipmentSlot.WEAPON);

// Interact with equipped item (for teleport jewelry, etc.)
SearchableItem ring = equipment.getItem(EquipmentSlot.RING);
if (ring != null) {
    ring.interact("Rub");
}
```

---

## Minimap & Orbs

```java
Minimap minimap = getWidgetManager().getMinimap();

// Health/Prayer/Run orbs
int healthPercent = minimap.getHitpointsPercentage();
int prayerPoints = minimap.getPrayerOrb().getValue();
int runEnergy = minimap.getRunOrb().getValue();
boolean isRunEnabled = minimap.isRunEnabled();
minimap.toggleRun(true);

// NPC/Item positions from minimap
List<WorldPosition> npcPositions = minimap.getNPCPositions();
List<WorldPosition> itemPositions = minimap.getItemPositions();
```

---

## Buff Overlays

```java
// For tracking buffs/debuffs shown on screen
BuffOverlay poisonOverlay = new BuffOverlay(this, POISON_SPRITE_ID);
if (poisonOverlay.isVisible()) {
    String value = poisonOverlay.getText();
    // Handle poison
}

// Waterskin tracking (when user has overlay enabled)
BuffOverlay waterskinOverlay = new BuffOverlay(this, WATERSKIN_SPRITE_ID);
if (waterskinOverlay.isVisible()) {
    String remaining = waterskinOverlay.getText();
}
```

---

## Custom Interface with OCR Detection

**Use Case**: Teleport interfaces, minigame interfaces, custom dialogues.

```java
public class MushroomTransportInterface extends CustomInterface {
    private static final String TITLE_TEXT = "Bittercap Mushrooms";
    private static final Rectangle TITLE_BOUNDS = new Rectangle(10, 5, 200, 20);
    private static final SearchablePixel ORANGE_TEXT_COLOR =
        new SearchablePixel(25, 100, 50, 10, 10, 10); // HSL

    private Map<ButtonType, Rectangle> buttons = null;

    public enum ButtonType {
        VERDANT_VALLEY("Verdant Valley"),
        MUSHROOM_MEADOW("Mushroom Meadow"),
        LUMBER_CLEARING("Lumber Clearing");

        private final String text;
        ButtonType(String text) { this.text = text; }
        public String getText() { return text; }
    }

    @Override
    public boolean isVisible() {
        Rectangle bounds = getBounds();
        if (bounds == null) return false;

        Rectangle titleArea = bounds.getSubRectangle(TITLE_BOUNDS);
        String text = core.getOCR().getText(
            Font.STANDARD_FONT_BOLD,
            titleArea,
            ORANGE_TEXT_COLOR
        );

        boolean visible = text.equalsIgnoreCase(TITLE_TEXT);

        // Register buttons dynamically if visible
        if (visible && buttons == null) {
            this.buttons = registerButtons(bounds);
        }
        return visible;
    }

    private Map<ButtonType, Rectangle> registerButtons(Rectangle bounds) {
        Map<ButtonType, Rectangle> buttons = new HashMap<>();

        // Find containers by corner sprite IDs (use debug tool to find these)
        List<Rectangle> containers = core.getImageAnalyzer()
            .findContainers(bounds, 913, 914, 915, 916);

        for (Rectangle container : containers) {
            String rawText = core.getOCR().getText(
                Font.SMALL_FONT,
                container,
                ORANGE_BUTTON_TEXT_COLOR
            ).trim();

            // Normalize OCR errors (I -> l is common)
            String normalizedText = rawText.replace('I', 'l').toLowerCase();

            for (ButtonType buttonType : ButtonType.values()) {
                if (normalizedText.equals(buttonType.getText().toLowerCase())) {
                    buttons.put(buttonType, new Rectangle(
                        container.x - bounds.x,
                        container.y - bounds.y,
                        container.width,
                        container.height
                    ));
                    break;
                }
            }
        }
        return buttons;
    }

    public boolean selectOption(ButtonType buttonType) {
        Rectangle buttonScreenBounds = getButtonScreenBounds(buttonType);
        if (buttonScreenBounds == null) return false;

        if (core.getFinger().tap(buttonScreenBounds)) {
            return core.pollFramesUntil(() -> !isVisible(), 5000);
        }
        return false;
    }
}

// Usage:
MushroomTransportInterface transport = new MushroomTransportInterface(this);
if (transport.isVisible()) {
    transport.selectOption(ButtonType.VERDANT_VALLEY);
}
```

---

## Spellbook Teleportation

**Use Case**: High-frequency teleport scripts, offering training, altar running.

```java
private static final int TELEPORT_COOLDOWN_MS = 1800; // Base cooldown
private long lastTeleportTime = 0;
private int teleportCount = 0;

private boolean castTeleportSpell() {
    // Wait for cooldown
    long remaining = getCooldownForSpell() -
        (System.currentTimeMillis() - lastTeleportTime);

    if (remaining > 0) {
        pollFramesUntil(
            () -> (System.currentTimeMillis() - lastTeleportTime) >= getCooldownForSpell(),
            (int) remaining + 100
        );
        return false;
    }

    // Cast the spell
    boolean success;
    try {
        success = getWidgetManager().getSpellbook().selectSpell(
            Spell.VARROCK_TELEPORT,  // Or your spell
            null  // null = auto-cast at current location
        );
    } catch (SpellNotFoundException e) {
        log(getClass(), "Spell sprite not found: " + e.getMessage());
        stop();
        return false;
    }

    if (success) {
        lastTeleportTime = System.currentTimeMillis();
        teleportCount++;
    }

    return success;
}

// Randomized cooldown (anti-ban)
private long getCooldownForSpell() {
    int roll = random(100);

    if (roll < 50) {
        return random(1800, 1901);  // ~1.8-1.9s
    } else if (roll < 90) {
        return random(1850, 2001);  // ~1.85-2.0s
    } else {
        return random(1900, 2301);  // ~1.9-2.3s
    }
}
```

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

## Best Practices

1. **Always check dialogue type** before selecting options
2. **Wait for dialogues to appear** using pollFramesUntil
3. **Handle level-up dialogues** in crafting/training loops
4. **Use OCR for custom interfaces** when standard widgets unavailable
5. **Track chatbox messages in onNewFrame()** for event detection
6. **Verify spell casting success** before continuing
7. **Add randomization to teleport cooldowns** for anti-ban
