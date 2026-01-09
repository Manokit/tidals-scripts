# XPTracker Class

**Package:** `com.osmb.api.trackers.experience`

**Type:** Class

**Extends:** `Object`

## Overview

The `XPTracker` class provides comprehensive experience tracking functionality for monitoring skill progression in Old School RuneScape. It tracks experience gained, calculates XP/hour rates, level progress, and estimates time to next level.

## Constructor

### `XPTracker(ScriptCore scriptCoreService, int currentXp)`
Creates a new XP tracker instance starting from the current experience amount.

**Parameters:**
- `scriptCoreService` - The script core service instance
- `currentXp` - The current experience to start tracking from

**Example:**
```java
int currentWoodcuttingXp = ctx.skills.getExperience(Skill.WOODCUTTING);
XPTracker tracker = new XPTracker(ctx, currentWoodcuttingXp);
```

---

## Experience Tracking Methods

### `getXp()`
Returns the current tracked experience.

**Returns:** `double` - The current experience value

---

### `setXp(int xp)`
Sets the current experience value.

**Parameters:**
- `xp` - The experience value to set

**Use case:** Update tracker when experience changes outside of normal tracking

---

### `incrementXp(double exp)`
Increments the tracked experience by the specified amount.

**Parameters:**
- `exp` - The amount of experience to add

**Use case:** Manually add experience when an action completes

---

### `getXpGained()`
Returns the total experience gained since tracking started.

**Returns:** `double` - The total experience gained

**Calculation:** Current XP - Start XP

---

### `getStartXp()`
Returns the starting experience value when the tracker was created.

**Returns:** `int` - The starting experience

---

## Rate Calculation Methods

### `getXpPerHour()`
Calculates and returns the experience gained per hour.

**Returns:** `int` - The experience per hour rate

**Calculation:** Based on XP gained and time elapsed since tracking started

---

## Level Methods

### `getLevel()`
Returns the current level based on tracked experience.

**Returns:** `int` - The current level (1-99)

---

### `getLevelForXp(double exp)`
Returns the level for a given experience amount.

**Parameters:**
- `exp` - The experience amount

**Returns:** `int` - The level for that experience (1-99)

**Use case:** Calculate what level a specific XP amount represents

---

### `getExperienceForLevel(int level)`
Returns the total experience required to reach a specific level.

**Parameters:**
- `level` - The target level (1-99)

**Returns:** `int` - The total experience needed for that level

**Example:**
```java
int xpFor99 = tracker.getExperienceForLevel(99); // Returns 13,034,431
```

---

## Progress Tracking Methods

### `getXpForNextLevel()`
Returns the remaining experience needed to reach the next level.

**Returns:** `double` - The XP remaining to next level

**Calculation:** XP for next level - Current XP

---

### `getLevelProgressPercentage()`
Returns the progress percentage toward the next level.

**Returns:** `int` - Percentage progress (0-100)

**Example:** If halfway to next level, returns 50

---

## Time Estimation Methods

### `timeToNextLevelMillis()`
Calculates the estimated time to reach the next level in milliseconds, based on current XP/hour rate.

**Returns:** `long` - Milliseconds until next level

**Note:** Returns based on current tracked XP/hour rate

---

### `timeToNextLevelMillis(double xpPH)`
Calculates the estimated time to reach the next level in milliseconds, using a specified XP/hour rate.

**Parameters:**
- `xpPH` - The XP per hour rate to use for calculation

**Returns:** `long` - Milliseconds until next level

**Use case:** Calculate time with a different rate than current

---

### `timeToNextLevelString()`
Returns a formatted string showing the estimated time to next level, based on current XP/hour rate.

**Returns:** `String` - Formatted time string (e.g., "2h 15m")

---

### `timeToNextLevelString(double xpPH)`
Returns a formatted string showing the estimated time to next level, using a specified XP/hour rate.

**Parameters:**
- `xpPH` - The XP per hour rate to use for calculation

**Returns:** `String` - Formatted time string (e.g., "2h 15m")

---

## Usage Examples

### Basic XP Tracking

```java
// Create tracker for Woodcutting
int startXp = ctx.skills.getExperience(Skill.WOODCUTTING);
XPTracker woodcuttingTracker = new XPTracker(ctx, startXp);

// In your script loop, manually update XP (if needed)
int currentXp = ctx.skills.getExperience(Skill.WOODCUTTING);
woodcuttingTracker.setXp(currentXp);

// Or increment when you know XP was gained
woodcuttingTracker.incrementXp(25); // Oak log gives 37.5 XP

// Check progress
double xpGained = woodcuttingTracker.getXpGained();
int xpPerHour = woodcuttingTracker.getXpPerHour();
System.out.println("Gained " + xpGained + " XP (" + xpPerHour + " XP/hr)");
```

### Displaying Progress

