
package com.reactlibrary;

import android.util.Log;

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
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.annotations.VisibleForTesting;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.joda.time.DateTime;

import javax.annotation.Nullable;

import rx.Subscriber;


public class RNCurbsideArriveModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

  private final ReactApplicationContext reactContext;
  @VisibleForTesting
  private static final String REACT_CLASS = "RNCurbsideArriveModule";

  public RNCurbsideArriveModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.reactContext.addLifecycleEventListener(this);
  }


  private void subscribe(final Type type, final String eventName, @Nullable final WritableMap params){
    Subscriber<Event> eventSubscriber = new Subscriber<Event>() {
      @Override
      public void onCompleted() {

      }

      @Override
      public void onError(Throwable e) {

      }

      @Override
      public void onNext(Event event) {
        DeviceEventManagerModule.RCTDeviceEventEmitter emitter = reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
        if(event.status == Status.SUCCESS) {
          emitter.emit(eventName, params);
        }else {
          emitter.emit("encounteredError", event.object);
        }
        unsubscribe();
      }
    };
    CSUserSession.getInstance().getEventBus().getObservable(Path.USER, type).subscribe(eventSubscriber);
  }

  /**
   *      Consumers should be using this method to create a new session with their usageToken.
   *
   *      Note: The usage token is scoped to the Production and Sandbox. Make sure to use the production usage token when deploying
   *      your app to the Google Play Store.
   *
   */

  @ReactMethod
  public void createSessionWithUsageToken(String usageToken) {
    CSUserSession.init(this.getReactApplicationContext(), new TokenCurbsideCredentialProvider(usageToken));
    Log.e(REACT_CLASS, "Usage Token set to :"+usageToken);
  }

  /**
   *
   *      trackingIdentifier for the user who is logged into the device. This may be null when the app is started, but as the
   *      user logs into the app, make sure this value is set. trackingIdentifier needs to be set to use session specific methods for starting
   *      trips or monitoring sites. This identifier will be persisted across application restarts.
   *
   *      When the user logs out, set this to null, which will inturn end the user session or monitoring session.
   *
   *      The maximum length of the trackingIdentifier is 36 characters.
   *
   */
  @ReactMethod
  public void setTrackingIdentifier(String trackingIdentifier) {
    Log.e(REACT_CLASS,"trackingIdentifier set to :"+trackingIdentifier);
    WritableMap tid = Arguments.createMap();
    tid.putString("trackingIdentifier",trackingIdentifier);
    if(trackingIdentifier == null) {
      subscribe(Type.UNREGISTER_TRACKING_ID,"UNREGISTER_TRACKING_ID",tid);
      CSUserSession.getInstance().unregisterTrackingIdentifier();
    } else {
      subscribe(Type.REGISTER_TRACKING_ID,"REGISTER_TRACKING_ID",tid);
      CSUserSession.getInstance().registerTrackingIdentifier(trackingIdentifier);
    }
  }

  /**
   *      Start a trip tracking the user to the site identified by the siteID. Call this method when
   *      the application thinks its appropriate to start tracking the user eg. Order is ready to be picked up at
   *      the site. This information is persisted across relaunch.
   *
   *      Sends Event on EventBus at path User for event type START_TRIP and events Success and Failure. For Failure event
   *      To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void startTripToSiteWithIdentifier(String siteID, String trackToken) {
    Log.e(REACT_CLASS,"start trip with site Identifier :"+siteID+" & Track token :"+trackToken);
    WritableMap singleTrip = Arguments.createMap();
    singleTrip.putString("siteID",siteID);
    singleTrip.putString("trackToken",trackToken);
    subscribe(Type.START_TRIP,"START_TRIP",singleTrip);
    CSUserSession.getInstance().startTripToSiteWithIdentifier(siteID,trackToken);
  }

  /**
   *      Start a trip tracking the user to the site identified by the siteID for the user's intent of arrival at a given time or a time window.
   *      Call this method when the application thinks its appropriate to start tracking the user.
   *
   *      If the user's intent is to arrive at the site at a given time, then populate the fromDate with the arrival time. e.g. Pick up
   *      a order at 6.00pm. Set the fromDate to be 6.00pm
   *
   *      If the user's intent is to arrive at the site at a given time window, then populate the fromDate and toDate with the time window.
   *
   *      Sends Event on EventBus at path User for event type START_TRIP and events Success and Failure. For Failure event
   *      To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void startTripToSiteWithIdentifierWithPickupWindow(String siteID, String trackToken, DateTime fromDate, DateTime toDate) {
    Log.e(REACT_CLASS, "start trip with the site Identifier :"+siteID+" & Track token :"+trackToken+" & fromDate :"+fromDate+" & toDate :"+toDate);
    WritableMap pickupWindowTrip = Arguments.createMap();
    pickupWindowTrip.putString("siteID",siteID);
    pickupWindowTrip.putString("trackToken",trackToken);
    pickupWindowTrip.putString("fromDate",fromDate.toString());
    pickupWindowTrip.putString("toDate",toDate.toString());
    subscribe(Type.START_TRIP,"START_TRIP",pickupWindowTrip);
    CSUserSession.getInstance().startTripToSiteWithIdentifierAndETA(siteID, trackToken, fromDate, toDate);
  }

  /**
   *      Completes the trip for the user to the site identified by the siteID with the given trackToken.
   *       If no trackToken is specified, then *all*   trips to this site  will be completed.
   *
   *      Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
   *
   *      Sends Event on EventBus at path User for event type COMPLETE_TRIP and events Success and Failure.
   *      To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void completeTripToSiteWithIdentifier(String siteID, String trackToken) {
    Log.e(REACT_CLASS, "Complete trip with the site Identifier :"+siteID+" & Track token :"+trackToken);
    WritableMap singleTrip = Arguments.createMap();
    singleTrip.putString("siteID",siteID);
    singleTrip.putString("trackToken",trackToken);
    subscribe(Type.COMPLETE_TRIP,"COMPLETE_TRIP",singleTrip);
    CSUserSession.getInstance().completeTripToSiteWithIdentifier(siteID, trackToken);
  }

  /**
   *      Cancels the trip for the user to the given site identified by the siteID with the given trackToken.
   *      If no trackToken is set, then *all* trips to this site are cancelled.
   *
   *      Do not call this when the user logs out, instead set the trackingIdentifier to nil when the user logs out.
   *
   *      Sends Event on EventBus at path User for event type CANCEL_TRIP and events Success and Failure.
   *      To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void cancelTripToSiteWithIdentifier(String siteID, String trackToken) {
    Log.e(REACT_CLASS, "Cancel trip with the site Identifier :"+siteID+" & Track token :"+trackToken);
    WritableMap singleTrip = Arguments.createMap();
    singleTrip.putString("siteID",siteID);
    singleTrip.putString("trackToken",trackToken);
    subscribe(Type.CANCEL_TRIP,"CANCEL_TRIP",singleTrip);
    CSUserSession.getInstance().cancelTripToSiteWithIdentifier(siteID, trackToken);
  }

  /**
   *      This method would complete all trips for this user across all devices.
   *
   *      Do not call this when the user logs out, instead call unregisterTrackingIdentifier() when the user logs out.
   *
   *      Sends Event on EventBus at path User for event type COMPLETE_ALL_TRIPS and events Success and Failure.
   *      To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void completeAllTrips() {
    Log.e(REACT_CLASS, "Complete all trips");
    WritableMap allTrips = Arguments.createMap();
    allTrips.putString("trackToken",null);
    subscribe(Type.COMPLETE_ALL_TRIPS, "COMPLETE_ALL_TRIPS", allTrips);
    CSUserSession.getInstance().completeAllTrips();
  }

  /**
   *      This method will cancels all trips for all sites for the user.
   *
   *      sDo not call this when the user logs out, instead call unregisterTrackingIdentifier() when the user logs out.

   *      Sends Event on EventBus at path User for event type CANCEL_ALL_TRIPS and events Success and Failure.
   *      To get handle to EventBus call CSUserSession.getInstance().getEventBus()
   */
  @ReactMethod
  public void cancelAllTrips() {
    Log.e(REACT_CLASS, "Cancel all trips");
    WritableMap allTrips = Arguments.createMap();
    allTrips.putString("trackToken",null);
    subscribe(Type.CANCEL_ALL_TRIPS, "CANCEL_ALL_TRIPS", allTrips);
    CSUserSession.getInstance().cancelAllTrips();
  }

  /**
   *      Contact info - fullName, email address, sms number, Vehicle info - Make, Model & License Plate of the user.
   */
  @ReactMethod
  public void setUserInfo(@Nullable String fullName, @Nullable String email, @Nullable String smsNumber,
                          @Nullable String vehicleLicensePlate, @Nullable String vehicleMake, @Nullable String vehicleModel) {
    Log.e(REACT_CLASS,"Setting user info Name : "+fullName+" Email : "+email+" SMS Number : "+smsNumber
            +" License Plate : "+vehicleLicensePlate+" Make : "+vehicleMake+" Model : "+vehicleModel);
    final CSUserInfo csUserInfo = new CSUserInfo(fullName, email,smsNumber, vehicleLicensePlate, vehicleMake, vehicleModel);
    CSUserSession.getInstance().setUserInfo(csUserInfo);
  }
  /**
   *      Returns true if the current user is being tracked for a order pickup at a site.
   */
  @ReactMethod
  public void isTracking(Promise promise) {
    boolean isTracking= CSUserSession.getInstance().isTracking();
    Log.e(REACT_CLASS, "Is current user being  tracked for a order pickup at a site :"+isTracking);
    promise.resolve(isTracking);
  }

  @Override
  public String getName() {
    return "Curbside";
  }

  @Override
  public void onHostResume() {

  }

  @Override
  public void onHostPause() {
  }

  @Override
  public void onHostDestroy() {
  }
}
