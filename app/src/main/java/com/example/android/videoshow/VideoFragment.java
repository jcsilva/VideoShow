package com.example.android.videoshow;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jcsilva on 22/01/17.
 */

public class VideoFragment extends android.support.v4.app.Fragment {

    private SampleGridViewAdapter gridViewAdapter;

    public VideoFragment(){}


    @Override
    public void onStart(){
        super.onStart();
        updateVideoGrid();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        GridView gv = (GridView) rootView.findViewById(R.id.gridview);

        gridViewAdapter = new SampleGridViewAdapter(getActivity() );

        gv.setAdapter(gridViewAdapter);

        return rootView;
    }


    private void updateVideoGrid(){
        new FetchVideoTask().execute();
    }

// MOst popular: /discover/movie?sort_by=popularity.desc&api_key=
    // Best rate: /discover/movie?sort_by=vote_average.desc&api_key=
// API CALL Example: https://api.themoviedb.org/3/movie/550?api_key=


    //Mais populares: https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=
    // Votados: https://api.themoviedb.org/3/discover/movie?sort_by=vote_average.desc&api_key=


    public class FetchVideoTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchVideoTask.class.getSimpleName();

        private final String BASE_POSTER_PATH = " http://image.tmdb.org/t/p/w185/";
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if (strings != null) {
                gridViewAdapter = new SampleGridViewAdapter(getActivity(), strings);
            }
        }

        @Override
        protected String[] doInBackground(String... s) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpsURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String videoListJsonStr = null;

            String sortCriteria = "popularity.desc";
            String apiKey = null;
            try {
                apiKey = Util.getProperty("apikey", getContext());
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error getting api key: " + e);
                e.printStackTrace();
                return null;
            }
            int page = 1;

            try {

                final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String PAGE_PARAM = "page";
                final String API_KEY_PARAM = "api_key";


                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, sortCriteria)
                        .appendQueryParameter(PAGE_PARAM, Integer.toString(page))
                        .appendQueryParameter(API_KEY_PARAM, apiKey)
                        .build();

                Log.v(LOG_TAG, builtUri.toString());
                URL url = new URL(builtUri.toString());

                // Create the request to themoviedb api, and open the connection
                urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                videoListJsonStr = buffer.toString();
                Log.v(LOG_TAG, videoListJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                e.printStackTrace();
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            Log.v(LOG_TAG, videoListJsonStr);

            try {
                return getVideoDataFromJson(videoListJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
                return null;
            }
        }


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getVideoDataFromJson(String videoJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESULTS = "results";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_ID = "id";

            JSONObject videoJson = new JSONObject(videoJsonStr);
            JSONArray resultsVideoArray = videoJson.getJSONArray(OWM_RESULTS);

            String[] results = new String[resultsVideoArray.length()];
            for(int i = 0; i < resultsVideoArray.length(); i++) {

                 // Get the JSON object representing the day
                JSONObject video = resultsVideoArray.getJSONObject(i);

                int id = video.getInt(OWM_ID);
                String posterPath = BASE_POSTER_PATH + video.getString(OWM_POSTER_PATH);

                results[i] = posterPath;
            }

            for (String v : results) {
                Log.v(LOG_TAG, "Video poster path: " + v);
            }
            return results;
        }
    }
}