```java
XPTracker tracker = new XPTracker(ctx, currentXp);

// Display current stats
int level = tracker.getLevel();
int progress = tracker.getLevelProgressPercentage();
double xpToNext = tracker.getXpForNextLevel();
String timeRemaining = tracker.timeToNextLevelString();

System.out.println("Level: " + level + " (" + progress + "%)");
System.out.println("XP to next level: " + xpToNext);
System.out.println("Time to next level: " + timeRemaining);
```

### Script Paint Integration

```java
private XPTracker xpTracker;

@Override
public void onStart() {
    int startXp = ctx.skills.getExperience(Skill.MINING);
    xpTracker = new XPTracker(ctx, startXp);
}

@Override
public void onPaint(Graphics2D g) {
    // Draw XP stats
    g.drawString("XP Gained: " + xpTracker.getXpGained(), 10, 50);
    g.drawString("XP/Hour: " + xpTracker.getXpPerHour(), 10, 70);
    g.drawString("Level: " + xpTracker.getLevel(), 10, 90);
    g.drawString("Progress: " + xpTracker.getLevelProgressPercentage() + "%", 10, 110);
    g.drawString("TTL: " + xpTracker.timeToNextLevelString(), 10, 130);
}
```

### Multi-Skill Tracking

```java
// Track multiple skills
Map<Skill, XPTracker> trackers = new HashMap<>();

// Initialize trackers
trackers.put(Skill.ATTACK, new XPTracker(ctx, ctx.skills.getExperience(Skill.ATTACK)));
trackers.put(Skill.STRENGTH, new XPTracker(ctx, ctx.skills.getExperience(Skill.STRENGTH)));
trackers.put(Skill.DEFENCE, new XPTracker(ctx, ctx.skills.getExperience(Skill.DEFENCE)));

// Update and display all
for (Map.Entry<Skill, XPTracker> entry : trackers.entrySet()) {
    Skill skill = entry.getKey();
    XPTracker tracker = entry.getValue();
    
    // Update
    int currentXp = ctx.skills.getExperience(skill);
    tracker.setXp(currentXp);
    
    // Display
    System.out.println(skill + ": " + tracker.getXpGained() + " XP gained");
}
```

### Goal Tracking

```java
XPTracker tracker = new XPTracker(ctx, currentXp);

// Check if goal reached
int goalLevel = 85;
int goalXp = tracker.getExperienceForLevel(goalLevel);
int currentXp = (int) tracker.getXp();

if (currentXp >= goalXp) {
    System.out.println("Goal level " + goalLevel + " reached!");
} else {
    int xpNeeded = goalXp - currentXp;
    System.out.println("Need " + xpNeeded + " more XP to reach level " + goalLevel);
}
```

### Time Estimates with Different Rates

```java
XPTracker tracker = new XPTracker(ctx, currentXp);

// Get time estimate with current rate
String currentRate = tracker.timeToNextLevelString();
System.out.println("At current rate: " + currentRate);

// Get time estimate with different rates
String fastRate = tracker.timeToNextLevelString(50000); // 50k XP/hr
String slowRate = tracker.timeToNextLevelString(20000); // 20k XP/hr

System.out.println("At 50k XP/hr: " + fastRate);
System.out.println("At 20k XP/hr: " + slowRate);

// Calculate XP needed for specific level
int level99Xp = tracker.getExperienceForLevel(99);
int currentLevel = tracker.getLevel();
int currentLevelXp = tracker.getExperienceForLevel(currentLevel);

double xpTo99 = level99Xp - tracker.getXp();
long hoursTo99 = (long) (xpTo99 / tracker.getXpPerHour());

System.out.println("Hours to 99: " + hoursTo99);
```

### Session Tracking

```java
private XPTracker sessionTracker;
private long sessionStartTime;

public void startSession() {
    int currentXp = ctx.skills.getExperience(Skill.FISHING);
    sessionTracker = new XPTracker(ctx, currentXp);
    sessionStartTime = System.currentTimeMillis();
}

public void displaySessionStats() {
    long sessionDuration = System.currentTimeMillis() - sessionStartTime;
    long hours = sessionDuration / (1000 * 60 * 60);
    long minutes = (sessionDuration / (1000 * 60)) % 60;
    
    System.out.println("=== Session Stats ===");
    System.out.println("Duration: " + hours + "h " + minutes + "m");
    System.out.println("XP Gained: " + sessionTracker.getXpGained());
    System.out.println("XP/Hour: " + sessionTracker.getXpPerHour());
    System.out.println("Levels Gained: " + 
        (sessionTracker.getLevel() - sessionTracker.getLevelForXp(sessionTracker.getStartXp())));
}
```

### Progress Bar Implementation

