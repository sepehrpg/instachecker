# InstaChecker

InstaChecker is an offline Android app that compares the follower and following lists in an
Instagram data export. It shows accounts that you follow but that do not follow you back at the
time of the export.

> The app does not monitor Instagram and cannot determine when somebody unfollowed you. It only
> compares the two lists contained in the ZIP file that you select.

<p align="center">
  <img src="/screenshots/scaninsta1.jpg?raw=true" alt="InstaChecker import screen" width="300" />
  <img src="/screenshots/scaninsta2.jpg?raw=true" alt="InstaChecker results screen" width="300" />
</p>

## Features

- Finds accounts that do not follow you back
- Opens and scans the Instagram ZIP export directly
- Finds relationship files at any folder depth and ignores unrelated export files
- Combines split files such as `followers_1.json`, `followers_2.json`, and later parts
- Supports Instagram's documented JSON and HTML export formats
- Includes best-effort XML compatibility for third-party or converted exports
- Lets you override follower and following file names in persistent Import settings
- Keeps analysis history locally with Room
- Processes all data on-device; no Instagram login or network connection is required

## Getting an Instagram export

Meta's current help flow starts in **Accounts Center → Your information and permissions → Export
your information**. Create an export for the relevant Instagram profile, choose **Followers and
following**, export to your device, and select either **JSON** or **HTML**. See Meta's official
[Instagram export instructions](https://www.facebook.com/help/instagram/181231772500920).

When the export is ready:

1. Download the ZIP file without reorganizing its contents.
2. Open InstaChecker and tap **Upload Instagram ZIP**.
3. Give the analysis a recognizable name.
4. Select the downloaded ZIP file.

You do not need to extract the archive yourself. XML is not listed by Meta as an Instagram export
option; support is included only as a compatibility fallback.

## File discovery

Instagram export folders and prefixes can vary. A commonly observed relationship layout is:

```text
<export root>/
└── connections/
    └── followers_and_following/
        ├── followers_1.json (or .html)
        ├── followers_2.json (optional)
        └── following.json (or .html)
```

InstaChecker does not depend on that folder hierarchy. It scans every ZIP entry, considers only
`.json`, `.html`, `.htm`, and `.xml` relationship candidates, and ignores other downloaded data.
Meta does not publish a stable contract for the internal ZIP paths, so this behavior is intentional.

If the basenames change to something unknown, open **Import settings** and enter the new follower
and following names. Names may include or omit the extension; for example, `f1` and `f2.json` are
both valid.

For the matching rules and supported data shapes, see
[Instagram export compatibility](docs/INSTAGRAM_EXPORTS.md).

## Architecture

The app uses Jetpack Compose, MVVM, Hilt, Room, Kotlin coroutines, and Kotlin serialization.

```text
UI (Compose)
  → MainActivityViewModel
    → InstagramExportImporter (ZIP discovery and parsing)
    → ExportFileSettingsStore (persistent name overrides)
    → UserRepository (comparison and domain-to-database mapping)
      → Room UserDao (atomic analysis storage)
```

ZIP parsing is isolated from Android UI code and covered by local JVM tests. Database writes for a
new analysis run in one Room transaction so a failed import cannot leave an empty history item.

## Build and test

Requirements:

- Android Studio with JDK 17
- Android SDK 35

Clone the repository and check out its default branch:

```bash
git clone https://github.com/sepehrpg/instachecker.git
cd instachecker
./gradlew test
./gradlew assembleDebug
```

Open the project in Android Studio to run it on an Android 5.0 (API 21) or newer device/emulator.

## Privacy and safety

- The app requests no Instagram credentials.
- Export contents are not uploaded and are not extracted into shared storage.
- ZIP entries are read as streams and bounded by entry count and uncompressed-size limits.
- Android cloud backup is disabled for the app.
- Only parsed usernames, profile links, and local analysis labels are stored in the Room database.

Instagram is a trademark of Meta Platforms, Inc. This project is independent and is not affiliated
with or endorsed by Meta or Instagram.
