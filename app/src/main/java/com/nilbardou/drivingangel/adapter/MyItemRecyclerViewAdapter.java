package com.nilbardou.drivingangel.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.transition.Hold;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nilbardou.drivingangel.HistorialFragment;
import com.nilbardou.drivingangel.MainActivity;
import com.nilbardou.drivingangel.PulsoFragment;
import com.nilbardou.drivingangel.R;
import com.nilbardou.drivingangel.models.Trayecto;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    Context mContext;
    List<Trayecto> trayecto_list;
    private List<Boolean> lista_borrado = new ArrayList<>();
    int duration = Toast.LENGTH_SHORT;
    Dialog mDialog;
    HistorialFragment historialFragment;
    int position, primero = 0;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String id_trayecto, titulo;
    private TextView dialog_trayecto, dialog_pulsoMed, dialog_pulsoMax, dialog_pulsoMin, dialog_cronoTray, dialog_cronoDesc,
            dialog_fecha;
    private EditText edit_trayecto;
    private Button borrar_btn, modificar_btn;

    Map<String, Integer> map = new HashMap<String, Integer>();



    public MyItemRecyclerViewAdapter(Context mContext, List<Trayecto> trayecto_list, HistorialFragment historialFragment) {
        this.mContext = mContext;
        this.trayecto_list = trayecto_list;
        this.historialFragment = historialFragment;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tray, fecha;
        private LinearLayout tray_item;
        private CheckBox checkBox;
        private View view;

        public ViewHolder(View itemView, HistorialFragment historialFragment) {
            super(itemView);

            tray = (TextView) itemView.findViewById(R.id.trayecto_txt);
            tray_item = (LinearLayout) itemView.findViewById(R.id.trayecto_item);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkBox_delete);
            fecha = (TextView) itemView.findViewById(R.id.fecha_trayecto);

            lista_borrado.add(0,false);


        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        final View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_historial, parent, false);


        final ViewHolder vHolder = new ViewHolder(view, historialFragment);



        mDialog = new Dialog(mContext);
        mDialog.setContentView(R.layout.trayecto);


        vHolder.tray_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                 dialog_trayecto = (TextView) mDialog.findViewById(R.id.tray_text);
                 edit_trayecto = (EditText) mDialog.findViewById(R.id.tray_edit);
                 dialog_pulsoMed = (TextView) mDialog.findViewById(R.id.pulso_medio);
                 dialog_pulsoMax = (TextView) mDialog.findViewById(R.id.pulso_max);
                 dialog_pulsoMin = (TextView) mDialog.findViewById(R.id.pulso_min);
                 dialog_cronoTray = (TextView) mDialog.findViewById(R.id.crono_tray);
                 dialog_cronoDesc = (TextView) mDialog.findViewById(R.id.crono_desc);
                 dialog_fecha = (TextView) mDialog.findViewById(R.id.data_try);
                 borrar_btn = (Button) mDialog.findViewById(R.id.borrar_try);
                 modificar_btn = (Button) mDialog.findViewById(R.id.modificar_try);

                dialog_trayecto.setVisibility(view.VISIBLE);
                edit_trayecto.setVisibility(view.GONE);
                modificar_btn.setText("Modificar");
                primero = 0;

                id_trayecto = trayecto_list.get(vHolder.getAdapterPosition()).getId_trayecto();
                dialog_trayecto.setText(trayecto_list.get(vHolder.getAdapterPosition()).getTrayecto());
                edit_trayecto.setText(trayecto_list.get(vHolder.getAdapterPosition()).getTrayecto());
                dialog_pulsoMed.setText(String.valueOf(trayecto_list.get(vHolder.getAdapterPosition()).getPulsoMedio()));
                dialog_pulsoMax.setText(String.valueOf(trayecto_list.get(vHolder.getAdapterPosition()).getPulsoMax()));
                dialog_pulsoMin.setText(String.valueOf(trayecto_list.get(vHolder.getAdapterPosition()).getPulsoMin()));
                dialog_cronoTray.setText(trayecto_list.get(vHolder.getAdapterPosition()).getCronoTrayecto());
                dialog_cronoDesc.setText(trayecto_list.get(vHolder.getAdapterPosition()).getCronoDescanso());
                dialog_fecha.setText(trayecto_list.get(vHolder.getAdapterPosition()).getFecha());

                
                mDialog.show();

                mDatabase = FirebaseDatabase.getInstance().getReference();
                mAuth = FirebaseAuth.getInstance();



                borrar_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String id = mAuth.getCurrentUser().getUid();

                        mDatabase.child("Usuarios").child(id).child("Trayectos").child(id_trayecto).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(mContext, "Borrado de la BD", duration).show();
                                }else {
                                    Toast.makeText(mContext, "No se ha podido borrar de la BD", duration).show();
                                }
                            }
                        });


                        trayecto_list.clear();
                        mDialog.dismiss();


                    }
                });

                modificar_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (primero == 0){
                            modificar_btn.setText("Guardar");
                            dialog_trayecto.setVisibility(view.GONE);
                            edit_trayecto.setVisibility(view.VISIBLE);
                            primero = 1;
                        }else{

                            final String id = mAuth.getCurrentUser().getUid();

                            titulo = edit_trayecto.getText().toString();

                            mDatabase.child("Usuarios").child(id).child("Trayectos").child(id_trayecto).child("título").setValue(titulo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(mContext, "Modificado", duration).show();
                                    }else {
                                        Toast.makeText(mContext, "No se ha podido modificar", duration).show();
                                    }
                                }
                            });
                            modificar_btn.setText("Modificar");

                            trayecto_list.clear();
                            mDialog.dismiss();
                        }

                    }
                });
            }
        });
        return vHolder;


    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.tray.setText(trayecto_list.get(position).getTrayecto());
        holder.fecha.setText(trayecto_list.get(position).getFecha());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean borrado = lista_borrado.get(position);
                if (borrado == true){

                    lista_borrado.set(position, false);
                }else{
                    lista_borrado.set(position, true);
                }


            }
        });

    }

    @Override
    public int getItemCount() {
        return trayecto_list.size();
    }


    public void setFilter(ArrayList<Trayecto> lista_trayecto){

        this.trayecto_list = new ArrayList<>();
        this.trayecto_list.addAll(lista_trayecto);
        notifyDataSetChanged();

    }

    public List getListaBorrado(){
        return  lista_borrado;
    }

    public void setTrayecto_list(List<Trayecto> trayecto_list) {
        this.trayecto_list = trayecto_list;
    }

    public List getLista(){
        return trayecto_list;
    }


    }

