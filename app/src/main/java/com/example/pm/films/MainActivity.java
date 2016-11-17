package com.example.pm.films;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** This class represents the MainActivity
 *  This activity displays a list of films when the app is opened */

public class MainActivity extends AppCompatActivity {

    private FilmAdapter mAdapter;
    private List<Film> mFilmList;

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

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

        // check connectivity and make a request to retrieve the film list
        if (isConnected()) {
            loadFilmData();
        } else {
            // Update empty state with no connection error message
            TextView emptyStateTextView = (TextView) findViewById(R.id.empty_view);
            emptyStateTextView.setVisibility(View.VISIBLE);
            emptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    /** A helper method for checking if there is network connection */
    private boolean isConnected() {

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    /** Loads the film data from the specified url */
    private void loadFilmData() {

        // get the current date range to build the url
        String[] datesRange = QueryUtils.getDates();

        // build the url. Currently queries the most popular films for the last month
        String url = QueryUtils.FILMS_URL_BITS[0] + datesRange[1] +
                QueryUtils.FILMS_URL_BITS[1] + datesRange[0] + QueryUtils.FILMS_URL_BITS[2] + QueryUtils.API_KEY;

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            ArrayList<Film> tempFilms = QueryUtils.parseFilmData(response);
                            for (Film f : tempFilms) {
                                mFilmList.add(f);
                            }
                            mAdapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            Log.e(LOG_TAG, "Problem parsing the JSON results", e);
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, error.getMessage());
            }
        });

        // Add the request to the RequestQueue
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonRequest);
    }

}