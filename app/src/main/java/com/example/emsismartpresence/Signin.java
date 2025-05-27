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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class Signin extends AppCompatActivity {

     private EditText etEmail,etPassword;//get email and password value
    private TextView move;
    private FirebaseAuth auth;
     private Button btnlogin;//get button value
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);
        //get the value of fields

        auth=FirebaseAuth.getInstance();
        etEmail=findViewById(R.id.EmailAddress);
        etPassword=findViewById(R.id.Password);
        btnlogin=findViewById(R.id.btn_register);
        move=findViewById(R.id.txt_register);
        btnlogin.setOnClickListener(v->authenticateUser());


        move.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Signin.this, Signup.class);
                startActivity(intent);
            }
        } );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.Signin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void authenticateUser(){
        String Email=etEmail.getText().toString().trim();
        String Password=etPassword.getText().toString().trim();

        if(Email.isEmpty()||Password.isEmpty()){
            Toast.makeText(Signin.this,"Fill the email the password fields!",Toast.LENGTH_SHORT).show();
            return;
        }
        auth.signInWithEmailAndPassword(Email,Password)
                .addOnCompleteListener(this,task->{
                    if (task.isSuccessful()){
                        Toast.makeText(Signin.this,"Authenticated Succesfully!",Toast.LENGTH_SHORT).show();
                        Intent in=new Intent(Signin.this,Home.class);
                        startActivity(in);

                    }else {
                        Toast.makeText(Signin.this,"error"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();

                    }

                });

    }
}