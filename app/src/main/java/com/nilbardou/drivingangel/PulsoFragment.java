package com.nilbardou.drivingangel;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.ExecutionException;


public class PulsoFragment extends Fragment {


    String datapath = "/message_path2";
    String TAG = "Mobile PulsoFragment";

    Handler handler;
    private TextView pulso, fecha;
    private Context mContext;
    private Button iniciar, continuar;
    private boolean conectado, pulso_inicial;
    private String pulso_base, fecha_pulso_base;
    private VarGlobal varGlobal = new VarGlobal();
    private long tStart = 0, tStart2 = 0;
    int duration = Toast.LENGTH_SHORT;

    FirebaseAuth mAuth;
    DatabaseReference mDataBase;




    public PulsoFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View main = inflater.inflate(R.layout.fragment_pulso, container, false);

        mContext = container.getContext();

        pulso = (TextView) main.findViewById(R.id.bpm_unico);
        iniciar = (Button) main.findViewById(R.id.btn_incal);
        fecha = (TextView) main.findViewById(R.id.ultimo_bpm);

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference();


        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                logthis(stuff.getString("logthis"));
                return true;
            }
        });


        iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                conectado = false;
                pulso_inicial = false;
                String message = "Start pulso";
                //Requires a new thread to avoid blocking the UI
                new SendThread(datapath, message).start();


                IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
                MessageReceiver messageReceiver = new MessageReceiver();
                LocalBroadcastManager.getInstance(mContext).registerReceiver(messageReceiver, messageFilter);

                conectado = varGlobal.getConectado();
                tStart = varGlobal.gettStart();
                tStart2 = varGlobal.gettStart2();

                if ((tStart != 0) || (tStart2 != 0)){
                    Toast.makeText(mContext, "Acaba el trayecto antes de usar esta funcionalidad", duration).show();
                }else{

                    Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        public void run() {

                            if (conectado == false){
                                reloj_conectado();
                            }else{
                                CalibrandoFragment calibrando =  new CalibrandoFragment();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                transaction.replace(R.id.container, calibrando);
                                transaction.commit();
                            }
                        }
                    }, 1500);
                }


            }
        });


        obtenerPulsoBaseUsuario();

        return main;
    }


    private void reloj_conectado(){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog dialog;
        final View conectadoPopupView= getLayoutInflater().inflate(R.layout.popup_calibre_conect, null);

        continuar = (Button) conectadoPopupView.findViewById(R.id.button_aceptar);

        builder.setView(conectadoPopupView);
        dialog = builder.create();
        dialog.show();

        continuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });


    }

    private void obtenerPulsoBaseUsuario(){
        String id = mAuth.getCurrentUser().getUid();

        mDataBase.child("Usuarios").child(id).child("Pulso base").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String pulso_base = snapshot.child("pulso").getValue().toString();
                    String fecha_pulso_base = snapshot.child("fecha").getValue().toString();
                    pulso.setText(pulso_base);
                    fecha.setText("Última modificación:  "+ fecha_pulso_base);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.v(TAG, "Main activity received message: " + message);
            // Display message in UI
            logthis(message);

        }
    }

    public void logthis(String newinfo) {
        if (newinfo.compareTo("Hello device 2") == 0) {
            conectado = true;
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
                        Log.e(TAG, "Send Interrupt occurred: " + exception);
                    }

                }

            } catch (ExecutionException exception) {
                sendmessage("Node Task failed: " + exception);
                Log.e(TAG, "Node Task failed: " + exception);

            } catch (InterruptedException exception) {
                Log.e(TAG, "Node Interrupt occurred: " + exception);
            }

        }
    }



}