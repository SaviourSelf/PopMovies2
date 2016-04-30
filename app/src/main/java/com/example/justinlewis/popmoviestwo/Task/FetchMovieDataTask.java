package com.example.justinlewis.popmoviestwo.Task;

import android.os.AsyncTask;
import android.util.Log;

import com.example.justinlewis.popmoviestwo.Objects.MovieData;
import com.example.justinlewis.popmoviestwo.Utility;

import org.json.JSONException;

import java.util.List;

/**
 * Created by Justin Lewis on 4/29/2016.
 */
public class FetchMovieDataTask extends AsyncTask<String, Void, MovieData[]> {

    List<MovieData> mList;

    public FetchMovieDataTask(List<MovieData> list)
    {
        mList = list;
    }

    private final String LOG_TAG = FetchMovieDataTask.class.getSimpleName();

    private final String FAVORITE = "FAVORITE";

    @Override
    protected MovieData[] doInBackground(String... params) {
        if (params.length == 0)
            return null;

        if (params[0].equals(FAVORITE)) {
            return Utility.getFavorites();
        }

        String movieUrl = Utility.getPopularMovieURL(params[0]);
        String json = Utility.readPopularMovieData(movieUrl);

        if (json == null || json.isEmpty()) {
            //No Internet, get from DB.
            return Utility.fetchFromDb();
        }

        MovieData[] movieData = null;

        try {
            movieData = Utility.getMoviePosters(json);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error getting JSON");
        }
        return movieData;
    }

    @Override
    protected void onPostExecute(MovieData[] capturedList) {
        super.onPostExecute(capturedList);
        mList.clear();
        for (MovieData s : capturedList)
            mList.add(s);
    }
}