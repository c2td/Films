package com.example.pm.films;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Film>> {

    private FilmAdapter mAdapter;
    private List<Film> mFilmList;
    private FilmLoader mFilmLoader;

    private static final int FILM_LOADER_ID = 1;

    @Override
    public Loader<List<Film>> onCreateLoader(int i, Bundle bundle) {

        // Create a new loader for implementing the network query
        mFilmLoader = new FilmLoader(this);
        return mFilmLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<Film>> loader, List<Film> films) {

        if (films != null && !films.isEmpty()) {
            for (Film film : films) {
                mFilmList.add(new Film(film.getId(), film.getTitle(), film.getPosterUrl(), film.getDescription()));
            }
           mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Film>> loader) {
        mFilmLoader = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mFilmList = new ArrayList<>();
        mAdapter = new FilmAdapter(this, mFilmList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // set up the loader and connectivity managers
        initializeLoaderAssets(FILM_LOADER_ID);
    }

    /** A helper method for initializing loader and connectivity manager */
    private void initializeLoaderAssets(int loaderId) {

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().initLoader(loaderId, null, this);
        } else {
            // Update empty state with no connection error message
            TextView emptyStateTextView = (TextView) findViewById(R.id.empty_view);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText(R.string.no_internet_connection);
        }

    }

}



