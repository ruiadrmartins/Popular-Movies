package pt.ruiadrmartins.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class TrailersActivity extends ActionBarActivity {

    static final String MOVIE_ID_KEY = "movieId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailers);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {

            // On phone, Trailer Activity controls TrailerFragment
            Bundle arguments = new Bundle();
            // If have to fetch data by id
            if(getIntent().hasExtra(MOVIE_ID_KEY)) {
                arguments.putInt(TrailersActivityFragment.TRAILERS_MOVIE_ID, getIntent().getIntExtra(MOVIE_ID_KEY, 0));
            } else {
                // If local storage by URI
                arguments.putParcelable(TrailersActivityFragment.TRAILERS_MOVIE_URI, getIntent().getData());
            }

            // Start Trailer Fragment
            TrailersActivityFragment fragment = new TrailersActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_detail, fragment)
                    .commit();
        }
    }

}
