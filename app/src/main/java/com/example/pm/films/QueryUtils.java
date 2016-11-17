package com.example.pm.films;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/* This is an utility class for parsing the retrieved JSON data */

class QueryUtils {

    // String constants for building query URLs to The Movie DB API (https://www.themoviedb.org/documentation/api)
    static final String[] FILMS_URL_BITS = {"http://api.themoviedb.org/3/discover/movie?primary_release_date.gte=",
            "&primary_release_date.lte=", "&sort_by=popularity.desc&"};
    static final String BASE_URL = "https://api.themoviedb.org/3/movie/";
    static final String API_KEY = "api_key=5680c63c995f784e60bf474f1aec61c0";
    static final String VIDEOS = "/videos?";
    static final String CREDITS = "/credits?";

    private static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w154/";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    // JSON attributes
    private static final String TAG_TITLE = "title";
    private static final String TAG_ID = "id";
    private static final String TAG_RESULTS = "results";
    private static final String TAG_POSTER_PATH = "poster_path";
    private static final String TAG_OVERVIEW = "overview";
    private static final String TAG_KEY = "key";
    private static final String TAG_CAST = "cast";
    private static final String TAG_CREW = "crew";
    private static final String TAG_JOB = "job";
    private static final String TAG_NAME = "name";
    private static final String DIRECTOR = "Director";

    /** A helper method for retrieving the date range between current date and month before */
    static String[] getDates() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();
        Date d1 = cal.getTime();
        cal.add(Calendar.DATE, -30);
        Date d2 = cal.getTime();

        String[] dates = new String[2];
        dates[0] = dateFormat.format(d1);
        dates[1] = dateFormat.format(d2);

        return dates;
    }

    /** Retrieves film credits listing, max three actors and a director */
    static ArrayList<String> parseCreditsData(JSONObject response) throws JSONException {

        // cast data
        JSONArray creditsArray = response.getJSONArray(TAG_CAST);

        // crew data
        JSONArray crewArray = response.getJSONArray(TAG_CREW);

        ArrayList<String> credits = new ArrayList<>();

        //  get max three names from the cast array
        for (int i = 0; i < 3 && i < creditsArray.length(); i++) {
            credits.add(creditsArray.getJSONObject(i).getString(TAG_NAME));
        }

        // find the director entry
        for (int i = 0; i < crewArray.length(); i++) {
            if (crewArray.getJSONObject(i).getString(TAG_JOB).equals(DIRECTOR)) {
                credits.add(crewArray.getJSONObject(i).getString(TAG_NAME));
                break;
            }
        }

        return credits;
    }

    /** Retrieves film trailer url */
    static String parseTrailerData(JSONObject response) throws JSONException {

        JSONArray videosArray = response.getJSONArray(TAG_RESULTS);

        if (videosArray.length() > 0) {
            // get the first trailer if available
            JSONObject currentFilm = videosArray.getJSONObject(0);
            return YOUTUBE_URL + currentFilm.getString(TAG_KEY);
        } else {
            return "N/A"; // not every film has a trailer available
        }
    }

    /** Retrieves the film listing */
    static ArrayList<Film> parseFilmData(JSONObject response) throws JSONException {

        ArrayList<Film> sFilms = new ArrayList<>();
        JSONArray resultsArray = response.getJSONArray(TAG_RESULTS);

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject currentFilm = resultsArray.getJSONObject(i);
            String title = currentFilm.getString(TAG_TITLE);
            long id = currentFilm.getLong(TAG_ID);
            String posterPath = currentFilm.getString(TAG_POSTER_PATH);
            String description = currentFilm.getString(TAG_OVERVIEW);
            if (!posterPath.equals("null") && !description.equals("")) {
                Film film = new Film(id, title, POSTER_BASE_URL + posterPath, description);
                sFilms.add(film);
            }
        }

        return sFilms;
    }
}
