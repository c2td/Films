package com.example.pm.films;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/** This class is responsible for loading the films list for MainActivity */

class FilmLoader extends AsyncTaskLoader<List<Film>> {

    private List<Film> mFilms;

    FilmLoader(Context context) {
        super(context);
        mFilms = new ArrayList<>();
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Film> loadInBackground() {

        // Perform the network request, parse the response, and extract a list of films
        // but only if the film list is empty
        if (mFilms.size() == 0) {
            mFilms = QueryUtils.fetchFilmData();
        }

        return mFilms;
    }
}
