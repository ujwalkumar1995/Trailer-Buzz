package com.example.trailerbuzz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

    private EditText mUsername;
    private EditText mLoginPassword;
    private Button mLoginButton;
    private TextView mRegisterText;
    private FirebaseAuth mAuth;
    private LinearProgressIndicator mProgressBar;
    private CheckBox mShowPassword;

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
        mRegisterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
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

    public void sendToVideosList(){
        Intent intent = new Intent(LoginActivity.this,VideosListActivity.class);
        startActivity(intent);
        finish();
    }
}