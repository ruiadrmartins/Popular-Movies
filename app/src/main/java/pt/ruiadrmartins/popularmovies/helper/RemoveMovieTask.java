package pt.ruiadrmartins.popularmovies.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import pt.ruiadrmartins.popularmovies.data.MovieContract;

/**
 * Task to add Movies outside of DetailActivityFragment
 */
public class RemoveMovieTask extends AsyncTask<Integer,Void,Void> {

    private final String LOG_TAG = RemoveMovieTask.class.getSimpleName();

    private Context mContext;

    public RemoveMovieTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Integer... params) {

        int movieId = params[0];

        int affectedRows = mContext.getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)}
        );

        if (affectedRows == 1) {

            // Remove reviews and trailers as well
            mContext.getContentResolver().delete(
                    MovieContract.ReviewEntry.CONTENT_URI,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)}
            );

            mContext.getContentResolver().delete(
                    MovieContract.TrailerEntry.CONTENT_URI,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)}
            );

        } else {
            Log.e(LOG_TAG, "Movie id " + String.valueOf(movieId) + " not deleted");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(mContext, "Movie removed from favorites!", Toast.LENGTH_SHORT).show();
    }
}
