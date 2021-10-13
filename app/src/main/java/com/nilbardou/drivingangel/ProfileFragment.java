package com.nilbardou.drivingangel;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ProfileFragment extends Fragment {

    private ImageButton perfil, ritmo_card;
    private TextView usuario;
    private Button datos_personales, calibraje;

    FirebaseAuth mAuth;
    DatabaseReference mDataBase;



    public ProfileFragment() {

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View main = inflater.inflate(R.layout.fragment_profile, container, false);
        usuario = (TextView) main.findViewById(R.id.text_usuario);
        perfil = (ImageButton) main.findViewById(R.id.btn_perfil);
        ritmo_card = (ImageButton) main.findViewById(R.id.btn_ritmcard);
        datos_personales = (Button) main.findViewById(R.id.btn_datos_pers);
        calibraje = (Button) main.findViewById(R.id.btn_calib);

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference();


        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null){

                    FinAuto_Fragment finAuto_fragment =  new FinAuto_Fragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, finAuto_fragment);
                    transaction.commit();
                }else {

                    PerfilFragment perfil_fragment =  new PerfilFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, perfil_fragment);
                    transaction.commit();
                }
            }
        });

        datos_personales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null){

                    FinAuto_Fragment finAuto_fragment =  new FinAuto_Fragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, finAuto_fragment);
                    transaction.commit();
                }else {

                    PerfilFragment perfil_fragment =  new PerfilFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, perfil_fragment);
                    transaction.commit();
                }
            }
        });


        ritmo_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PulsoFragment pulso_fragment =  new PulsoFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, pulso_fragment);
                transaction.commit();


            }
        });
        calibraje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PulsoFragment pulso_fragment =  new PulsoFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, pulso_fragment);
                transaction.commit();


            }
        });



        return main;

    }
}