package com.example.trailerbuzz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exoplayer.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

    //Login Details
    private EditText mUsername;
    private EditText mLoginPassword;
    private Button mLoginButton;
    private TextView mRegisterText;
    private LinearProgressIndicator mProgressBar;
    private CheckBox mShowPassword;
    private TextView mForgotPassword;

    //Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mUsername = (EditText) findViewById(R.id.user_email);
        mLoginPassword = (EditText) findViewById(R.id.user_password);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mRegisterText = (TextView) findViewById(R.id.register_text);
        mProgressBar = (LinearProgressIndicator) findViewById(R.id.login_progress_bar);
        mShowPassword = (CheckBox) findViewById(R.id.password_checkbox);
        mForgotPassword = (TextView) findViewById(R.id.forget_password);

        //Show Pasword based on checkbox selection
        mShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    mLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        //Login the user in case the user exists
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginEmail = mUsername.getText().toString();
                String password = mLoginPassword.getText().toString();

                if(!TextUtils.isEmpty(loginEmail) || TextUtils.isEmpty(password)){
                    mProgressBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginEmail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendToVideosList();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Error"+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }
            }
        });

        //Direct to register activity
        mRegisterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


        //Handle Forget Password Functionality
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openForgotPasswordDialog();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null){
            sendToVideosList();
        }

    }

    //Dialog to handle forget password
    private void openForgotPasswordDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View promptsView = layoutInflater.inflate(R.layout.forgot_password_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                LoginActivity.this);
        alertDialogBuilder.setView(promptsView);

        EditText userInput = (EditText) promptsView.findViewById(R.id.password_reset_email);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(userInput.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(Color.parseColor("#000000"));

        negativeButton.setTextColor(Color.parseColor("#000000"));
        alertDialog.getWindow().setBackgroundDrawableResource(R.color.white);
    }


    public void sendToVideosList(){
        Intent intent = new Intent(LoginActivity.this, VideosListActivity.class);
        startActivity(intent);
        finish();
    }
}