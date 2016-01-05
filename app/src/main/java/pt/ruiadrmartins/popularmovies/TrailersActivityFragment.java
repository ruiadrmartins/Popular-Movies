package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ruiadrmartins.popularmovies.data.MovieContract;
import pt.ruiadrmartins.popularmovies.data.Trailer;
import pt.ruiadrmartins.popularmovies.data.TrailerAdapter;
import pt.ruiadrmartins.popularmovies.data.TrailerCursorAdapter;
import pt.ruiadrmartins.popularmovies.helper.TrailersFetchHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrailersActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final static String TRAILERS_MOVIE_ID = "trailersMovieId";
    final static String TRAILERS_MOVIE_URI = "trailersMovieURI";
    final static String TRAILER_PARCELABLE_KEY = "trailerData";

    private ListView trailerListView;
    private TrailerAdapter adapter;
    private TrailerCursorAdapter cursorAdapter;
    private TextView noTrailersFound;
    private ArrayList<Trailer> trailerList;

    private static final int TRAILER_LOADER = 0;

    private int movieId;
    private Uri uri;

    public TrailersActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trailers, container, false);

        noTrailersFound = (TextView) rootView.findViewById(R.id.no_trailers_found);
        trailerListView = (ListView) rootView.findViewById(R.id.trailer_list);

        // Fetch arguments from bundle arguments
        Bundle arguments = getArguments();

        // If arguments exist
        if (arguments!= null) {
            // If fetch data from API
            if (arguments.containsKey(TRAILERS_MOVIE_ID)) {
                adapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>());
                trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        startVideoIntent(adapter.getItem(position));
                    }
                });
                trailerListView.setAdapter(adapter);

                // If data was saved
                if (savedInstanceState != null && savedInstanceState.containsKey(TRAILER_PARCELABLE_KEY)) {
                    trailerList = savedInstanceState.getParcelableArrayList(TRAILER_PARCELABLE_KEY);
                    updateTrailerList(trailerList);
                } else {
                    movieId = arguments.getInt(TRAILERS_MOVIE_ID);
                    fetchTrailers(movieId);
                }
            } else {
                // If data is local
                uri = arguments.getParcelable(TRAILERS_MOVIE_URI);
                cursorAdapter = new TrailerCursorAdapter(getActivity(), null, 0, TRAILER_LOADER);
                trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                        int keyIndex = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_KEY);
                        String key = cursor.getString(keyIndex);

                        int nameIndex = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_NAME);
                        String name = cursor.getString(nameIndex);

                        int siteIndex = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_SITE);
                        String site = cursor.getString(siteIndex);

                        Trailer trailer = new Trailer(movieId, key, name, site);
                        startVideoIntent(trailer);
                    }
                });
                trailerListView.setAdapter(cursorAdapter);
            }
        }

        return rootView;
    }

    public void startVideoIntent(Trailer trailer) {

        final String YOUTUBE_VIDEO_LINK_PREFIX = "https://www.youtube.com/watch?v=";

        if(trailer.site.toLowerCase().equals("youtube")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_VIDEO_LINK_PREFIX + trailer.key));

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private void fetchTrailers(int movieId) {
        FetchTrailerTask ftt = new FetchTrailerTask();
        ftt.execute(movieId);
    }

    private void updateTrailerList(ArrayList<Trailer> list) {
        if(list != null && list.size()>0) {
            adapter = new TrailerAdapter(getActivity(),list);
            trailerListView.setAdapter(adapter);
            noTrailersFound.setText("");
        } else {
            adapter = new TrailerAdapter(getActivity(),new ArrayList<Trailer>());
            trailerListView.setAdapter(adapter);
            noTrailersFound.setText(getResources().getText(R.string.no_trailers_found));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save data in saveInstanceState to access after
        // whatever triggered this
        outState.putParcelableArrayList(TRAILER_PARCELABLE_KEY, trailerList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        // If data is local
        if (arguments != null && arguments.containsKey(TRAILERS_MOVIE_URI)) {
            uri = arguments.getParcelable(TRAILERS_MOVIE_URI);
            movieId = Integer.valueOf(MovieContract.TrailerEntry.getMovieIdFromUri(uri));
            getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != uri) {

            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    //intent.getData(),
                    uri,
                    null,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
        if(data.moveToFirst()){
            noTrailersFound.setText("");
        } else {
            noTrailersFound.setText(getString(R.string.no_trailers_found));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    public class FetchTrailerTask extends AsyncTask<Integer,Void,ArrayList<Trailer>> {

        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        TrailersFetchHelper trailersFetchHelper = new TrailersFetchHelper(LOG_TAG);

        @Override
        protected ArrayList<Trailer> doInBackground(Integer... params) {
            return trailersFetchHelper.doInBackground(params);
        }

        @Override
        protected void onPostExecute(ArrayList<Trailer> list) {
            super.onPostExecute(list);
            adapter.clear();
            if(list!=null && list.size()>0) {
                trailerList = list;
                noTrailersFound.setText("");
                for (Trailer trailer: list) {
                    adapter.add(trailer);
                }
            }
            //updateTrailerList(list);
        }
    }
}
