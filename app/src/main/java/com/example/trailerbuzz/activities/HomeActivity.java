package com.example.trailerbuzz.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.exoplayer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(HomeActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            Intent intent = new Intent(HomeActivity.this, VideosListActivity.class);
            startActivity(intent);
            finish();
        }
    }
}