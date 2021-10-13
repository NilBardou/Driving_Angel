package com.nilbardou.drivingangel;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class CalibrandoFragment extends Fragment {

    private ImageView corazon, ritmo1, ritmo2;
    Handler handler;
    private Handler handler_rtm = new Handler();
    private Handler handler_cor = new Handler();
    private ObjectAnimator animatorX, animatorscale;
    private long animationDuration = 10000;
    private AnimatorSet animatorSet;
    private Context mContext;
    private String newinfo2, currentDateTimeString;
    private Integer pulso;
    int duration = Toast.LENGTH_SHORT;

    String TAG = "Mobile CalibrandoFragment";

    FirebaseAuth mAuth;
    DatabaseReference mDataBase;


    public CalibrandoFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View main = inflater.inflate(R.layout.fragment_calibrando, container, false);

        corazon = (ImageView) main.findViewById(R.id.ic_cor);
        ritmo1 = (ImageView) main.findViewById(R.id.ic_ritmo_card1);

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference();

        mContext = container.getContext();



        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle stuff = msg.getData();
                logthis2(stuff.getString("logthis"));
                return true;
            }
        });

        handler_rtm.postDelayed(ritmo_card, 0);
        handler_cor.postDelayed(ritmo_cor, 0);



        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        CalibrandoFragment.MessageReceiver messageReceiver = new CalibrandoFragment.MessageReceiver();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(messageReceiver, messageFilter);


        return main;
    }

    private void animacion_cor(){

        Animation animationScale = AnimationUtils.loadAnimation(mContext, R.anim.scale);
        corazon.startAnimation(animationScale);

    }

    private void animacion_pulso(){

        Animation animationLateral = AnimationUtils.loadAnimation(mContext, R.anim.lateral);
        ritmo1.startAnimation(animationLateral);

    }

    public Runnable ritmo_card = new Runnable() {
        @Override
        public void run() {


            animacion_pulso();

            handler_rtm.postDelayed(this, 5000);

        }
    };

    public Runnable ritmo_cor = new Runnable() {
        @Override
        public void run() {

            animacion_cor();

            handler_cor.postDelayed(this, 1000);

        }
    };

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
        newinfo2 = newinfo.substring(0,6);
        if (newinfo2.compareTo("pulso:") == 0) {
            newinfo = newinfo.substring(6, newinfo.length());
            pulso = Integer.parseInt(newinfo);

            currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

            Map<String, Object> map = new HashMap<>();
            map.put("pulso", pulso);
            map.put("fecha", currentDateTimeString.substring(0,currentDateTimeString.length()-9));

            String id = mAuth.getCurrentUser().getUid();

            mDataBase.child("Usuarios").child(id).child("Pulso base").setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(mContext, "Añadido al BD", duration).show();
                    }else {
                        Toast.makeText(mContext, "No se ha podido añadir a la BD", duration).show();
                    }
                }
            });


            PulsoFragment pulsoFragment =  new PulsoFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, pulsoFragment);
            transaction.commit();
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


}