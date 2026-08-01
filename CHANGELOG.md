# Changelog

All notable changes to MQTT Browser will be documented in this file.

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
