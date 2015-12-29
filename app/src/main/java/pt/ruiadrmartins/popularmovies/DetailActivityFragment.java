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
import pt.ruiadrmartins.popularmovies.helper.GetReviewsTask;
import pt.ruiadrmartins.popularmovies.helper.GetTrailersTask;

/**
 * Detail Activity Fragment
 */
public class DetailActivityFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

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
        String ratingValue = getString(R.string.format_rating, rating);
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

        if(Utilities.isStored(getActivity(), MovieContract.MovieEntry.TABLE_NAME, movieId)) favoritedMovie.setChecked(true);
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
        if (Utilities.isStored(getActivity(), MovieContract.MovieEntry.TABLE_NAME, movieId)) {
            intent.setData(MovieContract.ReviewEntry.buildReviewUri(movieId));
        } else {
            intent.putExtra("movieId", movieId);
        }
        startActivity(intent);
    }

    private void startTrailersIntent(int movieId) {
        Intent intent = new Intent(getActivity(), TrailersActivity.class);
        if(Utilities.isStored(getActivity(), MovieContract.MovieEntry.TABLE_NAME, movieId)) {
            intent.setData(MovieContract.TrailerEntry.buildTrailerUri(movieId));
        } else {
            intent.putExtra("movieId", movieId);
        }
        startActivity(intent);
    }

    private void addMovie(String name, String cover, String synopsis, double rating, String releaseDate){
        if(!Utilities.isStored(getActivity(), MovieContract.MovieEntry.TABLE_NAME, movieId)) {
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

            // Store reviews and trailers as well
            GetReviewsTask getReviewsTask = new GetReviewsTask(getActivity());
            getReviewsTask.execute(movieId);
            GetTrailersTask getTrailersTask = new GetTrailersTask(getActivity());
            getTrailersTask.execute(movieId);

            Toast.makeText(getActivity(), "Movie added to favorites!", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeMovie() {
        if(Utilities.isStored(getActivity(), MovieContract.MovieEntry.TABLE_NAME, movieId)) {
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

            // Remove reviews and trailers as well
            affectedRows = getActivity().getContentResolver().delete(
                    MovieContract.ReviewEntry.CONTENT_URI,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)}
            );

            if (affectedRows >= 1) {
                Toast.makeText(getActivity(), "Reviews deleted from favorites!", Toast.LENGTH_SHORT).show();
            } else if (affectedRows == 0){
                Toast.makeText(getActivity(), "No reviews deleted...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Something went wrong... Reviews", Toast.LENGTH_SHORT).show();
            }

            affectedRows = getActivity().getContentResolver().delete(
                    MovieContract.TrailerEntry.CONTENT_URI,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)}
            );

            if (affectedRows >= 1) {
                Toast.makeText(getActivity(), "Trailers deleted from favorites!", Toast.LENGTH_SHORT).show();
            } else if (affectedRows == 0){
                Toast.makeText(getActivity(), "No trailers deleted...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Something went wrong... Trailers", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(getActivity(), "Movie is not favorite!", Toast.LENGTH_SHORT).show();
        }
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
