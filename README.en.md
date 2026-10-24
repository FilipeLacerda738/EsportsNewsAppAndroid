# 🎮 Esports Hub - Android Client

<div align="center">

🇺🇸 English | 🇧🇷 [Português](./README.md)

### Your portal for following eSports matches in real time.

[![License](https://img.shields.io/github/license/FilipeLacerda738/EsportsNewsAppAndroid?style=for-the-badge\&logo=gnu\&color=2B3137\&labelColor=161B22)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge\&logo=android\&logoColor=white\&labelColor=161B22)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?style=for-the-badge\&logo=kotlin\&logoColor=white\&labelColor=161B22)](https://kotlinlang.org/)

<a href="https://github.com/FilipeLacerda738/EsportsNewsAppAndroid/releases/latest">
    <img src="https://img.shields.io/github/v/release/FilipeLacerda738/EsportsNewsAppAndroid?label=Download%20APK&logo=android&style=for-the-badge&color=3DDC84&labelColor=161B22" alt="Download APK">
</a>

<br><br>
<img src="assets/Demo_app.gif" width="200" />


</div>

---

> 🚀 **Complete Ecosystem (Fullstack)**
> This application does not consume third-party APIs directly. It is powered by a **Custom Python Backend (FastAPI)** built to protect the app against request limits and deliver data in milliseconds.
> 🔗 [Explore the Backend Engineering here](https://github.com/FilipeLacerda738/esports-pro-api.git)

---

## 🎯 The Challenge and the Product

**Esports Hub** was created to solve a clear problem: most sports score applications are slow, cluttered, and consume too much mobile data.

This project was built using state-of-the-art native Android development technologies (**Jetpack Compose, MVVM, Coroutines**) to deliver a fluid, lightweight experience fully focused on user retention.

## ⚡ Engineering Highlights and Optimizations

* 📉 **App Size Optimization (From 90MB to 15MB):** Rigorous implementation of `R8/ProGuard` rules and `shrinkResources`. The final APK size was reduced by **more than 80%**, optimizing conversion rates without breaking Retrofit deserialization thanks to `@Keep` annotations.
* 🛡️ **Security and Leak Prevention:** “Blind” error handling inside the `ViewModel` layer. In case of network failures, the app intercepts OkHttp/Retrofit exceptions to ensure server IPs or API URLs are never exposed to the end user.
* 🎨 **"Gunmetal Gray" Design System:** Complete migration to a dark theme focused on dynamic readability and visual ergonomics, inspired by Strafe/HLTV.

---

## 🛠 Tech Stack

| UI Layer                               | Architecture & State            | Connectivity              |
| :------------------------------------- | :------------------------------ | :------------------------ |
| **Jetpack Compose** (Material 3)       | **MVVM** (Model-View-ViewModel) | **Retrofit 2** + Gson     |
| **Coil** (Async Image Loading & Cache) | **StateFlow** (Reactive State)  | **OkHttp** (Interceptors) |
| **Custom Components**                  | **Coroutines** (Concurrency)    | **Dynamic URL Injection** |

---

## 🗺️ Roadmap

The application is constantly evolving. Here are the next engineering and feature milestones:

* **Current:** in-memory cache, pagination, search, filters, details and app updates. Offline persistence with Room is a future step.
* **[Phase 2] Analytical Vision:** Match detail pages with individual map status (BO3), bracket tracking, and momentum graphs.
* **[Phase 3] Monetization & Engagement:** Real-time odds integration, affiliate dashboard for regulated betting platforms, and a Pick'em prediction system.

---

# ⚙️ Installation

## Prerequisites

* Android Studio compatible with AGP 9.2.1 / Gradle 9.4.1
* JDK 21 (validated; Java 11 bytecode)
* Android SDK 35; minimum runtime Android 8.0 (API 26)

---

## 1. Clone the repository

```bash
git clone https://github.com/FilipeLacerda738/EsportsNewsAppAndroid.git
cd EsportsNewsAppAndroid
```

## 2. Configure your API KEY

Create a `local.properties` file in the project root:

```properties
API_KEY=your_access_key_here
DEBUG_API_BASE_URL=
```

---

## 3. Run the project

* Open the project in Android Studio
* Wait for Gradle synchronization
* Click **Run 'app'**

---

# 🌐 Network Architecture

The URL is selected by build variant. The example above points debug to Render. Without `DEBUG_API_BASE_URL`, debug defaults to `http://127.0.0.1:8000/`; use `http://10.0.2.2:8000/` for a local backend from the emulator. Release uses `your deployment`.

Keep the Android Studio `sdk.dir` setting in `local.properties`. Property values must not be quoted. `API_ACCESS_KEY` remains a fallback when `API_KEY` is absent. Do not commit this file.

All requests use this header:

```http
X-API-Key: YOUR_API_KEY
```

Match caching is in memory only, without Room or offline persistence: up to 40 pages and 20 details, fresh for 30 seconds and retained for up to 10 minutes. Displayed data remains available when revalidation fails. List and detail ViewModels own separate caches and jobs; Activity scope preserves history during navigation. Polling runs every 30 seconds while the destination is active and pauses when it leaves the screen.

Screens receive immutable state and callbacks; routes collect state with lifecycle. Repositories accept an API in their constructor, and match ViewModels accept replaceable clocks for testing.

Local validation without Render requests:

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lint
```

---

# 🤝 Contributions

Contributions are very welcome.

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

See `LICENSE` for more information.

---

<div align="center">

Built using Kotlin + Jetpack Compose

</div>
