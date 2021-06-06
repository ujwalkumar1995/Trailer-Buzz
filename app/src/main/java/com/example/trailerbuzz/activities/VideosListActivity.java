package com.example.trailerbuzz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.exoplayer.R;
import com.example.trailerbuzz.adapters.RecommendedVideosAdapter;
import com.example.trailerbuzz.adapters.VideoAdapter;
import com.example.trailerbuzz.adapters.ViewHolder;
import com.example.trailerbuzz.helper.Constants;
import com.example.trailerbuzz.helper.Videos;
import com.example.trailerbuzz.utilities.VolleySingleton;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class VideosListActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private RecyclerView mRecyclerView;
    private FirebaseDatabase database;
    private MaterialButton signout;
    private MaterialButton profile;
    private FirebaseAuth mAuth;
    private ImageView thumbnail;
    private RecyclerView mRecommendedRecyclerView;
    private RecommendedVideosAdapter mRecommendedVideoAdapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGenreDatabase;
    private HashSet<String> genreSet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);

        mAuth = FirebaseAuth.getInstance();
        signout = (MaterialButton) findViewById(R.id.signout);
        mRecyclerView = findViewById(R.id.video_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        database = FirebaseDatabase.getInstance();

        genreSet = new HashSet<>();
        databaseReference = database.getReference(Constants.VIDEOS);

        fetchRecommendedMovies();

        mRecommendedRecyclerView = findViewById(R.id.recommended_video_recycler_view);
        mRecommendedRecyclerView.setHasFixedSize(true);
        mRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));




        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(VideosListActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void fetchRecommendedMovies() {
        mGenreDatabase = database.getReference(Constants.GENRES);
        mGenreDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                String genres = snapshot.child("Genres").getValue().toString();
                String genreArray[] = genres.split(",");
                genreSet = new HashSet<>(Arrays.asList(genreArray));
                System.out.println("genreSet"+genreArray.length);
                System.out.println("genre"+genres);
                getRecommendationsFromApi();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void getRecommendationsFromApi() {

        for(String genre: genreSet){
            String searchQuery = Constants.RECOMMENDATION_API_GENRE + genre;
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,searchQuery,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            HashSet<String> trailers = fetchResults(response,genre);
                            fetchFinalRecommendations(trailers);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error instanceof NoConnectionError ||
                                    error instanceof NetworkError ||
                                    error instanceof TimeoutError) {
                                Toast.makeText(
                                        getApplicationContext(),
                                        getString(R.string.connection_unavailable),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(
                                        getApplicationContext(),
                                        getString(R.string.service_unavailable),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );
            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
        }
    }

    private void fetchFinalRecommendations(HashSet<String> trailers) {
        ArrayList<Videos> videoList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot children:snapshot.getChildren()){
                    Videos video = children.getValue(Videos.class);
                    if(trailers.contains(video.getSearchString()))
                        videoList.add(video);
                }
                mRecommendedVideoAdapter = new RecommendedVideosAdapter(videoList);
                mRecommendedRecyclerView.setAdapter(mRecommendedVideoAdapter);

                mRecommendedVideoAdapter.setOnItemClickListener(new RecommendedVideosAdapter.OnItemClickListener() {
                    public void onItemClick(int position) {
                        Videos video = videoList.get(position);
                        Intent intent = new Intent(VideosListActivity.this,VideoPlayerActivity.class);
                        intent.putExtra("id",video.getId());
                        intent.putExtra("name",video.getName());
                        startActivity(intent);
                        finish();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private HashSet<String> fetchResults(JSONArray response, String genre) {
        HashSet<String> trailerSet = new HashSet<>();
        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject responseObj = response.getJSONObject(i);
                String trailerName = responseObj.getString("Name").toLowerCase();
                trailerSet.add(trailerName);
            }
        } catch (Exception e) {

        }
        return trailerSet;
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Videos> options =
                new FirebaseRecyclerOptions.Builder<Videos>()
                .setQuery(databaseReference,Videos.class)
                .build();
        FirebaseRecyclerAdapter<Videos, ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Videos, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position, @NonNull @NotNull Videos model) {
                holder.setExoPlayer(getApplication(),model.getName(),model.getUrl(),model.getImageUrl(),model.getId());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(VideosListActivity.this, VideoPlayerActivity.class);
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
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);

    }
}