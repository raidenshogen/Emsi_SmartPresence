package com.example.emsismartpresence;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore  db;
    private String  userId;
    private EditText etEmail,etPassword,etConfirmPassword,etFullName;
    //get email and password value
    private TextView move;
    private Button btnregist;//get button value
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        //get the value of fields

        auth = FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
        etEmail=findViewById(R.id.EmailAddress);
        etPassword=findViewById(R.id.Password);
        btnregist=findViewById(R.id.btn_register);
        etConfirmPassword=findViewById(R.id.ConfirmPassword);
        etFullName=findViewById(R.id.FullName);
        move=findViewById(R.id.txt_login);
        btnregist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUser();
            }
        });


        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(Signup.this, Signin.class);
                startActivity(intent);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void store_user_firebase(String UID,String email,String name){
        Map<String,Object>users=new HashMap();
        users.put("user_email",email);
        users.put("user_inscription",new Timestamp(new Date()));
        users.put("user_name",name);
        db.collection("users").document(UID).set(users).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
              Toast.makeText(Signup.this,"user Stored",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Signup.this,"Storage Failed",Toast.LENGTH_LONG).show();

            }
        });


    }
    private void RegisterUser(){
        String Email=etEmail.getText().toString().trim();
        String Password=etPassword.getText().toString().trim();
        String ConfirmPassword=etConfirmPassword.getText().toString().trim();
        String FullName=etFullName.getText().toString().trim();
        if(Email.isEmpty()||Password.isEmpty()||FullName.isEmpty()||ConfirmPassword.isEmpty()){
            Toast.makeText(this,"Fill at least the email ,the password and fullname fields!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(ConfirmPassword.equals(Password)){
            auth.createUserWithEmailAndPassword(Email,Password)
                    .addOnCompleteListener(this,task->{
                        if (task.isSuccessful()){
                            Toast.makeText(Signup.this,"Registered Succesfully!",Toast.LENGTH_SHORT).show();
                            userId = auth.getCurrentUser().getUid();
                            store_user_firebase(userId,Email,FullName);
                            Intent in=new Intent(Signup.this,Signin.class);
                            in.putExtra("FullName",FullName);//envoyer le nom du redgister au sign in
                            startActivity(in);
                        }else {
                            Toast.makeText(Signup.this,"error"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });
        }else{
            Toast.makeText(Signup.this,"password mismatch",Toast.LENGTH_SHORT).show();

        }

    }

}