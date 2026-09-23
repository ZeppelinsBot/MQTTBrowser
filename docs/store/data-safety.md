# Google Play Data Safety — MQTT Browser (DRAFT)

Conservative draft for the Play Console "Data safety" form.
**The maintainer must review and confirm every statement before submission.**

---

## Declarations (draft answers)

**Does your app collect or share any of the required user data types?**

- **No — the app does not collect personal data.** ✅
- **No data is shared with third parties.** ✅
- **Data is NOT sold.** ✅

## Data types questionnaire (expected answers)

| Data type | Collected? | Shared? | Purposes |
|---|---|---|---|
| Personal info (name, email, …) | No | No | — |
| Financial info | No | No | — |
| Health info | No | No | — |
| Messages (emails, SMS, …) | No | No | — |
| Photos / videos / audio | No | No | — |
| Files and documents | No | No | — |
| Calendar | No | No | — |
| Contacts | No | No | — |
| Network activity / **data in transit** | See note below | No | — |
| App activity | No | No | — |
| Device / other IDs | No | No | — |

## Note — "data in transit" (Play may ask)

The app's whole purpose is connecting to **MQTT brokers the user themselves
configures** (host, port, credentials entered by the user). MQTT messages
therefore travel **directly between the user's device and the user's chosen
broker** — they are never routed through, stored on, or visible to the
developer.

Play's data-safety form has an option along the lines of *"Data is
transmitted over an encrypted network"* / *"Data in transit is safe"*.
**Draft, conservative answer:**

> Data is transmitted over an encrypted network **when the user enables
> TLS**. When the user deliberately connects to an unencrypted broker
> (`tcp://`, port 1883), traffic is not encrypted — this is the broker
> operator's/user's choice, not a data collection practice of the app.
> The developer does not collect, access, store, or share any of this data.

**TODO for the maintainer:** confirm whether to declare the optional
"Data in transit" collection row at all. Conservative recommendation:
**declare "No collection"**, because the developer never receives the data;
if Play's questionnaire forces a transit declaration (because MQTT traffic
exists), use the wording above.

---
*Draft — every statement needs the maintainer's confirmation before submission.*
