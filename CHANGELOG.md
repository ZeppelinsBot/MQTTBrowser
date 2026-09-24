# Changelog

All notable changes to MQTT Browser will be documented in this file.

## [1.21] - 2026-09-23

### Added
- Launcher icon fixed: adaptive icon now shows the actual full logo (old never-shown August concept vector replaced); versionCode 23
- Path-based subscribe mode: subscribe-FAB toggles between `+` (wildcard `#`) and `#` (single path `<path>/#`)
- Subscribe dialog with wildcard-deactivation warning when entering a path manually via FAB
- Long-press on any tree node (leaf and parent): menu with "Subscribe to this path only" and "Delete retained" (nodes without retained message open the subscribe confirmation directly)
- Reconnect popup in path mode: choose wildcard (Yes) or keep the saved path filter (No) — nothing is subscribed before the decision

### Changed
- Entering path mode unsubscribes `#`, clears the topic tree (it rebuilds from the new filter incl. retained messages) and subscribes `<path>/#`
- Switching back to wildcard unsubscribes the path filter, subscribes `#` and keeps the tree
- Subscribe mode and path filter persisted with the connection settings (lastSettings)
- Remaining German UI strings translated to English (full English i18n pass)

## [1.20] - 2026-09-23

### Fixed
- Message-count counter badge no longer overlaps the `>` navigation arrow on topic list leaf rows (badge now rendered inline)

## [1.19] - 2026-09-22

### Fixed
- Reconnect button now works — disconnect() was clearing lastSettings before reconnect() could use them

## [1.18] - 2026-09-21

### Added
- Delete retained messages via long-press on topic tree item

## [1.17] - 2026-09-21

### Added
- TLS/SSL encryption toggle for MQTT connections (ssl:// protocol)
- "Trust all certificates" option for self-signed certs (Homelab setups)
- CA certificate import via Android file picker (PEM/DER)
- Retained badge (📌) shown in topic tree and detail view
- Port auto-switches 1883 ↔ 8883 when TLS toggle changes

### Changed
- Connection auto-saved on successful connect (save button removed)
- CA certificate copied to app-internal storage (persists across restarts)

### Fixed
- Back button in tree view now disconnects and returns to connection screen
- APK signing (debug keystore)

## [1.16] - 2026-08-07

### Fixed
- Migration flag: old SharedPreferences check skipped after first successful migration (performance)

## [1.15] - 2026-08-07

### Security
- Connection credentials now stored with EncryptedSharedPreferences (AES256, Android Keystore)
- Automatic migration from old unencrypted SharedPreferences on first launch
- Old unencrypted data deleted after migration

## [1.14] - 2026-08-07

### Added
- Publish icon in DetailScreen TopAppBar — pre-fills topic path, editable
- Removed per-tree-node publish icons (too cluttered)

## [1.13] - 2026-08-07

### Added
- Publish icon (Send button) on every topic in the tree — pre-fills topic path, editable

## [1.12] - 2026-08-07

### Added
- MQTT Publish: send messages to any topic with QoS (0/1/2) and retain flag
- Publish dialog via new Send-FAB (second FAB button)

### Fixed
- TopAppBar action icons now clearly visible in dark mode (explicit onPrimaryContainer tint)
- Search icon tint improved for better contrast
- Deprecated ArrowBack/Send icons replaced with AutoMirrored variants

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
