package com.example.justinlewis.popmoviestwo.Task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.example.justinlewis.popmoviestwo.BuildConfig;
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

public class FetchMovieTrailerAndReviewTask extends AsyncTask<String, Void, MovieData> {

    private boolean forFavorites = false;
    private Context c;
    private MovieData model;
    private TextView tv;

    public FetchMovieTrailerAndReviewTask(Context c, MovieData model, TextView tv)
    {
        this.model = model;
        this.c = c;
        this.tv = tv;
    }


    @Override
    protected MovieData doInBackground(String... v) {

        if (v[0].equals("queryFavorite"))
        {
            forFavorites = true;
            isFavorite();
            return model;
        }

        if (!v[0].equals("ReviewsAndTrailers"))
        {
            forFavorites = true;
            updateDBFavoriteBackground(v[0]);
            return model;
        }
        String a,b;
        a= buildTrailersOrReviewsURL(model.getId() + "", "videos");
        b= buildTrailersOrReviewsURL(model.getId() + "", "reviews");
        ReviewObject [] r = null; //getReviewsFromUrl(b);
        TrailerObject [] t = null; //getVideosFromUrl(a);
        try {
            r = getReviewsFromUrl(b);
            t = getVideosFromUrl(a);
            updateDB(r,t);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        model.setReviewObject(r);
        model.setTrailerObject(t);
        return model;
    }
    @Override
    protected void onPostExecute(MovieData model) {
        super.onPostExecute(model);
        if (forFavorites == true)
            return;
        TrailerObject [] trailers = model.getTrailerObject();
        ReviewObject [] reviews = model.getReviewObject();

        String text = tv.getText() + "\n\n\nTrailers:\n\n";

        for (TrailerObject t : trailers)
        {
            text = text + t.trailerName + "\n" + t.trailerUrl + "\n\n";
        }

        for (ReviewObject r : reviews)
        {
            text = text + r.getContent()+ "\n";
        }
        tv.setText(text);
    }

    private void isFavorite()
    {
        Cursor cursor;
        cursor = c.getContentResolver().query(
                MovieProvider.CONTENT_URI,
                null,
                MovieProvider.FAVORITE_FIELD + " =? AND " + MovieProvider.ID_FIELD + " =? ",
                new String [] {"yes", model.getId() + ""},
                null
        );
        if (cursor.getCount() > 0) {
            model.setFavorite("yes");
        } else {
            model.setFavorite("no");
        }
        cursor.close();
    }


    private void updateDBFavoriteBackground(String str)
    {
        ContentValues values = new ContentValues();
        values.put(MovieProvider.FAVORITE_FIELD, str);
        int ret = c.getContentResolver().update(MovieProvider.CONTENT_URI, values, MovieProvider.ID_FIELD + " =? ", new String[] {model.getId() + ""});
        System.out.println("DB UPDATE FAVORITE RETURN: " + ret);
    }


    private void updateDB(ReviewObject [] reviewObjects, TrailerObject [] trailerObjects)
    {
        ContentValues values = new ContentValues();
        values.put(MovieProvider.REVIEW_FIELD, packReviews(reviewObjects));
        values.put(MovieProvider.TRAILER_FIELD, packTrailers(trailerObjects));

        int ret = c.getContentResolver().update(MovieProvider.CONTENT_URI, values, model.getId() + "", null);
        System.out.println("DB UPDATE REVIEW RETURN: " + ret);
    }


    private String buildTrailersOrReviewsURL(String movieId, String videosOrReviews)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(movieId)
                .appendPath(videosOrReviews)
                .appendQueryParameter("api_key", BuildConfig.MOVIE_API_KEY);

        return builder.build().toString();
    }

    private TrailerObject [] getVideosFromUrl(String url) throws JSONException
    {
        String data = readPopularMovieData(url);
        JSONObject fullJson = new JSONObject(data);
        JSONArray array = fullJson.getJSONArray("results");
        TrailerObject [] retVal = new TrailerObject [array.length()];
        for (int i = 0; i < array.length(); i++)
        {
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

    private ReviewObject [] getReviewsFromUrl(String url) throws JSONException
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


    private ReviewObject [] unpackReviews(byte [] blob)
    {
        String json = new String(blob);
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ArrayList<ReviewObject>>() {
        }.getType());
    }


    private String packReviews(ReviewObject[] r)
    {
        Gson gson = new Gson();
        return gson.toJson(r);
    }

    private String packTrailers(TrailerObject[] r)
    {
        Gson gson = new Gson();
        return gson.toJson(r);
    }

    private String readPopularMovieData(String movieUrl) {

        movieUrl = movieUrl.trim();
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String popularMovieJSON = null;

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
}