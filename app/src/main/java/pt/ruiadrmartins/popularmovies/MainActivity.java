package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import pt.ruiadrmartins.popularmovies.data.Movie;

public class MainActivity extends ActionBarActivity implements PopularMoviesFragment.Callback{

    private boolean mTwoPane;
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";
    private String sortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // So the activity has knowledge of the current sorting
        sortBy = Utilities.currentPreference(this);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.fragment_detail) != null) {
            mTwoPane = true;
            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_detail, new DetailActivityFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        } else {
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

    @Override
    public void onItemSelected(Uri uri) {
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
            // Launch Detail ACTIVITY
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(uri);
            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(Movie movie) {
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
            // Launch Detail ACTIVITY
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(PopularMoviesFragment.MOVIE_DATA_KEY, movie);
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
}
