package com.nilbardou.wear;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.wear.widget.BoxInsetLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;


import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static com.google.android.gms.wearable.Wearable.getMessageClient;
import static com.google.android.gms.wearable.Wearable.getNodeClient;

public class MainActivity extends WearableActivity implements MessageClient.OnMessageReceivedListener {

    private int pulso_inicial;
    private String pulso_inicial_str;
    private boolean stop = false;


    Handler handler3;


    private final static String TAG = "Wear MainActivity";
    String datapath = "/message_path";
    String datapath2 = "/message_path2";
    public MessageEvent messageEvent2;

    private TextView mTextView, pulso_txt;
    private ImageButton btnStart;
    private ImageButton btnPause;
    private Drawable imgStart;
    private SensorManager mSensorManager;
    private Sensor mHeartRateSensor;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);
        pulso_txt = (TextView) findViewById(R.id.heartRateText);
        btnStart = (ImageButton) findViewById(R.id.btnStart);
        btnPause = (ImageButton) findViewById(R.id.btnPause);



        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        /*
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        btnStart.setVisibility(ImageButton.GONE);
                        btnPause.setVisibility(ImageButton.VISIBLE);
                        pulso_txt.setText("Please wait...");
                        startMeasure();
                    }
                });

                btnPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        btnPause.setVisibility(ImageButton.GONE);
                        btnStart.setVisibility(ImageButton.VISIBLE);
                        pulso_txt.setText("--");
                        stopMeasure();
                    }
                });


        setAmbientEnabled();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        */

        setAmbientEnabled();






    }

    public void onResume() {
        super.onResume();
        getMessageClient(this).addListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getMessageClient(this).removeListener(this);
    }


    @Override
    public void onMessageReceived(@NonNull final MessageEvent messageEvent) {

        messageEvent2 = messageEvent;

        Log.d(TAG, "onMessageReceived() A message from watch was received:"
                + messageEvent.getRequestId() + " " + messageEvent.getPath());
        if (messageEvent.getPath().equals("/message_path")) {  //don't think this if is necessary anymore.
            String message =new String(messageEvent.getData());
            Log.v(TAG, "Wear activity received message: " + message);
            // Display message in UI
            pulso_txt.setText(message);

            if (message.equals("Stop pulso")){
                stop = true;
            }else{
                stop = false;
            }
            //here, send a message back.
            message = "Hello device";
            //Requires a new thread to avoid blocking the UI
            new SendThread(datapath, message).start();


            pulso_inicial = 0;



            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {

                    if (stop == false){

                        pulso_inicial = pulso_inicial();
                        pulso_inicial_str = String.format("%02d", pulso_inicial);
                        pulso_txt.setText(pulso_inicial_str);
                        pulso_inicial_str = "pulso2:" + pulso_inicial_str;
                        enviar_pulso(pulso_inicial_str, messageEvent);
                        handler.postDelayed(this, 15000);

                    }else {
                        handler.removeCallbacks(this);

                    }

                }
            }, 15000);


        } else if (messageEvent.getPath().equals("/message_path2")) {  //don't think this if is necessary anymore.
            String message =new String(messageEvent.getData());
            Log.v(TAG, "Wear activity received message: " + message);
            // Display message in UI
            pulso_txt.setText(message);
            //here, send a message back.
            message = "Hello device 2";
            //Requires a new thread to avoid blocking the UI
            new SendThread(datapath, message).start();

            pulso_inicial = 0;


            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    pulso_inicial = pulso_inicial();
                    pulso_inicial_str = String.format("%02d", pulso_inicial);
                    pulso_txt.setText(pulso_inicial_str);
                    pulso_inicial_str = "pulso:" + pulso_inicial_str;
                    enviar_pulso(pulso_inicial_str,messageEvent);
                }
            }, 15000);

        }

        }



    public int pulso_inicial (){

        final int random = new Random().nextInt(41) + 60;
        return random;

    }



    public void enviar_pulso(String pulso_inicial, MessageEvent messageEvent){

        String message =new String(messageEvent.getData());
        Log.v(TAG, "Wear activity received message: " + message);
        // Display message in UI
        //here, send a message back.
        message = pulso_inicial;
        //Requires a new thread to avoid blocking the UI
        new SendThread(datapath, message).start();
    }




    class SendThread extends Thread {
        String path;
        String message;

        //constructor
        SendThread(String p, String msg) {
            path = p;
            message = msg;
        }

        //sends the message via the thread.  this will send to all wearables connected, but
        //since there is (should only?) be one, so no problem.
        public void run() {
            //first get all the nodes, ie connected wearable devices.
            Task<List<Node>> nodeListTask =
                    getNodeClient(getApplicationContext()).getConnectedNodes();
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                List<Node> nodes = Tasks.await(nodeListTask);

                //Now send the message to each device.
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            getMessageClient(MainActivity.this).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        Integer result = Tasks.await(sendMessageTask);
                        Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                    } catch (ExecutionException exception) {
                        Log.e(TAG, "Task failed: " + exception);

                    } catch (InterruptedException exception) {
                        Log.e(TAG, "Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                Log.e(TAG, "Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Interrupt occurred: " + exception);
            }
        }
    }



    /*
    @Override
    public void onSensorChanged(SensorEvent event) {

        float mHeartRateFloat = event.values[0];

        int mHeartRate = Math.round(mHeartRateFloat);

        pulso_txt.setText(Integer.toString(mHeartRate));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void startMeasure() {
        boolean sensorRegistered = mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);
        Log.d("Sensor Status:", " Sensor registered: " + (sensorRegistered ? "yes" : "no"));
    }

    private void stopMeasure() {
        mSensorManager.unregisterListener(this);
    }*/

}


