package com.example.pm.films;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/** This class represents the FilmDetailsActivity
 *  This activity opens when a poster is clicked in the MainActivity */

public class FilmDetailsActivity extends AppCompatActivity {

    private Film mFilm;
    private ArrayList<String> requestUrls;
    private static final String LOG_TAG = FilmDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_film_details);

        mFilm = getIntent().getParcelableExtra(FilmAdapter.PARCELABLE_EXTRA);

        // create request urls for querying trailer and credits data
        requestUrls = new ArrayList<>();
        requestUrls.add(QueryUtils.BASE_URL + mFilm.getId() + QueryUtils.VIDEOS + QueryUtils.API_KEY);
        requestUrls.add(QueryUtils.BASE_URL + mFilm.getId() + QueryUtils.CREDITS + QueryUtils.API_KEY);

        // check connectivity and make a request to retrieve the details
        if (isConnected()) {
            loadDetailedData();
        }

        TextView title = (TextView) findViewById(R.id.details_title);
        title.setText(mFilm.getTitle());

        TextView description = (TextView) findViewById(R.id.details_description);
        description.setText(mFilm.getDescription());

        ImageView poster = (ImageView) findViewById(R.id.details_poster);

        // display the poster image
        Glide.with(this).load(mFilm.getPosterUrl()).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(poster);
    }

    /** A helper method for checking if there is network connection */
    private boolean isConnected() {

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    /** Loads the trailer url (if available) and credits data */
    private void loadDetailedData() {

        RequestQueue queue = Volley.newRequestQueue(this);

        for (final String url : requestUrls) {
            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            try {

                                if (url.contains(QueryUtils.CREDITS)) {
                                    // parse the credits data
                                    ArrayList<String> data = QueryUtils.parseCreditsData(response);
                                    mFilm.setCast(data);

                                    //update the credits view
                                    TextView castView = (TextView) findViewById(R.id.details_cast);
                                    StringBuilder cast = new StringBuilder();

                                    for (int i = 0; i < data.size(); i++) {

                                        // director entry
                                        if (i == data.size() - 1) {
                                            cast.append("\nD: ");
                                        }
                                        cast.append(data.get(i));
                                        if (i < data.size() - 1) {
                                            cast.append("\n");
                                        }
                                    }

                                    castView.setText(cast);

                                } else if (url.contains(QueryUtils.VIDEOS)) {

                                    // parse the film trailer data
                                    final String data = QueryUtils.parseTrailerData(response);
                                    mFilm.setVideoUrl(data);

                                    //update the trailer button url and make the button visible if the trailer exists
                                    if (!data.equals("N/A")) {
                                        Button mTrailer = (Button) findViewById(R.id.play_trailer);
                                        mTrailer.setVisibility(View.VISIBLE);
                                        mTrailer.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(data)));
                                            }
                                        });
                                    }
                                }

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

            queue.add(jsonRequest);
        }
    }
}
