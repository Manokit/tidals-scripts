# Discord Post Template

Every script should have a `discord_post.md` file in its root directory. This file is used for sharing the script on Discord and should be updated as features are added.

## Maintenance Guidelines

- **Initial Release**: Create the main post with all sections below
- **Feature Updates**: Update the main post sections when new features are added
- **Version Updates**: Add a new version section (v1.1, v1.2, etc.) at the bottom ONLY when explicitly requested
- **Keep it Current**: The main post should always reflect the current state of the script

---

## Template

```markdown
[Short description with wiki link](https://oldschool.runescape.wiki/w/Your_Activity) and what the script does. One or two sentences max.


**__Features__**

- **Feature Category 1:**
  - Bullet point detail
  - Another detail

- **Performance Stats** (if applicable):
  - XP/hr estimates with conditions noted

- **Quality of Life:**
  - Automatic handling of edge cases
  - Paint overlay with live stats
  - Error recovery (jail escape, stuck detection, etc.)



**__Requirements__**

- Level requirement 1
- Level requirement 2
- Any items or quests needed



**__Script Repository__**

**Download JAR:**  -> [jar](https://github.com/Manokit/tidals-scripts/tree/main/YourScriptName/jar)

**Source Code** -> [Github](https://github.com/Manokit/tidals-scripts/tree/main/YourScriptName)



**__How to Run__**

1. Download the `.jar` file from the repository
2. Place the file into your local scripts directory: `C:\Users\%username%\.osmb\Scripts`
3. Launch OSMB - the script will appear in your list as: `[LOCAL] YourScriptName`
4. Navigate to starting location
5. Any setup steps (tag NPCs, equip items, etc.)
6. Start the script and configure options



**__Tips__**

- Tip about optimal usage
- Tip about mode selection
- Tip about automatic features



**__Troubleshooting__**

- **Common Issue:** Solution or workaround

---


## v1.1 Update - Short Description

**Category:**

- Change 1
- Change 2

**Other fixes:**
- Bug fix 1
- Bug fix 2
```

---

## Example: TidalsCannonballThiever

See `TidalsCannonballThiever/discord_post.md` for a real example showing:
- Two operating modes with clear descriptions
- XP/hr estimates with conditions
- Smart detection features
- QoL features (banking, paint, error handling)
- Clear requirements
- Step-by-step setup instructions
- Tips for each mode
- Troubleshooting section
- Multiple version update sections (v1.2 through v1.6)
