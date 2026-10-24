# 🎮 Esports Hub - Android Client

<div align="center">

🇺🇸 English | 🇧🇷 [Português](./README.md)

### Your portal for following eSports matches in real time.

[![License](https://img.shields.io/github/license/FilipeLacerda738/EsportsNewsAppAndroid?style=for-the-badge&logo=gnu&color=2B3137&labelColor=161B22)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white&labelColor=161B22)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=161B22)](https://kotlinlang.org/)

<a href="https://github.com/FilipeLacerda738/EsportsNewsAppAndroid/releases/latest">
    <img src="https://img.shields.io/github/v/release/FilipeLacerda738/EsportsNewsAppAndroid?label=Download%20APK&logo=android&style=for-the-badge&color=3DDC84&labelColor=161B22" alt="Download APK">
</a>

<br><br>
<img src="assets/Demo_app.gif" width="200" alt="Esports Hub running on Android" />


</div>

---

> 🚀 **Complete Ecosystem (Fullstack)**
> Match queries are served by a **Custom Python Backend (FastAPI)** responsible for synchronizing PandaScore into PostgreSQL and providing a dedicated catalog to Android.
> 🔗 [Explore the Backend Engineering here](https://github.com/FilipeLacerda738/esports-pro-api.git)

---

## 🎯 The Challenge and the Product

**Esports Hub** brings together schedules, scores, and results for **Counter-Strike 2 and VALORANT** in a native Android app. It helps you follow teams and competitions through a dark interface, search, filters, and match details.

The project uses **Jetpack Compose, MVVM, and Coroutines**, with Portuguese, English, and Spanish interfaces. No account is required to follow matches.

### 🎮 What you can do

* **Schedules and results:** live, upcoming, and finished matches with competition, teams, time, and score.
* **Combined search and filters:** find teams by part of their name and filter by game, status, tier (S, A, B, or C), and day.
* **Calendar in your time zone:** eight days starting yesterday, with times converted to your phone's time zone.
* **Match details:** format, prize pool, team players, and map status when supplied by the source. Rosters do not necessarily represent a match's starting lineup.
* **Broadcasts:** links labeled by language and platform, opened in an external app.
* **Settings:** language selection and checks for newer app versions.

## ⚡ Engineering Highlights and Optimizations

* 📉 **APK Optimization:** release enables `R8/ProGuard` and `shrinkResources`; DTOs use `@Keep` to preserve Gson deserialization. Final size depends on the variant and included resources.
* 🛡️ **Security and Error Handling:** localized messages for network failures and mandatory HTTPS in release. Administrative keys, database credentials, and PandaScore tokens remain on the server.
* 🎨 **"Gunmetal Gray" Design System:** Complete migration to a dark theme focused on dynamic readability and visual ergonomics, inspired by Strafe/HLTV.
* 🔄 **Updates that preserve context:** in-memory caching and separate initial-load, refresh, and next-page states preserve valid data when a new request fails.
* ⏱️ **Screen-specific polling:** 30-second polling follows lifecycle. The list only polls page one under All/Live; details only poll `running` matches, with no active request or pending error.

---

## 🛠 Tech Stack

| UI Layer                               | Architecture & State            | Connectivity              |
| :------------------------------------- | :------------------------------ | :------------------------ |
| **Jetpack Compose** (Material 3)       | **MVVM** (Model-View-ViewModel) | **Retrofit 3** + Gson     |
| **Coil** (Async Image Loading & Cache) | **StateFlow** (Reactive State)  | **OkHttp** (Interceptors) |
| **Custom Components**                  | **Coroutines** (Concurrency)    | **Dynamic URL Injection** |

### 🧩 Code organization and state flow

A single `:app` module with package/applicationId `com.example.esportsnews`, organized by feature. `MatchesViewModel`, `MatchDetailViewModel`, and `AppUpdatesViewModel` are Activity-scoped and use repositories that accept an API through their constructor.

```text
Route → Screen / events → ViewModel → Repository → Retrofit → API
                            ↓
                        StateFlow → UI
```

Paths are relative to `app/src/main/java/com/example/esportsnews/`:

| Directory | Responsibility |
| :--- | :--- |
| `app/` | Activity, app composition, navigation, and drawer |
| `feature/matches/` | List, search, filters, calendar, pagination, and polling |
| `feature/matchdetail/` | Details, players, maps, and broadcasts |
| `feature/settings/` and `feature/updates/` | Language, settings, and app updates |
| `data/remote/`, `data/repository/`, and `data/cache/` | Contracts/DTOs, API access, and in-memory caching |
| `core/` | Dates/time zones, errors, links, theme, and shared components |

Versions in [gradle/libs.versions.toml](gradle/libs.versions.toml) and [app/build.gradle.kts](app/build.gradle.kts): Kotlin/Compose 2.2.10, Retrofit 3.0.0, Coil 2.7.0, AGP 9.2.1, and Gradle 9.4.1. Strings live in `res/values/`, `values-en/`, and `values-es/`.

---

## 🗺️ Roadmap

The application is constantly evolving. Future items are proposals without a committed delivery date:

* **Current:** in-memory cache, pagination, search, filters, details and app updates. Offline persistence with Room is a future step.
* **[Phase 2] Analytical Vision:** expand existing map details, bracket tracking, and momentum graphs.
* **[Phase 3] Monetization & Engagement:** Real-time odds integration, affiliate dashboard for regulated betting platforms, and a Pick'em prediction system.

---

# ⚙️ Installation

### 📱 Install and use

You need Android 8.0 or later and internet access to retrieve updated data. Use the **Download APK** button above or the [releases page](https://github.com/FilipeLacerda738/EsportsNewsAppAndroid/releases/latest), open the APK, and allow installation by your browser/file manager if Android prompts you.

On the home screen, combine filters and tap a match to view details. The drawer opens Settings. Update checks open the release page; users download and install updates themselves. Repository code may be ahead of the published APK, and the GIF may show an earlier version.

## Prerequisites

* Android Studio compatible with AGP 9.2.1 / Gradle 9.4.1
* JDK 21 for the Gradle daemon; Java 11 source compatibility
* Android SDK 35; minimum runtime Android 8.0 (API 26)

---

## 1. Clone the repository

```bash
git clone https://github.com/FilipeLacerda738/EsportsNewsAppAndroid.git
cd EsportsNewsAppAndroid
```

## 2. Configure your API KEY

> ⚠️ **Forking this project? Use your own backend and your own deployment.** Deploy the API on your infrastructure, configure your database and keys, and point the app to your service's HTTPS URL. The maintainers' infrastructure is not provided for use by forks.

In the root `local.properties`, preserve the `sdk.dir` generated by Android Studio and add:

```properties
API_KEY=your_access_key_here
DEBUG_API_BASE_URL=http://127.0.0.1:8000/
```

---

## 3. Run the project

* Open the project in Android Studio
* Wait for Gradle synchronization
* Click **Run 'app'**

To generate a debug APK from PowerShell with the environment configured:

```powershell
.\gradlew.bat :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`. On Linux/macOS, use `./gradlew`. `minSdk` is 26 and `compileSdk`/`targetSdk` are 35. `versionName` is defined in Gradle; `versionCode` is the larger of 59 and the Git commit count (count falls back to 1). Distribution signing must be configured separately by the maintainer.

---

# 🌐 Network Architecture

The URL is selected by build variant. Debug reads `DEBUG_API_BASE_URL`; when absent, it defaults to `http://127.0.0.1:8000/`. Do not leave the value empty: remove the property to use the default. **In your fork, configure the release variant's `API_BASE_URL` in `app/build.gradle.kts` with your own deployment's HTTPS URL**, such as `https://your-api.example.com/`. This is required before distributing your APK.

The URL must use HTTP(S), end in `/`, and contain no credentials, query, or fragment. For a local backend from an emulator, use `http://10.0.2.2:8000/`. On a USB-connected phone, forward the port and keep the loopback address:

```bash
adb reverse tcp:8000 tcp:8000
```

Start the backend separately following its README. Debug permits local HTTP; release blocks cleartext traffic.

Keep the Android Studio `sdk.dir` setting in `local.properties`. Property values must not be quoted. `API_ACCESS_KEY` remains a fallback when `API_KEY` is absent. The key must match the backend's `API_ACCESS_KEY`. Do not commit this file.

All requests use this header:

```http
X-API-Key: YOUR_API_KEY
```

Match caching is in memory only, without Room or offline persistence: up to 40 pages and 20 details, fresh for 30 seconds and retained for up to 10 minutes. Displayed data remains available when revalidation fails. List and detail ViewModels own separate caches and jobs; Activity scope preserves history during navigation. Polling runs every 30 seconds while the destination is active and pauses when it leaves the screen.

Screens receive immutable state and callbacks; routes collect state with lifecycle. Repositories accept an API in their constructor, and match ViewModels accept replaceable clocks for testing.

### 🔌 Consumed contracts

| Method and path | Usage |
| :--- | :--- |
| `GET /api/v1/matches/` | List envelope with `total`, `page`, `size`, `has_more`, and `items` |
| `GET /api/v1/matches/{match_id}/details` | Details with teams, players, and maps |
| `GET /api/v1/system/app-version` | `version`, `download_url`, and `release_notes` |

CS2 is sent as `CSGO`; VALORANT as `VALORANT`. Status values are `running`, `not_started`, and `finished`. Team search uses `team_query` with a 350 ms debounce. Local days become UTC intervals with inclusive `begin_at_gte` and exclusive `begin_at_lt`. With a team or day filter, the app requires **`X-Match-Search-Version: 1`** in the response to confirm filter support.

Pagination uses 15 items per page, deduplicates IDs, and restarts at page one on refresh. Cache limits count entries, not bytes. Generation/session tokens prevent older responses from replacing current filters or details. The read key embedded in the APK is extractable and does not authenticate a user; never configure an administrative key in the app.

### ℹ️ Availability and limitations

* Scores use polling and depend on backend synchronization and PandaScore coverage; updates are not guaranteed to be instantaneous.
* Current synchronization imports tiers S, A, and B. The C filter exists in the UI but may not find data in the synchronized catalog.
* The backend deletes finished matches whose start date is older than ten days; history is incomplete.
* The cache disappears when the process ends. The last successful query does not indicate when the source updated a match.
* Streams open external services; availability and playback depend on those services. Optional fields may be missing.
* The backend checks GitHub for the app version and falls back to `1.0.0` on lookup failure, which can prevent an update prompt.

### 🧪 Local validation

With JDK, SDK, and dependencies available, JVM tests use a fake API without querying Render:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Tests cover dates/time zones, contracts, pagination, cache, errors, links, streams, winners, updates, and ViewModel transitions. They do not replace visual checks, on-device lifecycle checks, or release serialization checks.

### 🛠️ Common problems

| Symptom | Check |
| :--- | :--- |
| Empty list | Clear filters and check connectivity, coverage, and backend synchronization. |
| HTTP 403 in development | Check the read key and rebuild after changing `local.properties`. |
| Local backend unreachable | Check server/port, the URL's trailing `/`, and USB forwarding. |
| Incompatible team/day search | Update the backend to support `X-Match-Search-Version: 1`. |
| Data refresh failed | Previous data may remain visible; retry after connectivity returns. |

---

# 🤝 Contributions

Contributions are very welcome. Include expected behavior, reproduction steps, Android version, and verification performed. Preserve translations, API contracts, and Android 8.0 compatibility; exclude keys, personal configuration, and signing files.

## Contribution Flow

```bash
# Fork the project

# Create a branch
git checkout -b feature/my-feature

# Commit changes
git commit -m "feat: new feature"

# Push changes
git push origin feature/my-feature
```

After that, simply open a Pull Request 🚀

---

# 📄 License

Distributed under the GNU General Public License v3.0 LICENSE.

See [LICENSE](LICENSE) for more information. Source notices permit GPL v3 or later. Sports data, images, brands, and broadcasts have their respective owners; the code license does not grant rights to those materials.

---

<div align="center">

Built using Kotlin + Jetpack Compose

</div>
