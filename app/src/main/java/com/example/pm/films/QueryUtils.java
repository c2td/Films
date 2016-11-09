package com.example.pm.films;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/* This is an utility class for connecting to the network and parsing the retrieved JSON data */

class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getName();

    // String constants for building query URLs to The Movie DB API (https://www.themoviedb.org/documentation/api)
    private static final String[] FILMS_URL_BITS = {"http://api.themoviedb.org/3/discover/movie?primary_release_date.gte=",
            "&primary_release_date.lte=", "&sort_by=popularity.desc&"};
    private static final String BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/w154/";
    private static final String API_KEY = "api_key=5680c63c995f784e60bf474f1aec61c0";
    private static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final String VIDEOS = "/videos?";
    private static final String CREDITS = "/credits?";

    // JSON attributes
    private static final String TAG_TITLE = "title";
    private static final String TAG_ID = "id";
    private static final String TAG_RESULTS= "results";
    private static final String TAG_POSTER_PATH= "poster_path";
    private static final String TAG_OVERVIEW= "overview";
    private static final String TAG_KEY= "key";
    private static final String TAG_CAST= "cast";
    private static final String TAG_NAME= "name";


    private static ArrayList<Film> extractFilmDataFromJson(String jsonResponse) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(jsonResponse)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding films to
        ArrayList<Film> films = new ArrayList<>();

        try {

            JSONObject baseJson = new JSONObject(jsonResponse);
            JSONArray resultsArray = baseJson.getJSONArray(TAG_RESULTS);

            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject currentFilm = resultsArray.getJSONObject(i);
                String title = currentFilm.getString(TAG_TITLE);
                long id = currentFilm.getLong(TAG_ID);
                String posterPath = currentFilm.getString(TAG_POSTER_PATH);
                String description = currentFilm.getString(TAG_OVERVIEW);
                if (!posterPath.equals("null") && !description.equals("")) {
                    Film film = new Film(id, title, POSTER_BASE_URL + posterPath, description);
                    films.add(film);
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        return films;
    }

    /** Fetches a list of films */
    static List<Film> fetchFilmData() {

        String[] datesRange = getDates();

        // Create URL object
        URL url = createUrl(FILMS_URL_BITS[0] + datesRange[1] +
                FILMS_URL_BITS[1] + datesRange[0] + FILMS_URL_BITS[2] + API_KEY);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of films
        return extractFilmDataFromJson(jsonResponse);

    }

    /** A helper method for retrieving the date range between current date and month before */
    private static String[] getDates() {

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

    /** Makes a separate Json request to retrieve and store video url */
    static String parseVideoData(Film film) {

        URL videoUrl = createUrl(BASE_URL + film.getId() + VIDEOS + API_KEY);
        try {
            String jsonResponse = makeHttpRequest(videoUrl);
            JSONObject baseJson = new JSONObject(jsonResponse);
            JSONArray videosArray = baseJson.getJSONArray(TAG_RESULTS);
            if (videosArray.length() > 0) {
                JSONObject currentFilm = videosArray.getJSONObject(0); // get the first trailer if available
                film.setVideoUrl(YOUTUBE_URL + currentFilm.getString(TAG_KEY));
            } else {
                film.setVideoUrl("N/A"); // not every film has a trailer available
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
        }

        return film.getVideoUrl();
    }

    /** Makes a separate Json request to retrieve and store credits (cast) data, maximum of 3 names */
    static String[] parseCreditsData(Film film) {

        String[] cast = new String[3];
        URL castUrl = createUrl(BASE_URL + film.getId() + CREDITS + API_KEY);

            try {
                String jsonResponse = makeHttpRequest(castUrl);
                JSONObject baseJson = new JSONObject(jsonResponse);
                JSONArray castArray = baseJson.getJSONArray(TAG_CAST);

                //  get max three names in the cast array if available
                if (castArray.length() > 2) {
                    for (int i = 0; i < 3; i++) {
                        cast[i] = castArray.getJSONObject(i).getString(TAG_NAME);
                    }
                } else {  // if less than three, get those
                    for (int i = 0; i < castArray.length(); i++) {
                        cast[i] = castArray.getJSONObject(i).getString(TAG_NAME);
                    }
                }
                film.setCast(cast);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the JSON results", e);
            }

        return cast;
    }

    /**vReturns new URL object from the given string URL */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /** Makes an HTTP request to the given URL and return a String as the response */
    private static String makeHttpRequest(URL url) throws IOException {

        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // If the request was successful (response code 200),
                // then read the input stream and parse the response.
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        return jsonResponse;
    }

    /** A helper method that reads from the input stream line by line and applies it to a StringBuilder object */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}
