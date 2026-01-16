package utilities.loadout;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Persists loadouts to Java Preferences for cross-session storage.
 *
 * <p>Uses Preferences.userRoot().node("tidals/loadouts") as the storage node.
 * Loadouts are serialized as JSON using LoadoutExporter/LoadoutImporter.
 *
 * <p>Key format: loadout name sanitized to alphanumeric + underscore, max 80 chars.
 * Default loadouts use "_default_{scriptName}" prefix to avoid collision.
 *
 * <p>All methods catch and log exceptions, returning null/false on failure
 * for graceful degradation.
 *
 * @see LoadoutExporter for serialization
 * @see LoadoutImporter for deserialization
 */
public class LoadoutPersistence {

    private static final String PREFS_NODE = "tidals/loadouts";
    private static final String DEFAULT_PREFIX = "_default_";
    private static final int MAX_KEY_LENGTH = 80;

    private LoadoutPersistence() {
        // utility class
    }

    /**
     * Gets the preferences node for loadout storage.
     */
    private static Preferences getPrefsNode() {
        return Preferences.userRoot().node(PREFS_NODE);
    }

    /**
     * Sanitizes a key to alphanumeric + underscore only, max 80 chars.
     *
     * @param key the key to sanitize
     * @return the sanitized key
     */
    private static String sanitizeKey(String key) {
        if (key == null || key.isEmpty()) {
            return "_unnamed_";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < key.length() && sb.length() < MAX_KEY_LENGTH; i++) {
            char c = key.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }

        String result = sb.toString();
        return result.isEmpty() ? "_unnamed_" : result;
    }

    /**
     * Saves a loadout to preferences.
     *
     * @param key the key to save under (will be sanitized)
     * @param loadout the loadout to save
     * @return true if saved successfully, false on error
     */
    public static boolean save(String key, Loadout loadout) {
        if (key == null || loadout == null) {
            return false;
        }

        try {
            String sanitizedKey = sanitizeKey(key);
            String json = LoadoutExporter.toJson(loadout);
            getPrefsNode().put(sanitizedKey, json);
            getPrefsNode().flush();
            return true;
        } catch (Exception e) {
            System.err.println("LoadoutPersistence.save failed for key '" + key + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads a loadout from preferences.
     *
     * @param key the key to load (will be sanitized)
     * @return the loaded Loadout, or null if not found or on error
     */
    public static Loadout load(String key) {
        if (key == null) {
            return null;
        }

        try {
            String sanitizedKey = sanitizeKey(key);
            String json = getPrefsNode().get(sanitizedKey, null);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return LoadoutImporter.fromJson(json);
        } catch (Exception e) {
            System.err.println("LoadoutPersistence.load failed for key '" + key + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Deletes a saved loadout from preferences.
     *
     * @param key the key to delete (will be sanitized)
     * @return true if deleted successfully, false on error
     */
    public static boolean delete(String key) {
        if (key == null) {
            return false;
        }

        try {
            String sanitizedKey = sanitizeKey(key);
            getPrefsNode().remove(sanitizedKey);
            getPrefsNode().flush();
            return true;
        } catch (Exception e) {
            System.err.println("LoadoutPersistence.delete failed for key '" + key + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Lists all saved loadout keys (excluding default loadouts).
     *
     * @return list of saved loadout keys, or empty list on error
     */
    public static List<String> list() {
        List<String> keys = new ArrayList<>();

        try {
            String[] allKeys = getPrefsNode().keys();
            for (String key : allKeys) {
                // exclude default loadout keys
                if (!key.startsWith(DEFAULT_PREFIX)) {
                    keys.add(key);
                }
            }
        } catch (BackingStoreException e) {
            System.err.println("LoadoutPersistence.list failed: " + e.getMessage());
        }

        return keys;
    }

    /**
     * Checks if a loadout exists in preferences.
     *
     * @param key the key to check (will be sanitized)
     * @return true if exists, false otherwise
     */
    public static boolean exists(String key) {
        if (key == null) {
            return false;
        }

        try {
            String sanitizedKey = sanitizeKey(key);
            String json = getPrefsNode().get(sanitizedKey, null);
            return json != null && !json.isEmpty();
        } catch (Exception e) {
            System.err.println("LoadoutPersistence.exists failed for key '" + key + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Saves a loadout as the default for a specific script.
     *
     * @param scriptName the script name (will be sanitized)
     * @param loadout the loadout to save as default
     * @return true if saved successfully, false on error
     */
    public static boolean saveDefault(String scriptName, Loadout loadout) {
        if (scriptName == null || loadout == null) {
            return false;
        }

        String defaultKey = DEFAULT_PREFIX + sanitizeKey(scriptName);
        try {
            String json = LoadoutExporter.toJson(loadout);
            getPrefsNode().put(defaultKey, json);
            getPrefsNode().flush();
            return true;
        } catch (Exception e) {
            System.err.println("LoadoutPersistence.saveDefault failed for script '" + scriptName + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads the default loadout for a specific script.
     *
     * @param scriptName the script name (will be sanitized)
     * @return the default Loadout for this script, or null if not found or on error
     */
    public static Loadout loadDefault(String scriptName) {
        if (scriptName == null) {
            return null;
        }

        String defaultKey = DEFAULT_PREFIX + sanitizeKey(scriptName);
        try {
            String json = getPrefsNode().get(defaultKey, null);
            if (json == null || json.isEmpty()) {
                return null;
            }
            return LoadoutImporter.fromJson(json);
        } catch (Exception e) {
            System.err.println("LoadoutPersistence.loadDefault failed for script '" + scriptName + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a script has a saved default loadout.
     *
     * @param scriptName the script name (will be sanitized)
     * @return true if a default exists, false otherwise
     */
    public static boolean hasDefault(String scriptName) {
        if (scriptName == null) {
            return false;
        }

        String defaultKey = DEFAULT_PREFIX + sanitizeKey(scriptName);
        try {
            String json = getPrefsNode().get(defaultKey, null);
            return json != null && !json.isEmpty();
        } catch (Exception e) {
            System.err.println("LoadoutPersistence.hasDefault failed for script '" + scriptName + "': " + e.getMessage());
            return false;
        }
    }

    /**
     * Clears the default loadout for a specific script.
     *
     * @param scriptName the script name (will be sanitized)
     * @return true if cleared successfully, false on error
     */
    public static boolean clearDefault(String scriptName) {
        if (scriptName == null) {
            return false;
        }

        String defaultKey = DEFAULT_PREFIX + sanitizeKey(scriptName);
        try {
            getPrefsNode().remove(defaultKey);
            getPrefsNode().flush();
            return true;
        } catch (Exception e) {
            System.err.println("LoadoutPersistence.clearDefault failed for script '" + scriptName + "': " + e.getMessage());
            return false;
        }
    }
}
