package pt.ruiadrmartins.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

/**
 * ReviewsActivity Used only on phone version
 */
public class ReviewsActivity extends ActionBarActivity {

    static final String REVIEWS_MOVIE_ID_KEY = "movieId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {

            // On phone, Review Activity controls ReviewFragment
            Bundle arguments = new Bundle();
            // If have to fetch data by id
            if(getIntent().hasExtra(REVIEWS_MOVIE_ID_KEY)) {
                arguments.putInt(ReviewsActivityFragment.REVIEWS_MOVIE_ID, getIntent().getIntExtra(REVIEWS_MOVIE_ID_KEY, 0));
            } else {
                // If local storage by URI
                arguments.putParcelable(ReviewsActivityFragment.REVIEWS_MOVIE_URI, getIntent().getData());
            }

            // Start Review Fragment
            ReviewsActivityFragment fragment = new ReviewsActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_detail, fragment)
                    .commit();
        }
    }

}
