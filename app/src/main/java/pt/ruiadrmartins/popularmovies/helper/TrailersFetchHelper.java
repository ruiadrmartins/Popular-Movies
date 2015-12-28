package pt.ruiadrmartins.popularmovies.helper;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import pt.ruiadrmartins.popularmovies.BuildConfig;
import pt.ruiadrmartins.popularmovies.data.Trailer;

/**
 * Created by ruimartins on 28-12-2015.
 */
public class TrailersFetchHelper {


    final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
    final String TRAILER_SUFFIX = "/videos?";
    final String API_PARAM = "api_key";

    private String LOG_TAG;

    public TrailersFetchHelper(String tag) {
        LOG_TAG = tag;
    }

    /**
     * Adapted from Udacity Sunshine App example
     * <a href="https://github.com/udacity/Sunshine-Version-2">Sunshine</a>
     * Changed
     * @param params
     * @return
     */
    public ArrayList<Trailer> doInBackground(Integer... params) {

        String dataJson;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        int movieId = params[0];

        try {
            Uri uri = Uri.parse(MOVIES_BASE_URL + String.valueOf(movieId) + TRAILER_SUFFIX).buildUpon()
                    .appendQueryParameter(API_PARAM, BuildConfig.TMDB_API_KEY)
                    .build();

            URL url = new URL(uri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
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
                return null;
            }

            dataJson = buffer.toString();

            Log.v("TR", dataJson);

            return getTrailerDataFromJson(movieId, dataJson);

        }  catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally{
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
    }

    private ArrayList<Trailer> getTrailerDataFromJson(int movieId, String trailerJson) throws JSONException {

        final String TRAILER_KEY = "key";
        final String TRAILER_NAME = "name";
        final String TRAILER_SITE = "site";

        JSONObject mJson = new JSONObject(trailerJson);
        JSONArray resultsArray = mJson.getJSONArray("results");

        ArrayList<Trailer> result = new ArrayList<>();

        for(int i=0;i<resultsArray.length();i++) {
            JSONObject trailerData = resultsArray.getJSONObject(i);

            String key = trailerData.getString(TRAILER_KEY);
            String name = trailerData.getString(TRAILER_NAME);
            String site = trailerData.getString(TRAILER_SITE);

            Trailer trailer = new Trailer(movieId,key,name,site);

            result.add(trailer);
        }

        return result;
    }
}
