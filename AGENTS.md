# Repository Guidelines

## Project Structure & Module Organization
- This is a Gradle multi-project repo; each script lives in a top-level folder (for example, `TidalsGemMiner/`, `TidalsCannonballThiever/`) with `src/main/java/` and `src/main/resources/`.
- Shared libraries live in `utilities/`; compiled jars are written to each module's `jar/` directory.
- The OSMB API jar is checked in at `API/API.jar` and referenced as a local dependency.
- Reference material is in `docs/`; sample scripts and patterns are in `examples/`.

## Build, Test, and Development Commands
- `osmb build TidalsGemMiner` builds and deploys a single script to the test machine (replace with the script folder name).
- `osmb build utilities` builds and deploys `utilities` when shared code changes.
- `osmb build <scriptName>` is the default for local iteration; it replaces manual `gradlew` jar builds.
- Java 17 is required; ensure `API/API.jar` exists before compiling. After code changes, rebuild the affected script.

## Coding Style & Naming Conventions
- Use 4-space indentation and standard Java naming: `PascalCase` classes, `camelCase` methods, `UPPER_SNAKE` constants.
- Match the script name to its main class and folder (for example, `TidalsGemMiner/src/main/java/main/TidalsGemMiner.java`).
- Common packages are `main/`, `tasks/`, and `utils/`; keep JavaFX setup in `ScriptUI.java` when used.
- Place assets (for example, `logo.png`) under `src/main/resources/`.

## Testing Guidelines
- There is no automated `src/test` suite; testing is manual in the OSMB client.
- Use purpose-built test scripts like `TidalsLoadoutTester` or `TidalsBankTester` when touching utilities or loadout flows.
- Cross-check behavior against relevant guides in `docs/` (timing, walkers, banking patterns).

## Commit & Pull Request Guidelines
- Commit messages are short, descriptive sentences; no Conventional Commit prefixes observed.
- Do not add `Co-Authored-By` trailers (see `CLAUDE.md`).
- PRs should list affected scripts/modules, jar builds performed, and manual test notes; add screenshots or logs when paint/UI changes.

## Security & Configuration Tips
- Put secrets in an `obf/` package (gitignored) and never commit API keys.
- Update `API/API.jar` deliberately when upgrading the OSMB client API.

## Reference Docs
- Read `CLAUDE.md` and the most relevant files in `docs/` before adding new scripts; avoid assuming API methods exist.
