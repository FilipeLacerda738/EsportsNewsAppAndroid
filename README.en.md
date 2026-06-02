# 🎮 Esports Hub - Android Client

<div align="center">

### Your ultimate portal for tracking CS2 and VALORANT matches in real time.

[![License](https://img.shields.io/github/license/FilipeLacerda738/esportsnews?style=for-the-badge\&logo=gnu\&color=2B3137\&labelColor=161B22)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge\&logo=android\&logoColor=white\&labelColor=161B22)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?style=for-the-badge\&logo=kotlin\&logoColor=white\&labelColor=161B22)](https://kotlinlang.org/)

</div>

---

> 🚀 **The backend is also Open Source!**
> This application consumes a dedicated API built with **Python (FastAPI)** and **PostgreSQL**.
>
> 🔗 [Esports Hub API Repository](https://github.com/FilipeLacerda738/esports-pro-api.git)

---

# 📚 Table of Contents

* [📱 Overview](#-overview)
* [🛠 Tech Stack](#-tech-stack)
* [✨ Features](#-features)
* [⚙️ Installation](#️-installation)
* [🌐 Network Architecture](#-network-architecture)
* [🤝 Contributing](#-contributing)
* [📄 License](#-license)

---

# 📱 Overview

**Esports Hub** is a native Android application designed to deliver:

* Live match scores
* Match schedules
* Real-time status updates
* Game filters
* Automatic synchronization

Built with the modern Android ecosystem using:

* Jetpack Compose
* Coroutines
* StateFlow
* MVVM Architecture
* Material Design 3

The project serves both as a functional application and as a modern Android study reference.

---

# 🛠 Tech Stack

| UI                | Architecture & Data | Networking |
| ----------------- | ------------------- | ---------- |
| Jetpack Compose   | MVVM                | Retrofit   |
| Material Design 3 | StateFlow           | OkHttp     |
| Coil              | Coroutines          | Gson       |

---

# ✨ Features

## 🔴 Real-Time Match Updates

Smart polling system for automatic live score synchronization.

## 📅 Dynamic Match Calendar

Quick filtering by weekdays directly from the UI.

## 🎮 Game Filters

Instant switching between:

* Counter-Strike 2
* VALORANT

## 🎨 Modern UI

Fully built with Jetpack Compose and Material Design 3.

## 🔄 Pull To Refresh

Native Android gesture support for manual updates.

---

# ⚙️ Installation

## Requirements

* Android Studio Iguana+
* JDK 17
* Android SDK 26+

---

## 1. Clone the repository

```bash id="xg2b1o"
git clone https://github.com/FilipeLacerda/EsportsNewsAppAndroid.git
cd EsportsNewsAppAndroid
```

---

## 2. Configure your API KEY

Create a `local.properties` file in the project root:

```properties id="xpf0ea"
API_KEY="your_api_key_here"
```

---

## 3. Run the project

* Open the project in Android Studio
* Wait for Gradle synchronization
* Click **Run 'app'**

---

# 🌐 Network Architecture

The application automatically switches between local and production environments using `BuildConfig.DEBUG`.

| Environment | Base URL                                |
| ----------- | --------------------------------------- |
| Debug       | `http://10.0.2.2:8000/`                 |
| Release     | `https://esports-pro-api.onrender.com/` |

---

All requests are authenticated using the following header:

```http id="e2wh22"
X-API-Key: YOUR_API_KEY
```

---

# 🤝 Contributing

Contributions are highly appreciated.

## Contribution workflow

```bash id="mbljxq"
# Fork the project

# Create a branch
git checkout -b feature/my-feature

# Commit changes
git commit -m "feat: add new feature"

# Push branch
git push origin feature/my-feature
```

Then simply open a Pull Request 🚀

---

# 🗺 Roadmap

* [ ] Match details screen
* [ ] WebSocket integration
* [ ] Offline cache with Room
* [ ] Push notifications
* [ ] Favorites system
* [ ] AMOLED theme

---

# 📄 License

Distributed under the MIT License.

See `LICENSE` for more information.

---

<div align="center">

Made with ❤️ using Kotlin + Jetpack Compose

</div>
