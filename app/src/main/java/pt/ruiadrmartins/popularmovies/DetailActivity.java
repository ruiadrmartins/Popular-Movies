package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import pt.ruiadrmartins.popularmovies.data.MovieContract;

/**
 * Used only on phone version
 */
public class DetailActivity extends ActionBarActivity implements DetailActivityFragment.Callback {

    static final String MOVIE_ID_KEY = "movieId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {

            // On phone, Detail Activity controls DetailFragment
            Bundle arguments = new Bundle();
            // If have to fetch data by id
            if(getIntent().hasExtra(MOVIE_ID_KEY)) {
                arguments.putParcelable(DetailActivityFragment.DETAIL_MOVIE_ID, getIntent().getParcelableExtra(MOVIE_ID_KEY));
            } else {
                // If local storage by URI
                arguments.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());
            }

            // Start Detail Fragment
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_detail, fragment)
                    .commit();
        }
    }

    /**
     * On Phone mode, DetailActivity controls trailer/review buttons behavior
     */

    /**
     * On Trailers button pressed, fetch API
     * Bundle will be built on TrailerActivity
     * @param movieId
     */
    @Override
    public void onTrailersSelected(int movieId) {
        // Launch Trailer ACTIVITY
        Intent intent = new Intent(this, TrailersActivity.class);
        if(Utilities.isStored(this, MovieContract.TrailerEntry.TABLE_NAME, movieId)) {
            intent.setData(MovieContract.TrailerEntry.buildTrailerUri(movieId));
        } else {
            intent.putExtra(TrailersActivity.MOVIE_ID_KEY, movieId);
        }
        startActivity(intent);
    }

    /**
     * On Trailers button pressed, local storage
     * Bundle will be built on TrailerActivity
     * @param uri
     */
    @Override
    public void onTrailersSelected(Uri uri) {
        int movieId = Integer.valueOf(MovieContract.MovieEntry.getMovieIdFromUri(uri));
        // Launch Trailer ACTIVITY
        Intent intent = new Intent(this, TrailersActivity.class);
        if(Utilities.isStored(this, MovieContract.TrailerEntry.TABLE_NAME, movieId)) {
            intent.setData(uri);
        } else {
            intent.putExtra(TrailersActivity.MOVIE_ID_KEY, movieId);
        }
        startActivity(intent);
    }

    @Override
    public void onReviewsSelected(int movieId) {

    }

    @Override
    public void onReviewsSelected(Uri uri) {

    }
}
