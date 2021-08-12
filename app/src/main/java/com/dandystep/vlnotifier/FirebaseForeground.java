package com.dandystep.vlnotifier;


import android.app.Notification;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseForeground extends FirebaseMessagingService {

    //to get foreground notifications
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setSmallIcon(R.drawable.vlnotifier_notification);
        notificationBuilder.setContentTitle(remoteMessage.getNotification().getTitle());
        notificationBuilder.setContentText(remoteMessage.getNotification().getBody());
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
