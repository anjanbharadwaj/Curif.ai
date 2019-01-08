package com.android.anjansree.curifai;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    // Declare firebase authentication instance variable
    private FirebaseAuth mAuth;


    // Declare UI variables that we will later use to access the views/widgets based on their ids
    Button login;
    EditText emailField;
    EditText passwordField;
    TextView signup;
    Button getstarted;
    Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signup = (TextView)findViewById(R.id.noAccountLabel);

        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root_layout);

        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
        // Initialize FirebaseAuth instance
        login = (Button) findViewById(R.id.loginButton);
        logout = (Button) findViewById(R.id.logout);

        getstarted = (Button) findViewById(R.id.getstarted);
        getstarted.setVisibility(View.INVISIBLE);
        logout.setVisibility(View.INVISIBLE);
        emailField = (EditText) findViewById(R.id.description_front);
        passwordField = (EditText) findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            signup.setVisibility(View.INVISIBLE);
            emailField.setVisibility(View.INVISIBLE);
            passwordField.setVisibility(View.INVISIBLE);
            login.setVisibility(View.INVISIBLE);
            getstarted.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);
            //imageview.se
            getstarted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),HomePage.class);
                    startActivity(intent);
                }
            });
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    signup.setVisibility(View.VISIBLE);
                    emailField.setVisibility(View.VISIBLE);
                    passwordField.setVisibility(View.VISIBLE);
                    login.setVisibility(View.VISIBLE);
                    getstarted.setVisibility(View.INVISIBLE);
                    logout.setVisibility(View.INVISIBLE);
                }
            });
            //they've already signed into the app
        }else{

        }

        // Intialize the widgets/views by using their ids


        // Create an onClickListener for the text that leads to the signup page
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        // Create an onClickListener for login button. It will use Firebase Auth to sign in.
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please fill out both fields", Toast.LENGTH_LONG).show();
                    return;
                }

                //Call the Firebase signInWithEmailAndPassword method.
                mAuth.signInWithEmailAndPassword(email, password)
                        // Add a listener that will run a method when the user has signed in.
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(getApplicationContext(), HomePage.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                //  }
            }
        });




    }

    public void changelogo(int w, int h){
        ImageView imageview = (ImageView)findViewById(R.id.imageView2);
        ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.root_layout);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
    // Changes the height and width to the specified *pixels*
        params.height = h;
        params.width = w;
        layout.setLayoutParams(params);

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
