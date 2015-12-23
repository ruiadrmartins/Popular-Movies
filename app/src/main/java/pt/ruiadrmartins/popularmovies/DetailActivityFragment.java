package pt.ruiadrmartins.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.os.Bundle;
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
    private Movie movieData;
    public final String MOVIE_PARCELABLE_KEY = "movieParcelable";

    private TextView detailTitle;
    TextView detailDate;
    TextView detailRating;
    TextView detailSynopsis;
    ImageView cover;
    Button reviewButton;
    Button trailerButton;

    private CheckBox favoritedMovie;
    private String sortBy;

    private static final int DETAIL_LOADER = 0;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        detailTitle = (TextView) rootView.findViewById(R.id.detail_movie_title);
        detailDate = (TextView) rootView.findViewById(R.id.detail_movie_date);
        detailRating = (TextView) rootView.findViewById(R.id.detail_movie_rating);
        detailSynopsis = (TextView) rootView.findViewById(R.id.detail_movie_synopsis);
        cover = (ImageView) rootView.findViewById(R.id.detail_movie_poster);
        reviewButton = (Button) rootView.findViewById(R.id.review_button);
        trailerButton = (Button) rootView.findViewById(R.id.trailer_button);
        favoritedMovie = (CheckBox) rootView.findViewById(R.id.checkFavorite);

        sortBy = Utilities.currentPreference(getActivity());
        if(!Utilities.sortIsFavorite(sortBy,getActivity())) {

            if (savedInstanceState == null || !savedInstanceState.containsKey(MOVIE_PARCELABLE_KEY)) {
                // Ger all info on a Parcelable Movie object
                Intent intent = getActivity().getIntent();
                movieData = intent.getParcelableExtra("movieData");
            } else {
                movieData = savedInstanceState.getParcelable(MOVIE_PARCELABLE_KEY);
            }

            movieId = movieData.movieId;
            updateViews(movieData.movieName, movieData.releaseDate, movieData.rating, movieData.synopsis, movieData.coverLink, rootView);
        }

        return rootView;
    }

    private void updateViews(final String name, final String releaseDate, final double rating, final String synopsis, final String coverLink, final View view) {

        detailTitle.setText(name);
        detailDate.setText(releaseDate);
        String ratingValue = String.valueOf(rating) + detailRating.getText();
        detailRating.setText(ratingValue);
        detailSynopsis.setText(synopsis);
        Picasso.with(view.getContext()).load(coverLink).placeholder(R.mipmap.ic_launcher).into(cover);

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReviewsIntent(movieId);
            }
        });

        trailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTrailersIntent(movieId);
            }
        });

        if(isStored(MovieContract.MovieEntry.TABLE_NAME)) favoritedMovie.setChecked(true);
        favoritedMovie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addMovie(name, coverLink, synopsis, rating, releaseDate);
                } else {
                    removeMovie();
                }
            }
        });
    }

    private void startReviewsIntent(int movieId) {
        Intent intent = new Intent(getActivity(), ReviewsActivity.class);
        if (isStored(MovieContract.MovieEntry.TABLE_NAME)) {
            intent.setData(MovieContract.ReviewEntry.buildReviewUri(movieId));
        } else {
            intent.putExtra("movieId", movieId);
        }
        startActivity(intent);
    }

    private void startTrailersIntent(int movieId) {
        Intent intent = new Intent(getActivity(), TrailersActivity.class);
        if(isStored(MovieContract.MovieEntry.TABLE_NAME)) {
            intent.setData(MovieContract.TrailerEntry.buildTrailerUri(movieId));
        } else {
            intent.putExtra("movieId", movieId);
        }
        startActivity(intent);
    }

    private void addMovie(String name, String cover, String synopsis, double rating, String releaseDate){
        if(!isStored(MovieContract.MovieEntry.TABLE_NAME)) {
            ContentValues movieValues = new ContentValues();

            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
            movieValues.put(MovieContract.MovieEntry.COLUMN_NAME, name);
            movieValues.put(MovieContract.MovieEntry.COLUMN_COVER_LINK, cover);
            movieValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, synopsis);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, rating);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

            getActivity().getContentResolver().insert(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieValues
            );

            Toast.makeText(getActivity(), "Movie added to favorites!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeMovie() {
        if(isStored(MovieContract.MovieEntry.TABLE_NAME)) {
            int affectedRows = getActivity().getContentResolver().delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)}
            );

            if (affectedRows == 1) {
                Toast.makeText(getActivity(), "Movie deleted from favorites!", Toast.LENGTH_SHORT).show();
            } else if (affectedRows == 0){
                Toast.makeText(getActivity(), "No movies deleted...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Movie is not favorite!", Toast.LENGTH_SHORT).show();
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

    private Cursor queryStoredTrailers() {
        return getActivity().getContentResolver().query(
                MovieContract.TrailerEntry.CONTENT_URI,
                null,
                MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)},
                null);
    }

    private Cursor queryStoredReviews() {
        return getActivity().getContentResolver().query(
                MovieContract.ReviewEntry.CONTENT_URI,
                null,
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{String.valueOf(movieId)},
                null);
    }

    private boolean isStored(String table) {
        Cursor cursor;
        switch(table){
            case MovieContract.MovieEntry.TABLE_NAME:
                cursor = queryStoredMovie();
                break;
            case MovieContract.ReviewEntry.TABLE_NAME:
                cursor = queryStoredReviews();
                break;
            case MovieContract.TrailerEntry.TABLE_NAME:
                cursor = queryStoredTrailers();
                break;
            default:
                cursor = null;
                break;
        }
        if(cursor != null)
            if (cursor.moveToFirst()) {
                cursor.close();
                return true;
            } else cursor.close();
        return false;
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

        int nameIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
        final String name = data.getString(nameIndex);

        int releaseIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        final String releaseDate = data.getString(releaseIndex);

        int ratingIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING);
        final double rating = data.getDouble(ratingIndex);

        int synopsisIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
        final String synopsis = data.getString(synopsisIndex);

        int coverIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_COVER_LINK);
        final String coverLink = data.getString(coverIndex);

        int idIndex = data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        movieId = data.getInt(idIndex);

        updateViews(name,releaseDate,rating,synopsis,coverLink,getView());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }
}
