package com.bullest.waterkeeper;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.NotificationCompat;

import java.util.Random;

/**
 * Created by gang on 03/08/2017.
 */

class MyNotification {
    public static Notification build(Context context) {
        final String KEY_TEXT_REPLY = "key_text_reply";

        Intent notificationResponseIntent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationResponseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Dismiss Action
        Intent dismissIntent = new Intent(context, DismissReciever.class);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 0, dismissIntent, 0);
        NotificationCompat.Action dismissAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_clear_black_24dp, "Dismiss", dismissPendingIntent)
                        .build();

        // TODO: 7/25/17 Implement snoozeAction. How to make it snooze?
        Intent snoozeIntent = new Intent(context, SnoozeReciever.class);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 0, snoozeIntent, 0);
        NotificationCompat.Action snoozeAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_notifications_black_24dp, "Snooze", snoozePendingIntent)
                        .build();

        // TODO: Implement Direct reply; the flow of the digest the input is not finished
        Intent replyIntent = new Intent(context, ReplyReciever.class);
        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyIntent, 0);
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel(context.getString(R.string.notification_input_hint))
                .build();
        NotificationCompat.Action replyAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_assignment_black_24dp, "Water Log", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        String[] notificationMessageArray = context.getResources().getStringArray(R.array.notificaion_message);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Time to Drink")
                .setContentText(notificationMessageArray[new Random().nextInt(notificationMessageArray.length)])
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
//                .setColor(getResources().getColor(R.color.colorPrimary))
//                .addAction(replyAction)
                .addAction(dismissAction)
                .setSmallIcon(R.drawable.ic_local_drink_lime_600_24dp)
                .setVibrate(new long[] { 500, 300, 400, 500, 1000})
                .setStyle(new android.support.v4.app.NotificationCompat.InboxStyle())
                //        { delay, vibrate, sleep, vibrate, sleep } pattern
                .setLights(Color.YELLOW, 500, 2000)
                .setSound(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification_sound))
                .build();

        return notification;
    }
}
