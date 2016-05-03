package com.example.justinlewis.popmoviestwo;

import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.justinlewis.popmoviestwo.Objects.MovieData;
import com.example.justinlewis.popmoviestwo.Task.FetchMovieTrailerAndReviewTask;
import com.squareup.picasso.Picasso;


public class MovieDetailFragment extends Fragment {

    public static final String ARG_MOVIE_DATA = "item_id";

    private MovieData mItem;

    TextView movieTitle;
    TextView moviePlot;
    TextView releaseDate;
    TextView voteAverage;
    ImageView imageView;
    MenuItem menuItem;

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
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.getItem(1);
        System.out.println("infalting...");
        this.menuItem = item;
        int icon = android.R.drawable.star_big_on;
        if (mItem != null && !mItem.getFavorite().equals("no")) {
            menu.getItem(1).setIcon(icon);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail, container, false);
        setHasOptionsMenu(true);

        Log.v("Item is null?", " " + (mItem == null));
        if (mItem != null) {
           ((TextView) rootView.findViewById(R.id.movie_title)).setText(mItem.getTitle());
            movieTitle = (TextView) rootView.findViewById(R.id.movie_title);
            moviePlot = (TextView) rootView.findViewById(R.id.plotSynopsisText);
            releaseDate = (TextView) rootView.findViewById(R.id.movieReleaseYear);
            voteAverage = (TextView) rootView.findViewById(R.id.movieVoteAverate);
            imageView = (ImageView) rootView.findViewById(R.id.moviePoster);

            voteAverage.setText("Vote average: " + mItem.getVote_average() + "/10");
            releaseDate.setText(mItem.getRelease_date());
            moviePlot.setText(mItem.getPlot_synopsis());
            movieTitle.setText(mItem.getTitle());
            Picasso.with(rootView.getContext()).load(mItem.getPoster_url())
                    .into(imageView);
            getReviewsAndTrailers();

        }

        return rootView;
    }
    public void getReviewsAndTrailers()
    {
        FetchMovieTrailerAndReviewTask t = new FetchMovieTrailerAndReviewTask(getContext(), mItem, moviePlot);
        t.execute("ReviewsAndTrailers");
    }
}
