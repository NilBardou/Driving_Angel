package com.nilbardou.drivingangel;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.ConnectionCallbacks;
import com.google.android.gms.common.api.internal.GoogleApiManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nilbardou.drivingangel.models.Trayecto;
import com.google.android.gms.wearable.Node;



import org.w3c.dom.NodeList;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.android.gms.wearable.Wearable.getMessageClient;
import static com.google.android.gms.wearable.Wearable.getNodeClient;


public class TrayectoFragment extends Fragment implements View.OnClickListener {

    String datapath = "/message_path";
    String datapath2 = "/stop_pulso";
    String TAG = "Mobile MainActivity";


    private TextView crono1, crono2, bpm, bpm2, pulso_min, pulso_max, pulso_med, crono_try, crono_desc, fecha;
    private boolean run, conectado;
    public ImageButton start, stop;
    Handler handler;
    long tMilisec, tStart, tStart2, tBuff, tBuff2, tUpdate = 0L;
    int sec,sec_des, min, min_des, milisec, milisec_des, hour, hour_des, identificador = 0, cont = 1,
            maximo, minimo, media, contador = 0, pulsoInt, pulso_unico, i = 0;
    List<Trayecto> lTrayecto;
    private String tmp_tray, tmp_desc, til_tray, pulso, currentDateTimeString, newinfo2, maximo_str, minimo_str, media_str,
            pulso_base, message;
    int duration = Toast.LENGTH_SHORT;
    private Context mContext;
    private EditText titulo_tray;
    private Button guardar_try, cancelar_try, si, no, siempre, ok;
    private PendingIntent pendingIntent,  pararPendingIntent, seguirPendingIntent;
    private final static String CHANNEL_ID = "NOTIFICATION";
    private final static int NOTIFICATION_ID = 0;
    private  VarGlobal varGlobal;


    FirebaseAuth mAuth;
    DatabaseReference mDataBase;


