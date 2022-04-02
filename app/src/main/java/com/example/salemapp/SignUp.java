package com.example.salemapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {

    //create object of DatabaseRefrences class to access firebase's realtime Database
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://measure-data-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        final EditText userNameTextField = findViewById(R.id.userNameTextField);
        final EditText emailTextField = findViewById(R.id.EmailTextField);
        final EditText pssswordTextField = findViewById(R.id.PssswordTextField);
        final EditText conformationPssswordTextField = findViewById(R.id.ConformationPssswordTextField);

        final Button registerBtn = findViewById(R.id.RegisterNewAccountButton);
        final Button loginNowBtn = findViewById(R.id.AlreadyHaveAccount);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
             public void onClick(View view) {
                // get data from EditTexts into String variables
                final String userNameTxt = userNameTextField.getText().toString();
                final String emailTxt =  emailTextField.getText().toString();
                final String passwordTxt = pssswordTextField.getText().toString();
                final String confrmationPasswordTxt = conformationPssswordTextField.getText().toString();

                // check if the user fill all the fields before sending data to firebase
                if (userNameTxt.isEmpty() || emailTxt.isEmpty() || passwordTxt.isEmpty() || confrmationPasswordTxt.isEmpty()){
                    Toast.makeText(SignUp.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                }

                // check if passwords are matching with each other
                else if(!passwordTxt.equals(confrmationPasswordTxt)){
                    Toast.makeText(SignUp.this, "Passwords are not matching", Toast.LENGTH_SHORT).show();
                }

            else{

                databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // Check the email is already registred
                        if (snapshot.hasChild(emailTxt)){
                            Toast.makeText(SignUp.this,"The email entered already registred",Toast.LENGTH_SHORT).show();
                        } else {
                            // sending data to firebase realtime database
                            databaseReference.child("Users").child(emailTxt).child("Fullname").setValue(userNameTxt);
                            databaseReference.child("Users").child(emailTxt).child("Email").setValue(emailTxt);
                            databaseReference.child("Users").child(emailTxt).child("Password").setValue(passwordTxt);

                            //show the successfull message if the user registerd
                            Toast.makeText(SignUp.this,"User registred successfully.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                         }
                     });

                }
            }
        });

        loginNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


}