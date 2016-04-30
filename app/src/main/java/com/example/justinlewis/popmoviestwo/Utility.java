package com.example.justinlewis.popmoviestwo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.example.justinlewis.popmoviestwo.Objects.MovieData;
import com.example.justinlewis.popmoviestwo.Objects.ReviewObject;
import com.example.justinlewis.popmoviestwo.Objects.TrailerObject;
import com.example.justinlewis.popmoviestwo.Provider.MovieProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Justin Lewis on 4/29/2016.
 */
public class Utility {

    public static int callCount = 0;
    public static Context c;
    public static String lastChosen;

    public static MovieData [] getFavorites()
    {
        String title, id, plot, vote, date, poster, favorite;
        int index = 0;
        MovieData [] data;
        Cursor cursor;
        cursor = c.getContentResolver().query(
                MovieProvider.CONTENT_URI,
                null,
                MovieProvider.FAVORITE_FIELD + " =? ",
                new String [] {"yes"},
                null
        );

        data = new MovieData[cursor.getCount()];

        if (cursor.getCount() > 0 && cursor.moveToFirst())
            do {
                title = cursor.getString(cursor.getColumnIndex(MovieProvider.TITLE_FIELD));
                id = cursor.getString(cursor.getColumnIndex(MovieProvider.ID_FIELD));
                plot = cursor.getString(cursor.getColumnIndex(MovieProvider.PLOT_FIELD));
                date= cursor.getString(cursor.getColumnIndex(MovieProvider.RELEASE_DATE_FIELD));
                vote = cursor.getString(cursor.getColumnIndex(MovieProvider.VOTER_AVERAGE_FIELD));
                poster = cursor.getString(cursor.getColumnIndex(MovieProvider.POSTER_URL_FIELD));
                favorite = cursor.getString(cursor.getColumnIndex(MovieProvider.FAVORITE_FIELD));


                data[index++] = new MovieData(id, title, date, poster, vote, plot, lastChosen, favorite);
            } while (cursor.moveToNext());
        cursor.close();
        return data;
    }

