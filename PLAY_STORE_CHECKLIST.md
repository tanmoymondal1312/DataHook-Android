# Play Store submission — what to enter, and what is left

Derived from the actual code (July 2026), not from guesswork. If the app's data
handling changes, re-check this file **and** `/privacy/`.

---

## 1. URLs to paste into the Play Console

| Console field | Value |
|---|---|
| Privacy policy | `https://datahook.mediaghor.com/privacy/` |
| Account deletion (Data safety → *Data deletion*) | `https://datahook.mediaghor.com/privacy/#data-deletion` |

Both are public and need no login. The deletion section documents **both**
routes Google asks for: in-app (Settings → Delete account) and a web/email
request for users who can no longer sign in.

---

## 2. Data safety form

### Does your app collect or share any of the required user data types? — **Yes**

### Is all of the user data collected by your app encrypted in transit? — **Yes**
All traffic is HTTPS.

### Do you provide a way for users to request that their data is deleted? — **Yes**
In-app (`Settings → Delete account`, which calls `DELETE /api/auth/me/`) and by
email.

### Data types to declare

| Data type | Collected | Shared | Optional? | Purpose |
|---|---|---|---|---|
| **Personal info → Email address** | Yes | No | Required | Account management |
| **Personal info → Name** | Yes | No | Required | Account management |
| **Personal info → User IDs** | Yes | No | Required | Account management |
| **App activity → Other user-generated content** | Yes | No | Required | App functionality — the submissions your endpoints receive |
| **App info & performance → Other** *(device push token)* | Yes | No | Required | App functionality — push delivery |
| **Photos** | Yes | No | **Optional** | App functionality — only the single image a user picks as a notification logo |

Notes for the "shared" column: pushing a notification sends its contents through
Google's FCM infrastructure to reach the device. Google treats a transfer to a
service provider acting on your behalf as *processing*, not *sharing*, so these
are declared **collected, not shared**. The privacy policy describes the FCM
transfer plainly either way.

### Data types to declare as **NOT** collected

Verified absent from the codebase — there is no analytics, ads, tracking or
crash-reporting SDK of any kind:

- Location (approximate or precise)
- Financial info
- Health & fitness
- Messages (SMS/email content)
- Contacts, Calendar
- Search history, Browsing history
- Installed apps
- Device or other IDs *for advertising*
- Crash logs, diagnostics, performance data

### Data collection disclosure caveat

The app stores the **IP address** each submission arrived from
(`Submission.source_ip`), for abuse-spotting. This is *not* the app user's own
IP — it belongs to whoever posted to the developer's endpoint from outside the
app. It is disclosed in the privacy policy under "Submissions". It does not map
onto a Data safety type collected *from the app user*, so it is not declared
there.

---

## 3. Permissions to justify

| Permission | Justification |
|---|---|
| `INTERNET` | Core — talking to the DataHook API |
| `POST_NOTIFICATIONS` | Alerts for new submissions. The app degrades gracefully if refused. |

No sensitive or restricted permissions are requested. The logo picker uses
`ActivityResultContracts.PickVisualMedia`, which is the system photo picker and
requires **no storage permission** — worth stating if a reviewer asks.

---

## 4. Still to do — these need you, not the code

### 4a. Release signing + Google Sign-In (blocking)

Google Sign-In only works for builds signed with a certificate whose SHA-1 is
registered in Firebase. Currently **only the debug certificate is registered**:

```
06:FD:32:F1:82:D0:F8:01:D9:CC:A0:54:2A:84:EB:12:D2:9B:40:D4   (debug)
```

With Play App Signing, Google re-signs your upload, so the certificate that
matters at runtime is **Google's app-signing certificate, not your upload key**.
That SHA-1 only exists after the first upload:

1. Upload the release AAB to the Play Console (internal testing track is fine).
2. Play Console → *Release → Setup → App integrity → App signing key
   certificate* → copy the **SHA-1**.
3. Firebase Console → Project settings → your Android app → **Add fingerprint**
   → paste it.
4. Download the refreshed `google-services.json` into `app/`, rebuild, re-upload.

**Until step 3, "Continue with Google" will fail for every Play-installed
user** while still working on your debug builds — an easy thing to miss.

### 4b. Build the release artifact

`app/build.gradle.kts` signs the release build from `keystore.properties` at the
project root (never committed) when that file exists; without it the bundle is
unsigned. No keystore yet? Use Android Studio → *Build → Generate Signed App
Bundle* to create one and fill in that file. Keep that keystore and its
passwords safe and backed up — losing it means you can't ship updates from the
same listing.

`isMinifyEnabled` is currently `false`. That is fine to ship; if you turn it on
later, test the release build end-to-end first, because kotlinx-serialization
DTOs need keep rules.

### 4c. Version for release

`versionCode = 4`, `versionName = "1.1"`. Bump `versionCode` for every upload.

---

## 5. Not verified on a device

Nothing in this project has been run on a physical device or emulator yet:
Google Sign-In, the notification redesign (subtitle / picture / logo), the
launcher and notification icons, and the delete-account flow have all been
built and unit/live-tested on the API side, but never seen on screen. Worth one
pass on a real device before publishing.
