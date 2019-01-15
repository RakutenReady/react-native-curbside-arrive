
# react-native-curbside-arrive

This library wraps the native Curbside Arrive SDK for iOS and Android, providing a unified JavaScript interface.


#### Installing dependencies

You will need Node, Yarn, Watchman, the React Native command line interface, Android Studio, and Xcode.
```
brew install yarn
brew install watchman

```
The easiest way to install Xcode is via the Mac App Store. Installing Xcode will also install the iOS Simulator and all the necessary tools to build your iOS app.
Download and install Android Studio. 

#### Getting started

Since this is not pushed to npm server you can not install it via NPM registry i.e `$ npm install react-native-curbside-arrive --save`

Clone the repository into your local working directory :

`$ git clone git@github.com:Curbside/react-native-curbside-arrive.git`

`$ npm install`

`$ npm install react-native`

`$ npm link` # create a global symlink

#### Manual installation

Change working directory to your react native app root directory and install <react-native-curbside-arrive>

`$ npm install`
`$ npm link react-native-curbside-arrive` #create a symlink locally to global <react-native-curbside-arrive>

Voila! now we can develop the two projects side-by-side without having to worry about publishing either of them to NPM registry

#### iOS

Follow all the 3 manual steps (https://facebook.github.io/react-native/docs/linking-libraries-ios) for linking  of <react-native-curbside-arrive> library in your react native app

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-curbside-arrive` and add `RNCurbsideArrive.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNCurbsideArrive.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<
5. <react-native-curbside-arrive> ios bridge needs Curbside framework to compile, please download via cocopods and add it to build path
6. Add location permission request in the App also please follow the permission setting on ios quick guide on develolper.curbside.com

- In `AppDelegate.m` add

  ```obj-c
  #import <CoreLocation/CLLocationManager.h>
  CLLocationManager *_locationManager;
  ```

  and in the `didFinishLaunchingWithOptions` method add:

   ```obj-c
  // Request Always location permission
  _locationManager = [[CLLocationManager alloc] init];
  if([_locationManager respondsToSelector:@selector(requestAlwaysAuthorization)])
      [_locationManager requestAlwaysAuthorization];
  ```

For quick reference of settings please refer to example app `$ react-native-curbside-arrive/exampleArriveApp/ios` -> `exampleArriveApp.xcworkspace `


#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `import com.reactlibrary.RNCurbsideArrivePackage;` to the imports at the top of the file
  - Add `new RNCurbsideArrivePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-curbside-arrive'
  	project(':react-native-curbside-arrive').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-curbside-arrive/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-curbside-arrive')
  	```
4. Insert the following lines above the dependencies block `android/app/build.gradle`:

    ```
	allprojects {
	    repositories {
	        jcenter()
	        maven { url "https://maven.google.com" }
	    }
	}

	repositories {
	    maven {url "https://raw.github.com/Curbside/curbside-android-sdk-release/master"}
	    mavenCentral()
	}    

    ```
 5. Curbside Android SDK needs `google-services.json` for the FCM integration at compile time, please follow FCM integration guide on developer.curbside.com to generate one. Sample file has been created to compile the android <react-native-curbside-arrive> bridge   

 6. Add to `AndroidManifest.xml`

```xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-feature android:name="android.hardware.location.gps" />
 ```
 7. Make sure the device you're using (also applies to emulators) has Google Play Services installed.


 For quick reference of settings please refer to example app `$ react-native-curbside-arrive/exampleArriveApp/android`      	


#### Usage

Please refer to `RNCurbsideArrive.m` in ios and `RNCurbsideArriveModule` in Android in <react-native-curbside-arrive> library for full list of methods

```js
import { NativeModules } from 'react-native';
const curbside = NativeModules.Curbside;
const USAGE_TOKEN = '5456fd004166c9226ec3f2cb1ec567f0556af771305cc3e38fb0c3419833f48b'; // Piecompany

// Consumers should be using this method to create a new session with their usageToken.
curbside.createSessionWithUsageToken(USAGE_TOKEN)

// trackingIdentifier for the user who is logged into the device
curbside.setTrackingIdentifier(trackingIdentifier)

// Start a trip tracking the user to the site identified by the siteID. Call this method when
// the application thinks its appropriate to start tracking the user eg. Order is ready to be picked up at
// the site.
curbside.startTripToSiteWithIdentifier(SITE_ID,TRACK_TOKEN)

```  