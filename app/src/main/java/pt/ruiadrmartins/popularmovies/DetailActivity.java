package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * DetailActivity Used only on phone version
 */
public class DetailActivity extends ActionBarActivity implements DetailActivityFragment.Callback {

    static final String DETAIL_MOVIE_ID_KEY = "movieId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {

            // On phone, Detail Activity controls DetailActivityFragment
            Bundle arguments = new Bundle();
            // If have to fetch data by id
            if(getIntent().hasExtra(DETAIL_MOVIE_ID_KEY)) {
                arguments.putParcelable(DetailActivityFragment.DETAIL_MOVIE_ID, getIntent().getParcelableExtra(DETAIL_MOVIE_ID_KEY));
            } else {
                // If local storage by URI
                arguments.putParcelable(DetailActivityFragment.DETAIL_MOVIE_URI, getIntent().getData());
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
        Intent intent = new Intent(this, TrailersActivity.class)
                .putExtra(TrailersActivity.TRAILERS_MOVIE_ID_KEY, movieId);
        startActivity(intent);
    }

    /**
     * On Trailers button pressed, local storage
     * Bundle will be built on TrailerActivity
     * @param uri
     */
    @Override
    public void onTrailersSelected(Uri uri) {
        // Launch Trailer ACTIVITY
        Intent intent = new Intent(this, TrailersActivity.class)
                .setData(uri);
        startActivity(intent);
    }

    /**
     * On Reviews button pressed, fetch API
     * Bundle will be built on TrailerActivity
     * @param movieId
     */
    @Override
    public void onReviewsSelected(int movieId) {
        // Launch Review ACTIVITY
        Intent intent = new Intent(this, ReviewsActivity.class)
                .putExtra(ReviewsActivity.REVIEWS_MOVIE_ID_KEY, movieId);
        startActivity(intent);
    }

    /**
     * On Reviews button pressed, local storage
     * Bundle will be built on TrailerActivity
     * @param uri
     */
    @Override
    public void onReviewsSelected(Uri uri) {
        // Launch Review ACTIVITY
        Intent intent = new Intent(this, ReviewsActivity.class)
                .setData(uri);
        startActivity(intent);
    }
}
