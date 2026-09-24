# Privacy Policy — MQTT Browser

**Effective date:** 24.09.2026

## Overview

MQTT Browser ("the app") is a standalone Android MQTT client. This policy
explains what the app does — and, more importantly, what it does **not** do.

## No account, no sign-up

The app does not require an account, registration, or sign-in. There is no
server operated by the developer that you connect an account to.

## No data collection by the developer

The app does **not** collect, transmit, store, or share any personal data
with the developer. Specifically:

- **No analytics.** The app contains no analytics SDKs.
- **No advertising.** The app contains no ad SDKs.
- **No tracking.** The app does not build profiles of you or your activity.
- **No developer-operated servers.** The app never contacts any server
  owned or controlled by the developer.

## Connections go only to brokers you configure

The app connects **directly and only** to MQTT brokers that **you** enter
and configure (host, port, credentials). MQTT messages — including topic
names, payloads, and credentials you enter — travel between your device and
the broker you chose:

- The developer never receives, sees, stores, or has access to this data.
- If you connect to an unencrypted broker (`tcp://`, typically port 1883),
  that traffic is not encrypted — this is determined by your broker
  configuration, not by the app. For encrypted connections, enable TLS
  (`ssl://`).

## Local storage

The app stores its settings — including broker addresses, connection
profiles, credentials, and certificates you import — **locally on your
device** (Android app-private storage / SharedPreferences). This data is
removed when you clear the app's data or uninstall the app. It is never
transmitted anywhere except to the broker you configured, when you connect.

## Permissions

- `INTERNET` / `ACCESS_NETWORK_STATE`: required to connect to MQTT brokers
  and detect network status.
- `WAKE_LOCK`: keeps the device awake during active connections so
  messages are not dropped while the screen is off.

## Children's privacy

The app is a general-purpose developer/technical tool and does not target
children.

## Changes to this policy

Any changes will be published in this file in the repository. The
effective date above will be updated when changes are made.

## Contact

Questions about this privacy policy: mbusinolib@gmail.com

Also available via the project's GitHub repository:
<https://github.com/ZeppelinsBot/MQTTBrowser>

---
*Play submission note: this policy must be reachable as a public URL.
Plan: publish via GitHub Pages from the repository root once the
release branch is merged (do NOT enable Pages until publishing).*
