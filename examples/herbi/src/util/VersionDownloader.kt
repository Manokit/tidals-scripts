package util

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Paths
import kotlin.math.abs

class VersionDownloader(
    private val fileName: String,
    private val downloadUrl: String,
    private val currentVersion: Double,
    private val log: (String) -> Unit,
) {

    /**
     * Checks GitLab for a new jar file based on version number and downloads it if available.
     * @return true if a new jar was downloaded, false otherwise
     */
    fun downloadNewJar(): Boolean {
        return try {
            val latestJar = newVersionAvailable() ?: return false

            val downloadDir = Paths.get(System.getProperty("user.home"), ".osmb", "Scripts").toFile()

            // Remove old jar files from Scripts directory
            val existingJars = downloadDir.listFiles { _, name -> name.contains(fileName, ignoreCase = true) && name.endsWith(".jar", ignoreCase = true) }
            existingJars?.forEach { oldJar ->
                try {
                    if (oldJar.delete()) {
                        log("Removed old jar file: ${oldJar.name}")
                    } else {
                        log("Failed to remove old jar file: ${oldJar.name}")
                    }
                } catch (e: Exception) {
                    log("Error removing old jar file ${oldJar.name}: ${e.message}")
                }
            }

            // Download the jar file
            val downloadUrl = "$downloadUrl/${latestJar.first}"
            val targetFile = File(downloadDir, latestJar.first)

            log("Downloading from: $downloadUrl")
            log("Saving to: ${targetFile.absolutePath}")

            val downloadConnection = URL(downloadUrl).openConnection() as HttpURLConnection
            downloadConnection.requestMethod = "GET"
            downloadConnection.connectTimeout = 30000
            downloadConnection.readTimeout = 30000

            val downloadResponseCode = downloadConnection.responseCode
            if (downloadResponseCode != HttpURLConnection.HTTP_OK) {
                log("Failed to download jar file. Response code: $downloadResponseCode")
                downloadConnection.disconnect()
                return false
            }

            // Download and save file
            FileOutputStream(targetFile).use { outputStream ->
                downloadConnection.inputStream.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            downloadConnection.disconnect()

            log("Successfully downloaded new jar: ${latestJar.first} (version ${latestJar.second})")
            log("Saved to: ${targetFile.absolutePath}")
            true

        } catch (e: Exception) {
            log("Error checking/downloading new jar: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    fun newVersionAvailable(): Pair<String, Double>? {
        val gitlabUrl = "https://gitlab.com/api/v4/projects/rats_rs%2Fosmb/repository/tree?path=herbi&ref=main"
        val downloadDir = Paths.get(System.getProperty("user.home"), ".osmb", "Scripts").toFile()

        // Create directory if it doesn't exist
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
            log("Created directory: ${downloadDir.absolutePath}")
        }

        // Fetch repository tree from GitLab API
        val connection = URL(gitlabUrl).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            log("Failed to fetch GitLab repository tree. Response code: $responseCode")
            return null
        }

        // Read response
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()

        // Parse JSON to find jar files (simple parsing - assumes jar files are in the response)
        // GitLab API returns JSON array with objects containing "name", "path", "type" fields
        val jarFiles = mutableListOf<Pair<String, Double>>()

        // Extract jar files from JSON response
        // Pattern: "name":"filename-vX.XX.jar"
        val jarPattern = """"name":"([^"]*\.jar)"""".toRegex()
        val versionPattern = """v?(\d+\.\d+)(?:\.\d+)?""".toRegex()

        jarPattern.findAll(response).forEach { matchResult ->
            val fileName = matchResult.groupValues[1]
            val versionMatch = versionPattern.find(fileName)
            if (versionMatch != null) {
                val version = versionMatch.groupValues[1].toDouble()
                jarFiles.add(Pair(fileName, version))
                log("Found jar file: $fileName with version: $version")
            }
        }

        if (jarFiles.isEmpty()) {
            log("No jar files found in GitLab repository")
            return null
        }

        // Find the jar with the highest version
        val latestJar = jarFiles.maxByOrNull { it.second }
        if (latestJar == null) {
            log("Could not determine latest jar file")
            return null
        }

        // Compare versions (allow small floating point differences)
        if (latestJar.second <= currentVersion || abs(latestJar.second - currentVersion) < 0.001) {
            log("No new version available. Current: $currentVersion, Latest: ${latestJar.second}")
            return null
        }

        log("New version found! Current: $currentVersion, Latest: ${latestJar.second}")
        return latestJar
    }

}