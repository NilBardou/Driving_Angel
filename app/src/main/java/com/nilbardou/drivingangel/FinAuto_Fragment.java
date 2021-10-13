package com.nilbardou.drivingangel;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

enum ProviderType{
    BASIC
}


public class FinAuto_Fragment extends Fragment {

    public Button signOut;
    FirebaseAuth mAuth;
    DatabaseReference mDataBase;
    public TextView mail_text;

    int duration = Toast.LENGTH_SHORT;

    private Context mContext;

    public FinAuto_Fragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = container.getContext();

        View main = inflater.inflate(R.layout.fragment_fin_auto_, container, false);

        signOut = main.findViewById(R.id.signOut_btn);
        mail_text = main.findViewById(R.id.emailTXV);

        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.signOut();
                Toast.makeText(mContext, "Cerrar sesión", duration).show();
                PerfilFragment perfilFragment =  new PerfilFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, perfilFragment);
                transaction.commit();

            }
        });


        obtenerInfoUsuario();

        return main;
    }

    private void obtenerInfoUsuario(){
        String id = mAuth.getCurrentUser().getUid();

        mDataBase.child("Usuarios").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String mail = snapshot.child("email").getValue().toString();
                    mail_text.setText(mail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}