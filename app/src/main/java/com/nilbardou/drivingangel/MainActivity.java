package com.nilbardou.drivingangel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toolbar;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static androidx.core.content.ContextCompat.getSystemService;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDataBase;
    public boolean prueba = false;
    private PendingIntent pendingIntent,  pararPendingIntent;
    private final static String CHANNEL_ID = "NOTIFICATION";
    private final static int NOTIFICATION_ID = 0;
    String TAG = "Mobile MainActivity", newinfo2;
    Handler handler, handler_setValores;
    private int pulsaciones, media_pulsaciones, contador_pulsaciones, minimo_pulsaciones, maximo_pulsaciones;
    VarGlobal varGlobal = new VarGlobal();

    private Context mContext;

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView=findViewById(R.id.bottomNavigation);

       /* if(getIntent().getExtras() != null) {
            String orderNotification = getIntent().getStringExtra("seguir");

            if (orderNotification.equals("seguir"))
            {
                bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new MapsActivity()).commit();
            }


        }else{
            bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new TrayectoFragment()).commit();
        }*/

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new TrayectoFragment()).commit();

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference();

        Prueba();

        /*Handler handler_alerta = new Handler();
        handler_alerta.postDelayed(new Runnable() {
            public void run() {

                pendingIntent();
                pararPendingIntent();
                createNotificationChannel();
                createNotification();

                parar_trayecto();

            }
        }, 5000);*/


    }


    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationMethod=new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectfragment= null;
                    switch (menuItem.getItemId()){
                        case R.id.historial:
                            selectfragment = new HistorialFragment();
                            break;

                        case R.id.mapa:
                            selectfragment = new MapsActivity();
                            break;

                        case R.id.trayecto:
                            selectfragment = new TrayectoFragment();
                            break;

                        case R.id.perfil:

                            selectfragment = new ProfileFragment();
                            break;

                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.container,selectfragment).commit();


                    return true;
                }
            };




    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificacion";
            //String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            //channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = ( NotificationManager ) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification(){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_cor);
        builder.setContentTitle("Notificacion android");
        builder.setContentText("Deberias parar");
        builder.setColor(Color.BLUE);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);
        builder.setVibrate(new long[]{1000,1000,1000});
        builder.setDefaults(Notification.DEFAULT_SOUND);
        builder.addAction(R.drawable.ic_cor, "Parar", pararPendingIntent);
        builder.setContentIntent(pendingIntent).setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }



    private void pendingIntent(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //intent.putExtra("seguir", "seguir");
    }

    private void pararPendingIntent(){
        Intent intent2 = new Intent(this, MainActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pararPendingIntent = PendingIntent.getActivity(this, 0, intent2, 0);
        //intent2.putExtra("parar", "parar");
    }

    public void Prueba(){

        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MainActivity.MessageReceiver messageReceiver = new MainActivity.MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
    }





    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v(TAG, "Main activity received message: " + message);
            // Display message in UI
            logthis2(message);
        }
    }

    public void logthis2(String newinfo) {

        if (newinfo.compareTo("Hello device") == 0) {
            media_pulsaciones = 0;
            contador_pulsaciones = 0;
            maximo_pulsaciones = 0;
            minimo_pulsaciones = 0;
            pulsaciones = 0;

        }else {

            newinfo2 = newinfo.substring(0, 7);
            if (newinfo2.compareTo("pulso2:") == 0) {
                newinfo = newinfo.substring(7, newinfo.length());
                pulsaciones = Integer.parseInt(newinfo);

                media_pulsaciones = media_pulsaciones + pulsaciones;
                contador_pulsaciones = contador_pulsaciones + 1;
                varGlobal.setContador(contador_pulsaciones);
                varGlobal.setMedia(media_pulsaciones);
                varGlobal.setPulso(pulsaciones);

                if (pulsaciones > maximo_pulsaciones || contador_pulsaciones == 1) {
                    maximo_pulsaciones = pulsaciones;
                    varGlobal.setMaximo(maximo_pulsaciones);

                }
                if (pulsaciones < minimo_pulsaciones || contador_pulsaciones == 1) {
                    minimo_pulsaciones = pulsaciones;
                    varGlobal.setMinimo(minimo_pulsaciones);

                }

                if (pulsaciones > 85){

                    pendingIntent();
                    pararPendingIntent();
                    createNotificationChannel();
                    createNotification();

                    parar_trayecto();

                }
            }
        }

    }

    private void parar_trayecto(){

        AlertDialog.Builder builder_parar = new AlertDialog.Builder(this);
        final AlertDialog dialog_parar;
        final View conectadoPopupView= getLayoutInflater().inflate(R.layout.pop_up_parar, null);

        builder_parar.setView(conectadoPopupView);
        dialog_parar = builder_parar.create();
        dialog_parar.show();


        Handler handler_alerta = new Handler();
        handler_alerta.postDelayed(new Runnable() {
            public void run() {

                dialog_parar.dismiss();
            }
        }, 5000);
    }



    public void sendmessage(String logthis) {
        Bundle b = new Bundle();
        b.putString("logthis", logthis);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;
        msg.what = 1; //so the empty message is not used!
        handler.sendMessage(msg);
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
        //since there is (should only?) be one, no problem.
        public void run() {

            //first get all the nodes, ie connected wearable devices.
            Task<List<Node>> nodeListTask =
                    Wearable.getNodeClient(mContext.getApplicationContext()).getConnectedNodes();
            try {
                // Block on a task and get the result synchronously (because this is on a background
                // thread).
                List<Node> nodes = Tasks.await(nodeListTask);

                //Now send the message to each device.
                for (Node node : nodes) {
                    Task<Integer> sendMessageTask =
                            Wearable.getMessageClient(mContext).sendMessage(node.getId(), path, message.getBytes());

                    try {
                        // Block on a task and get the result synchronously (because this is on a background
                        // thread).
                        Integer result = Tasks.await(sendMessageTask);
                        Log.v(TAG, "SendThread: message send to " + node.getDisplayName());

                    } catch (ExecutionException exception) {
                        sendmessage("failed to" + node.getDisplayName());
                        Log.e(TAG, "Send Task failed: " + exception);

                    } catch (InterruptedException exception) {
                        sendmessage("failed to" + node.getDisplayName());
                        Log.e(TAG, "Send Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                sendmessage("Node Task failed: " + exception);
                Log.e(TAG, "Node Task failed: " + exception);

            } catch (InterruptedException exception) {
                sendmessage("Node Task failed: " + exception);
                Log.e(TAG, "Node Interrupt occurred: " + exception);
            }

        }
    }



}