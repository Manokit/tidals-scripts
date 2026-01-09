# CachedObject<T> Class

**Package:** `com.osmb.api.utils`

**Type:** Generic Class

**Extends:** `Object`

## Overview

The `CachedObject<T>` class is a generic wrapper that associates an object with a screen UUID. This is useful for tracking objects that are tied to a specific game screen state, allowing you to invalidate cached data when the screen changes.

## Type Parameter

- `T` - The type of object being cached

## Constructor

### `CachedObject(UUID screenUUID, T object)`
Creates a new cached object with the specified screen UUID and object.

**Parameters:**
- `screenUUID` - The UUID representing the screen state when this object was cached
- `object` - The object to cache

---

## Methods

### `getObject()`
Returns the cached object.

**Returns:** `T` - The cached object

---

### `getScreenUUID()`
Returns the screen UUID associated with this cached object.

**Returns:** `UUID` - The screen UUID when this object was cached

---

### `toString()`
Returns a string representation of this cached object.

**Returns:** `String` - String representation

**Overrides:** `Object.toString()`

---

## Usage Examples

```java
// Create a cached object with current screen UUID
UUID currentScreenUUID = ctx.getScreenUUID(); // Hypothetical method
MyObject myObject = new MyObject("data");
CachedObject<MyObject> cached = new CachedObject<>(currentScreenUUID, myObject);

// Retrieve the cached object
MyObject retrieved = cached.getObject();

// Check if cache is still valid by comparing screen UUIDs
UUID cachedScreenUUID = cached.getScreenUUID();
if (cachedScreenUUID.equals(ctx.getScreenUUID())) {
    // Cache is still valid for current screen
    MyObject obj = cached.getObject();
    obj.doSomething();
} else {
    // Screen has changed, need to recache
    cached = new CachedObject<>(ctx.getScreenUUID(), new MyObject("new data"));
}

// Example: Caching search results
CachedObject<List<Point>> cachedPixels = null;

List<Point> getPixels() {
    UUID currentUUID = getCurrentScreenUUID();
    
    // Check if cache is valid
    if (cachedPixels != null && cachedPixels.getScreenUUID().equals(currentUUID)) {
        return cachedPixels.getObject();
    }
    
    // Cache is invalid or doesn't exist, search again
    List<Point> pixels = ctx.pixelAnalyzer.findPixels(searchArea, searchablePixel);
    cachedPixels = new CachedObject<>(currentUUID, pixels);
    return pixels;
}

// Example: Caching NPC clusters
CachedObject<List<PixelCluster>> cachedClusters = null;

List<PixelCluster> getNPCClusters() {
    UUID currentUUID = getCurrentScreenUUID();
    
    if (cachedClusters != null && cachedClusters.getScreenUUID().equals(currentUUID)) {
        // Screen hasn't changed, use cached clusters
        return cachedClusters.getObject();
    }
    
    // Screen changed or no cache, find new clusters
    List<Point> pixels = findNPCPixels();
    List<PixelCluster> clusters = ctx.pixelAnalyzer.groupPixels(pixels, 10.0, 20);
    cachedClusters = new CachedObject<>(currentUUID, clusters);
    return clusters;
}
```

## Common Use Cases

### 1. Caching Expensive Pixel Searches
```java
private CachedObject<List<Point>> cachedSearchResults = null;

public List<Point> getTargetPixels() {
    UUID currentScreen = getScreenUUID();
    
    if (cachedSearchResults != null && 
        cachedSearchResults.getScreenUUID().equals(currentScreen)) {
        return cachedSearchResults.getObject();
    }
    
    // Expensive pixel search
    List<Point> results = ctx.pixelAnalyzer.findPixelsOnGameScreen(
        searchArea,
        new SearchablePixel(180, 100, 57.5, 5)
    );
    
    cachedSearchResults = new CachedObject<>(currentScreen, results);
    return results;
}
```

### 2. Caching Game Objects
```java
private CachedObject<List<RSObject>> cachedObjects = null;

public List<RSObject> getNearbyTrees() {
    UUID currentScreen = getScreenUUID();
    
    if (cachedObjects != null && 
        cachedObjects.getScreenUUID().equals(currentScreen)) {
        return cachedObjects.getObject();
    }
    
    List<RSObject> trees = ctx.objects.getAll(
        obj -> obj.getName().equals("Tree")
    );
    
    cachedObjects = new CachedObject<>(currentScreen, trees);
    return trees;
}
```

