package pt.ruiadrmartins.popularmovies;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

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

import pt.ruiadrmartins.popularmovies.data.Movie;
import pt.ruiadrmartins.popularmovies.data.MovieContract;
import pt.ruiadrmartins.popularmovies.data.MoviesAdapter;
import pt.ruiadrmartins.popularmovies.data.MoviesCursorAdapter;
import pt.ruiadrmartins.popularmovies.data.Utilities;

public class PopularMoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String MOVIE_LIST_PARCELABLE_KEY = "movieList";
    private final String SORT_BY_KEY = "sortBy";
    private MoviesAdapter adapter;
    private MoviesCursorAdapter cursorAdapter;
    private ArrayList<Movie> movieList;
    private GridView gridView;
    private TextView noMoviesFound;

    private static final int CURSOR_LOADER_ID = 0;

    // Store sorting on this variable when not saving on onSaveInstanceState()
    private String sortBy;

    public PopularMoviesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get shared preferences for activity
        sortBy = Utilities.currentPreference(getActivity());

        // Get saved data if it was stored in savedInstanceState
        // or initialize movie list array
        if(savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_LIST_PARCELABLE_KEY)) {
            // No saved instance of movie list
            movieList = new ArrayList<>();
            updateMovieList();
        } else {
            if(savedInstanceState.containsKey(SORT_BY_KEY)) {
                if(sortBy != null && savedInstanceState.getString(SORT_BY_KEY).equals(sortBy)) {
                    // Sorting is the same
                    movieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_PARCELABLE_KEY);
                }
                else {
                    // Sorting changed from Settings
                    movieList = new ArrayList<>();
                    updateMovieList();
                }
            } else {
                // Device Rotation
                movieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_PARCELABLE_KEY);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        noMoviesFound = (TextView) rootView.findViewById(R.id.no_movies_found);
        gridView = (GridView) rootView.findViewById(R.id.gridview);

        updateViews();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Store current sorting setting
        sortBy = Utilities.currentPreference(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // check if sorting has changed on settings
        String newSort = Utilities.currentPreference(getActivity());
        if(!sortBy.equals(newSort)) {
            sortBy = newSort;
            updateMovieList();
            updateViews();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save list data in saveInstanceState to access after
        // whatever triggered this
        outState.putString(SORT_BY_KEY,sortBy);
        outState.putParcelableArrayList(MOVIE_LIST_PARCELABLE_KEY, movieList);
        super.onSaveInstanceState(outState);
    }

    private void updateViews() {
        if(Utilities.sortIsFavorite(sortBy, getActivity())) {
            cursorAdapter = new MoviesCursorAdapter(getActivity(), null, 0, CURSOR_LOADER_ID);
            gridView.setAdapter(cursorAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    if (cursor != null) {
                        int movieIdIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                        int movieId = cursor.getInt(movieIdIndex);
                        Intent intent = new Intent(getActivity(), DetailActivity.class)
                                .setData(MovieContract.MovieEntry.buildMovieUri(movieId));
                        startActivity(intent);
                    }
                }
            });
        } else {
            // Instatiate graphical stuff
            adapter = new MoviesAdapter(getActivity(), movieList);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Movie movieData = adapter.getItem(position);

                    Intent intent = new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra("movieData", movieData);
                    startActivity(intent);
                }
            });
        }
    }

    private void updateMovieList() {
        if(Utilities.sortIsFavorite(sortBy, getActivity())) {
            // Get movies from database
            Log.v("BBB", "AH HELL NO");
            getLoaderManager().restartLoader(CURSOR_LOADER_ID,null,this);

        } else {
            // Call AsyncTask to execute background fetching
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
            fetchMoviesTask.execute(sortBy);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        sortBy = Utilities.currentPreference(getActivity());
        if(Utilities.sortIsFavorite(sortBy,getActivity())) {
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getActivity(),
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
        if(cursor.moveToFirst()){
            noMoviesFound.setText("");
        } else {
            noMoviesFound.setText(getString(R.string.no_movies_found));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursorAdapter.swapCursor(null);
    }

    public class FetchMoviesTask extends AsyncTask<String,Void,ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Adapted from Udacity Sunshine App example
         * <a href="https://github.com/udacity/Sunshine-Version-2">Sunshine</a>
         * Changed
         * @param params
         * @return
         */
        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

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

        /**
         * Change movie list from app
         * */
        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            adapter.clear();
            if(movies!=null) {
                movieList = movies;
                noMoviesFound.setText("");
            }
            for (Movie movie: movies) {
                adapter.add(movie);
            }
        }
    }
}
