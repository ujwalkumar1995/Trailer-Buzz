package com.example.trailerbuzz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.exoplayer.R;
import com.example.trailerbuzz.helper.Constants;
import com.example.trailerbuzz.helper.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class ProfileActivity extends AppCompatActivity {

    //Profile Details
    private Button mUpdateButton;
    private TextView mFirstName;
    private TextView mLastName;
    private TextView mPhoneNo;
    private LinearProgressIndicator mProgressBar;
    private TextView mForgotPassword;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mUpdateButton = (Button) findViewById(R.id.update_button);
        mFirstName = (TextView) findViewById(R.id.user_first_name);
        mLastName = (TextView) findViewById(R.id.user_last_name);
        mPhoneNo = (TextView) findViewById(R.id.phone);
        mProgressBar = (LinearProgressIndicator) findViewById(R.id.profile_progress_bar);
        mForgotPassword = (TextView) findViewById(R.id.forget_password);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference(Constants.USERS);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        populateUserDetails();


        //Update Profile Details
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                String uId = currentUser.getUid();
                Users user = new Users();
                user.setFirstName(mFirstName.getText().toString());
                user.setLastName(mLastName.getText().toString());
                user.setPhone(mPhoneNo.getText().toString());
                mDatabaseReference.child(uId).setValue(user);

                Intent intent = new Intent(ProfileActivity.this, VideosListActivity.class);
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ProfileActivity.this, VideosListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Populate User Details from firebase
    private void populateUserDetails() {

        FirebaseUser user = mAuth.getCurrentUser();
        String uId = user.getUid();

        mDatabaseReference.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    String firstName = snapshot.getValue(Users.class).getFirstName();
                    String lastName = snapshot.getValue(Users.class).getLastName();
                    String phoneNo = snapshot.getValue(Users.class).getPhone();

                    mFirstName.setText(firstName);
                    mLastName.setText(lastName);
                    mPhoneNo.setText(phoneNo);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //Dialog to handle forget password
    private void openForgotPasswordDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        View promptsView = layoutInflater.inflate(R.layout.forgot_password_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ProfileActivity.this);
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
                                            Toast.makeText(ProfileActivity.this, "Email Sent", Toast.LENGTH_SHORT).show();
                                        }
                                        else{
                                            Toast.makeText(ProfileActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProfileActivity.this, VideosListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }
}