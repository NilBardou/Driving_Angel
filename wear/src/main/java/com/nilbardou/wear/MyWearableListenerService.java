package com.nilbardou.wear;

import android.app.VoiceInteractor;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class MyWearableListenerService extends WearableListenerService {
    String TAG = "wear listener";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if ((messageEvent.getPath().equals("/message_path"))|| (messageEvent.getPath().equals("/message_path2"))) {
            final String message = new String(messageEvent.getData());
            Log.v(TAG, "Message path received is: " + messageEvent.getPath());
            Log.v(TAG, "Message received is: " + message);


            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
            Log.v(TAG, "Message not received: ");
        }
    }

}
