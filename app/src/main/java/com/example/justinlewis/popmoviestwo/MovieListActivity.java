package com.example.justinlewis.popmoviestwo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.justinlewis.popmoviestwo.Task.FetchMovieDataTask;
import com.squareup.picasso.Picasso;

import com.example.justinlewis.popmoviestwo.Objects.MovieData;

import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    List<MovieData> mList;
    private String last;
    SimpleItemRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        last = "popular";
        Utility.lastChosen = last;
        Utility.c = this;
        mList = new ArrayList<MovieData>();


        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        */

        View recyclerView = findViewById(R.id.movie_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        FetchMovieDataTask t = new FetchMovieDataTask(mList, adapter);
        t.execute(last);

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new SimpleItemRecyclerViewAdapter(mList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<MovieData> mValues;
        private Context mContext;

        public SimpleItemRecyclerViewAdapter(List<MovieData> items, Context context) {
            this.mContext = context;
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.movie_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final MovieData movieData = mValues.get(position);
            Picasso.with(mContext).load(movieData.getPoster_url())
                    .into(holder.mPhoto);

            //final MovieData movieData = mValues.get(position);

            holder.mPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(MovieDetailFragment.ARG_MOVIE_DATA, movieData);
                        MovieDetailFragment fragment = new MovieDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.movie_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, MovieDetailActivity.class);
                        intent.putExtra(MovieDetailFragment.ARG_MOVIE_DATA, movieData);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final ImageView mPhoto;
            public MovieData mItem;

            public ViewHolder(View view) {
                super(view);
                mPhoto = (ImageView) view.findViewById(R.id.picture_image);
                mPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mPhoto.setAdjustViewBounds(true);
            }

            @Override
            public String toString() {
                return "";
            }
        }
    }
}
