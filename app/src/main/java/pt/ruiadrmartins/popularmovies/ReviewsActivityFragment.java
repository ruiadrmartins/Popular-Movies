package pt.ruiadrmartins.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

import pt.ruiadrmartins.popularmovies.data.MovieContract;
import pt.ruiadrmartins.popularmovies.data.Review;
import pt.ruiadrmartins.popularmovies.data.ReviewAdapter;
import pt.ruiadrmartins.popularmovies.data.ReviewCursorAdapter;
import pt.ruiadrmartins.popularmovies.helper.ReviewsFetchHelper;

/**
 * Fragment for ReviewsActivity
 */
public class ReviewsActivityFragment extends Fragment implements LoaderCallbacks<Cursor> {

    final static String REVIEWS_MOVIE_ID = "reviewsMovieId";
    final static String REVIEWS_MOVIE_URI = "reviewsMovieURI";
    final static String REVIEW_PARCELABLE_KEY = "reviewData";

    private ListView reviewListView;
    private ReviewAdapter adapter;
    private ReviewCursorAdapter cursorAdapter;
    private TextView noReviewsFound;
    private ArrayList<Review> reviewList;

    private static final int REVIEW_LOADER = 0;

    private int movieId;
    private Uri uri;

    public ReviewsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        noReviewsFound = (TextView) rootView.findViewById(R.id.no_reviews_found);
        reviewListView = (ListView) rootView.findViewById(R.id.review_list);

        // Fetch arguments from bundle arguments
        Bundle arguments = getArguments();

        // If arguments exist
        if(arguments!=null) {
            // If fetch data from API
            if (arguments.containsKey(REVIEWS_MOVIE_ID)) {
                adapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
                reviewListView.setAdapter(adapter);

                // If data was saved
                if (savedInstanceState != null && savedInstanceState.containsKey(REVIEW_PARCELABLE_KEY)) {
                    reviewList = savedInstanceState.getParcelableArrayList(REVIEW_PARCELABLE_KEY);
                    updateReviewList(reviewList);
                } else {
                    movieId = arguments.getInt(REVIEWS_MOVIE_ID);
                    fetchReviews(movieId);
                }
            } else {
                uri = arguments.getParcelable(REVIEWS_MOVIE_URI);
                cursorAdapter = new ReviewCursorAdapter(getActivity(), null, 0, REVIEW_LOADER);
                reviewListView.setAdapter(cursorAdapter);
            }
        }

        return rootView;
    }

    /**
     * Fetch reviews from API
     * @param movieId
     */
    private void fetchReviews(int movieId) {
        FetchReviewTask frt = new FetchReviewTask();
        frt.execute(movieId);
    }

    /**
     * Update data on Review list
     * @param list
     */
    private void updateReviewList(ArrayList<Review> list) {
        if(list!= null && list.size()>0) {
            adapter = new ReviewAdapter(getActivity(),list);
            reviewListView.setAdapter(adapter);
            noReviewsFound.setText("");
        } else {
            adapter = new ReviewAdapter(getActivity(),new ArrayList<Review>());
            reviewListView.setAdapter(adapter);
            noReviewsFound.setText(getResources().getText(R.string.no_reviews_found));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save data in saveInstanceState to access after
        // whatever triggered this
        outState.putParcelableArrayList(REVIEW_PARCELABLE_KEY, reviewList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        // If data is local
        if (arguments != null && arguments.containsKey(REVIEWS_MOVIE_URI)) {
            uri = arguments.getParcelable(REVIEWS_MOVIE_URI);
            movieId = Integer.valueOf(MovieContract.ReviewEntry.getMovieIdFromUri(uri));
            getLoaderManager().initLoader(REVIEW_LOADER, null, this);
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
            noReviewsFound.setText("");
        } else {
            noReviewsFound.setText(getString(R.string.no_reviews_found));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    public class FetchReviewTask extends AsyncTask<Integer,Void,ArrayList<Review>> {

        private final String LOG_TAG = FetchReviewTask.class.getSimpleName();

        ReviewsFetchHelper fetchHelper = new ReviewsFetchHelper(LOG_TAG);

        @Override
        protected ArrayList<Review> doInBackground(Integer... params) {
            return fetchHelper.doInBackground(params);
        }

        @Override
        protected void onPostExecute(ArrayList<Review> list) {
            super.onPostExecute(list);
            adapter.clear();
            if(list!=null && list.size()>0) {
                reviewList = list;
                noReviewsFound.setText("");
                for (Review review: list) {
                    adapter.add(review);
                }
            }
        }
    }
}
