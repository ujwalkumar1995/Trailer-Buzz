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
import com.example.trailerbuzz.adapters.MainVideoAdapter;
import com.example.trailerbuzz.adapters.RecommendedVideosAdapter;
import com.example.trailerbuzz.adapters.TopLikedVideosAdapter;
import com.example.trailerbuzz.helper.Constants;
import com.example.trailerbuzz.helper.Videos;
import com.example.trailerbuzz.utilities.VolleySingleton;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class VideosListActivity extends AppCompatActivity {

    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private MaterialButton signout;
    private FirebaseAuth mAuth;
    private RecyclerView mRecommendedRecyclerView;
    private RecommendedVideosAdapter mRecommendedVideoAdapter;
    private MainVideoAdapter mMainVideoAdapter;
    private RecyclerView mMainRecyclerView;

    private TopLikedVideosAdapter mTopLikedVideosAdapter;
    private RecyclerView mTopLikedRecyclerView;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGenreDatabase;
    private HashSet<String> mGenreSet;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);

        mAuth = FirebaseAuth.getInstance();
        signout = (MaterialButton) findViewById(R.id.signout);
        mMainRecyclerView = findViewById(R.id.video_recycler_view);
        mMainRecyclerView.setHasFixedSize(true);
        mMainRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mDatabase = FirebaseDatabase.getInstance();

        mGenreSet = new HashSet<>();
        mDatabaseReference = mDatabase.getReference(Constants.VIDEOS);
        mRecommendedRecyclerView = findViewById(R.id.recommended_video_recycler_view);
        mRecommendedRecyclerView.setHasFixedSize(true);
        mRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mTopLikedRecyclerView = findViewById(R.id.top_liked_recycler_view);
        mTopLikedRecyclerView.setHasFixedSize(true);
        mTopLikedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


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

    //Get the Genres selected by the current logged in user
    private void fetchRecommendedMovies() {
        mGenreDatabase = mDatabase.getReference(Constants.GENRES);
        mGenreDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                String genres = snapshot.child("Genres").getValue().toString();
                String genreArray[] = genres.split(",");
                mGenreSet = new HashSet<>(Arrays.asList(genreArray));
                getRecommendationsFromApi();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    //Fetch Results from Flask API using volley
    private void getRecommendationsFromApi() {
        System.out.println("setsize"+mGenreSet.size());
        for(String genre: mGenreSet){
            String searchQuery = Constants.RECOMMENDATION_API_GENRE + genre;
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,searchQuery,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            HashSet<String> trailers = parseJsonResults(response,genre);
                            setFinalRecommendations(trailers);
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

    //Set Trailers based on Genre in Recommendations Recycler View
    private void setFinalRecommendations(HashSet<String> trailers) {
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

    //Fetch Movies Based on Genre from Flask API
    private HashSet<String> parseJsonResults(JSONArray response, String genre) {
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
        populateVideosFromFirebase();
        fetchRecommendedMovies();
        populateTopLikedVideos();
    }

    public void populateVideosFromFirebase(){

        ArrayList<Videos> mainVideoList = new ArrayList<>();
        Query topVideos = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS).orderByChild("id").limitToFirst(6);
        topVideos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot children:snapshot.getChildren()){
                    Videos video = children.getValue(Videos.class);
                    mainVideoList.add(video);
                }
                mMainVideoAdapter = new MainVideoAdapter(mainVideoList);
                mMainRecyclerView.setAdapter(mMainVideoAdapter);
                mMainVideoAdapter.setOnItemClickListener(new MainVideoAdapter.OnItemClickListener() {
                    public void onItemClick(int position) {
                        Videos video = mainVideoList.get(position);
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

    public void populateTopLikedVideos(){

        ArrayList<Videos> topLikedVideos = new ArrayList<>();
        Query topVideos = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS).orderByChild("likeCount").limitToLast(6);
        topVideos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot children:snapshot.getChildren()){
                    Videos video = children.getValue(Videos.class);
                    topLikedVideos.add(video);
                }
                mTopLikedVideosAdapter = new TopLikedVideosAdapter(topLikedVideos);
                mTopLikedRecyclerView.setAdapter(mTopLikedVideosAdapter);
                mTopLikedVideosAdapter.setOnItemClickListener(new TopLikedVideosAdapter.OnItemClickListener() {
                    public void onItemClick(int position) {
                        Videos video = topLikedVideos.get(position);
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
}