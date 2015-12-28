package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.database.Cursor;
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
 * A placeholder fragment containing a simple view.
 */
public class ReviewsActivityFragment extends Fragment implements LoaderCallbacks<Cursor> {

    final String REVIEW_PARCELABLE_KEY = "reviewData";

    private static final int REVIEW_LOADER = 0;

    private ListView reviewListView;
    private ReviewAdapter adapter;
    private ReviewCursorAdapter cursorAdapter;
    private TextView noReviewsFound;
    private ArrayList<Review> reviewList;

    private int movieId = 0;

    public ReviewsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        noReviewsFound = (TextView) rootView.findViewById(R.id.no_reviews_found);
        reviewListView = (ListView) rootView.findViewById(R.id.review_list);

        Intent intent = getActivity().getIntent();
        if(intent.hasExtra("movieId")) {
            adapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
            reviewListView.setAdapter(adapter);

            if (savedInstanceState == null || !savedInstanceState.containsKey(REVIEW_PARCELABLE_KEY)) {
                intent = getActivity().getIntent();
                movieId = intent.getIntExtra("movieId", 0);
                fetchReviews(movieId);
            } else {
                reviewList = savedInstanceState.getParcelableArrayList(REVIEW_PARCELABLE_KEY);
                updateReviewList(reviewList);
            }
        } else {
            cursorAdapter = new ReviewCursorAdapter(getActivity(),null,0,REVIEW_LOADER);
            reviewListView.setAdapter(cursorAdapter);
        }

        return rootView;
    }

    private void fetchReviews(int movieId) {
        FetchReviewTask frt = new FetchReviewTask();
        frt.execute(movieId);
    }

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
        Intent intent = getActivity().getIntent();
        if(!intent.hasExtra("movieId")) {
            movieId = Integer.valueOf(MovieContract.ReviewEntry.getMovieIdFromUri(intent.getData()));
            if (Utilities.isStored(getActivity(), MovieContract.ReviewEntry.TABLE_NAME, movieId)) {
                getLoaderManager().initLoader(REVIEW_LOADER, null, this);
            }
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                null,
                null,
                null,
                null
        );
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
            //updateReviewList(list);
        }
    }
}
