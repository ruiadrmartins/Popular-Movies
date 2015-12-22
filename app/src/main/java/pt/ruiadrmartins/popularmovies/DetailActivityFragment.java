package pt.ruiadrmartins.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import pt.ruiadrmartins.popularmovies.data.Movie;
import pt.ruiadrmartins.popularmovies.data.MovieContract;
import pt.ruiadrmartins.popularmovies.data.Utilities;

/**
 * Detail Activity Fragment
 */
public class DetailActivityFragment extends Fragment implements LoaderCallbacks<Cursor> {

    int movieId;
    Movie movieData;
    public final String MOVIE_PARCELABLE_KEY = "movieParcelable";
    CheckBox favoritedMovie;
    private String sortBy;

    private static final int DETAIL_LOADER = 0;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        sortBy = Utilities.currentPreference(getActivity());
        if(!Utilities.sortIsFavorite(sortBy,getActivity())) {

            if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_PARCELABLE_KEY)) {
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
            Button reviewButton = (Button) rootView.findViewById(R.id.review_button);
            reviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startReviewsIntent(movieId);
                }
            });
            Button trailerButton = (Button) rootView.findViewById(R.id.trailer_button);
            trailerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTrailersIntent(movieId);
                }
            });

            favoritedMovie = (CheckBox) rootView.findViewById(R.id.checkFavorite);
            if(isMovieFavorite()) favoritedMovie.setChecked(true);
            favoritedMovie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Store movie, trailers and reviews to database
                        addMovie(movieData.movieName, movieData.coverLink, movieData.synopsis, movieData.rating, movieData.releaseDate);
                    } else {
                        // Remove movie, trailers and reviews from database
                        removeMovie();
                    }
                }
            });
        }

        return rootView;
    }

    private void startReviewsIntent(int movieId) {
        Intent intent = new Intent(getActivity(), ReviewsActivity.class);
        intent.putExtra("movieId", movieId);
        startActivity(intent);
    }

    private void startTrailersIntent(int movieId) {
        Intent intent = new Intent(getActivity(), TrailersActivity.class);
        intent.putExtra("movieId", movieId);
        startActivity(intent);
    }

    private void startCursorReviewIntent(int movieId) {
        Intent intent = new Intent(getActivity(), ReviewsActivity.class)
                .setData(MovieContract.ReviewEntry.buildReviewUri(movieId));
        startActivity(intent);
    }

    private void startCursorTrailerIntent(int movieId) {
        Intent intent = new Intent(getActivity(), TrailersActivity.class)
                .setData(MovieContract.TrailerEntry.buildTrailerUri(movieId));
        startActivity(intent);
    }

    private int addMovie(String name, String cover, String synopsis, double rating, String releaseDate){

        int returnMovieId;

        Cursor movieCursor = queryStoredMovie();

        if(isMovieFavorite()) {
            int movieIdIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
            returnMovieId = movieCursor.getInt(movieIdIndex);
        } else {
            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
            movieValues.put(MovieContract.MovieEntry.COLUMN_NAME, name);
            movieValues.put(MovieContract.MovieEntry.COLUMN_COVER_LINK, cover);
            movieValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, synopsis);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, rating);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

            Uri insertedUri = getActivity().getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieValues
            );

            returnMovieId = Integer.valueOf(MovieContract.MovieEntry.getMovieIdFromUri(insertedUri));

            Toast.makeText(getActivity(), "Movie added to favorites!", Toast.LENGTH_SHORT).show();
        }

        return returnMovieId;
    }

    private void removeMovie() {
        // Remove movie from DB
        Cursor movieCursor = queryStoredMovie();

        if(isMovieFavorite()) {
            int affectedRows = getActivity().getContentResolver().delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)}
            );

            if (affectedRows == 1) {
                Toast.makeText(getActivity(), "Movie deleted from favorites!", Toast.LENGTH_SHORT).show();
            } else if (affectedRows == 0){
                Toast.makeText(getActivity(), "No movies deleted...", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getActivity(), "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Something went wrong....", Toast.LENGTH_SHORT).show();
        }
    }

    private Cursor queryStoredMovie() {
        return getActivity().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)},
                null);
    }

    private boolean isMovieFavorite() {
        Cursor movieCursor = queryStoredMovie();
        return movieCursor != null && movieCursor.moveToFirst();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save data in saveInstanceState to access after
        // whatever triggered this
        outState.putParcelable(MOVIE_PARCELABLE_KEY, movieData);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        sortBy = Utilities.currentPreference(getActivity());
        if(Utilities.sortIsFavorite(sortBy,getActivity())) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) { return; }

        // Input all received information into respective views
        TextView detailTitle = (TextView) getView().findViewById(R.id.detail_movie_title);
        int nameIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
        final String name = data.getString(nameIndex);
        detailTitle.setText(name);

        TextView detailDate = (TextView) getView().findViewById(R.id.detail_movie_date);
        int releaseIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        final String releaseDate = data.getString(releaseIndex);
        detailDate.setText(releaseDate);

        TextView detailRating = (TextView) getView().findViewById(R.id.detail_movie_rating);
        int ratingIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING);
        final double rating = data.getDouble(ratingIndex);
        final String ratingValue = String.valueOf(rating) + detailRating.getText();
        detailRating.setText(ratingValue);

        TextView detailSynopsis = (TextView) getView().findViewById(R.id.detail_movie_synopsis);
        int synopsisIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
        final String synopsis = data.getString(synopsisIndex);
        detailSynopsis.setText(synopsis);

        ImageView cover = (ImageView) getView().findViewById(R.id.detail_movie_poster);
        int coverIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_COVER_LINK);
        final String coverLink = data.getString(coverIndex);
        Picasso.with(getView().getContext()).load(coverLink).placeholder(R.mipmap.ic_launcher).into(cover);

        // Get movie ID from site
        int idIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        movieId = data.getInt(idIndex);

        Button reviewButton = (Button) getView().findViewById(R.id.review_button);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCursorReviewIntent(movieId);
            }
        });

        Button trailerButton = (Button) getView().findViewById(R.id.trailer_button);
        trailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCursorTrailerIntent(movieId);
            }
        });

        favoritedMovie = (CheckBox) getView().findViewById(R.id.checkFavorite);
        if(isMovieFavorite()) favoritedMovie.setChecked(true);
        favoritedMovie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Store movie, trailers and reviews to database
                    addMovie(name, coverLink, synopsis, rating, releaseDate);
                } else {
                    // Remove movie, trailers and reviews from database
                    removeMovie();
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
