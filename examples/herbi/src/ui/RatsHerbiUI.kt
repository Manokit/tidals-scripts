package ui

import com.osmb.api.ScriptCore
import com.osmb.api.item.ItemID
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import util.VersionDownloader
import java.awt.Desktop
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.prefs.Preferences

class RatsHerbiUI(
    private val currentVersion: Double,
    private val script: ScriptCore
) {
    
    private val prefs = Preferences.userNodeForPackage(RatsHerbiUI::class.java)

    private val versionDownloader = VersionDownloader(
        fileName = "herbi",
        downloadUrl = "https://gitlab.com/rats_rs/osmb/-/raw/main/herbi",
        currentVersion = currentVersion,
        log = { message -> script.log("[VersionDownloader]", message) }
    )

    // Preference keys
    private val PREF_USE_STAMINAS = "ratsherbi_use_staminas"
    private val PREF_SELECTED_POTION = "ratsherbi_selected_potion"
    private val PREF_POTION_COUNT = "ratsherbi_potion_count"
    private val PREF_USE_HERB_SACK = "ratsherbi_use_herb_sack"
    
    // UI Components
    private lateinit var useStaminasCheckBox: CheckBox
    private lateinit var potionComboBox: ComboBox<StaminaPotion>
    private lateinit var potionCountSpinner: Spinner<Int>
    private lateinit var useHerbSackCheckBox: CheckBox
    private lateinit var versionUpdatePanel: VBox
    
    enum class StaminaPotion(val displayName: String, val itemId: Set<Int>) {
        STAMINA_POTION("Stamina Potion", setOf(ItemID.STAMINA_POTION1, ItemID.STAMINA_POTION2, ItemID.STAMINA_POTION3, ItemID.STAMINA_POTION4)),
        SUPER_ENERGY("Super Energy", setOf(ItemID.SUPER_ENERGY1, ItemID.SUPER_ENERGY2, ItemID.SUPER_ENERGY3, ItemID.SUPER_ENERGY4)),
        ENERGY_POTION("Energy Potion", setOf(ItemID.ENERGY_POTION1, ItemID.ENERGY_POTION2, ItemID.ENERGY_POTION3, ItemID.ENERGY_POTION4));

        override fun toString(): String {
            return displayName
        }
    }
    
    fun buildScene(core: ScriptCore): Scene {
        val mainBox = VBox(15.0).apply {
            style = "-fx-background-color: #2d3436; -fx-padding: 20; -fx-alignment: center"
            isFillWidth = true
            // Let VBox size to its content - no minimum height
            minHeight = 0.0
        }
        
        // Title
        val titleLabel = Label("Rats Herbiboar").apply {
            style = "-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-padding: 0 0 10 0"
        }
        
        // Version Update Notification Panel
        versionUpdatePanel = createVersionUpdatePanel()
        
        // Use Staminas Checkbox
        useStaminasCheckBox = CheckBox("Use Energy Saving Potions").apply {
            style = "-fx-text-fill: white; -fx-font-size: 14px"
            isSelected = prefs.getBoolean(PREF_USE_STAMINAS, false)
        }
        
        // Potion Selection
        val potionLabel = Label("Select Potion Type:").apply {
            style = "-fx-text-fill: white; -fx-font-size: 14px"
        }
        
        potionComboBox = ComboBox<StaminaPotion>().apply {
            items = FXCollections.observableArrayList(StaminaPotion.values().toList())
            style = "-fx-background-color: #636e72; -fx-text-fill: white; -fx-font-size: 12px"
            isDisable = !useStaminasCheckBox.isSelected
            
            // Restore saved selection
            val savedPotion = prefs.get(PREF_SELECTED_POTION, StaminaPotion.STAMINA_POTION.name)
            try {
                val potion = StaminaPotion.valueOf(savedPotion)
                selectionModel.select(potion)
            } catch (e: IllegalArgumentException) {
                selectionModel.select(StaminaPotion.STAMINA_POTION)
            }
        }

        val infoLabelHerbSack = Label("⚠ Herb sack must be manually set to the \"Open\" state before starting the bot").apply {
            style = "-fx-text-fill: #f39c12; -fx-font-size: 12px; -fx-font-weight: bold"
            isWrapText = true
        }
        
        // Warning Label
        val warningLabel = Label("⚠ HCIM Warning: If you are running on a HCIM make sure that you've disabled NPC attack options before running!").apply {
            style = "-fx-text-fill: #f39c12; -fx-font-size: 12px; -fx-font-weight: bold"
            isWrapText = true
        }
        
        // Potion Count Selection
        val potionCountLabel = Label("Number of Potions:").apply {
            style = "-fx-text-fill: white; -fx-font-size: 14px"
        }
        
        potionCountSpinner = Spinner<Int>().apply {
            valueFactory = SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, prefs.getInt(PREF_POTION_COUNT, 3))
            style = "-fx-background-color: #636e72; -fx-text-fill: white; -fx-font-size: 12px"
            isDisable = !useStaminasCheckBox.isSelected
        }
        
        // Use Herb Sack Checkbox
        useHerbSackCheckBox = CheckBox("Use Herb Sack").apply {
            style = "-fx-text-fill: white; -fx-font-size: 14px"
            isSelected = prefs.getBoolean(PREF_USE_HERB_SACK, true)
            infoLabelHerbSack.isVisible = isSelected
        }

        useHerbSackCheckBox.setOnAction {
            infoLabelHerbSack.isVisible = useHerbSackCheckBox.isSelected
        }
        
        // Event Handlers
        useStaminasCheckBox.setOnAction {
            potionComboBox.isDisable = !useStaminasCheckBox.isSelected
            potionCountSpinner.isDisable = !useStaminasCheckBox.isSelected
        }
        
        // Confirm Button
        val confirmButton = Button("Yes, I've disabled attack options").apply {
            style = """
                -fx-background-color: #27ae60;
                -fx-text-fill: white;
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-padding: 10 20;
                -fx-background-radius: 5;
            """.trimIndent()
            setOnAction { saveSettings() }
        }
        
        mainBox.children.addAll(
            titleLabel,
            versionUpdatePanel,
            useStaminasCheckBox,
            potionLabel,
            potionComboBox,
            potionCountLabel,
            potionCountSpinner,
            useHerbSackCheckBox,
            infoLabelHerbSack,
            warningLabel,
            confirmButton
        )
        
        // Wrap mainBox in ScrollPane for scrollable UI
        val scrollPane = ScrollPane(mainBox).apply {
            style = """
                -fx-background-color: #2d3436;
                -fx-border-color: transparent;
            """.trimIndent()
            isFitToWidth = true
            isFitToHeight = false
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbarPolicy = ScrollPane.ScrollBarPolicy.AS_NEEDED
            // Smooth scrolling
            isPannable = false
        }
        
        // Check for updates when UI is built
        checkForUpdates { mainBox.children.remove(versionUpdatePanel) }
        
        val scene = Scene(scrollPane, 420.0, 600.0)
        scene.fill = Color.web("#2d3436")
        scene.stylesheets.add("style.css")
        return scene
    }
    
    private fun saveSettings() {
        prefs.putBoolean(PREF_USE_STAMINAS, isUseStaminas())
        prefs.put(PREF_SELECTED_POTION, getSelectedPotion().name)
        prefs.putInt(PREF_POTION_COUNT, getPotionCount())
        prefs.putBoolean(PREF_USE_HERB_SACK, isUseHerbSack())
        
        // Close the window
        val stage = potionComboBox.scene.window as? Stage
        stage?.close()
    }
    
    // Getters for the script to use
    fun isUseStaminas(): Boolean {
        return useStaminasCheckBox.isSelected
    }
    
    fun getSelectedPotion(): StaminaPotion {
        return potionComboBox.value ?: StaminaPotion.STAMINA_POTION
    }
    
    fun getSelectedPotionIds(): Set<Int> {
        return getSelectedPotion().itemId
    }
    
    fun getPotionCount(): Int {
        return potionCountSpinner.value
    }
    
    fun isUseHerbSack(): Boolean {
        return useHerbSackCheckBox.isSelected
    }
    
    private fun createVersionUpdatePanel(): VBox {
        val panel = VBox(10.0).apply {
            style = """
                -fx-background-color: #34495e;
                -fx-border-color: #3498db;
                -fx-border-width: 2px;
                -fx-border-radius: 5px;
                -fx-background-radius: 5px;
                -fx-padding: 15;
                -fx-alignment: center;
                -fx-pref-width: 380;
                -fx-max-width: 380;
            """.trimIndent()
            isVisible = false
            isManaged = false  // Don't take up space when hidden
        }
        
        val updateLabel = Label().apply {
            style = """
                -fx-text-fill: #ecf0f1;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-wrap-text: true;
            """.trimIndent()
            isWrapText = true
            maxWidth = 350.0
            prefWidth = 350.0
        }
        
        // Changelog TextArea
        val changelogTextArea = TextArea().apply {
            style = """
                -fx-background-color: #2d3436;
                -fx-text-fill: #ecf0f1;
                -fx-font-size: 11px;
                -fx-font-family: monospace;
                -fx-border-color: #636e72;
                -fx-border-width: 1px;
                -fx-border-radius: 3px;
                -fx-background-radius: 3px;
                -fx-padding: 8;
            """.trimIndent()
            isEditable = false
            isWrapText = true
            prefRowCount = 20
            maxWidth = 350.0
            prefWidth = 350.0
            prefHeight = 150.0
            minHeight = 150.0
        }
        
        // Collapsible Changelog TitledPane
        val changelogPane = TitledPane("📋 View Changelog", changelogTextArea).apply {
            styleClass.add("changelog-titled-pane")
            isExpanded = false
            maxWidth = 350.0
            prefWidth = 350.0
        }
        
        val buttonBox = HBox(10.0).apply {
            style = "-fx-alignment: center"
        }
        
        val viewOnGitLabButton = Button("Download Manually").apply {
            style = """
                -fx-background-color: #3498db;
                -fx-text-fill: white;
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
            setOnAction {
                openGitLabUrl()
            }
        }
        
        val downloadButton = Button("Automatically Update").apply {
            style = """
                -fx-background-color: #27ae60;
                -fx-text-fill: white;
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                -fx-padding: 8 15;
                -fx-background-radius: 5;
                -fx-cursor: hand;
            """.trimIndent()
            setOnAction {
                downloadUpdate(updateLabel)
            }
        }
        
        buttonBox.children.addAll(viewOnGitLabButton, downloadButton)
        panel.children.addAll(updateLabel, buttonBox, changelogPane)
        
        // Store reference to changelog components for later use
        panel.properties["changelogPane"] = changelogPane
        panel.properties["changelogTextArea"] = changelogTextArea
        
        // Initially hide changelog pane
        changelogPane.isVisible = false
        changelogPane.isManaged = false
        
        return panel
    }
    
    private fun checkForUpdates(onNoUpdate: () -> Unit) {
        // Run version check in background thread to avoid blocking UI
        Thread {
            try {
                val latestVersion = versionDownloader.newVersionAvailable()
                Platform.runLater {
                    if (latestVersion != null) {
                        val (_, version) = latestVersion
                        versionUpdatePanel.isVisible = true
                        versionUpdatePanel.isManaged = true  // Take up space when visible
                        val updateLabel = versionUpdatePanel.children[0] as Label
                        updateLabel.text = "🆕 New version available: v$version"
                        updateLabel.isWrapText = true
                        
                        // Load changelog for this specific version
                        loadChangelog(version)
                    } else {
                        versionUpdatePanel.isVisible = false
                        versionUpdatePanel.isManaged = false  // Don't take up space when hidden
                        onNoUpdate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    
    private fun loadChangelog(version: Double) {
        // Load changelog in background thread
        Thread {
            try {
                val changelog = fetchChangelog(version)
                Platform.runLater {
                    val changelogTextArea = versionUpdatePanel.properties["changelogTextArea"] as? TextArea
                    val changelogPane = versionUpdatePanel.properties["changelogPane"] as? TitledPane
                    
                    if (changelog != null && changelog.isNotBlank()) {
                        // Show changelog pane if we have content
                        changelogTextArea?.text = changelog
                        changelogPane?.isVisible = true
                        changelogPane?.isManaged = true
                    } else {
                        // Hide changelog pane if no content
                        changelogPane?.isVisible = false
                        changelogPane?.isManaged = false
                    }
                }
            } catch (e: Exception) {
                Platform.runLater {
                    val changelogPane = versionUpdatePanel.properties["changelogPane"] as? TitledPane
                    // Hide changelog pane on error
                    changelogPane?.isVisible = false
                    changelogPane?.isManaged = false
                }
            }
        }.start()
    }
    
    private fun fetchChangelog(version: Double): String? {
        return try {
            val changelogUrl = "https://gitlab.com/rats_rs/osmb/-/raw/main/change.log"
            val connection = URL(changelogUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                script.log("[Changelog] Failed to fetch changelog. Response code: $responseCode")
                return null
            }
            
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val fullChangelog = reader.readText()
            reader.close()
            connection.disconnect()
            
            // Parse and extract only the section for this version
            extractVersionSection(fullChangelog, version)
        } catch (e: Exception) {
            script.log("[Changelog] Error fetching changelog: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    private fun extractVersionSection(fullChangelog: String, version: Double): String? {
        // Try different version format patterns
        val versionPatterns = listOf(
            "Version $version",
            "v$version",
            "Version ${version.toInt()}",
            "v${version.toInt()}",
            "$version",
            "${version.toInt()}"
        )
        
        val lines = fullChangelog.lines()
        var startIndex = -1
        var endIndex = lines.size
        
        // Find the start of the version section
        for (pattern in versionPatterns) {
            startIndex = lines.indexOfFirst { line ->
                line.trim().startsWith(pattern, ignoreCase = true) ||
                line.trim().startsWith("## $pattern", ignoreCase = true) ||
                line.trim().startsWith("# $pattern", ignoreCase = true) ||
                line.trim().equals(pattern, ignoreCase = true)
            }
            if (startIndex != -1) break
        }
        
        if (startIndex == -1) {
            script.log("[Changelog] Could not find version section for $version")
            return null
        }
        
        // Find the end of this version section (next version header or end of file)
        for (i in (startIndex + 1) until lines.size) {
            val line = lines[i].trim()
            // Check if this line is a new version header
            if (line.isNotEmpty() && (
                line.startsWith("Version ", ignoreCase = true) ||
                line.startsWith("v", ignoreCase = true) && line.matches(Regex("v?\\d+(\\.\\d+)?", RegexOption.IGNORE_CASE)) ||
                line.startsWith("## Version", ignoreCase = true) ||
                line.startsWith("# Version", ignoreCase = true) ||
                (line.startsWith("##") && line.length > 2 && line[2].isDigit())
            )) {
                // Check if it's a different version
                val isDifferentVersion = versionPatterns.none { pattern ->
                    line.contains(pattern, ignoreCase = true)
                }
                if (isDifferentVersion) {
                    endIndex = i
                    break
                }
            }
        }
        
        // Extract the section
        val versionSection = lines.subList(startIndex, endIndex)
            .joinToString("\n")
            .trim()
        
        return if (versionSection.isNotEmpty()) versionSection else null
    }
    
    private fun openGitLabUrl() {
        try {
            val uri = URI("https://gitlab.com/rats_rs/osmb/-/tree/main/herbi?ref_type=heads")
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri)
            } else {
                // Fallback for systems without Desktop support
                val runtime = Runtime.getRuntime()
                val os = System.getProperty("os.name").lowercase()
                when {
                    os.contains("win") -> runtime.exec("rundll32 url.dll,FileProtocolHandler $uri")
                    os.contains("mac") -> runtime.exec("open $uri")
                    os.contains("nix") || os.contains("nux") -> runtime.exec("xdg-open $uri")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun downloadUpdate(updateLabel: Label) {
        updateLabel.text = "⏳ Downloading update..."
        val downloadButton = (versionUpdatePanel.children[1] as HBox).children[1] as Button
        downloadButton.isDisable = true
        
        // Run download in a separate thread to avoid blocking UI
        Thread {
            try {
                val success = versionDownloader.downloadNewJar()
                Platform.runLater {
                    if (success) {
                        updateLabel.text = "✅ Update downloaded successfully!\nPlease restart the script to use the new version."
                        updateLabel.style = """
                            -fx-text-fill: #27ae60;
                            -fx-font-size: 14px;
                            -fx-font-weight: bold;
                            -fx-wrap-text: true;
                        """.trimIndent()
                        downloadButton.isVisible = false
                    } else {
                        updateLabel.text = "❌ Failed to download update.\nPlease try again or download manually from GitLab."
                        updateLabel.style = """
                            -fx-text-fill: #e74c3c;
                            -fx-font-size: 14px;
                            -fx-font-weight: bold;
                            -fx-wrap-text: true;
                        """.trimIndent()
                        downloadButton.isDisable = false
                    }
                }
            } catch (e: Exception) {
                Platform.runLater {
                    updateLabel.text = "❌ Error downloading update: ${e.message}\nPlease try again or download manually from GitLab."
                    updateLabel.style = """
                        -fx-text-fill: #e74c3c;
                        -fx-font-size: 14px;
                        -fx-font-weight: bold;
                        -fx-wrap-text: true;
                    """.trimIndent()
                    downloadButton.isDisable = false
                }
            }
        }.start()
    }
}
