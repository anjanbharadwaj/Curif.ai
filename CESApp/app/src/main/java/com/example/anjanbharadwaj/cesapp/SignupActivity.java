package com.example.anjanbharadwaj.cesapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    TextView hasAccount;
    EditText name;
    EditText email;
    EditText password;
    Button signup;
    private FirebaseAuth mAuth;
    RadioGroup group;
    CheckBox checkBox;
    EditText phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();




        if (mAuth.getCurrentUser() != null) {
            //they've already signed into the app
        }
        hasAccount = (TextView)findViewById(R.id.hasAccountLabel);
        name = (EditText)findViewById(R.id.name_front);
        email = (EditText)findViewById(R.id.description_front);
        password = (EditText)findViewById(R.id.password);
        signup = (Button)findViewById(R.id.signupButton);
        group = (RadioGroup) findViewById(R.id.radioGroup);
        checkBox = (CheckBox) findViewById(R.id.public_checkbox);
        phone = (EditText)findViewById(R.id.phone);

        hasAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailtxt = email.getText().toString().trim();
                String passwordtxt = password.getText().toString().trim();
                final String nametxt = name.getText().toString().trim();
                final String phonetxt = phone.getText().toString();
                Log.v("Email",emailtxt);
                Log.v("Pswd",passwordtxt);
                Log.v("name", nametxt);
                if (emailtxt.isEmpty() || passwordtxt.isEmpty() || nametxt.isEmpty() || phonetxt.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.v("Running", "running");
                mAuth.createUserWithEmailAndPassword(emailtxt, passwordtxt)
                        // Add a listener that will run a method when the user has signed in.
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    Log.v("NameTxt", nametxt);
                                    ref.child("Users").child(uid).child("Name").setValue(nametxt);


                                    //check the user privacy settings and update these in our database
                                    boolean can_profile_searched = false;

                                    if(checkBox.isChecked()) {
                                        can_profile_searched = true;
                                    }

                                    DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

                                    database.child("Phone").setValue(phonetxt);
                                    database.child("DataControlSettings").child("is_profile_searchable").setValue(can_profile_searched);

                                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                    startActivity(intent);

                                } else {
                                    Toast.makeText(getApplicationContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });


    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update the current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
