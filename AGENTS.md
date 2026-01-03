# OSMB Script Build Guide for AI Agents

> Complete guide for building, updating, and managing OSMB script JARs using Gradle

## Quick Start

To build any script JAR (e.g., dSunbleakWCer):

```bash
cd /Users/zaffre/Documents/Engineering/Projects/davys-scripts/dSunbleakWCer
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

The JAR will be output to: `dSunbleakWCer/jar/dSunbleakWCer.jar`

---

## Prerequisites

### Required Software
- **Gradle**: Installed via Homebrew (`/opt/homebrew/bin/gradle`)
- **Java 17+**: API.jar requires Java 17 (class version 61.0)
  - Available JDK: `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`
  - Check all installed: `/usr/libexec/java_home -V`

### Project Structure
```
davys-scripts/
├── API/
│   └── API.jar              # OSMB API dependency (Java 17)
├── dScriptName/
│   ├── build.gradle         # Build configuration
│   ├── settings.gradle      # Project name
│   ├── jar/
│   │   └── dScriptName.jar  # Output JAR
│   └── src/
│       └── main/
│           ├── java/        # Source code
│           └── resources/   # logo.png, etc.
└── settings.gradle          # Root multi-project config
```

---

## Build Configuration File

### Create `build.gradle` in Script Directory

Every script needs a `build.gradle` file in its root directory (e.g., `dSunbleakWCer/build.gradle`):

```groovy
plugins {
    id 'java'
}

group = 'com.osmb.scripts'
version = '1.3'  // Update this when incrementing version

repositories {
    mavenCentral()
}

dependencies {
    // osmb api jar dependency
    compileOnly files('../API/API.jar')
}

java {
    // api.jar requires java 17 (class version 61.0)
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

jar {
    archiveFileName = "${project.name}.jar"
    destinationDirectory = file("${projectDir}/jar")
    
    // prevent duplicate file errors when bundling resources
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    
    // include resources like logo.png
    from sourceSets.main.resources
}

// clean task to remove old jars
clean {
    delete "${projectDir}/jar"
}
```

### Key Configuration Fields

| Field | Purpose | Example |
|-------|---------|---------|
| `version` | JAR build version (metadata) | `'1.3'` |
| `group` | Maven-style group ID | `'com.osmb.scripts'` |
| `archiveFileName` | Output JAR filename | `"${project.name}.jar"` |
| `destinationDirectory` | Where to put the JAR | `file("${projectDir}/jar")` |
| `duplicatesStrategy` | Handle duplicate files | `DuplicatesStrategy.EXCLUDE` |

---

## Building Scripts

### Standard Build Commands

```bash
# navigate to script directory
cd /Users/zaffre/Documents/Engineering/Projects/davys-scripts/dScriptName

# set java 17 and build
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build

# alternative: just build (no clean)
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle build

# clean only (removes jar/ directory)
gradle clean
```

### One-Liner from Root

```bash
cd /Users/zaffre/Documents/Engineering/Projects/davys-scripts/dScriptName && JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

### Set JAVA_HOME Permanently (Optional)

Add to `~/.zshrc`:
```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
```

Then just run:
```bash
gradle clean build
```

---

## Updating Script Versions

When updating a script version (e.g., 1.2 → 1.3):

### 1. Update `build.gradle`

```groovy
version = '1.3'  // <-- update this
```

### 2. Update `@ScriptDefinition` in Java Source

Find the main script file (e.g., `dSunbleakWCer.java`):

```java
@ScriptDefinition(
        name = "dSunbleakWCer",
        description = "Chops and optionally banks Ironwood logs on Sunbleak island",
        skillCategory = SkillCategory.WOODCUTTING,
        version = 1.3,  // <-- update this
        author = "JustDavyy"
)
public class dSunbleakWCer extends Script {
    // ...
}
```

### 3. Rebuild

```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

---

## Common Issues & Solutions

### Issue: "permission denied: ./gradlew"

**Cause**: Gradle wrapper isn't executable or is missing files

**Solution**: Use system Gradle instead:
```bash
gradle clean build
```

Or fix permissions:
```bash
chmod +x gradlew
```

### Issue: "class file has wrong version 61.0, should be 55.0"

**Cause**: Trying to build with Java 11 instead of Java 17

**Solution**: Set JAVA_HOME to Java 17:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

### Issue: "package obf does not exist"

**Cause**: Code references `obf.Secrets` class (for stats tracking) which isn't available

**Solution**: Comment out the `sendStats()` method or stats-related code:

```java
private void sendStats(long gpEarned, long xpGained, long runtimeMs) {
    // stats tracking disabled - missing obf.Secrets dependency
    // to enable: add obf.Secrets class with STATS_URL and STATS_API fields
    /*
    try {
        // ... stats code here ...
    } catch (Exception e) {
        log("STATS", "❌ Error sending stats: " + e.getMessage());
    }
    */
}
```

### Issue: "Entry logo.png is a duplicate but no duplicate handling strategy has been set"

**Cause**: Resource files being included multiple times

**Solution**: Add to `build.gradle` jar task:
```groovy
jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // ...
}
```

### Issue: "Could not find or load main class org.gradle.wrapper.GradleWrapperMain"

**Cause**: Gradle wrapper files are corrupted or missing

**Solution**: Use system Gradle instead of `./gradlew`:
```bash
gradle clean build
```

---

## Creating New Scripts from Boilerplate

### 1. Create Directory Structure

```bash
cd /Users/zaffre/Documents/Engineering/Projects/davys-scripts
mkdir -p dNewScript/src/main/{java,resources}
mkdir -p dNewScript/jar
```

### 2. Create `settings.gradle`

```groovy
/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/8.1.1/userguide/multi_project_builds.html
 */

