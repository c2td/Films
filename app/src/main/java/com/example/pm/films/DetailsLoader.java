package com.example.pm.films;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/** This class is responsible for loading the results for FilmDetailsActivity */

class DetailsLoader extends AsyncTaskLoader<List<String>> {

    private Film mFilm;

    DetailsLoader(Context context, Film film) {
        super(context);
        mFilm = film;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<String> loadInBackground() {

        List<String> filmDetails = new ArrayList<>();

        // retrieve the video url
        filmDetails.add(QueryUtils.parseVideoData(mFilm));

        // retrieve the credits data
        String[] credits = QueryUtils.parseCreditsData(mFilm);

        // add the credits to filmDetails list
        for (String credit : credits) {
            if (credit != null) {
                filmDetails.add(credit);
            } else {
                filmDetails.add("");
            }
        }

        return filmDetails;
    }
}
