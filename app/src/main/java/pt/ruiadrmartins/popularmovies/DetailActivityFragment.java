package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import pt.ruiadrmartins.popularmovies.data.Movie;

/**
 * Detail Activity Fragment
 */
public class DetailActivityFragment extends Fragment {

    int movieId;
    Movie movieData;
    public final String MOVIE_PARCELABLE_KEY = "movieParcelable";

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if(savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_PARCELABLE_KEY)) {
            // Ger all info on a Parcelable Movie object
            Intent intent = getActivity().getIntent();
            movieData = intent.getParcelableExtra("movieData");
        } else {
            movieData = savedInstanceState.getParcelable(MOVIE_PARCELABLE_KEY);
        }

        // Input all received information into respective views
        TextView detailTitle = (TextView) rootView.findViewById(R.id.detail_movie_title);
        detailTitle.setText(movieData.movieName);

        TextView detailDate = (TextView) rootView.findViewById(R.id.detail_movie_date);
        detailDate.setText(movieData.releaseDate);

        TextView detailRating = (TextView) rootView.findViewById(R.id.detail_movie_rating);
        String rating = String.valueOf(movieData.rating) + detailRating.getText();
        detailRating.setText(rating);

        TextView detailSynopsis = (TextView) rootView.findViewById(R.id.detail_movie_synopsis);
        detailSynopsis.setText(movieData.synopsis);

        ImageView cover = (ImageView) rootView.findViewById(R.id.detail_movie_poster);
        Picasso.with(rootView.getContext()).load(movieData.coverLink).placeholder(R.mipmap.ic_launcher).into(cover);

        // Get movie ID from site
        movieId = movieData.movieId;

        //fetchExtraMovieData(movieId);
        TextView reviewClick = (TextView) rootView.findViewById(R.id.review_label);
        reviewClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReviewsIntent(movieId);
            }
        });
        TextView trailerClick = (TextView) rootView.findViewById(R.id.trailer_label);
        trailerClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrailersIntent(movieId);
            }
        });

        return rootView;
    }

    public void startReviewsIntent(int movieId) {
        Intent intent = new Intent(getActivity(), ReviewsActivity.class);
        intent.putExtra("movieId", movieId);
        startActivity(intent);
    }

    public void startTrailersIntent(int movieId) {
        Intent intent = new Intent(getActivity(), TrailersActivity.class);
        intent.putExtra("movieId", movieId);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save data in saveInstanceState to access after
        // whatever triggered this
        outState.putParcelable(MOVIE_PARCELABLE_KEY, movieData);
        super.onSaveInstanceState(outState);
    }
}
