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
import pt.ruiadrmartins.popularmovies.data.Movie;

/**
 * Created by ruimartins on 28-12-2015.
 */
public class MoviesFetchHelper {

    private String LOG_TAG;

    public MoviesFetchHelper(String tag) {
        LOG_TAG = tag;
    }

    /*
     * Adapted from Udacity Sunshine App example
     * <a href="https://github.com/udacity/Sunshine-Version-2">Sunshine</a>
     * Changed
     * @param params
     * @return
     */
    public ArrayList<Movie> doInBackground(String... params) {

        final String FORECAST_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
        final String SORT_PARAM = "sort_by";
        final String API_PARAM = "api_key";

        String moviesJson;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String order = params[0];

        try {
            Uri uri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, order)
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

            moviesJson = buffer.toString();

            return getMovieDataFromJson(moviesJson);

        }  catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON Error ", e);
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

    private ArrayList<Movie> getMovieDataFromJson(String movieJson) throws JSONException {

        final String POSTER_QUAL_HIGH = "w500";
        final String POSTER_QUAL_LOW = "w342";

        final String MOVIE_ID = "id";
        final String TITLE_ELEMENT = "title";
        final String POSTER_PATH_ELEMENT = "poster_path";
        final String SYNOPSIS_ELEMENT = "overview";
        final String RATING_ELEMENT = "vote_average";
        final String RELEASE_DATE_ELEMENT = "release_date";
        final String BASE_POSTER_URL = "http://image.tmdb.org/t/p/" + POSTER_QUAL_LOW + "/";

        JSONObject mJson = new JSONObject(movieJson);
        JSONArray resultsArray = mJson.getJSONArray("results");

        ArrayList<Movie> result = new ArrayList<>();

        for(int i=0;i<resultsArray.length();i++) {
            // original title

            JSONObject movieData = resultsArray.getJSONObject(i);

            int movieId = movieData.getInt(MOVIE_ID);
            String title = movieData.getString(TITLE_ELEMENT);
            String poster = BASE_POSTER_URL + movieData.getString(POSTER_PATH_ELEMENT);
            String synopsis = movieData.getString(SYNOPSIS_ELEMENT);
            double rating = movieData.getDouble(RATING_ELEMENT);
            String releaseDate = movieData.getString(RELEASE_DATE_ELEMENT);

            Movie movie = new Movie(movieId, title,poster,synopsis,rating,releaseDate);

            result.add(movie);
        }

        return result;
    }
}
