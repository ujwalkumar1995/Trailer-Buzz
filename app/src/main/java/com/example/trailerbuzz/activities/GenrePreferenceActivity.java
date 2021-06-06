package com.example.trailerbuzz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.exoplayer.R;
import com.example.trailerbuzz.helper.Constants;
import com.example.trailerbuzz.helper.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

public class GenrePreferenceActivity extends AppCompatActivity {

    private Button mActionButton;
    private Button mComedyButton;
    private Button mHorrorButton;
    private Button mRomanceButton;
    private Button mSavePreferences;
    private Users user;
    private LinearProgressIndicator mProgressBar;

    private boolean isActionSelected = false;
    private boolean isComedySelected = false;
    private boolean isHorrorSelected = false;
    private boolean isRomanceSelected = false;
    private boolean mProcessGenre = false;
    private int genreCount = 0;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabase,mGenreDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre_preference);

        Intent intent = getIntent();
        user = (Users)intent.getSerializableExtra("user");
        System.out.println("user"+user);

        mProgressBar = (LinearProgressIndicator) findViewById(R.id.progress_bar);
        mActionButton = (Button)  findViewById(R.id.action_button);
        mComedyButton = (Button)  findViewById(R.id.comedy_button);
        mHorrorButton = (Button)  findViewById(R.id.horror_button);
        mRomanceButton = (Button)  findViewById(R.id.romance_button);
        mSavePreferences = (Button)  findViewById(R.id.save_preferences);

        mSavePreferences.setEnabled(false);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabase = mFirebaseDatabase.getReference(Constants.USERS);
        mGenreDatabase = mFirebaseDatabase.getReference(Constants.GENRES);

        mSavePreferences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                if(mSavePreferences.isEnabled()){
                    mAuth.createUserWithEmailAndPassword(user.getEmail(),user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                String uid = currentUser.getUid();
                                mDatabase.child(uid).setValue(user);
                                generatePreferences();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                            }

                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                }
                else{
                    Toast.makeText(GenrePreferenceActivity.this, "Please select 3 Genres", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void generatePreferences() {
        StringBuilder sb = new StringBuilder();
        if(isActionSelected){
            sb.append(Constants.ACTION);
            sb.append(",");
        }
        if(isComedySelected){
            sb.append(Constants.COMEDY);
            sb.append(",");
        }
        if(isHorrorSelected){
            sb.append(Constants.HORROR);
            sb.append(",");
        }
        if(isRomanceSelected){
            sb.append(Constants.ROMANCE);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        mProcessGenre = true;
        if(mProcessGenre){
            mGenreDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    if(mProcessGenre) {
                        mGenreDatabase.child(mAuth.getCurrentUser().getUid()).child("Genres").setValue(sb.toString());
                        mProcessGenre = false;
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

    }

    @SuppressLint("ResourceAsColor")
    public void horrorButtonClicked(View view){
        isHorrorSelected = !isHorrorSelected;
        if(isHorrorSelected){
            genreCount++;
            mHorrorButton.setBackgroundResource(R.drawable.horror_selected);
        }
        else{
            genreCount--;
            mHorrorButton.setBackgroundResource(R.drawable.horror_unselected);
        }
        checkSaveButton();
    }

    public void actionButtonClicked(View view){
        isActionSelected = !isActionSelected;
        if(isActionSelected){
            genreCount++;
            mActionButton.setBackgroundResource(R.drawable.action_selected);
        }
        else{
            genreCount--;
            mActionButton.setBackgroundResource(R.drawable.action_unselected);
        }
        checkSaveButton();
    }

    public void comedyButtonClicked(View view){
        isComedySelected = !isComedySelected;
        if(isComedySelected){
            genreCount++;
            mComedyButton.setBackgroundResource(R.drawable.comedy_selected);
        }
        else{
            genreCount--;
            mComedyButton.setBackgroundResource(R.drawable.comedy_unselected);
        }
        checkSaveButton();
    }

    public void romanceButtonClicked(View view){
        isRomanceSelected = !isRomanceSelected;
        if(isRomanceSelected){
            genreCount++;
            mRomanceButton.setBackgroundResource(R.drawable.romance_selected);
        }
        else{
            genreCount--;
            mRomanceButton.setBackgroundResource(R.drawable.romance_unselected);
        }
        checkSaveButton();
    }

    private void checkSaveButton() {
        System.out.println(genreCount+"genreCount");
        if(genreCount==3){
            mSavePreferences.setEnabled(true);
            mSavePreferences.setBackgroundResource(R.drawable.save_activated);
        }
        else{
            mSavePreferences.setEnabled(false);
            mSavePreferences.setBackgroundResource(R.drawable.save_deactivated);
        }
    }
}