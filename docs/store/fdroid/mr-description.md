<!--For updates or other types of MRs replace the text below-->

## Abstract for new app inclusion

### Please introduce the functions and usage of your app in summary, and also attach some screenshots.

**MQTT Browser** (`com.mbusino.mqttbrowser`) is a native Android MQTT client built around a **hierarchical topic tree**: every topic on the broker shows up as a branch of a file-manager-like tree with per-node message counters, live updates, message history (including image previews) and payload formatting.

**Usage:** connect to any broker (host/port, optional TLS with custom CA import or explicit trust-all for test brokers, optional username/password, saved connection profiles) → browse the tree → tap a topic for its history and payload → publish with QoS/retain → long-press a node to subscribe to exactly that path (`path/#`) or delete a retained message.

Screenshots (identical to the upstream Fastlane folder):

![Connection screen](https://raw.githubusercontent.com/ZeppelinsBot/MQTTBrowser/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg)
![Topic tree](https://raw.githubusercontent.com/ZeppelinsBot/MQTTBrowser/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg)
![Path subscribe menu](https://raw.githubusercontent.com/ZeppelinsBot/MQTTBrowser/main/fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg)
![Subscribe path dialog](https://raw.githubusercontent.com/ZeppelinsBot/MQTTBrowser/main/fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg)

### Please elaborate why you wrote this app. Please highlight the unique functions it provides over other similar apps in F-Droid.

I wanted a desktop-"MQTT Explorer"-style topic **browser** on the phone for home-automation and IoT debugging. Existing MQTT clients in F-Droid focus on plain publish/subscribe to a fixed set of topics. Unique functions:

- **Hierarchical topic tree** with expand/collapse, natural sorting, branch statistics and a search/filter across the whole tree
- **Per-topic message history** (last 500) with raw payload, formatted view and image rendering (JPEG/PNG/GIF/BMP/WebP)
- **Single-path subscribe mode**: switch from wildcard `#` to `path/#` with one tap, persisted per connection (plus reconnect decision dialog)
- **Retained message management**: inspect retained payloads per node, copy or delete them
- **TLS/SSL with custom CA import** (PEM/DER → app-internal storage) and an explicit trust-all option for test brokers
- **Fully local**: no accounts, no cloud, no analytics — the app talks only to brokers the user configures

## Checklist

### Policy

* [x] The app complies with the [inclusion criteria](https://f-droid.org/docs/Inclusion_Policy). — GPL-3.0-only, no ads, no tracking, no NonFreeNet (brokers are user-configured), end-user app.
* [x] The original app author has been notified (and does not oppose the inclusion). — This MR is submitted by the author himself (GitLab `Zeppelin500` = GitHub `ZeppelinsBot`).
* [x] The upstream app source code repo contains the app metadata in a [Fastlane](https://gitlab.com/snippets/1895688) folder structure … The `en-US` locale must be included. — `fastlane/metadata/android/en-US/`: `title.txt`, `short_description.txt`, `full_description.txt`, `changelogs/25.txt` + `26.txt`, `images/icon.png` (512×512), `images/featureGraphic.png` (1024×500), `images/phoneScreenshots/1–4.jpg`.

### Docs

* [x] Please read the [guide](https://gitlab.com/fdroid/fdroiddata/-/blob/master/CONTRIBUTING.md) first if this is your first contribution.
* [x] Please make sure your metadata follows the best practice in [our templates](https://gitlab.com/fdroid/fdroiddata/tree/master/templates). — Matched against `templates/general.yml` and `templates/build-gradle.yml`; no comments, LF only.
* [x] Please read the [Build Metadata Reference](https://f-droid.org/docs/Build_Metadata_Reference/) and make sure your metadata is valid. — Validated locally against your `schemas/metadata.json` (`check-jsonschema`) **and** with `fdroid lint` — both green after fixing everything your CI jobs surfaced when I replicated them locally: unquoted `: ` in `Summary`, unquoted `versionName`/`CurrentVersion`, `AutoUpdateMode: Version %v` (schema expects plain `Version`), category `Tools` (replaced with `Connectivity`) and a duplicated line in the description.
* [x] Please read the [Quick Start Guide](https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/).

### Merge Request Setup

* [x] The title of this merge request should follow "New app: app name" format. — `New app: MQTT Browser`
* [x] Please make sure your fdroiddata fork is public and your branch is not protected. — Fork `Zeppelin500/fdroiddata` is public; this MR now uses the **dedicated branch `new-app-mqtt-browser`** (the fork's `master` is protected by default, so the submission moved to its own branch per the Git guide).
* [x] Please read [our Git guide](https://gitlab.com/fdroid/wiki/-/wikis/Tips-for-fdroiddata-contributors/Git-Usage) … Don't rebase your branch if there is no conflict. — No conflicts, no rebase needed.
* [x] All related fdroiddata and RFP issues have been referenced in this merge request. — Both trackers were searched for "MQTT Browser": no related issues exist, so the template's `Closes rfp#` line was removed as instructed by the template itself.
* [x] Please only submit one app in one MR. — One app, one metadata file.

### Metadata

* [x] Metadata must be put in `metadata/<applicationId>.yml`. — `metadata/com.mbusino.mqttbrowser.yml` (the only file in this MR).
* [x] Metadata must be a valid YAML file.
* [x] Metadata must use LF as line ending. — 43 lines, no CR characters.
* [x] Don't add summary/description/changelog/images or anything that should be provided in upstream repo … — Only the yml is added; all listing content comes from the upstream Fastlane folder.
* [x] Releases are tagged and auto update is enabled unless there is a special reason. — Tag `v1.24` exists; `AutoUpdateMode: Version %v`, `UpdateCheckMode: Tags`.
* [x] There is an issue tracker and contact info of the author so that we can report bugs and contact the author. — IssueTracker: GitHub issues; `AuthorEmail: mbusinolib@gmail.com` added in this revision.
* [x] An AuthorName must be added. It doesn't need to be the real name. — `AuthorName: ZeppelinsBot`
* [x] External repos are added as git submodules instead of srclibs … — None used.
* [ ] Enable [Reproducible Builds](https://f-droid.org/docs/Reproducible_Builds) … If you can't enable this, please add the reasons here. — **Not enabled yet:** this first release is not yet verified byte-reproducible (Gradle/AGP toolchain). Dependency metadata is already stripped (`dependenciesInfo` off) so artifacts stay comparable. If reproducible builds are required for inclusion I'm happy to work on them in a follow-up; for now please sign with your key as usual.
* [x] Setup abi split if the APK is large and the splitted ones can be much smaller. — APK ≈ 11.4 MB, no split needed.
* [x] Only the latest versions should be kept in the metadata before it's merged. — Single build entry (1.24 / 26).
* [x] Don't add any disabled versions in the metadata. — None.
* [x] The `commit` field should be the full hash. Please don't use tag or branch in commit. — **Fixed in this revision:** `ab68dc3e0d2f93ac8796c5f4002ca5d6e33715c7` (was the tag `v1.24` before).

### Pipeline

* [ ] All pipelines should pass. — Run 1 failed before any job was created (`failure_reason: "The pipeline failed due to the user not being verified."` — fresh account; human verification completed the same day). Run 2 executed the real jobs and failed; I replicated `schema validation` and `fdroid lint` locally and fixed everything they reported: invalid YAML scalar (unquoted `: ` in `Summary`), unquoted `versionName`/`CurrentVersion`, `AutoUpdateMode` format, invalid category and a duplicated description line. Both checks now pass locally — the rerun of this MR pipeline should be green, and if any job still fails I will fix it immediately.
* [x] All warnings and errors in the Reports tab should be fixed or explained.
* [x] F-Droid CI runners are under GitLab's FOSS program … — No payment/phone verification required anymore (account verified).

```text
/label ~"New App"
```
