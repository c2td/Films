package com.example.pm.films;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

/** This class represents the FilmDetailsActivity
 *  This activity opens when a poster is clicked in the MainActivity */

public class FilmDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<String>> {

    // loader id for cast query
    private static final int VIDEO_CAST_LOADER_ID = 2;

    private Film mFilm;
    private DetailsLoader mDetailsLoader;

    @Override
    public Loader<List<String>> onCreateLoader(int i, Bundle bundle) {

        // Create a new loader
        mDetailsLoader = new DetailsLoader(this, mFilm);
        return mDetailsLoader;
    }

    /** Updates the view with trailer button and cast data*/
    @Override
    public void onLoadFinished(Loader<List<String>> loader, final List<String> filmDetails) {

        // update the cast view
        TextView castView = (TextView) findViewById(R.id.details_cast);
        StringBuilder cast = new StringBuilder();

        // filmDetails contains video trailer url as first element and cast names as the next ones
        for (int i = 1; i < filmDetails.size(); i++) {
            cast.append(filmDetails.get(i));
            if (i < filmDetails.size() - 1) {
                cast.append("\n");
            }
        }

        castView.setText(cast);

        // update the trailer button url and make the button visible
        if (!filmDetails.get(0).equals("N/A")) {
            Button mTrailer = (Button) findViewById(R.id.play_trailer);
            mTrailer.setVisibility(View.VISIBLE);
            mTrailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(filmDetails.get(0))));
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<List<String>> loader) {
        mDetailsLoader = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_details);

        mFilm = getIntent().getParcelableExtra(FilmAdapter.PARCELABLE_EXTRA);

        TextView title = (TextView) findViewById(R.id.details_title);
        title.setText(mFilm.getTitle());

        TextView description = (TextView) findViewById(R.id.details_description);
        description.setText(mFilm.getDescription());

        ImageView poster = (ImageView) findViewById(R.id.details_poster);
        getLoaderManager().initLoader(VIDEO_CAST_LOADER_ID, null, this);

        // display the poster image
        Glide.with(this).load(mFilm.getPosterUrl()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(poster);
    }
}
