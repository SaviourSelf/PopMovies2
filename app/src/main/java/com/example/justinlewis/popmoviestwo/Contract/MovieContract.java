package com.example.justinlewis.popmoviestwo.Contract;

import android.net.Uri;

/**
 * Created by Justin Lewis on 4/10/2016.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.justinlewis.popmoviestwo";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY + "/movies");

    public static Uri buildMovieUri(String movieID)
    {
        return BASE_CONTENT_URI.buildUpon().appendPath(movieID).build();
    }

}
