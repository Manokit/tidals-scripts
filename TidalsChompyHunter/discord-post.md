:ChompyBird:  [Chompy bird hunting](https://oldschool.runescape.wiki/w/Chompy_bird_hunting) :ChompyBird:  automation for Western Provinces Diary. Handles the full loop: fill bellows, inflate toads, drop bait, hunt chompies, and optionally pluck corpses.

**__Features__**

- **Full Automation:**
  - Fills ogre bellows at nearby swamp bubbles (finds nearest, cycles through 5 locations if needed)
  - Inflates swamp toads and drops them as bait
  - Detects and attacks chompy spawns via pixel recognition
  - Tracks kill count synced with game chatbox
  - Live arrow count via buff overlay (stops when out of ammo)

- **Plucking Mode (Optional):**
  - Plucks chompy corpses for feathers
  - Extra pet roll chance per pluck
  - Reduces kills/hr but maximizes value

- **Diary Milestone Tracking:**
  - Tracks progress toward 30, 125, 300, 1000 kills
  - Paint shows kills to go for next milestone
  - Discord notifications when milestones reached

- **Anti-Crash:**
  - Auto-hops when another player enters your hunting area
  - Randomized 7-12 second threshold before hopping
  - Checks for occupied worlds on startup

- **Smart Hop/Break Handling:**
  - Waits for ground toads to be consumed before hopping
  - 60-second timeout prevents getting stuck

- **Discord Webhooks:**
  - Periodic progress updates (configurable interval)
  - Milestone notifications (30/125/300/1000)
  - Optional username in messages for multi-instance gamers


**__Supported Equipment__**

- **Bows:** Ogre Bow, Comp Ogre Bow
- **Arrows:** Ogre Arrow, Bronze/Iron/Steel/Black/Mithril/Adamant/Rune Brutal


**__Requirements__**

- [Big Chompy Bird Hunting](https://oldschool.runescape.wiki/w/Big_Chompy_Bird_Hunting) quest completed
- Ogre bow or Comp ogre bow equipped
- Ogre arrows or any brutal arrows equipped
- 2+ Ogre bellows in inventory
- 3+ free inventory slots



**__Known Issues__**

- Sometimes loses track of a bloated toad on the ground and removes the tile cube for it



**__Script Repository__**

**Download JAR:**  -> [jar](https://github.com/Manokit/tidals-scripts/tree/main/TidalsChompyHunter/jar)

**Source Code** -> [Github](https://github.com/Manokit/tidals-scripts/tree/main/TidalsChompyHunter)



**__How to Run__**

1. Download the `.jar` file from the repository
2. Place the file into your local scripts directory: `C:\Users\%username%\.osmb\Scripts`
3. Launch OSMB - the script will appear in your list as: `[LOCAL] TidalsChompyHunter`
4. Equip Ogre bow or Comp ogre bow and ogre arrows/brutal arrows
5. Have ogre bellows in inventory
6. Stand in the Feldip Hills chompy hunting area (near swamp bubbles)
7. **__TAG-ALL SWAMP TOADS__**
8. Start the script and configure options


**__Tips__**

- Bring 2-5 ogre bellows to reduce refill trips
- Discord webhooks are great for monitoring progress
- **Most heavily tested with Plucking mode enabled, use that for now, unless you want to be a champion and report bugs for non-plucking mode**
- Anti-crash is enabled by default 
- Configure hop/break profiles in OSMB for extended sessions