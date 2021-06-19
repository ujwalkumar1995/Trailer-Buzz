package com.example.trailerbuzz.helper;

public class Constants {

    //API Links
    public static final String RECOMMENDATION_API_MOVIE = "https://trailer-buzz.herokuapp.com/movie?title=";
    public static final String RECOMMENDATION_API_GENRE = "https://trailer-buzz.herokuapp.com/genre?genre_type=";

    //Intents
    public static final String VIDEO_INTENT_VIDEO_ID = "Video_Intent_Place_ID";
    public static final String VIDEO_INTENT_VIDEO_NAME = "Video_Intent_Video_Name";

    //Database References
    public static final String USERS = "Users";
    public static final String VIDEOS = "Videos";
    public static final String LIKES = "Likes";
    public static final String GENRES = "Genres";

    //Genre Types
    public static final String ACTION = "Action";
    public static final String COMEDY = "Comedy";
    public static final String HORROR = "Horror";
    public static final String ROMANCE = "Romance";

    //Maximum Genre Count
    public static final int GENRECOUNT = 3;
}