rootProject.name = 'dNewScript'
```

### 3. Create `build.gradle`

Copy the template from section "Build Configuration File" above, updating:
- `version = '1.0'` for new script
- Everything else can stay the same

### 4. Add Java Source Files

Create your script classes in `src/main/java/`

### 5. Add Resources

Add `logo.png` and other resources to `src/main/resources/`

### 6. Build

```bash
cd dNewScript
JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
```

---

## Build Process Breakdown

### What Happens During Build

1. **`:clean`** - Deletes the `jar/` directory
2. **`:compileJava`** - Compiles `.java` files to `.class` files
   - Uses API.jar as compile-only dependency
   - Targets Java 17 bytecode
3. **`:processResources`** - Copies resources (logo.png, etc.)
4. **`:classes`** - Combines compiled classes and resources
5. **`:jar`** - Packages everything into a JAR file
   - Output: `jar/dScriptName.jar`
6. **`:assemble`** - Finalizes all outputs
7. **`:build`** - Runs all tasks including tests (if present)

### Output Files

After successful build:
```
dScriptName/
├── build/
│   ├── classes/java/main/     # Compiled .class files
│   ├── resources/main/         # Processed resources
│   └── libs/                   # (not used, we use jar/)
└── jar/
    └── dScriptName.jar         # ✅ Final output JAR
```

---

## Batch Building Multiple Scripts

### Build All Scripts Script

Create `build-all.sh` in project root:

```bash
#!/bin/bash

# set java 17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home

# root directory
ROOT="/Users/zaffre/Documents/Engineering/Projects/davys-scripts"
cd "$ROOT"

# find all directories with build.gradle
for dir in */; do
    # skip API and other non-script directories
    if [[ "$dir" == "API/" || "$dir" == "Documentation/" ]]; then
        continue
    fi
    
    # check if build.gradle exists
    if [[ -f "${dir}build.gradle" ]]; then
        echo "📦 Building ${dir%/}..."
        cd "$dir"
        gradle clean build
        if [[ $? -eq 0 ]]; then
            echo "✅ ${dir%/} built successfully"
        else
            echo "❌ ${dir%/} failed to build"
        fi
        cd "$ROOT"
        echo ""
    fi
done

echo "🎉 All builds complete!"
```

Make it executable:
```bash
chmod +x build-all.sh
```

Run it:
```bash
./build-all.sh
```

---

## Verification

### Check JAR Was Created

```bash
ls -lh jar/
# should show: dScriptName.jar with size ~40-50KB

# check contents
unzip -l jar/dScriptName.jar | head -20
```

### Verify Manifest

```bash
unzip -p jar/dScriptName.jar META-INF/MANIFEST.MF
```

### Test JAR in OSMB

1. Copy JAR to OSMB scripts directory
2. Launch OSMB client
3. Script should appear in script selector
4. Check version matches your updated version

---

## AI Agent Workflow

When asked to build or update a script JAR:

1. **Check if `build.gradle` exists** in script directory
   - If not, create it using the template
2. **Update version** if requested
   - In `build.gradle`: `version = 'X.Y'`
   - In Java source: `@ScriptDefinition(version = X.Y, ...)`
3. **Check for `obf.Secrets` references**
   - If present and causing errors, comment out stats code
4. **Run build command**:
   ```bash
   cd /path/to/script && JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build
   ```
5. **Verify output**:
   ```bash
   ls -lh jar/
   ```
6. **Report success** with JAR location and size

---

## Reference: Java Versions

```bash
# check current java version
java -version

# list all installed java versions
/usr/libexec/java_home -V

# available java 17 installations on this system
/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home

# api.jar requires java 17 (class version 61.0)
```

---

## Troubleshooting Checklist

- [ ] Java 17+ is installed and JAVA_HOME is set correctly
- [ ] `build.gradle` exists in script directory
- [ ] `settings.gradle` has correct `rootProject.name`
- [ ] `../API/API.jar` path is correct (one level up from script dir)
- [ ] Source files are in `src/main/java/`
- [ ] Resources are in `src/main/resources/`
- [ ] No references to `obf.Secrets` (or they're commented out)
- [ ] `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` is set in jar task
- [ ] Gradle daemon isn't stuck (try `gradle --stop` then rebuild)

---

## Additional Notes

### Why compileOnly for API.jar?

The API.jar is already present in the OSMB runtime environment. Using `compileOnly` means:
- We can compile against it
- It won't be bundled into our JAR (keeps size small)
- OSMB loads it at runtime

### Why Java 17?

The API.jar is compiled with Java 17 (class version 61.0). Our scripts must be compiled with Java 17+ to be compatible with the API.

### Gradle vs ./gradlew

- `gradle` - System-wide Gradle installation
- `./gradlew` - Project-specific Gradle wrapper (often broken in this repo)
- **Use system `gradle`** for reliable builds

---

## Quick Command Reference

```bash
# build single script
cd dScriptName && JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home gradle clean build

# clean only
gradle clean

# build without clean
gradle build

# stop gradle daemon (if stuck)
gradle --stop

# check java version
java -version

# list all java installations
/usr/libexec/java_home -V

# check if jar exists
ls -lh jar/*.jar

# inspect jar contents
unzip -l jar/dScriptName.jar

# check jar manifest
unzip -p jar/dScriptName.jar META-INF/MANIFEST.MF
```

---

**Last Updated**: January 2, 2026  
**OSMB API Version**: Requires Java 17+ (class version 61.0)  
**Gradle Version**: 8.13
