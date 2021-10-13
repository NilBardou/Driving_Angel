package com.nilbardou.drivingangel;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nilbardou.drivingangel.adapter.MyItemRecyclerViewAdapter;
import com.nilbardou.drivingangel.models.Trayecto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class HistorialFragment extends Fragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener, View.OnLongClickListener {

    public boolean isActionMode = false;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Toolbar toolbar;
    private int cont = 0;
    public int pos= 1;
    int duration = Toast.LENGTH_SHORT;
    private Context mContext;



    private TextView prueba;
    private RecyclerView myrecyclerview;
    private List<Trayecto> lsTrayecto = new ArrayList<>();
    private List<Trayecto> lista_selecTray = new ArrayList<>();
    private MyItemRecyclerViewAdapter recyclerViewAdapter;
    private HistorialFragment historialFragment;
    private CheckBox checkBox;


    public HistorialFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_historial_list, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mContext = container.getContext();

        myrecyclerview = (RecyclerView) view.findViewById(R.id.list_tray);
        //prueba = (TextView) view.findViewById(R.id.prueba);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox_delete);

        getTrayectoFromBD();
        myrecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));


        return view;
    }


    public void getTrayectoFromBD (){

        final String id = mAuth.getCurrentUser().getUid();


        mDatabase.child("Usuarios").child(id).child("Trayectos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    for (DataSnapshot ds : snapshot.getChildren()){
                        String id_trayecto = ds.getKey();
                        String titulo = ds.child("título").getValue().toString();
                        String pulso = ds.child("pulso").getValue().toString();
                        String tiempo_tray = ds.child("tiempo_trayecto").getValue().toString();
                        String tiempo_desc = ds.child("tiempo_descanso").getValue().toString();
                        String fecha = ds.child("fecha").getValue().toString();
                        String pulso_max = ds.child("maximo").getValue().toString();
                        String pulso_min = ds.child("minimo").getValue().toString();
                        lsTrayecto.add(new Trayecto(id_trayecto,titulo,tiempo_tray,tiempo_desc, pulso, pulso_min, pulso_max, fecha));
                    }

                    Collections.reverse(lsTrayecto);

                    recyclerViewAdapter = new MyItemRecyclerViewAdapter(getContext(),lsTrayecto, historialFragment);
                    myrecyclerview.setAdapter(recyclerViewAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater){

        inflater.inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.buscador);
        MenuItem deleteItem = menu.findItem(R.id.delete);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search");


        super.onCreateOptionsMenu(menu, inflater);

        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int cont = 0;
                int n =  lsTrayecto.size();
                int m = recyclerViewAdapter.getListaBorrado().size();


                for (int i = 0; i< (n); i++){
                    //if (recyclerViewAdapter.getListaBorrado().get(i).equals(true)){
                        String id_trayecto = lsTrayecto.get(i).getId_trayecto();

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
                    //}

                }


                //lsTrayecto.clear();
                //recyclerViewAdapter.setTrayecto_list(lsTrayecto);

                return false;
            }
        });

    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        recyclerViewAdapter.setFilter((ArrayList<Trayecto>) lsTrayecto);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        try {

            ArrayList<Trayecto> lista_filtrada = filter((ArrayList<Trayecto>) lsTrayecto,newText);
            recyclerViewAdapter.setFilter(lista_filtrada);


        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    private ArrayList<Trayecto> filter (ArrayList<Trayecto> trayectos , String texto){

        ArrayList<Trayecto> lista_filtrada= new ArrayList<>();

        try {

            texto =  texto.toLowerCase();

            for( Trayecto trayecto: trayectos){
                String tray = trayecto.getTrayecto().toLowerCase();

                if (tray.contains(texto)){
                    lista_filtrada.add(trayecto);

                }

            }

        }catch (Exception e){

            e.printStackTrace();
        }

        return lista_filtrada;
    }

    @Override
    public boolean onLongClick(View v) {

        isActionMode = true;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_delete);

        return true;
    }
/*
    public void MakeSelection(View v, int adapterPosition) {

        if (((CheckBox)v).isChecked()){
            lista_selecTray.add(lsTrayecto.get(adapterPosition));
            cont ++;
            updatecount();
        }else{
            lista_selecTray.remove(lsTrayecto.get(adapterPosition));
            cont --;
            updatecount();
        }


    }

    public void updatecount (){
        prueba.setText(String.format("%02d", cont));
    }*/
}