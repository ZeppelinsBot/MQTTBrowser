# Changelog

All notable changes to MQTT Browser will be documented in this file.

## [1.11] - 2026-08-02

### Added
- Bigger logo (40dp) in all 3 toolbars
- Delta time between messages (+12s, +2m, +1h)

## [1.10] - 2026-08-02 - 2026-08-02

### Added
- App logo as launcher icon (all 5 densities) and in toolbars

## [1.9] - 2026-08-02

### Fixed
- Password field masked with dots in saved connections

## [1.8] - 2026-08-02

### Added
- Image rendering in detail view: JPEG, PNG, GIF, BMP, WebP auto-detection from raw MQTT bytes

## [1.7] - 2026-08-02

### Added
- Version number displayed on connection screen

### Changed
- APK releases named as MQTTBrowser-vX.Y.apk

## [1.6] - 2026-08-02

### Fixed
- Natural sort for topic tree: "1_energy" now sorts before "19_energy"

## [1.5] - 2026-08-02

### Fixed
- Expand/Collapse: text was never truncated (Compose maxLines unreliable)
- Pre-truncate to 8 lines before rendering instead of relying on maxLines
- needsExpand threshold raised from 400 to 600 chars (fewer false positives)

## [1.4] - 2026-08-01

### Added
- Expand/collapse for long messages (default 8 lines, "▶ tap to expand")
- JSON Diff mode: changed values highlighted in green bold, unchanged dimmed in grey
- Diff ON/OFF toggle in detail view top bar

### Info
- Designed for Waveshare-style devices that send full JSON every second

## [1.3] - 2026-08-01

### Added
- JSON pretty-print: JSON payloads are now formatted with 2-space indentation
- Non-JSON payloads (plain text, numbers) remain unchanged

### Fixed
- TopAppBar title in Connection Screen: "MQTT Explorer" → "MQTT Browser"

## [1.2] - 2026-08-01

### Changed
- Renamed project from "MQTT Explorer" to "MQTT Browser"
- Application ID: `com.mbusino.mqttbrowser`
- Project folder, APK, and app name now consistently use "MQTT Browser"

### Added
- Auto-reconnect when app returns from background
- Reconnect button in top bar when connection is lost
- Reconnect banner in tree screen when disconnected
- Branch statistics on parent nodes: shows record count and total message count per subtree (e.g. "📂 5 records · 142 msgs")

### Fixed
- Connection no longer drops silently when app is backgrounded

## [1.1] - 2026-08-01

### Fixed
- App crash on "Connect" button — replaced `MqttAndroidClient` (Paho Android Service, broken on Android 10+) with `MqttAsyncClient` (pure Java Paho client)
- Removed `MqttService` from AndroidManifest (no longer needed)

### Changed
- `connect()` no longer requires Android Context parameter
- Dependency: removed `org.eclipse.paho.android.service`, kept only `org.eclipse.paho.client.mqttv3`

## [1.0] - 2026-08-01

### Added
- Initial release
- Topic tree browser with hierarchical view
- Message history per topic with timestamps (HH:mm:ss.SSS)
- Connection screen with broker URL, port, username/password
- Save and load connection profiles (SharedPreferences)
- Subscribe/unsubscribe to topics and wildcards
- Topic search and filter
- Expand All / Collapse All tree controls
- Copy message to clipboard
- Material 3 UI with dark mode support
- Live updates via MQTT subscription (#)
