// Copyright 2020 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.firebase.messaging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class CustomFlutterFirebaseMessagingReceiver extends FlutterFirebaseMessagingReceiver {
  public static int CALL_NOTIFICATION_ID = 434125;

  @Override
  public void onReceive(Context context, Intent intent) {
    if (ContextHolder.getApplicationContext() == null) {
      ContextHolder.setApplicationContext(context.getApplicationContext());
    }
    RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
    /////// overrideeeeeee
    if (!FlutterFirebaseMessagingUtils.isApplicationForeground(context) && remoteMessage.getData().containsKey("is_call")) {
      String source_user_json = remoteMessage.getData().get("source_user");
      String caller_name = "CALLER";
      try {
        JSONObject o = new JSONObject(source_user_json);
        if (o.has("doctor") && !o.isNull("doctor")) {
          JSONObject doctor = o.getJSONObject("doctor");
          caller_name = doctor.getString("first_name") + " " + doctor.getString("last_name");
        } else if (o.has("client") && !o.isNull("client")) {
          JSONObject client = o.getJSONObject("client");
          caller_name = client.getString("first_name") + " " + client.getString("last_name");

        } else {
          caller_name = o.getString("name");
        }
      } catch (JSONException e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
      Intent contentIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
      contentIntent.putExtra("TYPE", "CONTENT");
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Intent answerIntent = new Intent(contentIntent);
        answerIntent.putExtra("TYPE", "ANSWER");
        Intent denyIntent = new Intent(contentIntent);
        denyIntent.putExtra("TYPE", "DENY");
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(context, 1, answerIntent, PendingIntent.FLAG_ONE_SHOT);
        PendingIntent denyPendingIntent = PendingIntent.getActivity(context, 2, denyIntent, PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setOngoing(true);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setContentIntent(contentPendingIntent);
        builder.setFullScreenIntent(contentPendingIntent, true);
        builder.setSmallIcon(android.R.drawable.ic_menu_call);
        builder.setContentTitle(caller_name + " يتصل بك");
        builder.setContentText("مكالمة صوتية");
        builder.setAutoCancel(true);


        // Use builder.addAction(..) to add buttons to answer or reject the call.
        Notification.Action acceptAction = new Notification.Action.Builder(Icon.createWithResource(context, android.R.drawable.ic_menu_call), "رد", answerPendingIntent).build();
        Notification.Action declineAction = new Notification.Action.Builder(Icon.createWithResource(context, android.R.drawable.ic_menu_close_clear_cancel), "رفض", denyPendingIntent).build();
        builder.addAction(acceptAction);
        builder.addAction(declineAction);
        NotificationManager notificationManager;
        notificationManager =
          (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          NotificationChannel channel = new NotificationChannel("callnotification", "مكالمات واردة", NotificationManager.IMPORTANCE_HIGH);
          Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
          channel.setSound(ringtoneUri, new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build());
          notificationManager.createNotificationChannel(channel);
          builder.setChannelId("callnotification");
        }
        Notification notification = builder.build();
        notification.flags = Notification.FLAG_INSISTENT;
        notificationManager.notify("callnotification", CALL_NOTIFICATION_ID, notification);
      } else {
        context.startActivity(contentIntent);
      }
    } else
      super.onReceive(context, intent);
  }
}
