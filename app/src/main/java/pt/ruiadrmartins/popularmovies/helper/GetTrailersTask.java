package pt.ruiadrmartins.popularmovies.helper;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

import pt.ruiadrmartins.popularmovies.data.MovieContract;
import pt.ruiadrmartins.popularmovies.data.Trailer;

/**
 * Task to get trailers outside of TrailersActivityFragment
 */
public class GetTrailersTask extends AsyncTask<Integer,Void,Void> {

    private final String LOG_TAG = GetReviewsTask.class.getSimpleName();

    TrailersFetchHelper fetchHelper = new TrailersFetchHelper(LOG_TAG);

    private Context mContext;

    public GetTrailersTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Integer... params) {
        // Fetch online
        ArrayList<Trailer> trailers = fetchHelper.doInBackground(params);

        // Store info
        ContentValues[] values = new ContentValues[trailers.size()];

        for(int i=0;i<trailers.size();i++) {
            ContentValues trailerValues = new ContentValues();
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, trailers.get(i).movieId);
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_KEY, trailers.get(i).key);
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_NAME, trailers.get(i).name);
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_SITE, trailers.get(i).site);
            values[i] = trailerValues;
        }

        mContext.getContentResolver().bulkInsert(
                MovieContract.TrailerEntry.CONTENT_URI,
                values);

        return null;
    }
}
