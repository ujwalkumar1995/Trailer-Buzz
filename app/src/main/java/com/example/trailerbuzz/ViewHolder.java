package com.example.trailerbuzz;

import android.annotation.SuppressLint;
import android.app.Application;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media2.exoplayer.external.upstream.DefaultHttpDataSourceFactory;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exoplayer.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.squareup.picasso.Picasso;


public class ViewHolder extends RecyclerView.ViewHolder{

    SimpleExoPlayer simpleExoPlayer;
    PlayerView playerView;
    ImageView thumbnail;

    public ViewHolder(@NonNull @org.jetbrains.annotations.NotNull View itemView) {
        super(itemView);
    }

    public void setExoPlayer(Application application, String name, String url,String imageUrl,String id){
        TextView videoName = (TextView) itemView.findViewById(R.id.item_video_name);
        thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        videoName.setText(name);

        try{
            Uri videoUri = Uri.parse(url);
            Picasso.get().load(imageUrl).into(thumbnail);
        }
        catch(Exception e){
            Log.e("ViewHolder","exo player error"+e.toString());
        }
    }
}
