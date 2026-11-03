# Repository Guidelines

## Project Structure & Module Organization

Crnk is a multi-module Java library implementing JSON:API. Gradle modules are listed in `settings.gradle`; feature modules live at the repository root (for example, `crnk-core`, `crnk-client`, and `crnk-data`). Code follows the standard `src/main/java` and `src/test/java` layout, with package paths under `io.crnk`. Generated code and language-specific tooling live under `crnk-gen`; integration samples are in `crnk-integration-examples`; user documentation is in `crnk-documentation`.

## Build, Test, and Development Commands

Use the included Gradle wrapper from the repository root:

- `./gradlew build` compiles modules, runs their tests, and assembles outputs.
- `./gradlew test` runs the test suites.
- `./gradlew :crnk-core:test` tests one module; replace `crnk-core` with the module being changed.
- `./gradlew clean build` removes prior outputs and performs a full build.

The build targets Java 8 compatibility. Some modules include generated artifacts or integration examples, so prefer focused module tasks during development and the full build before submitting broad changes.

## Coding Style & Naming Conventions

Follow the repository `.editorconfig`: UTF-8, LF line endings, tabs with a four-column width, and trimmed trailing whitespace. Java packages use lowercase names and mirror `io.crnk` paths; classes and interfaces use `UpperCamelCase`, methods and fields use `lowerCamelCase`, and constants use the established style in their surrounding class. Keep changes consistent with nearby code and avoid unrelated formatting edits.

## Testing Guidelines

Tests use JUnit (with the Gradle test task configured for the JUnit Platform) and commonly use `*Test` or `*IntTest` class names. Place tests in the matching module's `src/test/java` tree. Add or update tests for behavior changes, and run the narrowest relevant task first, followed by `./gradlew build` when practical. There is no repository-wide coverage threshold stated in the build.

## Commit & Pull Request Guidelines

Recent commits use short, direct subjects, often imperative or issue-oriented (for example, `Fix build` or `fix for 854`). Keep commit subjects concise and focused. Pull requests should explain the user-visible change and motivation, link related issues where available, identify affected modules, and include test/build results. Add screenshots only when changing visual documentation or UI assets.
