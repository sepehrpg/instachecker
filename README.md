# Instagram Unfollower Checker 📉📱

A lightweight Android app to help users find out **who unfollowed them on Instagram**.  
The app works completely **offline** by analyzing the user's **Instagram data export**.


## 🎥 Final Project Screenshots
![Screen 1](/screenshots/scaninsta1.jpg?raw=true)
![Screen 2](/screenshots/scaninsta2.jpg?raw=true)


## 🚧 Development Status

This project is a work-in-progress. Expect frequent changes and new features coming soon.


## 🔧 Features

- Detect users you follow but who don't follow you back
- Offline processing for better privacy (no login required)
- Clean and modern UI with Jetpack Compose
- Fast local data comparison
- Lightweight and private


## 🚀 How it works

1. Open the **Instagram** app or website.
2. Go to: Menu (Settings) -> Accounts Centre -> Your information and permissions -> Download your information (Download or transfer information)
3. Choose **"Some of your information"**.
4. Select only **"Followers and Following"**.
5. ⚠️ **IMPORTANT:** In the format section, **change the format from HTML to JSON**.
6. Request the download and wait for the ZIP file to be generated.
7. Once downloaded, import the ZIP file into the app.
8. The app will analyze the data and show a list of users you follow who don’t follow you back.

## 🛠 Tech Stack

- **Jetpack Compose** – Modern declarative UI toolkit
- **MVVM architecture** – Modularized structure
- **Room** – Local database storage
- **Hilt** – Dependency Injection
- **Kotlin** – Modern, expressive language for Android


## 🛡️ Privacy

This app works **completely offline**. Your data is never uploaded anywhere.

## 📥 How to Run

Clone the repo and open in Android Studio:

```bash
git clone https://github.com/sepehrpg/instachecker.git