    public static String buildImageURL(String imagePath)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w185")
                .appendPath(imagePath.substring(1));
        return builder.build().toString();
    }

    public static MovieData [] fetchFromDb()
    {
        String title, id, plot, vote, date, poster, favorite;
        int index = 0;
        MovieData [] data;
        Cursor cursor;
        cursor = c.getContentResolver().query(
                MovieProvider.CONTENT_URI,
                null,
                MovieProvider.SOURCE_FIELD + " =?",         //Where favorite field = true
                new String [] {lastChosen},
                null
        );

        data = new MovieData[cursor.getCount()];

        if (cursor.getCount() > 0 && cursor.moveToFirst())
            do {
                title = cursor.getString(cursor.getColumnIndex(MovieProvider.TITLE_FIELD));
                id = cursor.getString(cursor.getColumnIndex(MovieProvider.ID_FIELD));
                plot = cursor.getString(cursor.getColumnIndex(MovieProvider.PLOT_FIELD));
                date= cursor.getString(cursor.getColumnIndex(MovieProvider.RELEASE_DATE_FIELD));
                vote = cursor.getString(cursor.getColumnIndex(MovieProvider.VOTER_AVERAGE_FIELD));
                poster = cursor.getString(cursor.getColumnIndex(MovieProvider.POSTER_URL_FIELD));
                favorite = cursor.getString(cursor.getColumnIndex(MovieProvider.FAVORITE_FIELD));

                data[index++] = new MovieData(id, title, date, poster, vote, plot, lastChosen, favorite);
            } while (cursor.moveToNext());
        cursor.close();
        return data;
    }

    public static String buildTrailersOrReviewsURL(String movieId, String videosOrReviews)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(movieId)
                .appendPath(videosOrReviews)
                .appendQueryParameter("api_key", BuildConfig.MOVIE_API_KEY);

        //System.out.println(builder.build().toString());
        return builder.build().toString();
    }

    public static String readPopularMovieData(String movieUrl) {

        movieUrl = movieUrl.trim();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String popularMovieJSON = null;

        System.out.println("Call count: " + callCount++);

        try {
            URL url = new URL(movieUrl);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            popularMovieJSON = buffer.toString();
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return popularMovieJSON;
    }

    public static TrailerObject [] getVideosFromUrl(String url) throws JSONException
    {
        String data = readPopularMovieData(url);
        JSONObject fullJson = new JSONObject(data);
        JSONArray array = fullJson.getJSONArray("results");
        TrailerObject [] retVal = new TrailerObject [array.length()];
        for (int i = 0; i < array.length(); i++)
        {
            //https://www.youtube.com/watch?v=
            JSONObject o = array.getJSONObject(i);
            String key = o.getString("key");
            key = "https://www.youtube.com/watch?v=" + key;
            retVal[i] = new TrailerObject(
                    o.getString("name"),
                    key
            );
        }
        return retVal;
    }

    public static ReviewObject [] getReviewsFromUrl(String url) throws JSONException
    {
        String data = readPopularMovieData(url);
        JSONObject fullJson = new JSONObject(data);
        JSONArray array = fullJson.getJSONArray("results");
        ReviewObject [] retVal = new ReviewObject [array.length()];
        for (int i = 0; i < array.length(); i++)
        {
            JSONObject o = array.getJSONObject(i);
            retVal[i] = new ReviewObject(
                    o.getString("id"),
                    o.getString("content"),
                    o.getString("author"),
                    o.getString("url")
            );
        }
        return retVal;
    }

    public static String getPopularMovieURL(String popOrRated)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(popOrRated)
                .appendQueryParameter("api_key", BuildConfig.MOVIE_API_KEY);

        //System.out.println(builder.build().toString());
        return builder.build().toString();
    }

    public static ReviewObject [] unpackReviews(byte [] blob)
    {
        String json = new String(blob);
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ArrayList<ReviewObject>>(){}.getType());
    }


    public static String packReviews(ReviewObject [] r)
    {
        Gson gson = new Gson();
        return gson.toJson(r);
    }

    public static String packTrailers(TrailerObject[] r)
    {
        Gson gson = new Gson();
        return gson.toJson(r);
    }

    public static  MovieData[] getMoviePosters(String jsonString) throws JSONException
    {
        JSONObject fullJson = new JSONObject(jsonString);
        JSONArray array = fullJson.getJSONArray("results");
        MovieData [] retVal = new MovieData [array.length()];
        for (int i = 0; i < array.length(); i++)
        {
            ContentValues values = new ContentValues();
            JSONObject o = array.getJSONObject(i);
            retVal[i] = new MovieData(
                    o.getString("id"),
                    o.getString("title"),                      // Title
                    o.getString("release_date"),               // Release Date
                    buildImageURL(o.getString("poster_path")), // Poster Url
                    o.getString("vote_average"),               // Vote average
                    o.getString("overview"),                   // Plot
                    lastChosen,
                    "no");                               // Source (Popular / Top)
            //Log.v(LOG_TAG, "ID: " + retVal[i].getId());

            String a,b;


            //If it doesn't exist in the DB, create it in the DB.
            Cursor cursor;
            cursor = c.getContentResolver().query(
                    MovieProvider.CONTENT_URI,
                    null,
                    MovieProvider.ID_FIELD + " =?",
                    new String [] {retVal[i].getId() + ""},
                    null
            );

            //RateLimit is 30 requests per 10 seconds;
            //This will fail!

            ReviewObject [] r = null; // = getReviewsFromUrl(b);
            TrailerObject [] t = null;// = getVideosFromUrl(a);

            retVal[i].setReviewObject(r);
            retVal[i].setTrailerObject(t);

            if (cursor != null && cursor.getCount() == 0) {
                values.put(MovieProvider.ID_FIELD, retVal[i].getId());
                values.put(MovieProvider.PLOT_FIELD, retVal[i].getPlot_synopsis());
                values.put(MovieProvider.POSTER_URL_FIELD, retVal[i].getPoster_url());
                values.put(MovieProvider.RELEASE_DATE_FIELD, retVal[i].getRelease_date());
                values.put(MovieProvider.TITLE_FIELD, retVal[i].getTitle());
                values.put(MovieProvider.VOTER_AVERAGE_FIELD, retVal[i].getVote_average());
                values.put(MovieProvider.SOURCE_FIELD, lastChosen);
                values.put(MovieProvider.REVIEW_FIELD, packReviews(r));
                values.put(MovieProvider.TRAILER_FIELD, packTrailers(t));
                values.put(MovieProvider.FAVORITE_FIELD, "no");
                Uri uri = c.getContentResolver().insert(MovieProvider.CONTENT_URI, values);
                cursor.close();
            }

        }
        return retVal;
    }
}
