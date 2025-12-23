<div align="center">

  <br />
  <img src="https://cdn-icons-png.flaticon.com/512/53/53283.png" alt="SportHub Logo" width="120" height="120">
  
  <h1 align="center">SportHub</h1>

  <p align="center">
    <strong>The Ultimate Fusion of Live Football Analytics & Short-Form Video Entertainment</strong>
    <br />
    <br />
    <a href="#-tech-stack">Tech Stack</a>
    ·
    <a href="#-setup--installation">Setup Guide</a>
    ·
    <a href="#-features-deep-dive">Features</a>
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Kotlin-1.9.20-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Android-Jetpack_Compose-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
    <img src="https://img.shields.io/badge/Media3-ExoPlayer-4285F4?style=for-the-badge&logo=youtube&logoColor=white" alt="ExoPlayer" />
    <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase" />
  </p>
</div>

<br />

---

## 📱 App Interface

<div align="center">
  <table border="0">
    <tr>
      <td align="center" width="33%">
        <b>Live Dashboard</b><br/>
        <img src="https://github.com/user-attachments/assets/eb989ed5-f604-4fa0-af99-352d237367f2" width="100%" alt="Dashboard" style="border-radius: 10px;">
      </td>
      <td align="center" width="33%">
        <b>Match Analytics</b><br/>
        <img src="https://github.com/user-attachments/assets/f2e15667-3d4b-47a8-9b40-161b6707bb79"  width="100%" alt="Analytics" style="border-radius: 10px;">
      </td>
      <td align="center" width="33%">
        <b>Reels Feed</b><br/>
        <img src="https://github.com/user-attachments/assets/a7027c82-72ae-40e5-b681-710b25ba45b6"  width="100%" alt="Reels" style="border-radius: 10px;">
      </td>
    </tr>
    <tr>
      <td align="center" width="33%">
        <b>Tactical Lineups</b><br/>
        <img src="https://github.com/user-attachments/assets/d0378c91-4abd-4f59-9dde-42d94d501883"  width="100%" alt="Lineups" style="border-radius: 10px;">
      </td>
      <td align="center" width="33%">
        <b>Team Squad</b><br/>
        <img src="https://github.com/user-attachments/assets/e9a27b6c-1f57-42f1-8a47-6adb2c0ea878"  width="100%" alt="Squad" style="border-radius: 10px;">
      </td>
      <td align="center" width="33%">
        <b>Search</b><br/>
        <img src="https://github.com/user-attachments/assets/46b1b477-29c0-4d3d-8bb9-70d99f386121"  width="100%" alt="Search" style="border-radius: 10px;">
      </td>
    </tr>
  </table>
</div>

---

## 💡 About The Project

**SportHub** redefines the football companion app experience. In an era where fans constantly switch between score apps and social media, SportHub bridges the gap. It provides a unified platform where deep statistical analysis meets the addictive engagement of short-form video content.

Built with a **"Dark Mode First"** design philosophy, the application offers a cinematic and distraction-free environment for users to track their favorite teams (Premier League, La Liga, UCL, UPL) while consuming highlights, fan reactions, and tactical breakdowns in a seamless vertical feed.

### 🌟 Key Highlights

* **Zero-Latency Data:** Real-time synchronization of scores, match events, and league tables.
* **Immersive Media:** A high-performance video engine powered by **Media3 ExoPlayer**, optimized for instant playback and smooth scrolling (TikTok-style).
* **Resilient Architecture:** Built to handle network instability and API rate limits gracefully with intelligent error handling.
* **Global Timezone Intelligence:** Automatically adapts match schedules to the user's local time (defaulting to Kyiv/EET for precision).

---

## 🛠 Tech Stack

SportHub is engineered using modern Android development standards, focusing on scalability, clean code, and performance.

| Category | Technologies Used |
| :--- | :--- |
| **Language** | **Kotlin** (100% Native) |
| **UI Framework** | **Jetpack Compose** (Material Design 3) |
| **Architecture** | **MVVM** (Model-View-ViewModel) + Clean Architecture |
| **Concurrency** | **Coroutines** & **Flow** |
| **Network** | **Retrofit 2** + **OkHttp 3** (Custom Interceptors) |
| **Media Streaming** | **AndroidX Media3 (ExoPlayer)** |
| **Image Loading** | **Coil** (Async Coroutine Image Loading) |
| **Backend** | **Firebase Firestore** & **Firebase Auth** |
| **Data Provider** | **API-Football v3** (RESTful API) |

---

## 🧩 Features Deep Dive

### 1. The Match Center
The core of SportHub is its robust match tracking system.
* **Smart Calendar Strip:** A custom-built horizontal calendar that supports a wide date range (-14 to +14 days), formatted strictly for API compatibility (`yyyy-MM-dd`) while displaying user-friendly dates.
* **Live Updates:** Matches update in real-time, displaying status (HT, FT, Live Minute) and scores.

### 2. Analytical Detail View
Clicking a match reveals a comprehensive dashboard:
* **Timeline:** A minute-by-minute breakdown of goals, cards, and substitutions.
* **Visual Lineups:** A rendered football pitch showing player formations and starting XI.
* **H2H & Form:** Historical data analysis to provide context before the kick-off.
* **Dynamic Tables:** League standings that intelligently fetch data based on the match season.

### 3. The Reels Ecosystem
A vertical video feed designed for high engagement.
* **Optimized Buffering:** Videos preload intelligently to ensure instant playback.
* **Interactive UI:** Overlays for author details, likes, and descriptions.
* **Playback Control:** Automatic pause/play management when scrolling.

---

## ⚙️ Setup & Installation

To run SportHub locally, follow these steps:

### Prerequisites
* Android Studio Hedgehog (2023.1.1) or newer.
* JDK 17.
* An active account at [API-Football](https://dashboard.api-football.com/).

### Step-by-Step

1.  **Clone the Repo**
    ```bash
    git clone [https://github.com/your-username/SportHub.git](https://github.com/your-username/SportHub.git)
    ```

2.  **Configure API Key**
    The app requires an API key to fetch data.
    * Navigate to: `app/src/main/java/com/example/sporthub/RetrofitInstance.kt`
    * Replace the placeholder with your key:
        ```kotlin
        .addHeader("x-apisports-key", "YOUR_SECRET_KEY_HERE")
        ```

3.  **Firebase Configuration**
    * Go to the [Firebase Console](https://console.firebase.google.com/).
    * Create a new project.
    * Download the `google-services.json` file.
    * Place it in the `/app` root directory.

4.  **Build & Run**
    * Sync Gradle files.
    * Select your emulator or physical device.
    * Run the application.

---

## 🚧 Roadmap

We are constantly improving SportHub. Here is the vision for future updates:

* [ ] **Push Notification Engine:** Server-side implementation for goal alerts via FCM.
* [ ] **User Content Creation:** Tools for users to trim, upload, and tag their own football clips.
* [ ] **Social Graph:** Follow system, comment threads, and algorithmic feed personalization.
* [ ] **Offline Mode:** Room Database caching to view stats without an internet connection.

---

## 🤝 Contributing

Contributions make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.  Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.  Push to the Branch (`git push origin feature/AmazingFeature`)
5.  Open a Pull Request

---

<div align="center">
  <h3>Built amx122 ❤️ for the Beautiful Game</h3>
  <p>SportHub © 2025</p>
</div>
