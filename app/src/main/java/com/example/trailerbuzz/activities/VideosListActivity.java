package com.example.trailerbuzz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.example.trailerbuzz.helper.Users;
import com.example.trailerbuzz.helper.Videos;
import com.example.trailerbuzz.utilities.VolleySingleton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class VideosListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
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

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    private String firstName;
    private String lastName;
    private String phoneNo;

    private HashSet<String> trailerSet;
    private ArrayList<Videos> videoList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);

        mAuth = FirebaseAuth.getInstance();
        mMainRecyclerView = findViewById(R.id.video_recycler_view);
        mMainRecyclerView.setHasFixedSize(true);
        mMainRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mDatabase = FirebaseDatabase.getInstance();

        mGenreSet = new HashSet<>();
        mRecommendedRecyclerView = findViewById(R.id.recommended_video_recycler_view);
        mRecommendedRecyclerView.setHasFixedSize(true);
        mRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mTopLikedRecyclerView = findViewById(R.id.top_liked_recycler_view);
        mTopLikedRecyclerView.setHasFixedSize(true);
        mTopLikedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);

        trailerSet = new HashSet<>();
        videoList = new ArrayList<>();


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setNavigationViewListener();

    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            System.out.println("Inside");
            mAuth.signOut();
            Intent intent = new Intent(VideosListActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(VideosListActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = findViewById(R.id.my_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //Get the Genres selected by the current logged in user
    private void fetchRecommendedMovies() {
        mGenreDatabase = mDatabase.getReference(Constants.GENRES);
        mGenreDatabase.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                String genres = snapshot.child("Genres").getValue().toString();
                System.out.println("this");
                String genreArray[] = genres.split(",");
                mGenreSet = new HashSet<>(Arrays.asList(genreArray));
                getRecommendationsFromApi();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Fetch Results from Flask API using volley
    private void getRecommendationsFromApi() {
        System.out.println("videosize3");
        for(String genre: mGenreSet){
            String searchQuery = Constants.RECOMMENDATION_API_GENRE + genre;
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,searchQuery,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            trailerSet = parseJsonResults(response, genre);
                            System.out.println(trailerSet.size()+"trailerset");
                            if(trailerSet.size()!=0) {
                                setFinalRecommendations(trailerSet);
                            }

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

        System.out.println("videosize1");

        Query getVideos = mDatabase.getReference(Constants.VIDEOS).orderByChild("id");
        //FirebaseDatabase.getInstance().getReference(Constants.VIDEOS).addListenerForSingleValueEvent(new ValueEventListener() {
        getVideos.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot children:snapshot.getChildren()){
                    Videos video = children.getValue(Videos.class);
                    if(trailers.contains(video.getSearchString()))
                        videoList.add(video);
                }
                System.out.println(videoList.size()+"videosize");
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
        populateUserDetails();
    }

    private void populateUserDetails() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String uId = user.getUid();

        DatabaseReference userDatabase = mDatabase.getReference(Constants.USERS);

        userDatabase.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                firstName = snapshot.getValue(Users.class).getFirstName();
                lastName = snapshot.getValue(Users.class).getLastName();
                phoneNo = snapshot.getValue(Users.class).getPhone();

                NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
                View headerView =  navigationView.getHeaderView(0);
                TextView nav_name = (TextView) headerView.findViewById(R.id.full_name);
                nav_name.setText(firstName+" "+lastName);
                TextView nav_phone = (TextView) headerView.findViewById(R.id.phone_no);
                nav_phone.setText(phoneNo);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void populateVideosFromFirebase(){

        ArrayList<Videos> mainVideoList = new ArrayList<>();
        Query topVideos = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS).orderByChild("id").limitToFirst(10);
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
        Query topVideos = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS).orderByChild("likeCount").limitToLast(10);
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