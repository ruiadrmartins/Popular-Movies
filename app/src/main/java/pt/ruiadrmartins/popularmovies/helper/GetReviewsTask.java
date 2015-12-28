package pt.ruiadrmartins.popularmovies.helper;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ruiadrmartins.popularmovies.data.MovieContract;
import pt.ruiadrmartins.popularmovies.data.Review;

// Task to get reviews outside of ReviewsActivityFragment
public class GetReviewsTask extends AsyncTask<Integer,Void,ArrayList<Review>> {

    private final String LOG_TAG = GetReviewsTask.class.getSimpleName();

    ReviewsFetchHelper fetchHelper = new ReviewsFetchHelper(LOG_TAG);

    private Context mContext;

    public GetReviewsTask(Context context) {
        mContext = context;
    }

    @Override
    protected ArrayList<Review> doInBackground(Integer... params) {
        return fetchHelper.doInBackground(params);
    }

    @Override
    protected void onPostExecute(ArrayList<Review> reviews) {
        ContentValues[] values = new ContentValues[reviews.size()];

        for(int i=0;i<reviews.size();i++) {
            ContentValues reviewValues = new ContentValues();
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, reviews.get(i).movieId);
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, reviews.get(i).author);
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_CONTENT, reviews.get(i).content);
            reviewValues.put(MovieContract.ReviewEntry.COLUMN_URL,reviews.get(i).url);
            values[i] = reviewValues;
        }

        int insertCount = mContext.getContentResolver().bulkInsert(
                MovieContract.ReviewEntry.CONTENT_URI,
                values);

        if(insertCount!=0 && insertCount==reviews.size())
            Toast.makeText(mContext, "Reviews added!", Toast.LENGTH_SHORT).show();

    }
}
