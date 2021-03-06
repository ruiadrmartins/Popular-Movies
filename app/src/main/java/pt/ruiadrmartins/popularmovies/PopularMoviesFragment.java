package pt.ruiadrmartins.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;

import pt.ruiadrmartins.popularmovies.data.Movie;
import pt.ruiadrmartins.popularmovies.data.MovieContract;
import pt.ruiadrmartins.popularmovies.data.MoviesAdapter;
import pt.ruiadrmartins.popularmovies.data.MoviesCursorAdapter;
import pt.ruiadrmartins.popularmovies.helper.MoviesFetchHelper;

public class PopularMoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String MOVIE_LIST_PARCELABLE_KEY = "movieList";
    private final String SORT_BY_KEY = "sortBy";
    private final String SELECTED_ITEM_KEY = "selectedItem";

    private MoviesAdapter adapter;
    private MoviesCursorAdapter cursorAdapter;
    private ArrayList<Movie> movieList;
    private GridView gridView;
    private TextView noMoviesFound;
    private TextView sortTV;

    private int selectedItem = 0;

    private static final int CURSOR_LOADER_ID = 0;

    // Store sorting on this variable when not saving on onSaveInstanceState()
    private String sortBy;

    public interface Callback {
        void onItemSelected(Uri uri);
        void onItemSelected(Movie movie);
    }

    public PopularMoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get shared preferences for activity
        sortBy = Utilities.currentPreference(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        noMoviesFound = (TextView) rootView.findViewById(R.id.no_movies_found);
        gridView = (GridView) rootView.findViewById(R.id.gridview);
        sortTV = (TextView) rootView.findViewById(R.id.sort);
        sortTV.setText(getString(R.string.format_sort, Utilities.getSortingName(sortBy, getActivity())));

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
                    if(movieList!=null && movieList.size()>0) noMoviesFound.setText("");
                }
                else {
                    // Sorting changed from Settings
                    movieList = new ArrayList<>();
                    updateMovieList();
                }
            } else {
                // Device Rotation
                movieList = savedInstanceState.getParcelableArrayList(MOVIE_LIST_PARCELABLE_KEY);
                if(movieList!=null && movieList.size()>0) noMoviesFound.setText("");
            }
        }

        updateViews();

        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_ITEM_KEY)) {
            selectedItem = savedInstanceState.getInt(SELECTED_ITEM_KEY);
            if(gridView!=null)
                gridView.smoothScrollToPosition(selectedItem);
        }

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
        sortTV.setText(getString(R.string.format_sort, Utilities.getSortingName(sortBy, getActivity())));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save list data in saveInstanceState to access after
        // whatever triggered this
        outState.putString(SORT_BY_KEY,sortBy);
        outState.putParcelableArrayList(MOVIE_LIST_PARCELABLE_KEY, movieList);
        outState.putInt(SELECTED_ITEM_KEY,selectedItem);
        super.onSaveInstanceState(outState);
    }

    /**
     * Change UI elements, when necessary
     */
    void updateViews() {
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

                        ((Callback) getActivity())
                                .onItemSelected(MovieContract.MovieEntry.buildMovieUri(movieId));
                        selectedItem = position;
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

                    ((Callback) getActivity()).onItemSelected(movieData);
                    selectedItem = position;
                }
            });
        }
    }

    /**
     * Update list of movies, when list changed
     */
    void updateMovieList() {
        sortBy = Utilities.currentPreference(getActivity());
        if(Utilities.sortIsFavorite(sortBy, getActivity())) {
            // Get movies from database
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
        gridView.smoothScrollToPosition(selectedItem);

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

        MoviesFetchHelper mfh = new MoviesFetchHelper(LOG_TAG);

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {
            return mfh.doInBackground(params);
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
                for (Movie movie: movies) {
                    adapter.add(movie);
                }
            } else {
                noMoviesFound.setText(getString(R.string.no_movies_found));
            }
        }
    }
}
