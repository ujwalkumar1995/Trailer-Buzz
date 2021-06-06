
package com.example.trailerbuzz.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.exoplayer.R;
import com.example.trailerbuzz.adapters.VideoAdapter;
import com.example.trailerbuzz.helper.Constants;
import com.example.trailerbuzz.helper.Videos;
import com.example.trailerbuzz.utilities.VolleySingleton;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class VideoPlayerActivity extends AppCompatActivity {

    private PlaybackStateListener playbackStateListener;
    private static final String TAG = VideoPlayerActivity.class.getName();


    private String trailerTitle = "";
    private boolean fullscreen = false;
    private boolean playWhenReady = true;
    private String id = "";
    private boolean mProcessLike = false;
    private int likesCount = 0;
    private int currentWindow = 0;
    private long playbackPosition = 0;


    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference,likeReference;


    private PlayerView playerView;
    private SimpleExoPlayer player;


    private MaterialTextView likeCount;
    private ShapeableImageView thumbnail;
    private MaterialTextView description;
    private MaterialTextView starcast;
    private MaterialTextView releaseDate;
    private MaterialTextView title;
    private ImageView fullscreenButton;
    private LinearLayout videoDetailsLayout;
    private RecyclerView recyclerView;
    private VideoAdapter mVideoAdapter;
    private ImageView likeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        thumbnail = (ShapeableImageView) findViewById(R.id.thumbnail_image);
        videoDetailsLayout = (LinearLayout) findViewById(R.id.videoDetails);
        fullscreenButton = (ImageView) findViewById(R.id.exo_fullscreen_icon);
        description = (MaterialTextView) findViewById(R.id.description);
        starcast = (MaterialTextView) findViewById(R.id.star_cast);
        releaseDate = (MaterialTextView) findViewById(R.id.release_date);
        title = (MaterialTextView) findViewById(R.id.trailer_title);
        likeButton = (ImageView) findViewById(R.id.like_button);
        likeCount = (MaterialTextView) findViewById(R.id.like_count);


        recyclerView = findViewById(R.id.recommendations_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference(Constants.VIDEOS);
        likeReference = database.getReference(Constants.LIKES);

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProcessLike = true;
                if(mProcessLike){
                    likeReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(mProcessLike) {
                                if (snapshot.child(id).hasChild(mAuth.getCurrentUser().getUid())) {
                                    likesCount--;
                                    updateLikeCount();
                                    likeReference.child(id).child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                } else {
                                    likesCount++;
                                    updateLikeCount();
                                    likeReference.child(id).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
                                    mProcessLike = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                        }
                    });
                }
            }
        });





        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fullscreen) {
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.icon_full_screen_96dp));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    if(getSupportActionBar() != null){
                        getSupportActionBar().show();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = (int) ( 250 * getApplicationContext().getResources().getDisplayMetrics().density);
                    playerView.setLayoutParams(params);
                    fullscreen = false;
                    videoDetailsLayout.setVisibility(View.VISIBLE);
                }else{
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.icon_shrink_96dp));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    if(getSupportActionBar() != null){
                        getSupportActionBar().hide();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = params.MATCH_PARENT;
                    playerView.setLayoutParams(params);
                    fullscreen = true;
                    videoDetailsLayout.setVisibility(View.GONE);
                }
            }
        });

        playerView = findViewById(R.id.video_view);
        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        trailerTitle = intent.getStringExtra("name");

        populateTrailerDetails();
        populateLikeDetails();

        playbackStateListener = new PlaybackStateListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        if (player == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = new SimpleExoPlayer.Builder(this)
                    .setTrackSelector(trackSelector)
                    .build();
        }
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS);
        reference.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot snapshots: snapshot.getChildren()) {
                    String videoUrl = snapshots.getValue(Videos.class).getUrl();
                    DataSource.Factory dataSourceFactory =
                            new DefaultHttpDataSourceFactory("video");
                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(videoUrl));
                    player.setPlayWhenReady(playWhenReady);
                    player.seekTo(currentWindow, playbackPosition);
                    player.addListener(playbackStateListener);
                    player.prepare(mediaSource,false,false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);

    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private class PlaybackStateListener implements Player.EventListener{

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE-";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING-";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY-";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED-";
                    break;
                default:
                    stateString = "UNKNOWN_STATE-";
                    break;
            }
            Log.d(TAG, "changed state to " + stateString);
        }
    }

    @Override
    public void onBackPressed() {

        if(fullscreen==false) {
            super.onBackPressed();
            player.stop();
            releasePlayer();
            Intent intent = new Intent(VideoPlayerActivity.this, VideosListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else{
            fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.icon_full_screen_96dp));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if(getSupportActionBar() != null){
                getSupportActionBar().show();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int) ( 250 * getApplicationContext().getResources().getDisplayMetrics().density);
            playerView.setLayoutParams(params);
            fullscreen = false;
            videoDetailsLayout.setVisibility(View.VISIBLE);
        }

    }

    public void populateTrailerDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS);
        reference.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot snapshots: snapshot.getChildren()) {
                    likesCount = snapshots.getValue(Videos.class).getLikeCount();
                    likeCount.setText(Integer.toString(likesCount));
                    title.setText(snapshots.getValue(Videos.class).getName());
                    description.setText(snapshots.getValue(Videos.class).getDescription());
                    releaseDate.setText(snapshots.getValue(Videos.class).getReleaseDate());
                    starcast.setText("Starring:  "+snapshots.getValue(Videos.class).getStarCast());
                    Picasso.get().load(snapshots.getValue(Videos.class).getImageUrl()).into(thumbnail);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        fetchRecommendationsList();
    }

    public void populateLikeDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        likeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.child(id).hasChild(mAuth.getCurrentUser().getUid())){
                    likeButton.setImageResource(R.drawable.ic_baseline_favorite_24);
                }
                else{
                    likeButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void updateLikeCount() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS);
        reference.child(id).child("likeCount").setValue(likesCount);
        likeCount.setText(Integer.toString(likesCount));
    }

    public void fetchRecommendationsList(){
        String searchQuery = Constants.RECOMMENDATION_API_MOVIE + trailerTitle;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,searchQuery,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Set trailerSet = fetchResults(response,trailerTitle);
                        fetchFinalRecommendations(trailerSet);
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

    public void fetchFinalRecommendations(Set<String> trailerSet) {
        ArrayList<Videos> videoList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for(DataSnapshot children:snapshot.getChildren()){
                    Videos video = children.getValue(Videos.class);
                    if(trailerSet.contains(video.getSearchString()))
                        videoList.add(video);
                }
                mVideoAdapter = new VideoAdapter(videoList);
                recyclerView.setAdapter(mVideoAdapter);

                mVideoAdapter.setOnItemClickListener(new VideoAdapter.OnItemClickListener() {
                    public void onItemClick(int position) {
                        Videos video = videoList.get(position);
                        Intent intent = new Intent(VideoPlayerActivity.this,VideoPlayerActivity.class);
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

    public Set<String> fetchResults(JSONArray response,String title){

        Set<String> trailerSet = new HashSet<>();
        try {
            for (int i = 0; i < response.length(); i++) {
                  JSONObject responseObj = response.getJSONObject(i);
                  String trailerName = responseObj.getString("Name");
                  if(trailerName.equalsIgnoreCase(trailerTitle))
                      continue;
                  trailerSet.add(trailerName);
            }
        } catch (Exception e) {

        }
        return trailerSet;

    }

}
