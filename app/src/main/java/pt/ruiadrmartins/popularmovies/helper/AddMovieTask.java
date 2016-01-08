package pt.ruiadrmartins.popularmovies.helper;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import pt.ruiadrmartins.popularmovies.data.Movie;
import pt.ruiadrmartins.popularmovies.data.MovieContract;

/**
 * Task to add Movies outside of DetailActivityFragment
 */
public class AddMovieTask extends AsyncTask<Movie,Void,Void> {

    private final String LOG_TAG = AddMovieTask.class.getSimpleName();

    private Context mContext;

    public AddMovieTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Movie... params) {
        Movie movie = params[0];

        byte[] coverBlob;

        ContentValues movieValues = new ContentValues();

        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.movieId);
        movieValues.put(MovieContract.MovieEntry.COLUMN_NAME, movie.movieName);
        movieValues.put(MovieContract.MovieEntry.COLUMN_COVER_LINK, movie.coverLink);
        movieValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movie.synopsis);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, movie.rating);
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.releaseDate);
        coverBlob=getMovieCoverBlob(movie.coverLink);
        movieValues.put(MovieContract.MovieEntry.COLUMN_COVER_BLOB, coverBlob);

        mContext.getContentResolver().insert(
                MovieContract.MovieEntry.CONTENT_URI,
                movieValues
        );

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(mContext, "Movie added to favorites", Toast.LENGTH_SHORT).show();
    }

    private byte[] getMovieCoverBlob(String coverLink) {
        byte[] coverBlob = null;

        DefaultHttpClient mHttpClient = new DefaultHttpClient();
        HttpGet mHttpGet = new HttpGet(coverLink);
        HttpResponse mHttpResponse;
        try {
            mHttpResponse = mHttpClient.execute(mHttpGet);
            if (mHttpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = mHttpResponse.getEntity();
                coverBlob = EntityUtils.toByteArray(entity);
            }
            return coverBlob;
        } catch (IOException e) {
            return null;
        }
    }
}
