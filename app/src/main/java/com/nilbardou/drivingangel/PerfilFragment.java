package com.nilbardou.drivingangel;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nilbardou.drivingangel.ui.home.HomeFragment;

import java.security.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;


public class PerfilFragment extends Fragment  {

    public Button singUp, logIn, restePswd;
    public EditText email, password;
    private String mail, paswd;
    int duration = Toast.LENGTH_SHORT;


    FirebaseAuth mAuth;
    DatabaseReference mDataBase;

    private Context mContext;
    private ProgressDialog mDialog;

    public PerfilFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = container.getContext();

        View main = inflater.inflate(R.layout.fragment_perfil, container, false);

        singUp = main.findViewById(R.id.signUp_btn);
        logIn = main.findViewById(R.id.logIn_btn);
        restePswd = main.findViewById(R.id.rest_con_btn);
        email = main.findViewById(R.id.email);
        password = main.findViewById(R.id.password);


        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference();
        mDialog = new ProgressDialog(mContext);


        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mail = email.getText().toString();
                paswd = password.getText().toString();

                if (!mail.isEmpty() && !paswd.isEmpty()){

                    logInUsuario();

                }else{

                    Toast.makeText(mContext, "Campos vacios", duration).show();
                }
                }
        });

        singUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mail = email.getText().toString();
                paswd = password.getText().toString();


                if (!mail.isEmpty() && !paswd.isEmpty()){

                    if(paswd.length() <= 6){
                        Toast.makeText(mContext, "La contraseña debe tener más de seis carácteres", duration).show();
                    }else {
                        registrarUsuario();
                    }
                }else{
                    Toast.makeText(mContext, "No ha completado los campos", duration).show();
                }

            }
        });


        restePswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mail = email.getText().toString();

                if (!mail.isEmpty()){
                    mDialog.setMessage("Espera un momento...");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();
                    resetearContraseña();
                }else{
                    Toast.makeText(mContext, "Ingresa el email", duration).show();
                }
            }
        });

        return main;
    }

    private void registrarUsuario(){
        mAuth.createUserWithEmailAndPassword(mail, paswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    Map<String, Object> map = new HashMap<>();
                    map.put("email", mail);
                    map.put("paswword", paswd);

                    String id = mAuth.getCurrentUser().getUid();

                    mDataBase.child("Usuarios").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if (task2.isSuccessful()){

                                Toast.makeText(mContext, "Registro", duration).show();
                                FinAuto_Fragment finAuto_fragment =  new FinAuto_Fragment();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                transaction.replace(R.id.container, finAuto_fragment);
                                transaction.commit();

                            }else{
                                Toast.makeText(mContext, "No se ha podido registrar este usuario", duration).show();
                            }
                        }
                    });

                }else{
                    Toast.makeText(mContext, "No se ha podido registrar este usuario", duration).show();
                }

            }
        });
    }

    public void logInUsuario(){
        mAuth.signInWithEmailAndPassword(mail,paswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task3) {
                if (task3.isSuccessful()){

                    Toast.makeText(mContext, "Se ha iniciado la sesión", duration).show();

                    FinAuto_Fragment finAuto_fragment =  new FinAuto_Fragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, finAuto_fragment);
                    transaction.commit();

                }else{
                    Toast.makeText(mContext, "No se ha podido iniciar la sesión", duration).show();
                }
            }
        });
    }


    private void resetearContraseña(){

        mAuth.setLanguageCode("es");
        mAuth.sendPasswordResetEmail(mail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task4) {

                if (task4.isSuccessful()){
                    Toast.makeText(mContext, "Se pudo enviar el correo para restablecer la contraseña", duration).show();

                }else{
                    Toast.makeText(mContext, "No se pudo enviar el correo para restablecer la contraseña", duration).show();

                }

                mDialog.dismiss();
            }
        });

    }

}