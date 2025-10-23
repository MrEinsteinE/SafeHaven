
# 📱 SafeHaven - Women Safety Android App

**SafeHaven** is a real-time safety application developed to help women quickly alert trusted contacts during emergencies. With features like shake detection for SOS, fake call simulation, and access to safety laws and self-defense tips, this app empowers users to seek help discreetly and effectively.

---

## 🔐 Key Features

- 🚨 **SOS Alert via Shake:** Sends an SMS with live location to selected emergency contacts when the user shakes the device.
- 📍 **Live Location Sharing:** Automatically detects and sends the user's GPS location in emergencies.
- 📞 **Fake Call Simulation:** Displays a realistic incoming call screen to help escape unsafe situations.
- 📋 **Emergency Contact Selection:** Allows users to choose up to 5 trusted contacts from their phonebook.
- ⚖️ **Basic Laws and Rights:** Educates users about important legal rights related to women’s safety.
- 🥋 **Self Defense Tips:** Offers practical self-defense techniques to stay safe.
- 📡 **Foreground Service Notification:** Keeps a persistent notification to indicate that SOS detection is active.

---

## 🏗️ Application Architecture

```
+-------------------------+
|     MainActivity        | <---- Foreground Dashboard
+-------------------------+
|                         |
|  -> Select Contacts     | --> ContactSelectionActivity
|  -> Basic Laws          | --> LawsActivity
|  -> Self Defense Tips   | --> DefenseActivity
|  -> Start/Stop SOS      | --> ServiceMine (Background Service)
|                         |
+-------------------------+

+-------------------------+
|  ServiceMine            |
+-------------------------+
|  - Detects Shake        |
|  - Fetches Location     |
|  - Sends SOS SMS        |
+-------------------------+

+-------------------------+
| SharedPreferences       |
+-------------------------+
|  - Stores contacts      |
|  - Stores settings      |
+-------------------------+
```

---

## 🖼️ Screens / UI Flow

1. **MainActivity**: Dashboard with navigation buttons and status display.
2. **RegisterNumberActivity**: Redirected automatically if no contacts are saved.
3. **ContactSelectionActivity**: Allows multi-contact selection using `ListView`.
4. **LawsActivity**: Displays basic women's legal rights.
5. **DefenseActivity**: Shows self-defense information.
6. **Fake Call Activity (Future Scope)**: Simulates an incoming call UI.
7. **ServiceMine**: Background service for shake detection and SMS sending.

---

## ⚙️ How It Works

1. **User selects emergency contacts** from the phonebook.
2. App **starts a foreground service** and listens for shake events.
3. On shake detection:
   - Current **location is retrieved**.
   - **SMS is sent** to all saved contacts with a location link.
   - A **toast/notification** confirms SOS dispatch.

---

## 🧪 Required Permissions

- `SEND_SMS`
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`
- `READ_CONTACTS`
- `FOREGROUND_SERVICE` (for Android 9+)

> All permissions are requested dynamically at runtime using the Activity Result API.


---

## ✅ Setup Instructions

1. **Clone this repository:**
   ```bash
   git clone https://github.com/yourusername/safehaven.git
   ```

2. **Open in Android Studio** and sync Gradle.

3. **Add dependencies** in `build.gradle` (if not already):
   ```gradle
   implementation 'com.google.android.gms:play-services-location:21.0.1'
   implementation 'com.github.tbouron:shake-detector:1.0.1'
   ```

4. **Run the project** on an Android device or emulator with necessary permissions.

---

## 🛠️ Future Enhancements

- 🎤 Voice command to trigger SOS
- 🛑 Emergency stop button in notification
- 🔊 Siren sound on shake
- 🌐 Cloud backup of contacts and history
- 🔐 App Lock with fingerprint/PIN
- 👮‍♂️ Integration with police/emergency APIs

---


## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

> 💡 *SafeHaven is more than an app — it’s a silent companion ensuring your safety when it matters most.*
## 📦 Download
[![GitHub release (latest)](https://img.shields.io/github/v/release/MrEinsteinE/SafeHaven?style=for-the-badge)](https://github.com/MrEinsteinE/SafeHaven/releases/latest)

➡️ **[Download the latest SafeHaven APK](https://github.com/MrEinsteinE/SafeHaven/releases/latest)**  