### 3. Caching UI State
```java
private CachedObject<InventoryState> cachedInventory = null;

class InventoryState {
    List<Item> items;
    int freeSlots;
    
    InventoryState(List<Item> items, int freeSlots) {
        this.items = items;
        this.freeSlots = freeSlots;
    }
}

public InventoryState getInventoryState() {
    UUID currentScreen = getScreenUUID();
    
    if (cachedInventory != null && 
        cachedInventory.getScreenUUID().equals(currentScreen)) {
        return cachedInventory.getObject();
    }
    
    List<Item> items = ctx.inventory.getAll();
    int freeSlots = ctx.inventory.getFreeSlots();
    InventoryState state = new InventoryState(items, freeSlots);
    
    cachedInventory = new CachedObject<>(currentScreen, state);
    return state;
}
```

### 4. Cache Invalidation Pattern
```java
private CachedObject<DetectionResult> cache = null;
private UUID lastKnownScreen = null;

public DetectionResult getDetectionResult() {
    UUID currentScreen = getScreenUUID();
    
    // Check if screen changed
    if (lastKnownScreen == null || !lastKnownScreen.equals(currentScreen)) {
        // Screen changed, invalidate cache
        cache = null;
        lastKnownScreen = currentScreen;
    }
    
    // Use cache if available
    if (cache != null && cache.getScreenUUID().equals(currentScreen)) {
        return cache.getObject();
    }
    
    // Perform expensive detection
    DetectionResult result = performExpensiveDetection();
    cache = new CachedObject<>(currentScreen, result);
    return result;
}
```

## Important Notes

- **Screen UUID Correlation:** The screen UUID should uniquely identify the game screen state
- **Cache Invalidation:** Always check if the screen UUID matches before using cached data
- **Thread Safety:** This class is not thread-safe; synchronize access if using from multiple threads
- **Memory Management:** Be mindful of what you cache - large objects can consume memory
- **Immutability:** The screen UUID is set at construction and cannot be changed
- **Null Handling:** The class accepts null for both parameters, handle nulls appropriately

## When to Use CachedObject

**Good Use Cases:**
- Caching expensive pixel searches that don't change within a frame
- Storing pre-computed clusters or detection results
- Caching game object lists that are valid for the current screen
- Reducing redundant API calls or expensive operations

**Avoid Using For:**
- Data that changes frequently within the same screen state
- Very small objects where the caching overhead isn't worth it
- Objects that need to be accessed from multiple threads without synchronization
- Situations where a simple variable would suffice

## Performance Considerations

- Comparing UUIDs is fast, making cache validity checks very efficient
- The cached object is stored by reference, not copied
- No automatic cache expiration - you must manually check validity
- Consider the trade-off between caching overhead and operation cost

## Related Patterns

```java
// Pattern 1: Lazy initialization with cache
private CachedObject<ExpensiveResult> cache = null;

public ExpensiveResult getResult() {
    if (cache == null || !isValidCache(cache)) {
        cache = new CachedObject<>(getScreenUUID(), computeResult());
    }
    return cache.getObject();
}

// Pattern 2: Cache with TTL (time-to-live)
private CachedObject<Data> cache = null;
private long cacheTime = 0;
private static final long CACHE_TTL = 1000; // 1 second

public Data getData() {
    long now = System.currentTimeMillis();
    
    if (cache != null && 
        cache.getScreenUUID().equals(getScreenUUID()) &&
        (now - cacheTime) < CACHE_TTL) {
        return cache.getObject();
    }
    
    Data data = fetchData();
    cache = new CachedObject<>(getScreenUUID(), data);
    cacheTime = now;
    return data;
}

// Pattern 3: Multi-level caching
private CachedObject<ProcessedData> processedCache = null;
private CachedObject<RawData> rawCache = null;

public ProcessedData getProcessedData() {
    UUID currentScreen = getScreenUUID();
    
    // Check processed cache first
    if (processedCache != null && 
        processedCache.getScreenUUID().equals(currentScreen)) {
        return processedCache.getObject();
    }
    
    // Get raw data (possibly from its own cache)
    RawData raw = getRawData();
    
    // Process and cache
    ProcessedData processed = process(raw);
    processedCache = new CachedObject<>(currentScreen, processed);
    return processed;
}
```

## Best Practices

1. **Always validate cache before use** - Check screen UUID matches
2. **Handle null cases** - Both object and UUID can be null
3. **Clear caches when appropriate** - Set to null when no longer needed
4. **Document cache lifetime** - Make it clear when caches are invalidated
5. **Consider memory usage** - Don't cache excessively large objects
6. **Profile performance** - Ensure caching actually improves performance
7. **Use meaningful type parameters** - `CachedObject<List<Point>>` is clearer than `CachedObject<List>`
