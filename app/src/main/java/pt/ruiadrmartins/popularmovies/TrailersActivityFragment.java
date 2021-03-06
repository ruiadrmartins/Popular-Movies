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
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
 * Fragment for TrailersActivity
 */
public class TrailersActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    final static String LOG_TAG = TrailersActivityFragment.class.getSimpleName();
    final static String TRAILERS_MOVIE_ID = "trailersMovieId";
    final static String TRAILERS_MOVIE_URI = "trailersMovieURI";
    final static String TRAILER_PARCELABLE_KEY = "trailerData";

    final static String YOUTUBE_VIDEO_LINK_PREFIX = "https://www.youtube.com/watch?v=";

    private ListView trailerListView;
    private TrailerAdapter adapter;
    private TrailerCursorAdapter cursorAdapter;
    private TextView noTrailersFound;
    private ArrayList<Trailer> trailerList;

    private ShareActionProvider mShareActionProvider;

    private static final int TRAILER_LOADER = 0;

    private int movieId;
    private Uri uri;

    private String shareYoutubeUrl = "Check out this trailer: ";


    public TrailersActivityFragment() {
        setHasOptionsMenu(true);
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

    /**
     * Launches video in Browser or Youtube app
     * @param trailer
     */
    public void startVideoIntent(Trailer trailer) {
        if(trailer.site.toLowerCase().equals("youtube")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_VIDEO_LINK_PREFIX + trailer.key));

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    /**
     * Fetch trailers from API
     * @param movieId
     */
    private void fetchTrailers(int movieId) {
        FetchTrailerTask ftt = new FetchTrailerTask();
        ftt.execute(movieId);
    }

    /**
     * Update data on Trailer list
     * @param list
     */
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_trailers, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareYoutubeUrl);
        return shareIntent;
    }

    // Call to update the share intent
    public void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
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
            return new CursorLoader(
                    getActivity(),
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
            int keyIndex = data.getColumnIndex(MovieContract.TrailerEntry.COLUMN_KEY);
            shareYoutubeUrl += YOUTUBE_VIDEO_LINK_PREFIX + data.getString(keyIndex);
            setShareIntent(createShareTrailerIntent());
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
                boolean first = true;
                for (Trailer trailer: list) {
                    if(first) {
                        shareYoutubeUrl += YOUTUBE_VIDEO_LINK_PREFIX + trailer.key;
                        setShareIntent(createShareTrailerIntent());
                        first=false;
                    }
                    adapter.add(trailer);
                }
            }
        }
    }
}
