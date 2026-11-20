# DataHook (Android)

A Jetpack Compose client for the DataHook Django REST backend. Developers sign up,
create typed API endpoints, grab a shareable ingest URL + secret key + code
snippets, and see submissions (with push notifications) as external sites POST data.

- **Language / UI:** Kotlin, Jetpack Compose (Material 3), single Activity
- **Architecture:** MVVM (ViewModel + StateFlow), Kotlin Coroutines, manual DI
- **Networking:** Retrofit + OkHttp + kotlinx.serialization, JWT auth interceptor
  with transparent refresh on 401
- **Storage:** DataStore (Preferences) for access/refresh tokens
- **Nav:** Navigation-Compose
- **Push:** Firebase Cloud Messaging
- **Base URL:** `https://datahook.mediaghor.com/` (in `data/remote/ApiConfig.kt`)

Package: `com.mediaghor.datahook` · minSdk 26 · targetSdk 36 · compileSdk 36 · AGP 8.9.1 · Gradle 8.14.4 · Kotlin 2.4.21.

---

## 1. What you need to do before it fully works: Firebase

The app already compiles and runs with a **placeholder** `app/google-services.json`.
Push notifications only start working once you drop in a real one:

1. Go to the [Firebase console](https://console.firebase.google.com) → **Add project**
   (or pick an existing one).
2. Inside the project, **Add app → Android**.
   - **Android package name:** `com.mediaghor.datahook` *(must match exactly)*
   - Nickname / debug SHA-1 are optional for FCM.
3. Download the generated **`google-services.json`** and replace the placeholder file
   at **`app/google-services.json`**.
4. In the console, open **Project settings → Cloud Messaging** and copy either:
   - the **Server key** (legacy HTTP API), or
   - set up a **service account** for the HTTP v1 API,
   and give whichever your Django backend uses to send pushes. The app registers its
   device token via `POST /api/devices/ {fcm_token}` on login and on token refresh, so
   your backend just needs to send an FCM message to the stored tokens when a
   submission arrives.
5. Recommended FCM **data** payload from the backend (so tapping deep-links correctly):
   ```json
   {
     "endpoint_id": "123",
     "endpoint_name": "Contact form",
     "body": "New submission received"
   }
   ```
   The app builds the notification title as `New submission · {endpoint_name}` and, on
   tap, opens that endpoint's **Submissions** tab. It reads `endpoint_id` /
   `endpoint_name` from the data payload (and falls back to the `notification` block if
   present).

That's it — no other configuration is required. The base URL is already set.

---

## 2. Build & run

Open the project in Android Studio, let it sync, then Run. The Gradle daemon is
pinned to JDK 21 in `gradle.properties` (`org.gradle.java.home`), so the bundled
Studio JBR works as the launcher — Gradle 8.x's script compiler cannot parse
Java 25. Or from the terminal with any JDK 17–24 on `JAVA_HOME`:

```bash
./gradlew :app:assembleDebug      # build the debug APK
./gradlew :app:installDebug       # install on a connected device/emulator
```

Then: register → create an endpoint → add typed attributes → copy a snippet → POST to
the ingest URL from anywhere → watch the submission appear → get a push.

---

## 3. Screens

| Screen | What it does |
|---|---|
| **Auth** | Login/Register toggle, validation, loading + error states. Persists tokens, registers FCM token. |
| **Dashboard** | Endpoint cards (name, submission & attribute counts), pull-to-refresh, empty state, FAB → create, overflow → Settings / Logout. |
| **Create Endpoint** | Name + optional description → creates and opens detail. |
| **Endpoint Detail** | Tabs: **Overview** (ingest URL + API key with copy, rotate-key w/ confirm, notify toggle, delete), **Attributes** (bottom-sheet add/edit with slugified key, type dropdown, required switch), **Snippets** (js_fetch / cURL / HTML form with copy), **Submissions** (search, paginated, long-press to delete, export CSV/JSON + share). |
| **Settings** | Account email, app version, logout. |

---

## 4. API contract assumptions

The app calls the exact endpoints in the spec. A couple of response shapes weren't
fully pinned down, so if your backend differs, these are the only spots to check:

- **`GET /api/endpoints/`** is treated as a **plain JSON array** of endpoints. If your
  backend paginates it (`{count,next,previous,results}`), change
  `DataHookApi.getEndpoints()` to return a paginated wrapper and unwrap `.results` in
  `EndpointRepository`.
- **Submission payload field** is assumed to be `data` (a JSON object of key→value). If
  your serializer names it `payload`/`values`, update `Submission.data`'s `@SerialName`
  in `data/remote/dto/SubmissionDtos.kt`.
- **Submissions list** is DRF-paginated (`count/next/previous/results`, size 20).
- Field/DTO names use `@SerialName` for snake_case (`submission_count`, `api_key`,
  `ingest_url`, `notify_on_submit`, `js_fetch`, `html_form`, `fcm_token`, `created_at`).
  Unknown JSON keys are ignored, so extra fields won't break parsing.

Base URL lives in one place: `app/src/main/java/com/mediaghor/datahook/data/remote/ApiConfig.kt`.
