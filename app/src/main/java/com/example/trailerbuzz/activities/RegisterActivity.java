package com.example.trailerbuzz.activities;

import androidx.appcompat.app.AppCompatActivity;

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
import com.example.trailerbuzz.helper.Constants;
import com.example.trailerbuzz.helper.Users;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    //User Details
    private EditText mUserEmail;
    private EditText mFirstName;
    private EditText mLastName;
    private EditText mPhone;
    private EditText mUserPassword;
    private EditText mConfirmPassword;
    private Button mRegisterButton;
    private TextView mLoginText;
    private LinearProgressIndicator mProgressBar;
    private CheckBox mShowPassword;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = mFirebaseDatabase.getReference(Constants.USERS);

        mFirstName = (EditText) findViewById(R.id.user_first_name);
        mLastName = (EditText) findViewById(R.id.user_last_name);
        mPhone = (EditText) findViewById(R.id.phone);
        mUserEmail =  (EditText) findViewById(R.id.user_email);
        mUserPassword =  (EditText) findViewById(R.id.user_password);
        mConfirmPassword = (EditText) findViewById(R.id.user_password);
        mRegisterButton = (Button) findViewById(R.id.register_button);
        mLoginText = (TextView)  findViewById(R.id.login_text);
        mProgressBar = (LinearProgressIndicator) findViewById(R.id.register_progress_bar);
        mShowPassword = (CheckBox) findViewById(R.id.password_checkbox);

        //Handle Click on Show password checkbox
        mShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    mUserPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else{
                    mUserPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        //Handle Click on Register Button
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mUserEmail.getText().toString();
                String password = mUserPassword.getText().toString();
                String confirmPass = mConfirmPassword.getText().toString();
                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();
                String phoneNo = mPhone.getText().toString();
                Users user = new Users();
                user.setEmail(email);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setPassword(password);
                user.setPhone(phoneNo);

                if(!TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPass)){
                    if(confirmPass.equals(password)){
                        sendToGenrePreferenceActivity(user);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),R.string.password_dont_match,Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        mLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
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

    public void sendToGenrePreferenceActivity(Users user) {
        Intent intent = new Intent(RegisterActivity.this, GenrePreferenceActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    public void sendToVideosList(){
        Intent intent = new Intent(RegisterActivity.this, VideosListActivity.class);
        startActivity(intent);
        finish();
    }

}