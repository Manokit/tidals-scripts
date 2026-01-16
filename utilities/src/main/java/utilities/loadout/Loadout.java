package utilities.loadout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Aggregates all container slots for a complete loadout configuration.
 *
 * <p>Container sizes:
 * <ul>
 *   <li>Equipment: 11 slots (matches OSRS worn equipment)</li>
 *   <li>Inventory: 28 slots</li>
 *   <li>Rune Pouch: 4 slots (nullable)</li>
 *   <li>Bolt Pouch: 4 slots (nullable)</li>
 *   <li>Quiver: 1 slot (nullable)</li>
 * </ul>
 *
 * <p>Equipment slot indices:
 * 0=head, 1=cape, 2=amulet, 3=ammo, 4=weapon, 5=body, 6=shield, 7=legs, 8=gloves, 9=boots, 10=ring
 *
 * @see EquipmentSlot for slot definitions and layout
 */
public class Loadout {

    // container sizes
    public static final int EQUIPMENT_SIZE = 11;
    public static final int INVENTORY_SIZE = 28;
    public static final int RUNE_POUCH_SIZE = 4;
    public static final int BOLT_POUCH_SIZE = 4;
    public static final int QUIVER_SIZE = 1;

    // equipment slot constants
    public static final int SLOT_HEAD = 0;
    public static final int SLOT_CAPE = 1;
    public static final int SLOT_AMULET = 2;
    public static final int SLOT_AMMO = 3;
    public static final int SLOT_WEAPON = 4;
    public static final int SLOT_BODY = 5;
    public static final int SLOT_SHIELD = 6;
    public static final int SLOT_LEGS = 7;
    public static final int SLOT_GLOVES = 8;
    public static final int SLOT_BOOTS = 9;
    public static final int SLOT_RING = 10;

    private final String name;
    private final LoadoutItem[] equipment;
    private final LoadoutItem[] inventory;
    private LoadoutItem[] runePouch;
    private LoadoutItem[] boltPouch;
    private LoadoutItem[] quiver;

    /**
     * Creates a new Loadout with the given name.
     * All container slots are initialized to null.
     *
     * @param name the loadout name (required)
     */
    public Loadout(String name) {
        this.name = name;
        this.equipment = new LoadoutItem[EQUIPMENT_SIZE];
        this.inventory = new LoadoutItem[INVENTORY_SIZE];
        this.runePouch = null;
        this.boltPouch = null;
        this.quiver = null;
    }

    /**
     * Gets the loadout name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    // equipment methods

    /**
     * Gets a defensive copy of the equipment array.
     *
     * @return a copy of the equipment array (11 slots)
     */
    public LoadoutItem[] getEquipment() {
        return Arrays.copyOf(equipment, EQUIPMENT_SIZE);
    }

    /**
     * Gets a single equipment slot.
     *
     * @param slot the slot index (0-10, use SLOT_* constants or EquipmentSlot enum)
     * @return the item at that slot, or null if empty
     * @throws ArrayIndexOutOfBoundsException if slot is out of range
     */
    public LoadoutItem getEquipmentSlot(int slot) {
        return equipment[slot];
    }

    /**
     * Sets an equipment slot.
     *
     * @param slot the slot index (0-10, use SLOT_* constants or EquipmentSlot enum)
     * @param item the item to set, or null to clear
     * @throws ArrayIndexOutOfBoundsException if slot is out of range
     */
    public void setEquipment(int slot, LoadoutItem item) {
        equipment[slot] = item;
    }

    // inventory methods

    /**
     * Gets a defensive copy of the inventory array.
     *
     * @return a copy of the inventory array (28 slots)
     */
    public LoadoutItem[] getInventory() {
        return Arrays.copyOf(inventory, INVENTORY_SIZE);
    }

    /**
     * Gets a single inventory slot.
     *
     * @param slot the slot index (0-27)
     * @return the item at that slot, or null if empty
     * @throws ArrayIndexOutOfBoundsException if slot is out of range
     */
    public LoadoutItem getInventorySlot(int slot) {
        return inventory[slot];
    }

