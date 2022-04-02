package com.example.salemapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginPage extends AppCompatActivity {
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://measure-data-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        final TextInputEditText EmailLoginTextField = findViewById(R.id.EmailLoginTextField);
        final TextInputEditText PasswordTextField = findViewById(R.id.PasswordTextField);
        final Button Login = findViewById(R.id.Login);
        final Button AlreadyHaveAccount = findViewById(R.id.AlreadyHaveAccount);
        final Button ForgetPasswordButton = findViewById(R.id.ForgetPasswordButton);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 final String Email = EmailLoginTextField.getText().toString();
                 final String Password = PasswordTextField.getText().toString();

                 if (Email.isEmpty() || Password.isEmpty()){
                     Toast.makeText(LoginPage.this, "Please enter Your Name and Password", Toast.LENGTH_SHORT).show();
                 }
                 else{

                     databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                         @Override
                         public void onDataChange(@NonNull DataSnapshot snapshot) {

                             // check if the email is exist already
                             if (snapshot.hasChild(Email)){

                                 final String getPassword = snapshot.child(Email).child("Password").getValue(String.class);

                                 if (getPassword.equals(PasswordTextField)){
                                     Toast.makeText(LoginPage.this, "Successfull login",Toast.LENGTH_SHORT).show();

                                     // main activity on success
                                     startActivity(new Intent(LoginPage.this, MainActivity.class));
                                     finish();
                                 } else {
                                     Toast.makeText(LoginPage.this,"Wrong Password",Toast.LENGTH_SHORT).show();
                                 }
                             } else {
                                 Toast.makeText(LoginPage.this,"Wrong E-mail",Toast.LENGTH_SHORT).show();
                             }

                         }

                         @Override
                         public void onCancelled(@NonNull DatabaseError error) {

                         }
                     });

                 }
            }
        });

        AlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open the register activity
                startActivity(new Intent(LoginPage.this, SignUp.class));
            }
        });
    }
}