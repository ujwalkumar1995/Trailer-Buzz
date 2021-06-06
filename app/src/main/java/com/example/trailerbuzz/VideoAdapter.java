package com.example.trailerbuzz;

import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exoplayer.R;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder>{

    private ArrayList<Videos> mVideoList;
    private OnItemClickListener mListener;
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

    @NonNull
    @NotNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_item,parent,false);
        VideoViewHolder videoViewHolder = new VideoViewHolder(view,mListener);
        return videoViewHolder;
    }

    public VideoAdapter(ArrayList<Videos> videos){
        this.mVideoList = videos;
    }
    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        Videos video = mVideoList.get(position);
        Uri videoUri = Uri.parse(video.getImageUrl());
        Picasso.get().load(videoUri).into(holder.mImageView);
        holder.mTextView.setText(video.getName());
    }



    @Override
    public int getItemCount() {
        return mVideoList.size();
    }

    public static class  VideoViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView mTextView;
        public VideoViewHolder(@NonNull @NotNull View itemView, OnItemClickListener listener) {

            super(itemView);
            mImageView = itemView.findViewById(R.id.thumbnail);
            mTextView = itemView.findViewById(R.id.item_video_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }
}