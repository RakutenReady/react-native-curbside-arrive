package com.reactlibrary;

import android.app.Notification;
import android.util.Log;

import com.curbside.sdk.CSConstants;
import com.curbside.sdk.CSSite;
import com.curbside.sdk.CSUserSession;
import com.curbside.sdk.credentialprovider.TokenCurbsideCredentialProvider;
import com.curbside.sdk.event.Event;
import com.curbside.sdk.event.Path;
import com.curbside.sdk.event.Status;
import com.curbside.sdk.event.Type;
import com.curbside.sdk.model.CSUserInfo;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.firebase.messaging.RemoteMessage;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import rx.Subscriber;
import rx.exceptions.OnErrorNotImplementedException;

public class CurbsideArriveModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  @VisibleForTesting
  private static final String REACT_CLASS = "CurbsideArriveModule";
  private final ReactApplicationContext reactContext;

  public CurbsideArriveModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addLifecycleEventListener(this);
  }

  @Override
  public String getName() {
    return "Curbside";
  }

  /**
   * Consumers should be using this method to create a new session with their usageToken.
   * <p>
   * Note: The usage token is scoped to the Production and Sandbox. Make sure to use the production
   * usage token when deploying your app to the Google Play Store.
   */
  @ReactMethod
  public void createSessionWithUsageToken(@Nonnull final String usageToken) {
    Log.i(REACT_CLASS, String.format("Usage Token set to :%s", usageToken));
    CSUserSession.init(this.getReactApplicationContext(), new TokenCurbsideCredentialProvider(usageToken));
  }

  /**
   * trackingIdentifier for the user who is logged into the device.
   * This may be null when the app is started, but as the user logs into the app,
   * make sure this value is set.
   * <p>
   * trackingIdentifier needs to be set to use session specific methods for starting
   * trips or monitoring sites. This identifier will be persisted across application restarts.
   * <p>
   * When the user logs out, set this to null, which will in turn end the user session.
   * <p>
   * The maximum length of the trackingIdentifier is 36 characters.
   */
  @ReactMethod
  public void setTrackingIdentifier(@Nullable final String trackingIdentifier) {
    Log.i(REACT_CLASS, String.format("trackingIdentifier set to :%s", trackingIdentifier));
    WritableMap tid = Arguments.createMap();
    tid.putString("trackingIdentifier", trackingIdentifier);
    if (trackingIdentifier == null) {
      subscribe(Type.UNREGISTER_TRACKING_ID, "UNREGISTER_TRACKING_ID", tid);
      CSUserSession.getInstance().unregisterTrackingIdentifier();
    } else {
      subscribe(Type.REGISTER_TRACKING_ID, "REGISTER_TRACKING_ID", tid);
      CSUserSession.getInstance().registerTrackingIdentifier(trackingIdentifier);
    }
  }

  /**
   * Start a trip tracking the user to the site identified by the siteID. Call this method when
   * the application thinks its appropriate to start tracking the user eg. Order is ready to be picked up at
   * the site. This information is persisted across relaunch.
   * <p>
   * Sends Event on EventBus at path User for event type START_TRIP and events Success and Failure. For Failure event
   * To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void startTripToSiteWithIdentifier(@Nonnull final String siteID, @Nonnull final String trackToken) {
    Log.i(REACT_CLASS, String.format("start trip with site Identifier :%s & Track token :%s", siteID, trackToken));
    WritableMap singleTrip = Arguments.createMap();
    singleTrip.putString("siteID", siteID);
    singleTrip.putString("trackToken", trackToken);
    subscribe(Type.START_TRIP, "START_TRIP", singleTrip);
    CSUserSession.getInstance().startTripToSiteWithIdentifier(siteID, trackToken);
  }

  /**
   * Start a trip tracking the user to the site identified by the siteID. Call this method when
   * the application thinks its appropriate to start tracking the user eg. curbside (i.e tripType) order is ready to be picked up at.
   *
   * <p>
   * The possible tripType values are :
   * <ul>
   * <li>CSConstants.CSTripTypeCarryOut
   * <li>CSConstants.CSTripTypeCurbside
   * <li>CSConstants.CSTripTypeDriveThru
   * <li>CSConstants.CSTripTypeDineIn
   * </ul>
   * Please contact Rakuten Ready (i.e. platform@rakutenready.com) to add additional tripTypes
   * </p>
   * <p>
   * This information is persisted across relaunch.
   * <p>
   * Sends Event on EventBus at path User for event type START_TRIP and events Success and Failure. For Failure event
   * To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void startTripToSiteWithIdentifierWithTripType(@Nonnull final String siteID, @Nonnull final String trackToken, @Nonnull final String tripType) {
    Log.i(REACT_CLASS, String.format("start trip with site Identifier :%s & Track token :%s", siteID, trackToken));
    WritableMap singleTrip = Arguments.createMap();
    singleTrip.putString("siteID", siteID);
    singleTrip.putString("trackToken", trackToken);
    singleTrip.putString("tripType", tripType);
    subscribe(Type.START_TRIP, "START_TRIP", singleTrip);
    CSUserSession.getInstance().startTripToSiteWithIdentifier(siteID, trackToken, tripType);
  }

  /**
   * Start a trip tracking the user to the site identified by the siteID for the user's intent of arrival at a given time or a time window.
   * Call this method when the application thinks its appropriate to start tracking the user.
   * <p>
   * If the user's intent is to arrive at the site at a given time, then populate the fromDate with the arrival time. e.g. Pick up
   * a order at 6.00pm. Set the fromDate to be 6.00pm
   * <p>
   * If the user's intent is to arrive at the site at a given time window, then populate the fromDate and toDate with the time window.
   * <p>
   * Sends Event on EventBus at path User for event type START_TRIP and events Success and Failure. For Failure event
   * To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void startTripToSiteWithIdentifierWithPickupWindow(@Nonnull final String siteID, @Nonnull final String trackToken, @Nonnull final DateTime fromDate, @Nullable final DateTime toDate) {
    Log.i(REACT_CLASS, String.format("start trip with the site Identifier :%s & Track token :%s & fromDate :%s & toDate :%s", siteID, trackToken, fromDate, toDate));
    WritableMap pickupWindowTrip = Arguments.createMap();
    pickupWindowTrip.putString("siteID", siteID);
    pickupWindowTrip.putString("trackToken", trackToken);
    pickupWindowTrip.putString("fromDate", fromDate.toString());
    if (toDate != null) pickupWindowTrip.putString("toDate", toDate.toString());
    subscribe(Type.START_TRIP, "START_TRIP", pickupWindowTrip);
    CSUserSession.getInstance().startTripToSiteWithIdentifierAndETA(siteID, trackToken, fromDate, toDate);
  }

  /**
   * Start a trip tracking the user to the site identified by the siteID for the user's intent of arrival between a time window. Call this method when
   * the application thinks its appropriate to start tracking the user.
   * <p>
   * <ul>
   * <li>If the user's intent is to arrive at the site at a given time, then populate the fromDate with the arrival time. e.g. Pick up order at 6.00pm. Set the fromDate to be 6.00pm
   * <li>If the user's intent is to arrive at the site at a given time window, then populate the fromDate and toDate with the time window.
   * <li>If user's order type is known, then populate tripType with the value. e.g CSConstants.CSTripTypeCarryOut or CSConstants.CSTripTypeCurbside
   * </ul>
   * </p>
   * <p>
   * The possible tripType values are :
   * <ul>
   * <li>CSConstants.CSTripTypeCarryOut
   * <li>CSConstants.CSTripTypeCurbside
   * <li>CSConstants.CSTripTypeDriveThru
   * <li>CSConstants.CSTripTypeDineIn
   * </ul>
   * Please contact Rakuten Ready (i.e. platform@rakutenready.com) to add additional tripTypes
   * </p>
   * <p>
   * Sends Event on EventBus at path User for event type START_TRIP and events Success and Failure. For Failure event
   * To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void startTripToSiteWithTypeIdentifierWithPickupWindow(@Nonnull final String siteID, @Nonnull final String trackToken, @Nonnull final DateTime fromDate, @Nullable final DateTime toDate,
                                                                @Nonnull final String tripType) {
    Log.i(REACT_CLASS, String.format("start trip with the site Identifier :%s & Track token :%s & fromDate :%s & toDate :%s & tripType :%s", siteID, trackToken, fromDate, toDate, tripType));
    WritableMap pickupWindowTrip = Arguments.createMap();
    pickupWindowTrip.putString("siteID", siteID);
    pickupWindowTrip.putString("trackToken", trackToken);
    pickupWindowTrip.putString("fromDate", fromDate.toString());
    if (toDate != null) pickupWindowTrip.putString("toDate", toDate.toString());
    pickupWindowTrip.putString("tripType", tripType);
    subscribe(Type.START_TRIP, "START_TRIP", pickupWindowTrip);
    CSUserSession.getInstance().startTripToSiteWithIdentifierAndETA(siteID, trackToken, fromDate, toDate, tripType);
  }

  /**
   * Completes the trip for the user to the site identified by the siteID with the given trackToken.
   * If no trackToken is specified, then *all*   trips to this site  will be completed.
   * <p>
   * Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
   * <p>
   * Sends Event on EventBus at path User for event type COMPLETE_TRIP and events Success and Failure.
   * To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void completeTripToSiteWithIdentifier(@Nonnull final String siteID, @Nullable final String trackToken) {
    Log.i(REACT_CLASS, String.format("Complete trip with the site Identifier :%s & Track token :%s", siteID, trackToken));
    WritableMap singleTrip = Arguments.createMap();
    singleTrip.putString("siteID", siteID);
    singleTrip.putString("trackToken", trackToken);
    subscribe(Type.COMPLETE_TRIP, "COMPLETE_TRIP", singleTrip);
    CSUserSession.getInstance().completeTripToSiteWithIdentifier(siteID, trackToken);
  }

  /**
   * Cancels the trip for the user to the given site identified by the siteID with the given trackToken.
   * If no trackToken is set, then *all* trips to this site are cancelled.
   * <p>
   * Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
   * <p>
   * Sends Event on EventBus at path User for event type CANCEL_TRIP and events Success and Failure.
   * To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void cancelTripToSiteWithIdentifier(@Nonnull final String siteID, @Nullable final String trackToken) {
    Log.i(REACT_CLASS, String.format("Cancel trip with the site Identifier :%s & Track token :%s", siteID, trackToken));
    WritableMap singleTrip = Arguments.createMap();
    singleTrip.putString("siteID", siteID);
    singleTrip.putString("trackToken", trackToken);
    subscribe(Type.CANCEL_TRIP, "CANCEL_TRIP", singleTrip);
    CSUserSession.getInstance().cancelTripToSiteWithIdentifier(siteID, trackToken);
  }

  /**
   * This method would complete all trips for this user across all devices.
   * <p>
   * Do not call this when the user logs out, instead call unregisterTrackingIdentifier() when the user logs out.
   * <p>
   * Sends Event on EventBus at path User for event type COMPLETE_ALL_TRIPS and events Success and Failure.
   * To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void completeAllTrips() {
    Log.i(REACT_CLASS, "Complete all trips");
    WritableMap allTrips = Arguments.createMap();
    allTrips.putNull("trackToken");
    subscribe(Type.COMPLETE_ALL_TRIPS, "COMPLETE_ALL_TRIPS", allTrips);
    CSUserSession.getInstance().completeAllTrips();
  }

  /**
   * This method will cancels all trips for all sites for the user.
   * <p>
   * sDo not call this when the user logs out, instead call unregisterTrackingIdentifier() when the user logs out.
   * Sends Event on EventBus at path User for event type CANCEL_ALL_TRIPS and events Success and Failure.
   * To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void cancelAllTrips() {
    Log.i(REACT_CLASS, "Cancel all trips");
    WritableMap allTrips = Arguments.createMap();
    allTrips.putNull("trackToken");
    subscribe(Type.CANCEL_ALL_TRIPS, "CANCEL_ALL_TRIPS", allTrips);
    CSUserSession.getInstance().cancelAllTrips();
  }

  /**
   * Contact info - fullName, email address, sms number, Vehicle info - Make, Model & License Plate of the user.
   */
  @ReactMethod
  public void setUserInfo(@Nullable String fullName, @Nullable String email, @Nullable String smsNumber,
                           @Nullable String vehicleMake, @Nullable String vehicleModel, @Nullable String vehicleLicensePlate) {
    Log.i(REACT_CLASS, String.format("Setting user info Name : %s Email : %s SMS Number : %s License Plate : %s Make : %s Model : %s", fullName, email, smsNumber, vehicleLicensePlate, vehicleMake, vehicleModel));
    final CSUserInfo csUserInfo = new CSUserInfo(fullName, email, smsNumber, vehicleLicensePlate, vehicleMake, vehicleModel);
    CSUserSession.getInstance().setUserInfo(csUserInfo);
  }

  /**
   * Returns true if the current user is being tracked for a order pickup at a site.
   * Promise returns true if the user is being tracked
   */
  @ReactMethod
  public void isTracking(@Nonnull final Promise promise) {
    boolean isTracking = CSUserSession.getInstance().isTracking();
    Log.i(REACT_CLASS, String.format("Is current user being  tracked for a order pickup at a site :%s", isTracking));
    promise.resolve(isTracking);
  }

  /**
   * Returns an trackingIdentifier of the currently tracked user
   */
  @ReactMethod
  public void getTrackingIdentifier(@Nonnull final Promise promise) {
    String trackingIdentifier = CSUserSession.getInstance().getTrackingIdentifier();
    Log.i(REACT_CLASS, String.format("currently tracked user's trackingIdentifier :%s", trackingIdentifier));
    promise.resolve(trackingIdentifier);
  }

  /**
   * Returns all currently tracked CSSite(s)
   * Promise returns the set of CSSite(s)
   */
  @ReactMethod
  public void getTrackedSites(@Nonnull final Promise promise) {
    Set trackedSites = CSUserSession.getInstance().getTrackedSites();
    Log.i(REACT_CLASS, String.format("currently tracked CSSite(s) :%s", trackedSites));
    promise.resolve(trackedSites);
  }

  /**
   * Call this method whenever FCM is received on your FCMMessagingService and pass the payload
   *
   * @param remoteMessage message received on FirebaseMessagingService
   *                      Promise returns true/false based on its ability to handle the message
   */
  @ReactMethod
  public void handlePushNotification(@Nonnull final RemoteMessage remoteMessage, @Nonnull final Promise promise) {
    Log.i(REACT_CLASS, String.format("push notification payload :%s", remoteMessage));
    promise.resolve(CSUserSession.getInstance().handlePushNotification(remoteMessage));
  }

  /**
   * Call this method to notify the user in the CSMonitoringSession of the arrival of this user
   * at the site.
   *
   * @param site contains the site identifier to notify the user in the CSMonitoringSession of the arrival.
   */
  @ReactMethod
  public void notifyMonitoringSessionUserOfArrivalAtSite(@Nonnull final CSSite site) {
    Log.i(REACT_CLASS, String.format("CSSite :%s", site));
    WritableMap siteMap = Arguments.createMap();
    siteMap.putString("siteID", site.getSiteIdentifier());
    subscribe(Type.NOTIFY_MONITORING_SESSION_USER, "NOTIFY_MONITORING_SESSION_USER", siteMap);
    CSUserSession.getInstance().notifyMonitoringSessionUserOfArrivalAtSite(site);
  }

  /**
   * Clients on Android OS 8.0+ require that a foreground service to be started so that the app can receive locations.
   * For the foreground service to be started, notification object is required which will be shown to the user when the service is running.
   * <p>
   * {@link android.app.NotificationChannel} NotificationChannel should be defined for your app for Android 8.0+ versions.
   * {@link Notification} Notification object should have that Channel ID , Channel Name, Title, Icon and Text values set so as to show the notification in the foreground service.
   * More details https://developer.rakutenready.com/en/docs/getting-started/quickstart-android-app/#step-4-set-notification-object-for-foreground-service-for-apps-targeting-os-80
   * </p>
   * <p>
   * call setNotificationForForegroundService method once notification object is returned as part of Promise
   */
  @ReactMethod
  public void createNotification(@Nonnull final ReadableMap notificationConfig, @Nonnull final Promise promise) {
    Log.i(REACT_CLASS, String.format("notification config :%s", notificationConfig));
    NotificationHelper.getInstance(getReactApplicationContext()).createNotification(notificationConfig, promise);
  }

  /**
   * Clients on Android OS 8.0+ require that a foreground service to be started so that the app can receive locations.
   * For the foreground service to be started, notification object is required which will be shown to the user when the service is running.
   * <p>
   * {@link android.app.NotificationChannel} NotificationChannel should be defined for your app for Android 8.0+ versions.
   * {@link Notification} Notification object should have that Channel ID , Channel Name, Title, Icon and Text values set so as to show the notification in the foreground service.
   * More details https://developer.rakutenready.com/en/docs/getting-started/quickstart-android-app/#step-4-set-notification-object-for-foreground-service-for-apps-targeting-os-80
   * </p>
   * <p>
   * calls setNotificationForForegroundService method on notification object
   */
  @ReactMethod
  public void createNotificationForForegroundService(@Nonnull final ReadableMap notificationConfig, @Nonnull final Promise promise) {
    Log.i(REACT_CLASS, String.format("notification config :%s", notificationConfig));
    Notification mNotification = NotificationHelper.getInstance(getReactApplicationContext()).createNotification(notificationConfig, promise);
    if (mNotification != null) {
      Log.i(REACT_CLASS, String.format("notification config :%s and with notification obj :%s", notificationConfig, mNotification));
      CSUserSession.getInstance().setNotificationForForegroundService(mNotification);
    }
  }

  /**
   * Clients on Android OS 8.0+ require that a foreground service to be started so that the app can receive locations.
   * For the foreground service to be started, notification object is required which will be shown to the user when the service is running.
   * <p>
   * {@link android.app.NotificationChannel} NotificationChannel should be defined for your app for Android 8.0+ versions.
   * {@link Notification} Notification object should have that Channel ID , Channel Name, Title, Icon and Text values set so as to show the notification in the foreground service.
   * More details https://developer.rakutenready.com/en/docs/getting-started/quickstart-android-app/#step-4-set-notification-object-for-foreground-service-for-apps-targeting-os-80
   * </p>
   * <p>
   * calls setNotificationForForegroundService method on notification object and Minutes before pickup time
   */
  @ReactMethod
  public void createNotificationForForegroundServiceWithPickupWindow(@Nonnull final ReadableMap notificationConfig, @Nonnull final Minutes minBeforePickupTime, @Nonnull final Promise promise) {
    Notification mNotification = NotificationHelper.getInstance(getReactApplicationContext()).createNotification(notificationConfig, promise);
    if (mNotification != null) {
      Log.i(REACT_CLASS, String.format("notification obj :%s and minutes before pickup time :%s", mNotification, minBeforePickupTime));
      CSUserSession.getInstance().setNotificationForForegroundService(mNotification, minBeforePickupTime);
    }
  }

  /**
   * Clients on Android OS 8.0+ require that a foreground service to be started so that the app can receive locations.
   * For the foreground service to be started, notification object is required which will be shown to the user when the service is running.
   * <p>
   * Foreground Service will only be started when there is a trip to track. If Pickup start time is specified while starting a trip
   * by default foreground Services will start around 60 minutes ( i.e timing depends on system optimizations) before pickup start time,
   * in case Pickup start time is less than 60 minutes at the time of start trip the foreground service would start almost instantly
   * at time of starting the trip.
   * <p>
   * If developer wants to control timing of activation of persistent foreground services refer method :
   * <p>
   * setNotificationForForegroundService(final Notification notification,final Minutes minBeforePickupTime)
   * <p>
   * The service will be stopped when there is no trip to track.
   *
   * <p>
   * {@link android.app.NotificationChannel} NotificationChannel should be defined for your app for Android 8.0+ versions.
   * {@link Notification} Notification object should have that Channel ID set so as to show the notification in the foreground service.
   * </p>
   *
   * @param notification notification to be shown when foreground service is started
   */
  @ReactMethod
  public void setNotificationForForegroundService(@Nonnull final Notification notification) {
    Log.i(REACT_CLASS, String.format("notification :%s", notification));
    CSUserSession.getInstance().setNotificationForForegroundService(notification);
  }

  /**
   * Clients on Android OS 8.0+ require that a foreground service to be started so that the app can receive locations.
   * For the foreground service to be started, notification object is required which will be shown to the user when the service is running.
   * <p>
   * Foreground Service will only be started when there is a trip to track.
   * Foreground Service will start around specified `minBeforePickupTime` value before scheduled Pickup start time, since this starting of Foreground Service is
   * dependent on OS system optimizations and priority process it's recommended to add buffer to this value.
   * <p>
   * The service will be stopped when there is no trip to track. If Pickup start time is specified while starting a trip
   * passed in `minBeforePickupTime` value in minutes would initiate activation of Foreground service at this time before Pickup Time
   * <p>
   * E.g. Pick up a order at 6.00pm and Foreground service intent to start 30 minutes before Pickup Time. Set the fromDate to be 6.00pm in startTripToSiteWithIdentifier method.
   * Set 30 minutes in method setNotificationForForegroundService(notification,Minutes.minutes(30))
   * <p>
   * The minimum lower bound for `minBeforePickupTime` is 15 minutes.
   *
   * <p>
   * {@link android.app.NotificationChannel} NotificationChannel should be defined for your app for Android 8.0+ versions.
   * {@link Notification} Notification object should have that Channel ID set so as to show the notification in the foreground service.
   * </p>
   *
   * @param notification        notification to be shown when foreground service is started
   * @param minBeforePickupTime minutes before pickup time when foreground service activation is requested
   */
  @ReactMethod
  public void setNotificationForForegroundServiceWithPickupWindow(@Nonnull final Notification notification, @Nonnull final Minutes minBeforePickupTime) {
    Log.i(REACT_CLASS, String.format("notification :%s minutes before pickup time:%s", notification, minBeforePickupTime));
    CSUserSession.getInstance().setNotificationForForegroundService(notification, minBeforePickupTime);
  }

  /**
   * Subscribe events to EventBus
   */
  @ReactMethod
  public void subscribe(@Nonnull final Type type, @Nonnull final String eventName, @Nullable final WritableMap params) {
    Subscriber<Event> eventSubscriber = new Subscriber<Event>() {
      @Override
      public void onCompleted() {

      }

      @Override
      public void onError(Throwable e) {
        throw new OnErrorNotImplementedException(e);
      }

      @Override
      public void onNext(Event event) {
        DeviceEventManagerModule.RCTDeviceEventEmitter emitter = reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        if (Status.SUCCESS != event.status) {
          if (Status.TRUE == event.status) {
            CSSite site = (CSSite) event.object;
            emitter.emit(eventName, site.getSiteIdentifier());
          } else emitter.emit("encounteredError", event.object);
        } else {
          emitter.emit(eventName, params);
        }
        unsubscribe();
      }
    };
    CSUserSession.getInstance().getEventBus().getObservable(Path.USER, type).subscribe(eventSubscriber);
  }

  /**
   * Called either when the host activity receives a resume event  or
   * if the native module that implements this is initialized while the host activity is already
   * resumed. Always called for the most current activity.
   */
  @Override
  public void onHostResume() {

  }

  /**
   * Called when host activity receives pause event. Always called
   * for the most current activity.
   */
  @Override
  public void onHostPause() {

  }

  /**
   * Called when host activity receives destroy event. Only called
   * for the last React activity to be destroyed.
   */
  @Override
  public void onHostDestroy() {

  }
}
