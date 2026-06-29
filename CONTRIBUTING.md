# Contributing to Globber

Thanks for your interest in improving Globber! This document explains how to
build the project, the conventions we follow, and how to submit changes.

## Code of Conduct

This project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By
participating, you are expected to uphold it.

## Prerequisites

- **JDK 17**
- **Android SDK** with platform 35 (compileSdk/targetSdk = 35, minSdk = 29)
- Android Studio (latest stable) recommended, or the Gradle CLI

## Building

```bash
git clone https://github.com/salahu01/Globber.git
cd Globber

# Debug build (installable, signed with the debug key)
./gradlew assembleDebug

# Release build (UNSIGNED — see "Signing" below)
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run lint
./gradlew lint
```

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Signing a release build

The repository ships **no signing config** — release APKs are unsigned so that
no private keystore lives in the repo. To produce an installable release build,
sign it with your own keystore:

```bash
apksigner sign \
  --ks my-release.keystore \
  --out app-release.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

Or generate a keystore first:

```bash
keytool -genkey -v -keystore my-release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 -alias globber
```

Never commit a keystore, `keystore.properties`, or any signing credentials.

## Project structure

```
app/src/main/java/com/salah/callblocker/
├── data/        Room entities, DAOs, repositories, settings store
├── domain/      RuleMatcher — the pattern-matching core
├── service/     CallScreeningService, contacts check, notifications
└── ui/          Compose screens (dashboard, rules, log, settings) + theme
```

## Coding conventions

- **Language:** Kotlin, Jetpack Compose for UI.
- **Style:** follow the existing code; keep `./gradlew lint` clean.
- **Formatting:** respect [`.editorconfig`](.editorconfig).
- Keep functions small and readable; match the surrounding code's idiom.
- Add/adjust unit tests for logic changes (especially `RuleMatcher`).

## Commit messages

- Use the imperative mood: "Add X", "Fix Y", not "Added"/"Fixed".
- Keep the subject ≤ 50 chars; add a body explaining *why* when non-obvious.
- Reference issues where relevant: `Fixes #123`.

## Pull requests

1. Fork and create a topic branch from `master`.
2. Make your change; ensure `./gradlew assembleDebug test lint` passes.
3. Fill out the PR template.
4. Keep PRs focused — one logical change per PR.

## Reporting bugs / requesting features

Use the [issue templates](.github/ISSUE_TEMPLATE/). For security issues, do
**not** open a public issue — see [SECURITY.md](SECURITY.md).