    public TrayectoFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lTrayecto = new ArrayList<>();
        NotificationManagerCompat notificationManager2 = NotificationManagerCompat.from(getContext());
        notificationManager2.cancel(NOTIFICATION_ID);

    }

    @Override
    public void onStart() {
        super.onStart();

        //conectado = ((VarGlobal)getActivity().getApplication()).getConectado();
        cont = ((VarGlobal)getActivity().getApplication()).getCont();

    }

    @Override
    public void onStop() {
        super.onStop();

        ((VarGlobal)getActivity().getApplication()).setConectado(conectado);
        ((VarGlobal)getActivity().getApplication()).setCont(cont);

        handler.removeCallbacks(cronometro1);
        handler.removeCallbacks(cronometro2);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View main = inflater.inflate(R.layout.fragment_trayecto, container, false);


        start = (ImageButton) main.findViewById(R.id.start_btn);
        stop = (ImageButton) main.findViewById(R.id.stop_btn);
        crono1 = (TextView) main.findViewById(R.id.crono1);
        crono2 = (TextView) main.findViewById(R.id.crono2);
        bpm = (TextView) main.findViewById(R.id.bpm);
        bpm2 = (TextView) main.findViewById(R.id.bpm2);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                logthis2(stuff.getString("logthis"));
                return true;
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference();

        mContext = container.getContext();

        obtenerPulsoBaseUsuario();

        tStart = ((VarGlobal)getActivity().getApplication()).gettStart();
        tStart2 = ((VarGlobal)getActivity().getApplication()).gettStart2();

        if ((tStart != 0) || (tStart2 != 0)){


            IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
            MessageReceiver messageReceiver = new MessageReceiver();
            LocalBroadcastManager.getInstance(mContext).registerReceiver(messageReceiver, messageFilter);

            handler.removeCallbacks(cronometro1);
            handler.removeCallbacks(cronometro2);

            run = ((VarGlobal)getActivity().getApplication()).getRun();

            if (run == true){

                conectado = ((VarGlobal)getActivity().getApplication()).getConectado();
                Handler handler2 = new Handler();
                handler2.postDelayed(new Runnable() {
                    public void run() {

                        if (conectado == false){
                            reloj_conectado();
                        }
                    }
                }, 1000);

                min_des = ((VarGlobal)getActivity().getApplication()).getMin_desc();
                sec_des = ((VarGlobal)getActivity().getApplication()).getSec_desc();
                hour_des = ((VarGlobal)getActivity().getApplication()).getHour_desc();

                if ((hour_des != 0) || (min_des != 0) || (sec_des != 0)){

                    if (hour == 0) {
                        crono2.setText(String.format("%02d", min_des) + ":" + String.format("%02d", sec_des));

                    }else{
                        crono2.setText(String.format("%02d", hour_des) + ":" + String.format("%02d", min_des));

                    }
                }

                tBuff = ((VarGlobal)getActivity().getApplication()).gettBuff();
                tBuff2 = ((VarGlobal)getActivity().getApplication()).gettBuff2();
                handler.postDelayed(cronometro1, 0);
                run = true;
                crono1.setTextColor(0xFF6200EE);
                crono2.setTextColor(0xFF989898);
                start.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));

            }else{

                min = ((VarGlobal)getActivity().getApplication()).getMin();
                sec = ((VarGlobal)getActivity().getApplication()).getSec();
                hour = ((VarGlobal)getActivity().getApplication()).getHour();
                conectado = ((VarGlobal)getActivity().getApplication()).getConectado();

                if ((hour != 0) || (min != 0) || (sec != 0)){

                    if (hour == 0) {
                        crono1.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));

                    }else{
                        crono1.setText(String.format("%02d", hour) + ":" + String.format("%02d", min));

                    }
                }

                tBuff = ((VarGlobal)getActivity().getApplication()).gettBuff();
                tBuff2 = ((VarGlobal)getActivity().getApplication()).gettBuff2();
                handler.postDelayed(cronometro2, 0);
                run = false;
                crono1.setTextColor(0xFF989898);
                crono2.setTextColor(0xFF6200EE);
                start.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
            }
            pulsoInt = ((VarGlobal)getActivity().getApplication()).getPulso();
            if (pulsoInt > 0){
                pulso = String.format("%02d", pulsoInt);
                bpm.setTextColor(0xFF6200EE);
                bpm2.setTextColor(0xFF6200EE);
                bpm2.setTextColor(0xFF6200EE);
                bpm.setText(pulso);
            }

        }


        start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!run) {

                    if (cont == 1) {
                        message = "Start pulso";
                        //Requires a new thread to avoid blocking the UI
                        new SendThread(datapath, message).start();

                        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
                        MessageReceiver messageReceiver = new MessageReceiver();
                        LocalBroadcastManager.getInstance(mContext).registerReceiver(messageReceiver, messageFilter);
                        cont = 0;
                    }

                   Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        public void run() {

                            if (conectado == false){
                                reloj_conectado();
                            }
                        }
                    }, 1000);

                    tBuff2 += tMilisec;
                    handler.removeCallbacks(cronometro2);
                    tStart = SystemClock.uptimeMillis();
                    ((VarGlobal)getActivity().getApplication()).settStart(tStart);
                    handler.postDelayed(cronometro1, 0);
                    run = true;
                    ((VarGlobal)getActivity().getApplication()).setRun(run);
                    crono1.setTextColor(0xFF6200EE);
                    crono2.setTextColor(0xFF989898);
                    start.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                    ((VarGlobal)getActivity().getApplication()).setSec_desc(sec_des);
                    ((VarGlobal)getActivity().getApplication()).setMin_desc(min_des);
                    ((VarGlobal)getActivity().getApplication()).setHour_desc(hour_des);
                    ((VarGlobal)getActivity().getApplication()).settBuff2(tBuff2);

                } else {
                    tBuff += tMilisec;
                    handler.removeCallbacks(cronometro1);
                    tStart2 = SystemClock.uptimeMillis();
                    ((VarGlobal)getActivity().getApplication()).settStart2(tStart2);
                    handler.postDelayed(cronometro2, 0);
                    run = false;
                    ((VarGlobal)getActivity().getApplication()).setRun(run);
                    crono1.setTextColor(0xFF989898);
                    crono2.setTextColor(0xFF6200EE);
                    start.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));

                    ((VarGlobal)getActivity().getApplication()).setSec(sec);
                    ((VarGlobal)getActivity().getApplication()).setMin(min);
                    ((VarGlobal)getActivity().getApplication()).setHour(hour);
                    ((VarGlobal)getActivity().getApplication()).settBuff(tBuff);
                }
            }

        });

        stop.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                message = "Stop pulso";
                new SendThread(datapath, message).start();

                handler.removeCallbacks(cronometro1);
                handler.removeCallbacks(cronometro2);

                tmp_desc = String.format("%02d", min_des) + ":" + String.format("%02d", sec_des);
                tmp_tray = String.format("%02d", min) + ":" + String.format("%02d", sec);

                tituloDialogo();

                tMilisec = 0L; tStart = 0L; tStart2 = 0L; tBuff = 0L; tBuff2 = 0L;tUpdate = 0L;
                sec = 0; min = 0; hour = 0; milisec = 0; sec_des = 0; min_des = 0; hour_des = 0; milisec_des = 0;
                cont = 1;

                crono1.setText("00:00");
                crono1.setTextColor(0xFF989898);
                crono2.setText("00:00");
                crono2.setTextColor(0xFF989898);
                bpm.setText("00");
                bpm.setTextColor(0xFF989898);
                bpm2.setTextColor(0xFF989898);
                run = false;
                start.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                conectado = false;
                ((VarGlobal)getActivity().getApplication()).settStart(tStart);
                ((VarGlobal)getActivity().getApplication()).settStart2(tStart2);
                ((VarGlobal)getActivity().getApplication()).setRun(run);
                ((VarGlobal)getActivity().getApplication()).setConectado(conectado);
                ((VarGlobal)getActivity().getApplication()).setMin(min);
                ((VarGlobal)getActivity().getApplication()).setSec(sec);
                ((VarGlobal)getActivity().getApplication()).setHour(hour);
                ((VarGlobal)getActivity().getApplication()).setMin_desc(min);
                ((VarGlobal)getActivity().getApplication()).setSec_desc(sec);
                ((VarGlobal)getActivity().getApplication()).setHour_desc(hour);
                ((VarGlobal)getActivity().getApplication()).settBuff(tBuff);
                ((VarGlobal)getActivity().getApplication()).settBuff2(tBuff2);
            }
        });

        return main;
    }


    private void reloj_conectado(){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog dialog;
        final View conectadoPopupView= getLayoutInflater().inflate(R.layout.pop_up_tray_conectado, null);

        si = (Button) conectadoPopupView.findViewById(R.id.button_si);
        no = (Button) conectadoPopupView.findViewById(R.id.button_no);
        siempre = (Button) conectadoPopupView.findViewById(R.id.button_siempre);

        builder.setView(conectadoPopupView);
        dialog = builder.create();
        dialog.show();

        si.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        siempre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectado = true;
                ((VarGlobal)getActivity().getApplication()).setConectado(conectado);
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                handler.removeCallbacks(cronometro1);
                handler.removeCallbacks(cronometro2);

                tMilisec = 0L; tStart = 0L; tStart2 = 0L; tBuff = 0L; tBuff2 = 0L;tUpdate = 0L;
                sec = 0; min = 0; hour = 0; milisec = 0; sec_des = 0; min_des = 0; hour_des = 0; milisec_des = 0;
                cont = 1;

                crono1.setText("00:00");
                crono1.setTextColor(0xFF989898);
                crono2.setText("00:00");
                crono2.setTextColor(0xFF989898);
                bpm.setText("00");
                bpm.setTextColor(0xFF989898);
                run = false;
                start.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                conectado = false;
                ((VarGlobal)getActivity().getApplication()).settStart(tStart);
                ((VarGlobal)getActivity().getApplication()).settStart2(tStart2);
                ((VarGlobal)getActivity().getApplication()).setRun(run);
                ((VarGlobal)getActivity().getApplication()).setConectado(conectado);
                ((VarGlobal)getActivity().getApplication()).setMin(min);
                ((VarGlobal)getActivity().getApplication()).setSec(sec);
                ((VarGlobal)getActivity().getApplication()).setHour(hour);
                ((VarGlobal)getActivity().getApplication()).setMin_desc(min);
                ((VarGlobal)getActivity().getApplication()).setSec_desc(sec);
                ((VarGlobal)getActivity().getApplication()).setHour_desc(hour);
                ((VarGlobal)getActivity().getApplication()).settBuff(tBuff);
                ((VarGlobal)getActivity().getApplication()).settBuff2(tBuff2);
                ((VarGlobal)getActivity().getApplication()).setMinimo(0);
                ((VarGlobal)getActivity().getApplication()).setMaximo(0);
                ((VarGlobal)getActivity().getApplication()).setMedia(0);
                ((VarGlobal)getActivity().getApplication()).setPulso(0);
                ((VarGlobal)getActivity().getApplication()).setContador(0);

                dialog.dismiss();
            }
        });
    }

    private void tituloDialogo() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog dialog;
        final View trayectoPopupView = getLayoutInflater().inflate(R.layout.popup_trayecto, null);

        titulo_tray = (EditText) trayectoPopupView.findViewById(R.id.titulo_tray);
        pulso_max = (TextView) trayectoPopupView.findViewById(R.id.pulso_max);
        pulso_min = (TextView) trayectoPopupView.findViewById(R.id.pulso_min);
        pulso_med = (TextView) trayectoPopupView.findViewById(R.id.pulso_medio);
        crono_try = (TextView) trayectoPopupView.findViewById(R.id.crono_tray);
        crono_desc = (TextView) trayectoPopupView.findViewById(R.id.crono_desc);
        fecha = (TextView) trayectoPopupView.findViewById(R.id.date_try);

        guardar_try = (Button) trayectoPopupView.findViewById(R.id.guardar_btn);
        cancelar_try = (Button) trayectoPopupView.findViewById(R.id.borrar_btn);

        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

        contador = ((VarGlobal)getActivity().getApplication()).getContador();

        if (contador != 0){
            media = ((VarGlobal)getActivity().getApplication()).getMedia();
            media = media /contador;

            maximo = ((VarGlobal)getActivity().getApplication()).getMaximo();
            minimo = ((VarGlobal)getActivity().getApplication()).getMinimo();

            maximo_str = String.format("%02d", maximo);
            minimo_str = String.format("%02d", minimo);
            media_str = String.format("%02d", media);
        }else {

            maximo_str = "00";
            minimo_str = "00";
            media_str = "00";
        }

        pulso_max.setText(maximo_str);
        pulso_min.setText(minimo_str);
        pulso_med.setText(media_str);
        crono_try.setText(tmp_tray);
        crono_desc.setText(tmp_desc);
        fecha.setText(currentDateTimeString);

        builder.setView(trayectoPopupView);
        dialog = builder.create();
        dialog.show();

        guardar_try.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = mAuth.getCurrentUser().getUid();

                til_tray = titulo_tray.getText().toString();

                Map<String, Object> map = new HashMap<>();
                map.put("título", til_tray);
                map.put("pulso", media_str);
                map.put("maximo", maximo_str);
                map.put("minimo", minimo_str);
                map.put("tiempo_trayecto",tmp_tray);
                map.put("tiempo_descanso",tmp_desc);
                map.put("fecha", currentDateTimeString);

                dialog.dismiss();

                mDataBase.child("Usuarios").child(id).child("Trayectos").push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(mContext, "Añadido al BD", duration).show();
                        }else {
                            Toast.makeText(mContext, "No se ha podido añadir a la BD", duration).show();
                        }
                    }
                });

                maximo_str = "00"; media_str = "00"; minimo_str = "00";


                ((VarGlobal)getActivity().getApplication()).setMaximo(0);
                ((VarGlobal)getActivity().getApplication()).setMinimo(0);
                ((VarGlobal)getActivity().getApplication()).setMedia(0);
                ((VarGlobal)getActivity().getApplication()).setContador(0);
            }
        });

        cancelar_try.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maximo_str = "00"; media_str = "00"; minimo_str = "00";
                dialog.dismiss();

                maximo = 0; minimo = 0; contador = 0; media = 0;

                ((VarGlobal)getActivity().getApplication()).setMaximo(0);
                ((VarGlobal)getActivity().getApplication()).setMinimo(0);
                ((VarGlobal)getActivity().getApplication()).setMedia(0);
                ((VarGlobal)getActivity().getApplication()).setContador(0);
            }
        });
    }


    @Override
    public void onClick(View v) {

    }

    public Runnable cronometro1 = new Runnable() {
        @Override
        public void run() {

            tMilisec = SystemClock.uptimeMillis() - tStart;
            tUpdate = tBuff + tMilisec;
            sec = (int) (tUpdate/1000);
            hour = (sec/3600)%24;
            min = (sec/60)%60;
            sec =  sec%60;
            milisec = (int) (tUpdate%100);

            if (hour == 0) {
                crono1.setText(String.format("%02d", min) + ":" + String.format("%02d", sec));

            }else{
                crono1.setText(String.format("%02d", hour) + ":" + String.format("%02d", min));

            }

            handler.postDelayed(this, 60);

        }
    };


    public Runnable cronometro2 = new Runnable() {
        @Override
        public void run() {

            tMilisec = SystemClock.uptimeMillis() - tStart2;
            tUpdate = tBuff2 + tMilisec;
            sec_des = (int) (tUpdate/1000);
            hour_des = (sec_des/3600)%24;
            min_des = (sec_des/60)%60;
            sec_des =  sec_des%60;
            milisec_des = (int) (tUpdate%100);

            if (hour == 0) {
                crono2.setText(String.format("%02d", min_des) + ":" + String.format("%02d", sec_des));

            }else{
                crono2.setText(String.format("%02d", hour_des) + ":" + String.format("%02d", min_des));

            }

            handler.postDelayed(this, 60);
        }
    };


    //setup a broadcast receiver to receive the messages from the wear device via the listenerService.
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
            conectado = true;

        }else {

            newinfo2 = newinfo.substring(0, 7);
            if (newinfo2.compareTo("pulso2:") == 0) {
                newinfo = newinfo.substring(7, newinfo.length());
                bpm.setText(newinfo);
                bpm.setTextColor(0xFF6200EE);
                bpm2.setTextColor(0xFF6200EE);
                pulsoInt = Integer.parseInt(newinfo);

            }
        }
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


    private void obtenerPulsoBaseUsuario(){
        String id = mAuth.getCurrentUser().getUid();

        mDataBase.child("Usuarios").child(id).child("Pulso base").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String pulso_base = snapshot.child("pulso").getValue().toString();
                    pulso_unico = Integer.parseInt(pulso_base);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    
}

