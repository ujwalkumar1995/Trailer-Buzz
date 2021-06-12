
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
    private boolean mFullscreen = false;
    private boolean mPlayWhenReady = true;
    private String id = "";
    private boolean mProcessLike = false;
    private int mLikesCount = 0;
    private int mCurrentWindow = 0;
    private long mPlaybackPosition = 0;


    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference,mLikeReference;


    private PlayerView mPlayerView;
    private SimpleExoPlayer mPlayer;


    private MaterialTextView mLikeCount;
    private ShapeableImageView mThumbnail;
    private MaterialTextView mDescription;
    private MaterialTextView mStarcast;
    private MaterialTextView mReleaseDate;
    private MaterialTextView mTitle;
    private ImageView mFullscreenButton;
    private LinearLayout mVideoDetailsLayout;
    private RecyclerView mRecyclerView;
    private VideoAdapter mVideoAdapter;
    private ImageView mLikeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        mThumbnail = (ShapeableImageView) findViewById(R.id.thumbnail_image);
        mVideoDetailsLayout = (LinearLayout) findViewById(R.id.videoDetails);
        mFullscreenButton = (ImageView) findViewById(R.id.exo_fullscreen_icon);
        mDescription = (MaterialTextView) findViewById(R.id.description);
        mStarcast = (MaterialTextView) findViewById(R.id.star_cast);
        mReleaseDate = (MaterialTextView) findViewById(R.id.release_date);
        mTitle = (MaterialTextView) findViewById(R.id.trailer_title);
        mLikeButton = (ImageView) findViewById(R.id.like_button);
        mLikeCount = (MaterialTextView) findViewById(R.id.like_count);


        mRecyclerView = findViewById(R.id.recommendations_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference(Constants.VIDEOS);
        mLikeReference = mDatabase.getReference(Constants.LIKES);

        mLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProcessLike = true;
                if(mProcessLike){
                    mLikeReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                            if(mProcessLike) {
                                if (snapshot.child(id).hasChild(mAuth.getCurrentUser().getUid())) {
                                    mLikesCount--;
                                    updateLikeCount();
                                    mLikeReference.child(id).child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                } else {
                                    mLikesCount++;
                                    updateLikeCount();
                                    mLikeReference.child(id).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getEmail());
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





        mFullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFullscreen) {
                    mFullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.icon_full_screen_96dp));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    if(getSupportActionBar() != null){
                        getSupportActionBar().show();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mPlayerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = (int) ( 250 * getApplicationContext().getResources().getDisplayMetrics().density);
                    mPlayerView.setLayoutParams(params);
                    mFullscreen = false;
                    mVideoDetailsLayout.setVisibility(View.VISIBLE);
                }else{
                    mFullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.icon_shrink_96dp));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    if(getSupportActionBar() != null){
                        getSupportActionBar().hide();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mPlayerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = params.MATCH_PARENT;
                    mPlayerView.setLayoutParams(params);
                    mFullscreen = true;
                    mVideoDetailsLayout.setVisibility(View.GONE);
                }
            }
        });

        mPlayerView = findViewById(R.id.video_view);
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
        if ((Util.SDK_INT <= 23 || mPlayer == null)) {
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
        if (mPlayer == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(
                    trackSelector.buildUponParameters().setMaxVideoSizeSd());
            mPlayer = new SimpleExoPlayer.Builder(this)
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
                    mPlayer.setPlayWhenReady(mPlayWhenReady);
                    mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);
                    mPlayer.addListener(playbackStateListener);
                    mPlayer.prepare(mediaSource,false,false);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        mPlayerView.setPlayer(mPlayer);
        mPlayerView.setKeepScreenOn(true);

    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlaybackPosition = mPlayer.getCurrentPosition();
            mCurrentWindow = mPlayer.getCurrentWindowIndex();
            mPlayWhenReady = mPlayer.getPlayWhenReady();
            mPlayer.removeListener(playbackStateListener);
            mPlayer.release();
            mPlayer = null;
        }
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        mPlayerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
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

        if(mFullscreen==false) {
            super.onBackPressed();
            mPlayer.stop();
            releasePlayer();
            Intent intent = new Intent(VideoPlayerActivity.this, VideosListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        else{
            mFullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlayerActivity.this, R.drawable.icon_full_screen_96dp));
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if(getSupportActionBar() != null){
                getSupportActionBar().show();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mPlayerView.getLayoutParams();
            params.width = params.MATCH_PARENT;
            params.height = (int) ( 250 * getApplicationContext().getResources().getDisplayMetrics().density);
            mPlayerView.setLayoutParams(params);
            mFullscreen = false;
            mVideoDetailsLayout.setVisibility(View.VISIBLE);
        }

    }

    public void populateTrailerDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS);
        reference.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for(DataSnapshot snapshots: snapshot.getChildren()) {
                    mLikesCount = snapshots.getValue(Videos.class).getLikeCount();
                    mLikeCount.setText(Integer.toString(mLikesCount));
                    mTitle.setText(snapshots.getValue(Videos.class).getName());
                    mDescription.setText(snapshots.getValue(Videos.class).getDescription());
                    mReleaseDate.setText(snapshots.getValue(Videos.class).getReleaseDate());
                    mStarcast.setText("Starring:  "+snapshots.getValue(Videos.class).getStarCast());
                    Picasso.get().load(snapshots.getValue(Videos.class).getImageUrl()).into(mThumbnail);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        fetchRecommendationsListFromAPI();
    }

    public void populateLikeDetails() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        mLikeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.child(id).hasChild(mAuth.getCurrentUser().getUid())){
                    mLikeButton.setImageResource(R.drawable.ic_baseline_favorite_24);
                }
                else{
                    mLikeButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    public void updateLikeCount() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.VIDEOS);
        reference.child(id).child("likeCount").setValue(mLikesCount);
        mLikeCount.setText(Integer.toString(mLikesCount));
    }

    public void fetchRecommendationsListFromAPI(){
        String searchQuery = Constants.RECOMMENDATION_API_MOVIE + trailerTitle;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,searchQuery,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Set trailerSet = parseJsonResult(response,trailerTitle);
                        filterSearchResults(trailerSet);
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

    public void filterSearchResults(Set<String> trailerSet) {
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
                mRecyclerView.setAdapter(mVideoAdapter);

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

    public Set<String> parseJsonResult(JSONArray response,String title){

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
