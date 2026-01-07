
# PixelAnalyzer API Documentation (Verbose)

Source: https://osmb.co.uk/documentation/com/osmb/api/visual/PixelAnalyzer.html

---

## Overview

`PixelAnalyzer` is a core visual analysis interface in the OSMB API. It provides tools for
detecting pixels, clusters, animation states, and respawn circles from captured screen images.
It is primarily used for game automation, visual state detection, and UI-driven logic.

The analyzer works by comparing cached screen images and applying color, shape, and clustering
logic to infer higher-level visual meaning.

---

## Nested Types

### `PixelAnalyzer.RespawnCircle`
Represents a detected respawn circle on screen.

Used to identify temporary circular animations such as respawn indicators.

---

### `PixelAnalyzer.RespawnCircleDrawType`
Enum describing how a respawn circle is drawn.

Possible values depend on visual stroke and fill patterns used by the game client.

---

## Core Pixel Access

### `Color getPixelAt(int x, int y)`
Returns the color of the pixel at the given screen coordinates.

---

### `boolean isPixelAt(Color color, int x, int y)`
Checks whether the pixel at the given position matches the provided color.

---

## Pixel Searching

### `Point findPixel(Color color)`
Finds the first occurrence of a pixel matching the given color.

---

### `List<Point> findPixels(Color color)`
Finds all pixels matching the given color across the image.

---

### `List<Point> findPixels(Color color, Shape shape)`
Finds all matching pixels restricted to a specific shape.

---

### `Point findPixel(Color color, Shape shape)`
Returns the first matching pixel within a shape.

---

## Game Screen Restricted Search

### `Point findPixelOnGameScreen(Color color)`
Searches only the game viewport area.

---

### `List<Point> findPixelsOnGameScreen(Color color)`
Returns all matching pixels on the game screen.

---

### `List<Point> findPixelsOnGameScreen(Color color, Shape shape)`
Searches within a shape constrained to the game viewport.

---

## Pixel Clustering

### `List<Cluster> findClusters(Color color)`
Groups nearby pixels of the same color into clusters.

---

### `List<Cluster> findClusters(Color color, Shape shape)`
Cluster detection within a shape.

---

### `List<Cluster> findClustersOnGameScreen(Color color)`
Cluster detection limited to the game screen.

---

### `List<Cluster> findClustersOnGameScreen(Color color, Shape shape)`
Combines game screen restriction with shape filtering.

---

## Grouping Utilities

### `List<List<Point>> groupPixels(List<Point> pixels, int maxDistance)`
Groups pixels based on spatial proximity.

Used internally for clustering logic.

---

## Animation Detection

### `boolean isAnimating(double minDifferenceFactor)`
Checks whether enough pixels have changed between frames to indicate animation.

---

### `boolean isAnimating(double minDifferenceFactor, Shape shape)`
Same as above, but restricted to a region.

---

### `boolean isPlayerAnimating(double minDifferenceFactor)`
Specialized animation check tuned for detecting player movement or actions.

---

## Respawn Circle Detection

### `Set<RespawnCircleDrawType> findRespawnCircleTypes()`
Detects which respawn circle draw styles are currently visible.

---

### `RespawnCircle getRespawnCircle()`
Returns the detected respawn circle if present.

---

### `RespawnCircle getRespawnCircle(RespawnCircleDrawType type)`
Returns a respawn circle matching a specific draw type.

---

## Highlight Utilities

### `Rectangle getHighlightBounds()`
Returns the bounding box of the currently highlighted region.

---

## Notes for LLM Usage

- Most methods are **pure visual queries**
- State depends on **cached vs current frame**
- Clustering output is spatially significant
- Animation detection relies on **pixel difference ratios**
- Shapes are commonly `Rectangle` or `Polygon`

This file is suitable for:
- LLM embedding
- MCP / tool context
- Cursor / Claude Code indexing
- Visual automation reasoning

---
