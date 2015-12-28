package pt.ruiadrmartins.popularmovies.helper;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;

import pt.ruiadrmartins.popularmovies.data.MovieContract;
import pt.ruiadrmartins.popularmovies.data.Trailer;

/**
 * Created by ruimartins on 28-12-2015.
 */
public class GetTrailersTask extends AsyncTask<Integer,Void,ArrayList<Trailer>> {

    private final String LOG_TAG = GetReviewsTask.class.getSimpleName();

    TrailersFetchHelper fetchHelper = new TrailersFetchHelper(LOG_TAG);

    private Context mContext;

    public GetTrailersTask(Context context) {
        mContext = context;
    }

    @Override
    protected ArrayList<Trailer> doInBackground(Integer... params) {
        return fetchHelper.doInBackground(params);
    }

    @Override
    protected void onPostExecute(ArrayList<Trailer> trailers) {
        ContentValues[] values = new ContentValues[trailers.size()];

        for(int i=0;i<trailers.size();i++) {
            ContentValues trailerValues = new ContentValues();
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_MOVIE_ID, trailers.get(i).movieId);
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_KEY, trailers.get(i).key);
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_NAME, trailers.get(i).name);
            trailerValues.put(MovieContract.TrailerEntry.COLUMN_SITE, trailers.get(i).site);
            values[i] = trailerValues;
        }

        int insertCount = mContext.getContentResolver().bulkInsert(
                MovieContract.TrailerEntry.CONTENT_URI,
                values);

        if(insertCount!=0 && insertCount==trailers.size())
            Toast.makeText(mContext, "Trailers added!", Toast.LENGTH_SHORT).show();
    }
}
