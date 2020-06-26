# react-native-curbside-arrive

The Curbside ARRIVE SDK delivers reliable arrival prediction. Battle-tested by millions of customer arrivals in real world commerce, ARRIVE sends an accurate alert before arrival and gives you true visibility into customer ETA and dwell time. ARRIVE hooks easily into a mobile commerce infrastructure and on-site apps or POS for notifications.

This library wraps the native Curbside Arrive SDK for iOS and Android, providing a unified JavaScript interface.

## Arrive SDK Prerequisite
Register for an account at [Curbside Platform](https://merchantcloud.curbside.com). Sign in and do the following:
* Generate a [usage token](https://merchantcloud.curbside.com/account?accessTab=tokens&accountTab=access)
* Create a [site](https://merchantcloud.curbside.com/account?accountTab=sites)

## Running
* Grant permission to use location services
* Add a tracking identifier
* Add a track token
* Add a [site identifier](https://merchantcloud.curbside.com/account?accountTab=sites) that was created on the Curbside Platform.
* Start track
* Go on a test drive

## View User Trip
* Check [ARRIVE dashboard](https://merchantcloud.curbside.com) to see your current location and to note that your status changed from In-Transit to Arrived.
* Use the monitor app to view detailed information about your arrival.

## Quick Start Guides
- [iOS App Quick Start](https://developer.curbside.com/en/docs/getting-started/quickstart-ios-app/)
- [Android App Quick Start](https://developer.curbside.com/en/docs/getting-started/quickstart-android-app/)


The documentation is mostly in Objective-C, Swift and Java, but React Native module library classes `CurbsideArrive.m` and `CurbsideArriveModule.java` methods are almost 1:1 with the native APIs,so the docs should still be quite useful. Helpful [glossary](https://developer.rakutenready.com/en/docs/reference/glossary/) to get familiar with imprtant Arrive terms.

### Installing dependencies
You will need Node, Yarn, Watchman, the React Native command line interface, Android Studio, and Xcode.
```
brew install yarn
brew install watchman

```
The easiest way to install Xcode is via the Mac App Store. Installing Xcode will also install the iOS Simulator and all the necessary tools to build your iOS app.

Download and install Android Studio.

### Getting started
```sh
$ npm install react-native-curbside-arrive
```

or

```sh
$ yarn add react-native-curbside-arrive
```
### Installation
From React-Native 0.60.0 linking packages using react-native link has become redundant. [Autolink](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md) has been added to the React-Native CLI which means that iOS will now use cocoapods and Android will use gradle.

<details>
  <summary>Linker for React Native 0.59 or below</summary>
	
```
$ react-native link react-native-curbside-arrive
```
</details>

<details>
  <summary>iOS (CocoaPods) for React Native 0.59 or below</summary>
	
Install [CocoaPods](https://cocoapods.org/) and set up your Podfile like it is described in the [react-native documentation](https://facebook.github.io/react-native/docs/integration-with-existing-apps#configuring-cocoapods-dependencies).

In your `ios/Podfile`, add these snippets:
  ```
  pod 'react-native-curbside-arrive', path: '../node_modules/react-native-curbside-arrive/ios/'
  pod 'Curbside', '3.31'
  ```
Finally, run `pod install`.
</details>

<details>
  <summary>iOS (Manually for React Native 0.59 or below)</summary>
	
  Follow [Manual linking](https://reactnative.dev/docs/linking-libraries-ios#manual-linking) documentation.

- In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`

- Go to `node_modules` ➜ `react-native-curbside-arrive` and add `CurbsideArrive.xcodeproj`

- In XCode, in the project navigator, select your project. Add `libCurbsideArrive.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`

- Follow the [instructions](https://developer.rakutenready.com/en/docs/getting-started/quickstart-ios-app/#install-the-sdk-with-cocoapods) to install the Arrive SDK and add Curbside framework to build path.

</details>

<details>
  <summary>Android for React Native 0.59 or below</summary>
	
For React-Native 0.60 or higher, no manual linking is needed.
You can override following gradle properties from your root build.gradle file.

```gradle
     ext {
        buildToolsVersion = "28.0.3"
        minSdkVersion = 21
        compileSdkVersion = 29
        targetSdkVersion = 29
    }
   dependencies {
        classpath('com.android.tools.build:gradle:3.5.3')
        classpath ("com.google.gms:google-services:4.3.3") // google play services
    }
```

1. Open up `android/app/src/main/java/[...]/MainApplication.java`

   - Add `import com.reactlibrary.RNCurbsideArrivePackage;` to the imports at the top of the file
   - Add `new RNCurbsideArrivePackage()` to the list returned by the `getPackages()` method

2. Append the following lines to `android/settings.gradle`:

   ```gradle
   include ':react-native-curbside-arrive'
   project(':react-native-curbside-arrive').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-curbside-arrive/android')
   
   ```

3. Insert the following lines inside the `dependencies` block in `android/app/build.gradle`:

   ```gradle
   dependencies {
     implementation project(':react-native-curbside-arrive')
   }
   
   ```

4. Insert the following lines above the dependencies block `android/app/build.gradle`:

    ```gradle
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
5. Add entry for Google Services plugin at the end of `android/app/build.gradle`:

    ```gradle
	apply plugin: 'com.google.gms.google-services' 
    ```
6. Curbside Android SDK needs `google-services.json` for the FCM integration at compile time, please setup FCM like it is described in the [Android quick start guide](https://developer.rakutenready.com/en/docs/getting-started/quickstart-android-app/#step-3-add-fcm-in-your-app)

Android ARRIVE requires location from the device in order to notify the site’s associate of the customer's arrival. Starting from OS 6.0, Android introduced doze mode. When the device is in doze mode, the OS tries to conserve battery by restricting the app's access to the network. ARRIVE SDK addresses this issue by sending a high priority FCM to the client whenever it needs a location from the device. A high priority FCM will wake up the sleeping device and provide a small window for the app to make a network call.

</details>

### Setup

<details>
  <summary>iOS</summary>

 - Update `Info.plist` with location keys and background mode as it is described in the [iOS quick start guide.](https://developer.rakutenready.com/en/docs/getting-started/quickstart-ios-app/#step-3-ask-for-location-services-permissions)

 - Helpful blog on requesting [location permissions](https://medium.com/rakutenready/requesting-location-permissions-in-ios-9e5a3b814a8b) in iOS app.

</details>

<details>
  <summary>Android</summary>

 - Setup FCM like it is described in the [Android quick start guide](https://developer.rakutenready.com/en/docs/getting-started/quickstart-android-app/#step-3-add-fcm-in-your-app)

 - Add all wanted permissions in your AndroidManifest.xml file based on your project requirement.
 ```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
<!-- Required for ACTIVITY Monitoring -->
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<!-- Required for foreground services on P+. -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" /> 
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```
</details>

### Sample React Native Arrive App

Example [app](https://github.com/RakutenReady/react-native-curbside-arrive/tree/master/exampleArriveApp) demonstrating the functionality of the `react-native-curbside-arrive` library. Sample app contains example usage of Geolocation,location permissions and Firebase cloud messaging integration for notification.

iOS

- `cd exampleArriveApp/ios && pod install`
- `open example/ios/exampleArriveApp.xcworkspace` and Run from Xcode
                       ```
                       or
                       ```
- `cd exampleArriveApp`
- `react-native run-ios`

Android

- in Android Studio open `exampleArriveApp/android/` folder and run from there
                       ```
                       or
                       ```
- `cd exampleArriveApp`
- `react-native run-android`


## Usage

Please refer to [CurbsideArrive.m](https://github.com/RakutenReady/react-native-curbside-arrive/blob/master/ios/CurbsideArrive.m) in iOS and [CurbsideArriveModule.java](https://github.com/RakutenReady/react-native-curbside-arrive/blob/master/android/src/main/java/com/reactlibrary/CurbsideArriveModule.java) in Android in library for full list of platform specific methods e.g setting`userOnTheWay` attribute and corresponding start trip methods in iOS, Foreground services and notification channel setup methods in Android.

```javascript
import { NativeModules } from 'react-native';
const curbside = NativeModules.Curbside;

/**
 * Consumers should be using this method to create a new session with their usageToken.
 * Note: The usage token is scoped to the Production and Sandbox.
*/
curbside.createSessionWithUsageToken("USAGE_TOKEN")

/**
 * trackingIdentifier for the user who is logged into the device.
 * This may be null when the app is started, but as the user logs into the app,
 * make sure this value is set.
 *
 * trackingIdentifier needs to be set to use session specific methods for starting
 * trips or monitoring sites. This identifier will be persisted across application restarts.
 * 
 * When the user logs out, set this to null, which will in turn end the user session.
 * 
 * The maximum length of the trackingIdentifier is 36 characters.
 */
curbside.setTrackingIdentifier("USER_UNIQUE_TRACKING_ID")

/**
 * This optional method sets the userInfo which has more attributes about the user e.g. fullName, 
 * email address, sms number, Vehicle info - Make, Model & License Plate of the user.
 *
 * (optional)
 */
 curbside.setUserInfo("FULL_NAME","EMAIL_ADDRESS","SMS_NUMBER","CAR_MAKE","CAR_MODEL","CAR_LICENSE_PLATE")
/**
 * Start a trip tracking the user to the site identified by the siteID. Call this method when
 * the application thinks its appropriate to start tracking the user eg. Order is ready to be picked up at
 * the site. This information is persisted across relaunch.

 * Track Token is a piece of data that uniquely identifies a trip. 
 * Track tokens must be unique among trips. Failure to do so returns an error when starting a trip. 
 * A track token has a maximum of 60 characters. Suitable tokens include reservation numbers, 
 * order IDs, or any similar piece of data that is generated to identify a business transaction.
 *
 */
curbside.startTripToSiteWithIdentifier("SITE_ID","TRACK_TOKEN")

/**
 * Start a trip tracking the user to the site identified by the siteID. Call this method when
 * the application thinks its appropriate to start tracking the user eg. Order is ready to be picked up at
 * the site. This information is persisted across relaunch.

 * Track Token is a piece of data that uniquely identifies a trip. 
 * Track tokens must be unique among trips. Failure to do so returns an error when starting a trip. 
 * A track token has a maximum of 60 characters. Suitable tokens include reservation numbers, 
 * order IDs, or any similar piece of data that is generated to identify a business transaction.
 *
 * Note: An overload method with the tripType parameter (the possible values are `carry-out`,
 * drive-thru`, `curbside` or `dine-in`).
 */
curbside.startTripToSiteWithIdentifierWithTripType("SITE_ID","TRACK_TOKEN","TRIP_TYPE")

/**
 * Start a trip tracking the user to the site identified by the siteID for the user's intent of arrival at a given time or a time window.
 * Call this method when the application thinks its appropriate to start tracking the user.
 * 
 * If the user's intent is to arrive at the site at a given time, then populate the fromDate with the arrival time. e.g. Pick up
 * a order at 6.00pm. Set the fromDate to be 6.00pm
 *
 * If the user's intent is to arrive at the site at a given time window, then populate the fromDate and toDate with the time window.
 */
curbside.startTripToSiteWithIdentifierWithPickupWindow("SITE_ID","TRACK_TOKEN","FROM_DATE","TO_DATE")

/**
 * Start a trip tracking the user to the site identified by the siteID for the user's intent of arrival at a given time or a time window.
 * Call this method when the application thinks its appropriate to start tracking the user.
 * 
 * If the user's intent is to arrive at the site at a given time, then populate the fromDate with the arrival time. e.g. Pick up
 * a order at 6.00pm. Set the fromDate to be 6.00pm
 *
 * If the user's intent is to arrive at the site at a given time window, then populate the fromDate and toDate with the time window.
 * 
 * Note: An overload method with the tripType parameter (the possible values are `carry-out`,
 * drive-thru`, `curbside` or `dine-in`).
 */
curbside.startTripToSiteWithTypeIdentifierWithPickupWindow("SITE_ID","TRACK_TOKEN","FROM_DATE","TO_DATE","TRIP_TYPE")

/**
 * Completes the trip for the user to the site identified by the siteID with the given trackToken.
 * If no trackToken is specified, then *all*   trips to this site  will be completed.
 * 
 * Note: Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
 * 
 */
curbside.completeTripToSiteWithIdentifier("SITE_ID","TRACK_TOKEN")

/**
 * Cancels the trip for the user to the given site identified by the siteID with the given trackToken.
 * If no trackToken is set, then *all* trips to this site are cancelled.
 *
 * Note: Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
 */
curbside.cancelTripToSiteWithIdentifier("SITE_ID","TRACK_TOKEN")

```

## License
See the repository root directory for licensing information.
