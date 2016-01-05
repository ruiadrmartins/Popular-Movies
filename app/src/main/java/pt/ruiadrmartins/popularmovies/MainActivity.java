package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import pt.ruiadrmartins.popularmovies.data.Movie;

public class MainActivity extends ActionBarActivity implements PopularMoviesFragment.Callback, DetailActivityFragment.Callback{

    private boolean mTwoPane;
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private static final String TRAILERS_FRAGMENT_TAG = "TFTAG";
    private String sortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // So the activity has knowledge of the current sorting
        sortBy = Utilities.currentPreference(this);
        setContentView(R.layout.activity_main);

        // Fid out if it is in tablet or phone mode
        if(findViewById(R.id.fragment_detail) != null) {
            // Tablet
            mTwoPane = true;
            // Instantiate Detail activity
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail, new DetailActivityFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            // Phone
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // Defines Settings for all children
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * On Movie selected, local storage
     * @param uri
     */
    @Override
    public void onItemSelected(Uri uri) {
        // If Tablet
        if(mTwoPane){
            // Launch Detail FRAGMENT
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, uri);

            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            detailActivityFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail, detailActivityFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            // If Phone,
            // Bundle will be built on DetailActivity
            // Launch Detail ACTIVITY
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(uri);
            startActivity(intent);
        }
    }

    /**
     * On Movie selected, fetch API
     * @param movie
     */
    @Override
    public void onItemSelected(Movie movie) {
        // If tablet
        if(mTwoPane){
            // Launch Detail FRAGMENT
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_MOVIE_ID, movie);

            DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
            detailActivityFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_detail, detailActivityFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        } else {
            // If phone
            // Bundle will be built on DetailActivity

            // Launch Detail ACTIVITY
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailActivity.MOVIE_ID_KEY, movie);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String newSort = Utilities.currentPreference(this);
        if(!sortBy.equals(newSort)) {
            PopularMoviesFragment pmf = (PopularMoviesFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_main);
            if(pmf!=null) {
                sortBy = newSort;
                pmf.updateMovieList();
                pmf.updateViews();
            }
            /*
            DetailActivityFragment daf = (DetailActivityFragment) getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG);
            if(daf!=null) {

            }*/
        }
    }

    /**
     * On Tablet mode, MainActivity controls trailer/review buttons behavior
     */

    /**
     * On Trailers button pressed, fetch API
     * @param movieId
     */
    @Override
    public void onTrailersSelected(int movieId) {
        // Launch Trailer FRAGMENT
        Bundle args = new Bundle();
        args.putInt(TrailersActivityFragment.TRAILERS_MOVIE_ID, movieId);

        TrailersActivityFragment trailersActivityFragment = new TrailersActivityFragment();
        trailersActivityFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail, trailersActivityFragment, TRAILERS_FRAGMENT_TAG)
                .commit();
    }

    /**
     * On Trailers button pressed, local storage
     * @param uri
     */
    @Override
    public void onTrailersSelected(Uri uri) {
        // Launch Trailer FRAGMENT
        Bundle args = new Bundle();
        args.putParcelable(TrailersActivityFragment.TRAILERS_MOVIE_URI, uri);

        TrailersActivityFragment trailersActivityFragment = new TrailersActivityFragment();
        trailersActivityFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail, trailersActivityFragment, TRAILERS_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onReviewsSelected(int movieId) {

    }

    @Override
    public void onReviewsSelected(Uri uri) {

    }
}
