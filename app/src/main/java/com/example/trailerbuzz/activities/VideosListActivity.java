package com.example.trailerbuzz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
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
import com.example.trailerbuzz.adapters.SearchVideoAdapter;
import com.example.trailerbuzz.adapters.TopLikedVideosAdapter;
import com.example.trailerbuzz.helper.Constants;
import com.example.trailerbuzz.helper.Users;
import com.example.trailerbuzz.helper.Videos;
import com.example.trailerbuzz.utilities.VolleySingleton;
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
import java.util.Collections;
import java.util.HashSet;

import static com.example.trailerbuzz.helper.Constants.VIDEO_INTENT_VIDEO_ID;
import static com.example.trailerbuzz.helper.Constants.VIDEO_INTENT_VIDEO_NAME;


public class VideosListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //Firebase database and auth
    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference mGenreDatabase;

    //Recommended Recycler View
    private RecyclerView mRecommendedRecyclerView;
    private RecommendedVideosAdapter mRecommendedVideoAdapter;

    //Top Recycler View
    private MainVideoAdapter mMainVideoAdapter;
    private RecyclerView mMainRecyclerView;

    //Trending Recycler View
    private TopLikedVideosAdapter mTopLikedVideosAdapter;
    private RecyclerView mTopLikedRecyclerView;

    //Search Recycler View
    private SearchVideoAdapter mSearchVideoAdapter;
    private RecyclerView mSearchRecyclerView;
    private NestedScrollView mNestedScrollView;

    //Progress Bar on Load
    private ProgressBar mProgressBar;
    private LinearLayout mMainContent;

    //Drawer Layout
    public DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mActionBarDrawerToggle;

    //Populate User Details in Nav Drawer
    private String mFirstName;
    private String mLastName;
    private String mPhoneNo;

    //Variables to store recommended trailers
    private HashSet<String> trailerSet;
    private ArrayList<Videos> videoList;
    private HashSet<String> mGenreSet;
    private int totalGenreCount= Constants.GENRECOUNT;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos_list);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        mProgressBar = findViewById(R.id.progress_bar);
        mMainContent = findViewById(R.id.main_list_layout);

        mDrawerLayout = findViewById(R.id.my_drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.nav_open, R.string.nav_close);


        trailerSet = new HashSet<>();
        videoList = new ArrayList<>();

        mGenreSet = new HashSet<>();

        //Top Most Recycler View
        mMainRecyclerView = findViewById(R.id.video_recycler_view);
        mMainRecyclerView.setHasFixedSize(true);
        mMainRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Recommended Videos
        mRecommendedRecyclerView = findViewById(R.id.recommended_video_recycler_view);
        mRecommendedRecyclerView.setHasFixedSize(true);
        mRecommendedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Trending / Most Liked Videos
        mTopLikedRecyclerView = findViewById(R.id.top_liked_recycler_view);
        mTopLikedRecyclerView.setHasFixedSize(true);
        mTopLikedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        //Search Recycler View
        mNestedScrollView = findViewById(R.id.nested_scroll_view);
        mSearchRecyclerView = findViewById(R.id.search_recycler_view);
        mSearchRecyclerView.setHasFixedSize(true);
        mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setNavigationViewListener();
    }


    @Override
    protected void onStart() {
        super.onStart();
        populateVideosFromFirebase();
        populateTopLikedVideos();
        populateUserDetails();
        fetchRecommendedMovies();
    }

    //Handle Search Functionality
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_bar,menu);
        MenuItem menuItem = menu.findItem(R.id.search_icon);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search Here!");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(query == null || query.length() == 0 || query.equals("")) {
                    mSearchRecyclerView.setAdapter(null);
                    return false;
                }
                query = query.toLowerCase();
                fetchSearchedMovies(query);
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                mMainContent.setVisibility(View.VISIBLE);
                mSearchRecyclerView.setAdapter(null);
                mNestedScrollView.setVisibility(View.GONE);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainContent.setVisibility(View.GONE);
                mNestedScrollView.setVisibility(View.VISIBLE);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    //Fetch searched movies based on query string
    private void fetchSearchedMovies(String searchString) {
        System.out.println("I am here");
        ArrayList<Videos> mainVideoList = new ArrayList<>();
        Query topVideos = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS).orderByChild("searchString").startAt(searchString).endAt(searchString+"\uf8ff");
        topVideos.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot children:snapshot.getChildren()){
                    Videos video = children.getValue(Videos.class);
                    mainVideoList.add(video);
                }
                mSearchVideoAdapter = new SearchVideoAdapter(mainVideoList);
                mSearchRecyclerView.setAdapter(mSearchVideoAdapter);
                mSearchVideoAdapter.setOnItemClickListener(new SearchVideoAdapter.OnItemClickListener() {
                    public void onItemClick(int position) {
                        Videos video = mainVideoList.get(position);
                        startVideoPlayerActivity(video);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    //Initialize Navigation Drawer
    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    //Handle Clicks in Navigation Drawer
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            mAuth.signOut();
            Intent intent = new Intent(VideosListActivity.this, LoginActivity.class);
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

    //Handle Drawer open and close
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        for(String genre: mGenreSet){
            String searchQuery = Constants.RECOMMENDATION_API_GENRE + genre;
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                    Request.Method.GET,searchQuery,
                    null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            totalGenreCount--;
                            parseJsonResults(response, genre);
                            if(totalGenreCount==0)
                                setFinalRecommendations(trailerSet);


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            totalGenreCount--;
                            if(totalGenreCount==0)
                                setFinalRecommendations(trailerSet);

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

    //Fetch Movies Based on Genre from Flask API
    public HashSet<String> parseJsonResults(JSONArray response, String genre) {
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

    //Set Trailers based on Genre in Recommendations Recycler View
    private void setFinalRecommendations(HashSet<String> trailers) {
        Query getVideos = mDatabase.getReference(Constants.VIDEOS).orderByChild("id");
        getVideos.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot children:snapshot.getChildren()){
                    Videos video = children.getValue(Videos.class);
                    if(trailers.contains(video.getSearchString()))
                        videoList.add(video);
                }
                mRecommendedVideoAdapter = new RecommendedVideosAdapter(videoList);
                mRecommendedRecyclerView.setAdapter(mRecommendedVideoAdapter);

                mMainContent.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);

                mRecommendedVideoAdapter.setOnItemClickListener(new RecommendedVideosAdapter.OnItemClickListener() {
                    public void onItemClick(int position) {
                        Videos video = videoList.get(position);
                        startVideoPlayerActivity(video);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    //Populate user details in navigation drawer
    private void populateUserDetails() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String uId = user.getUid();

        DatabaseReference userDatabase = mDatabase.getReference(Constants.USERS);

        userDatabase.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                mFirstName = snapshot.getValue(Users.class).getFirstName();
                mLastName = snapshot.getValue(Users.class).getLastName();
                mPhoneNo = snapshot.getValue(Users.class).getPhone();

                NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
                View headerView =  navigationView.getHeaderView(0);
                TextView nav_name = (TextView) headerView.findViewById(R.id.full_name);
                nav_name.setText(mFirstName+" "+mLastName);
                TextView nav_phone = (TextView) headerView.findViewById(R.id.phone_no);
                nav_phone.setText(mPhoneNo);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //Populate videos in top most recycler view
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
                        startVideoPlayerActivity(video);
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    //Populate most liked videos
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
                Collections.sort(topLikedVideos);
                mTopLikedVideosAdapter = new TopLikedVideosAdapter(topLikedVideos);
                mTopLikedRecyclerView.setAdapter(mTopLikedVideosAdapter);
                mTopLikedVideosAdapter.setOnItemClickListener(new TopLikedVideosAdapter.OnItemClickListener() {
                    public void onItemClick(int position) {
                        Videos video = topLikedVideos.get(position);
                        startVideoPlayerActivity(video);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    //Start video player activity
    public void startVideoPlayerActivity(Videos video){
        Intent intent = new Intent(VideosListActivity.this,VideoPlayerActivity.class);
        intent.putExtra(VIDEO_INTENT_VIDEO_ID,video.getId());
        intent.putExtra(VIDEO_INTENT_VIDEO_NAME,video.getName());
        startActivity(intent);
        finish();
    }
}