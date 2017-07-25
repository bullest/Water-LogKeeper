package com.bullest.waterkeeper;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

/**
 * Created by yunfezhang on 7/25/17.
 */

public class ReplyReciever extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        int amount = 0;

        if (remoteInput != null) {
            amount = Integer.parseInt(remoteInput.getString("key_text_reply"));
            Log.d("WaterKeeper", "" + amount);
        }
        if (amount != 0){
            Calendar now = Calendar.getInstance();
            WaterRecord record = new WaterRecord(amount, now.getTimeInMillis());
            record.save();
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(999);

    }

}