```java
public void drawProgressBar(Graphics2D g, XPTracker tracker) {
    int x = 10, y = 200;
    int width = 200, height = 20;
    
    // Background
    g.setColor(Color.GRAY);
    g.fillRect(x, y, width, height);
    
    // Progress fill
    int progress = tracker.getLevelProgressPercentage();
    int fillWidth = (width * progress) / 100;
    g.setColor(Color.GREEN);
    g.fillRect(x, y, fillWidth, height);
    
    // Border
    g.setColor(Color.BLACK);
    g.drawRect(x, y, width, height);
    
    // Text
    g.setColor(Color.WHITE);
    String text = progress + "% (" + (int)tracker.getXpForNextLevel() + " XP to " + (tracker.getLevel() + 1) + ")";
    g.drawString(text, x + 5, y + 15);
}
```

### Milestone Tracking

```java
private XPTracker tracker;
private Set<Integer> achievedMilestones = new HashSet<>();

public void checkMilestones() {
    int currentLevel = tracker.getLevel();
    
    // Define milestones
    int[] milestones = {10, 20, 30, 40, 50, 60, 70, 80, 90, 99};
    
    for (int milestone : milestones) {
        if (currentLevel >= milestone && !achievedMilestones.contains(milestone)) {
            System.out.println("Milestone reached: Level " + milestone + "!");
            achievedMilestones.add(milestone);
            
            // Play notification, log to file, etc.
        }
    }
}
```

### Comparing Multiple Trackers

```java
public void compareSkills(XPTracker skill1, XPTracker skill2, String name1, String name2) {
    System.out.println("=== Skill Comparison ===");
    
    System.out.println(name1 + ":");
    System.out.println("  XP Gained: " + skill1.getXpGained());
    System.out.println("  XP/Hour: " + skill1.getXpPerHour());
    System.out.println("  Level: " + skill1.getLevel());
    
    System.out.println(name2 + ":");
    System.out.println("  XP Gained: " + skill2.getXpGained());
    System.out.println("  XP/Hour: " + skill2.getXpPerHour());
    System.out.println("  Level: " + skill2.getLevel());
    
    // Determine which is faster
    if (skill1.getXpPerHour() > skill2.getXpPerHour()) {
        System.out.println(name1 + " is gaining XP faster!");
    } else {
        System.out.println(name2 + " is gaining XP faster!");
    }
}
```

## Important Notes

### Accuracy
- XP/hour calculation is based on elapsed time since tracker creation
- Longer tracking periods provide more accurate XP/hour rates
- Short tracking periods may show inflated or deflated rates

### Time Estimates
- Time to next level assumes constant XP/hour rate
- Actual time may vary based on performance changes
- Use custom XP/hour values for more accurate predictions

### Level Calculations
- Uses OSRS experience formula
- Supports levels 1-99
- Returns level based on total XP, not current skill level in-game

### Manual Updates
- Call `setXp()` or `incrementXp()` to update tracker
- Tracker doesn't automatically poll game state
- Update frequency affects XP/hour accuracy

## Common Patterns

### Hourly Progress Report
```java
private long lastReportTime = 0;

public void checkHourlyReport(XPTracker tracker) {
    long currentTime = System.currentTimeMillis();
    
    if (currentTime - lastReportTime >= 3600000) { // 1 hour
        System.out.println("=== Hourly Report ===");
        System.out.println("XP Gained: " + tracker.getXpGained());
        System.out.println("Current XP/Hour: " + tracker.getXpPerHour());
        System.out.println("Current Level: " + tracker.getLevel());
        
        lastReportTime = currentTime;
    }
}
```

### Goal Percentage
```java
public double getGoalPercentage(XPTracker tracker, int goalLevel) {
    int startXp = tracker.getStartXp();
    int goalXp = tracker.getExperienceForLevel(goalLevel);
    double currentXp = tracker.getXp();
    
    double totalNeeded = goalXp - startXp;
    double gained = currentXp - startXp;
    
    return (gained / totalNeeded) * 100.0;
}
```

### Reset Tracker
```java
public void resetTracker(XPTracker tracker) {
    int currentXp = ctx.skills.getExperience(Skill.MINING);
    tracker.setXp(currentXp);
    // Note: This doesn't reset start XP, create new instance for that
}
```

## Best Practices

1. **Create tracker on script start** - Initialize with current XP
2. **Update regularly** - Call setXp() or incrementXp() frequently
3. **Long-term tracking** - Longer sessions = more accurate rates
4. **Store start XP** - Keep reference to initial XP for comparisons
5. **Multiple trackers** - Track different skills separately
6. **Display updates** - Show stats in paint or console
7. **Handle resets** - Create new tracker if restarting tracking
8. **Goal setting** - Use for motivation and progress tracking
9. **Rate monitoring** - Compare XP/hour to optimize methods
10. **Milestone celebration** - Track and celebrate level achievements

## Related Classes

- `Skill` - Enum representing different skills
- `Skills` - Interface for accessing skill data
- `ScriptCore` - Core script functionality

## Performance Considerations

- Tracker is lightweight, minimal overhead
- XP/hour calculation is simple division
- Time estimates are instant calculations
- No continuous background processing
- Safe to create multiple trackers
- Updates are immediate, no delays
