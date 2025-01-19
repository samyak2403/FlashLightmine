# FlashlightMine

<img src="https://github.com/samyak2403/FlashLightmine/blob/master/app/src/main/res/mipmap-hdpi/ic_launcher_round.webp" width="100px"/>


[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Get it on GitHub" height="80">](https://github.com/samyak2403/FlashLightmine/releases/latest)


FlashlightMine is an Android application that allows users to toggle their device flashlight using a button or by shaking the device. The app provides a simple and interactive UI for this purpose.

## Features

- **Flashlight Control**: Turn the flashlight ON/OFF with a button.
- **Shake Detection**: Shake your device to toggle the flashlight.
- **Dynamic UI**: 
  - Changes the background color and light bulb icon based on flashlight status.
  - Updates the status bar color dynamically.
  - Applies dynamic tint to UI elements for better visual feedback.
- **Snackbar Notifications**: Inform users about additional functionalities (e.g., shake detection).

## Screenshots

<table style="width: 100%; text-align: center; border-collapse: collapse;">
  <tr>
    <td><img src="images/1.png" width="250px" /></td>
    <td><img src="images/2.png" width="250px" /></td>

</table>
    
## Getting Started

### Prerequisites

- Android Studio installed on your computer.
- A device with a flashlight and accelerometer sensor (or an emulator with simulated hardware).

### Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/samyak2403/FlashLightmine.git
   ```

2. Open the project in Android Studio.
3. Build and run the project on a compatible Android device or emulator.

## Code Overview

### MainActivity

The primary activity handles the following:

1. **Flashlight Control**:
   - Toggles the flashlight using the `CameraManager` API.
   - Updates the UI to reflect the flashlight status.

2. **Shake Listener**:
   - Uses the accelerometer sensor to detect device shakes.
   - Toggles the flashlight if a shake is detected above the threshold (2.5 G).

3. **Dynamic UI Updates**:
   - Changes background color and status dynamically.
   - Updates the tint of decorative ellipses based on flashlight state.

### Key Functions

- `toggleFlashlight()`: Toggles the flashlight and updates the UI.
- `setupShakeListener()`: Registers a sensor listener to detect shakes.
- `setEllipsesTint(isFlashlightOn: Boolean)`: Updates the tint of UI ellipses.

## Dependencies

This app uses the following Android libraries:

- [Material Components](https://material.io/develop/android) for Snackbar and UI design.
- [Camera2 API](https://developer.android.com/reference/android/hardware/camera2/package-summary) for flashlight control.

## Permissions

Add the following permissions in your `AndroidManifest.xml` file:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.FLASHLIGHT" />
```

## How to Use

1. Launch the app.
2. Tap the ON/OFF button to toggle the flashlight.
3. Shake your device to toggle the flashlight dynamically.

## Future Enhancements

- Add customization for shake sensitivity.
- Include more dynamic animations for better user interaction.

## Author

[Samyak Kamble](https://github.com/samyak2403/IPTVmine)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
