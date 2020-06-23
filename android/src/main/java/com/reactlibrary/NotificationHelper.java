package com.reactlibrary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.annotations.VisibleForTesting;

public class NotificationHelper {

  @VisibleForTesting
  private static final String REACT_CLASS = "NotificationHelper";
  private static NotificationHelper instance = null;
  private NotificationManager mNotificationManager;
  private Context context;

  private NotificationHelper(Context context) {
    mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    this.context = context;
  }

  public static synchronized NotificationHelper getInstance(Context context) {
    if (instance == null) {
      instance = new NotificationHelper(context);
    }
    return instance;
  }

  /**
   * Creates notification channel with notification object for clients on Android OS 8.0+
   * Required key fields in config : id, name, title, text, and icon
   * Returns notification object
   *
   * @param notificationConfig
   * @param promise
   */
  Notification createNotification(ReadableMap notificationConfig, Promise promise) {
    if (notificationConfig == null) {
      Log.e(REACT_CLASS, "createNotificationChannel: invalid config");
      promise.reject("ERROR_INVALID_CONFIG", "Channel config is invalid");
      return null;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (!notificationConfig.hasKey("id")) {
        promise.reject("ERROR_INVALID_CONFIG", "Channel `id` field is required");
        return null;
      }
      String channelId = notificationConfig.getString("id");
      if (!notificationConfig.hasKey("name")) {
        promise.reject("ERROR_INVALID_CONFIG", "Channel `name` field is required");
        return null;
      }
      String channelName = notificationConfig.getString("name");
      int channelImportance = notificationConfig.hasKey("importance") ?
              notificationConfig.getInt("importance") : NotificationManager.IMPORTANCE_HIGH;

      if (channelId == null || channelName == null) {
        promise.reject("ERROR_INVALID_CONFIG", "Channel `id` or `name` is not specified");
        return null;
      }

      String notificationTitle = notificationConfig.hasKey("title") ?
              notificationConfig.getString("title") : "Curbside";

      String notificationText = notificationConfig.hasKey("text") ?
              notificationConfig.getString("text") : "Just pull into the store. We will have your order ready!";

      String notificationSmallIcon = notificationConfig.hasKey("icon") ?
              notificationConfig.getString("icon") : "ic_notification";

      String notificationLargeIcon = notificationConfig.hasKey("largeIcon") ?
              notificationConfig.getString("largeIcon") : "ic_launcher";

      int largeIconResId = getResourceIdForResourceName(this.context, notificationLargeIcon);
      Bitmap largeIconBitmap = BitmapFactory.decodeResource(this.context.getResources(), largeIconResId);

      NotificationChannel channel = new NotificationChannel(channelId, channelName, channelImportance);
      mNotificationManager.createNotificationChannel(channel);

      Notification.Builder mNotificationBuilder = new Notification.Builder(this.context, channelId)
              .setContentTitle(notificationTitle)
              .setContentText(notificationText)
              .setSmallIcon(getResourceIdForResourceName(this.context, notificationSmallIcon))
              .setOngoing(true);

      if (largeIconResId != 0) {
        mNotificationBuilder.setLargeIcon(largeIconBitmap);
      }
      Notification mNotification = mNotificationBuilder.build();
      promise.resolve(mNotification);
      return mNotification;
    } else {
      promise.reject("ERROR_ANDROID_VERSION", "Notification channel can be created on Android O+");
    }
    return null;
  }

  private int getResourceIdForResourceName(Context context, String resourceName) {
    int resourceId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    if (resourceId == 0) {
      resourceId = context.getResources().getIdentifier(resourceName, "mipmap", context.getPackageName());
    }
    return resourceId;
  }

}
