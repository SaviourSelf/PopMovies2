package com.example.justinlewis.popmoviestwo;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.justinlewis.popmoviestwo.Objects.DummyContent;
import com.example.justinlewis.popmoviestwo.Objects.MovieData;


public class MovieDetailFragment extends Fragment {

    public static final String ARG_MOVIE_DATA = "item_id";

    private MovieData mItem;


    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_MOVIE_DATA)) {

            mItem = getArguments().getParcelable(ARG_MOVIE_DATA);
            Log.v("Item title", " " + mItem.getTitle());
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(mItem.getTitle());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        // Show the dummy content as text in a TextView.
        Log.v("Item is null?", " " + (mItem == null));
        if (mItem != null) {
           ((TextView) rootView.findViewById(R.id.movie_title)).setText(mItem.getTitle());
        }

        return rootView;
    }
}
