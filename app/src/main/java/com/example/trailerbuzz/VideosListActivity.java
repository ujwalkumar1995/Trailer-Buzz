package com.example.trailerbuzz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.exoplayer.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import org.jetbrains.annotations.NotNull;


public class VideosListActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private FirebaseDatabase database;
    private MaterialButton signout;
    private FirebaseAuth mAuth;
    private ImageView thumbnail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);

        mAuth = FirebaseAuth.getInstance();
        signout = (MaterialButton) findViewById(R.id.signout);
        recyclerView = findViewById(R.id.video_recycler_view);
        recyclerView.setHasFixedSize(true);
        //RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this,LinearLayoutManager.HORIZONTAL);
        //recyclerView.setLayoutManager(mLayoutManager);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Videos");

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(VideosListActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Videos> options =
                new FirebaseRecyclerOptions.Builder<Videos>()
                .setQuery(databaseReference,Videos.class)
                .build();
        FirebaseRecyclerAdapter<Videos,ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Videos, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position, @NonNull @NotNull Videos model) {
                holder.setExoPlayer(getApplication(),model.getName(),model.getUrl(),model.getImageUrl(),model.getId());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(VideosListActivity.this,VideoPlayerActivity.class);
                        intent.putExtra("id", model.getId());
                        intent.putExtra("name",model.getName());
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @NonNull
            @NotNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item,parent,false);
                return new ViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter);

    }
}