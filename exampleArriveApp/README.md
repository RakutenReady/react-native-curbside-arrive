## react-native-curbside-arrive example project

Example project demonstrating the functionality of the `react-native-curbside-arrive` library.

Please note that this project uses `package.json` from the parent folder to simplify developing the library while testing it on this example project. If you use `react-native-curbside-arrive` in your own project, please follow instructions in the main [README](../README.md), and understand that the project structure will be slightly different than in this example project.

Example project uses `react-native-geolocation-service` library is drop-in replacement for the React Native's Geolocation API, the usage is pretty straight forward, with some extra error cases to handle.

One thing to note, for android this library assumes that location permission is already granted by the user, so you have to use `PermissionsAndroid` to request for permission before making the location request.

Also includes sample [Firebase](https://rnfirebase.io/) messaging service integration.   



#### Starting the simulator

iOS

- `cd exampleArriveApp && pod install`
- `open exampleArriveApp/ios/exampleArriveApp.xcworkspace` and Run from Xcode
                       ```
                       or
                       ```
                   
- `cd exampleArriveApp`
- `react-native run-ios`


#### Starting the Emulator

Android

- in Android Studio open `exampleArriveApp/android/` folder and run from there
                       ```
                       or
                       ```
- `cd exampleArriveApp`
- `react-native run-android`

#### FAQ
<details>

<summary>Android :java.lang.NoClassDefFoundError: Failed resolution of: Lcom/google/android/gms/internal/zzbgl;</summary>	

Check if `play-services-analytics` dependency is added in `build.gradle` file as shown below as example :

```xml

    implementation 'com.google.android.gms:play-services-gcm:17.0.0'
    implementation 'com.google.android.gms:play-services-location:17.0.0'
    implementation 'com.google.android.gms:play-services-maps:17.0.0'
    implementation 'com.google.firebase:firebase-core:17.0.0'
    implementation 'com.google.firebase:firebase-messaging:17.0.0'
    implementation 'com.google.android.gms:play-services-analytics:17.0.0'
```
</details>    