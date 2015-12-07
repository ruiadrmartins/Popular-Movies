package pt.ruiadrmartins.popularmovies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class ReviewsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
