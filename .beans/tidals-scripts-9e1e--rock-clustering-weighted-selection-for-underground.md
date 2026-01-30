---
# tidals-scripts-9e1e
title: Rock clustering & weighted selection for underground gem mine
status: completed
type: feature
priority: normal
created_at: 2026-01-30T03:35:10Z
updated_at: 2026-01-30T03:38:26Z
---

Break up repetitive mining patterns in underground mine with hardcoded rock clusters and weighted random selection.

## Summary
Implemented sticky cluster-based mining for the underground gem mine. Player stays in a cluster until all rocks are on cooldown, then picks a new cluster weighted toward the closest. Banking resets the active cluster for variety between trips.

## Changes Made

### Mine.java
- Added 11 hardcoded `UNDERGROUND_CLUSTERS` with exact rock positions
- Added `activeClusterIndex` (sticky cluster state) and `clusterFirstPick` flag
- Added `wasActive` tracking to reset cluster after banking
- `selectFromCluster()`: picks between 2 closest clusters (85/15 weighted), but only if 2nd cluster is within 3 tiles of 1st
- `selectWeightedRockFromCluster()`: weighted random on first rock pick in a new cluster, greedy nearest after that
- `selectGreedyNearest()`: extracted helper for upper mine and fallback
- `clusterCentroidDistance()`: helper for sorting clusters by distance
- `getAvailableRocksInCluster()`: maps hardcoded positions to live RSObjects

### Locations.java
- Extended `UNDERGROUND_MINING_AREA` width from 26 to 34 (x: 2825-2859) to cover eastern clusters (10, 11) at x=2852-2857

## Design Decisions
- **85/15 cluster weighting** (skew 0.15): mostly stays efficient, occasional variety
- **3-tile distance guard**: if 2nd cluster is >3 tiles farther, always pick closest (no human runs that far)
- **Greedy nearest within cluster**: only the initial rock pick is weighted, rest is pure efficiency
- **First rock pick heavily weighted** (skew 0.08): ~90%+ chance of picking one of the 2 closest rocks when entering a new cluster, rocks 3+ are rare but possible
- **Post-bank reset**: `wasActive` flag detects when Mine re-activates after Bank ran, resets cluster choice