    /**
     * Sets an inventory slot.
     *
     * @param slot the slot index (0-27)
     * @param item the item to set, or null to clear
     * @throws ArrayIndexOutOfBoundsException if slot is out of range
     */
    public void setInventorySlot(int slot, LoadoutItem item) {
        inventory[slot] = item;
    }

    // rune pouch methods

    /**
     * Returns whether this loadout has a rune pouch defined.
     *
     * @return true if rune pouch is set
     */
    public boolean hasRunePouch() {
        return runePouch != null;
    }

    /**
     * Gets a defensive copy of the rune pouch array.
     *
     * @return a copy of the rune pouch array (4 slots), or null if not set
     */
    public LoadoutItem[] getRunePouch() {
        return runePouch != null ? Arrays.copyOf(runePouch, RUNE_POUCH_SIZE) : null;
    }

    /**
     * Sets the rune pouch contents.
     *
     * @param items array of up to 4 items, or null to clear
     */
    public void setRunePouch(LoadoutItem[] items) {
        if (items == null) {
            this.runePouch = null;
        } else {
            this.runePouch = new LoadoutItem[RUNE_POUCH_SIZE];
            System.arraycopy(items, 0, this.runePouch, 0, Math.min(items.length, RUNE_POUCH_SIZE));
        }
    }

    // bolt pouch methods

    /**
     * Returns whether this loadout has a bolt pouch defined.
     *
     * @return true if bolt pouch is set
     */
    public boolean hasBoltPouch() {
        return boltPouch != null;
    }

    /**
     * Gets a defensive copy of the bolt pouch array.
     *
     * @return a copy of the bolt pouch array (4 slots), or null if not set
     */
    public LoadoutItem[] getBoltPouch() {
        return boltPouch != null ? Arrays.copyOf(boltPouch, BOLT_POUCH_SIZE) : null;
    }

    /**
     * Sets the bolt pouch contents.
     *
     * @param items array of up to 4 items, or null to clear
     */
    public void setBoltPouch(LoadoutItem[] items) {
        if (items == null) {
            this.boltPouch = null;
        } else {
            this.boltPouch = new LoadoutItem[BOLT_POUCH_SIZE];
            System.arraycopy(items, 0, this.boltPouch, 0, Math.min(items.length, BOLT_POUCH_SIZE));
        }
    }

    // quiver methods

    /**
     * Returns whether this loadout has a quiver defined.
     *
     * @return true if quiver is set
     */
    public boolean hasQuiver() {
        return quiver != null;
    }

    /**
     * Gets a defensive copy of the quiver array.
     *
     * @return a copy of the quiver array (1 slot), or null if not set
     */
    public LoadoutItem[] getQuiver() {
        return quiver != null ? Arrays.copyOf(quiver, QUIVER_SIZE) : null;
    }

    /**
     * Sets the quiver contents.
     *
     * @param items array of up to 1 item, or null to clear
     */
    public void setQuiver(LoadoutItem[] items) {
        if (items == null) {
            this.quiver = null;
        } else {
            this.quiver = new LoadoutItem[QUIVER_SIZE];
            if (items.length > 0) {
                this.quiver[0] = items[0];
            }
        }
    }

    /**
     * Returns a flat list of all non-null items across all containers.
     * Useful for restock logic to iterate over everything that needs to be obtained.
     *
     * @return list of all items in this loadout
     */
    public List<LoadoutItem> getAllItems() {
        List<LoadoutItem> allItems = new ArrayList<>();

        // add equipment items
        for (LoadoutItem item : equipment) {
            if (item != null) {
                allItems.add(item);
            }
        }

        // add inventory items
        for (LoadoutItem item : inventory) {
            if (item != null) {
                allItems.add(item);
            }
        }

        // add rune pouch items
        if (runePouch != null) {
            for (LoadoutItem item : runePouch) {
                if (item != null) {
                    allItems.add(item);
                }
            }
        }

        // add bolt pouch items
        if (boltPouch != null) {
            for (LoadoutItem item : boltPouch) {
                if (item != null) {
                    allItems.add(item);
                }
            }
        }

        // add quiver items
        if (quiver != null) {
            for (LoadoutItem item : quiver) {
                if (item != null) {
                    allItems.add(item);
                }
            }
        }

        return allItems;
    }
}
