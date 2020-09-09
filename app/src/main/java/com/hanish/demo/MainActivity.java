package com.hanish.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    EditText email,password;
    Button button,signout,signin;
    private FirebaseAuth mAuth;
    RelativeLayout auth;
    LinearLayout home;
    TextView forgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        button = findViewById(R.id.signup);
        mAuth = FirebaseAuth.getInstance();
        forgot = findViewById(R.id.forgot);
        auth = findViewById(R.id.auth);
        home = findViewById(R.id.home);
        signout = findViewById(R.id.signout);
        signin = findViewById(R.id.signin);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();

            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signInUser();

            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                email.setText("");
                password.setText("");
               updateUi(mAuth.getCurrentUser());
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!email.getText().toString().isEmpty()) {
                    sendResetPasswordEmail();
                }else
                {
                    Toast.makeText(MainActivity.this, "Please enter your email address",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendResetPasswordEmail()
    {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Password reset mail is sent.",
                                    Toast.LENGTH_SHORT).show();

                        } else {

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "There is no id registered with this email address.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean checkifEmailVerified() {

        return mAuth.getCurrentUser().isEmailVerified();
    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUi(currentUser);
    }

    private void updateUi(FirebaseUser currentUser) {
        if(currentUser == null)
        {
            // this means there is no user so a new user needs to be registered
            auth.setVisibility(View.VISIBLE);
            home.setVisibility(View.GONE);
        }
        else {
            //this means new user have verified himself by signing in again, so now user can be redirected to Home Page.
            // this is not a appropriate implementation. Just to avoid creating different activity or fragment for Sign Up and Sign In
            // I have used this method. If you want to use save method then you need to save isSignedIn and haveAccount booleans
            // in SharedPreferences.
            auth.setVisibility(View.GONE);
            home.setVisibility(View.VISIBLE);

        }

    }

    private void signInUser() {
        String mEmail = email.getText().toString().trim();
        String mPassword = password.getText().toString().trim();
        if (mEmail.isEmpty()) {
            email.setError("Email Required");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            email.setError("Valid Email Required");
            email.requestFocus();
            return;
        }
        if (mPassword.isEmpty() || password.length() < 6) {
            password.setError("Minimum 7 characters required");
            password.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(checkifEmailVerified()) {
                                Toast.makeText(MainActivity.this, "Signed In Successfully",
                                        Toast.LENGTH_SHORT).show();
                                updateUi(user);
                            }
                            else{
                                Toast.makeText(MainActivity.this, "Please Verify your email address first",
                                        Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                            }
                        } else {Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUi(null);
                        }

                        // ...
                    }
                });
    }

    private void registerNewUser(){
        String mEmail = email.getText().toString().trim();
        String mPassword = password.getText().toString().trim();
        if (mEmail.isEmpty()) {
            email.setError("Email Required");
            email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(mEmail).matches()) {
            email.setError("Valid Email Required");
            email.requestFocus();
            return;
        }
        if (mPassword.isEmpty() || password.length() < 6) {
            password.setError("Minimum 7 characters required");
            password.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            sendVerificationEmail();
                            email.setText("");
                            password.setText("");
                            Toast.makeText(MainActivity.this, "Verify your email",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUi(null);
                        }
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseAuth.getInstance().signOut();

                        } else {
                            // if email is not sent then you can show a button which will trigger this function again

                        }
                    }
                });
    }


}