package io.flutter.plugins.firebasemessaging;

import android.content.Intent;

import com.google.firebase.messaging.RemoteMessage;

public class CustomFlutterFirebaseMessagingService extends FlutterFirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().containsKey("is_call")) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            startActivity(intent);
        } else
            super.onMessageReceived(remoteMessage);
    }
}
