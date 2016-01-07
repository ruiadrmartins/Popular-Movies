package pt.ruiadrmartins.popularmovies;

import android.content.ContentValues;
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
import android.widget.LinearLayout;
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

    static final String DETAIL_MOVIE_URI = "DetailURI";
    static final String DETAIL_MOVIE_ID = "movieId";
    static final String MOVIE_PARCELABLE_KEY = "movieParcelable";

    private int movieId;
    private Uri uri;
    private Movie movieData;

    private TextView detailTitle;
    private TextView detailDate;
    private TextView detailRating;
    private TextView detailSynopsis;
    private ImageView cover;
    private Button reviewButton;
    private Button trailerButton;
    private CheckBox favoritedMovie;
    private LinearLayout detailLayout;
    private TextView noMovieSelected;

    private static final int DETAIL_LOADER = 0;

    public interface Callback {
        void onTrailersSelected(int movieId);
        void onTrailersSelected(Uri uri);
        void onReviewsSelected(int movieId);
        void onReviewsSelected(Uri uri);
    }

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
        detailLayout = (LinearLayout) rootView.findViewById(R.id.detailLayout);
        noMovieSelected = (TextView) rootView.findViewById(R.id.no_movie_selected);

        // Fetch bundled arguments
        Bundle arguments = getArguments();
        // If arguments exist
        if(arguments!=null) {
            detailLayout.setVisibility(View.VISIBLE);
            noMovieSelected.setText("");
            // If fetch local data
            if (arguments.containsKey(DetailActivityFragment.DETAIL_MOVIE_URI)) {
                uri = arguments.getParcelable(DetailActivityFragment.DETAIL_MOVIE_URI);
            } else if (arguments.containsKey(DetailActivityFragment.DETAIL_MOVIE_ID)) {
                // If data is stored in savedState
                if (savedInstanceState != null && savedInstanceState.containsKey(MOVIE_PARCELABLE_KEY)) {
                    movieData = savedInstanceState.getParcelable(MOVIE_PARCELABLE_KEY);
                    movieId = movieData.movieId;
                } else {
                    // Fetch data from API
                    movieData = arguments.getParcelable(DetailActivityFragment.DETAIL_MOVIE_ID);
                    movieId = movieData.movieId;
                }
                updateViews(movieData.movieName, movieData.releaseDate, movieData.rating, movieData.synopsis, movieData.coverLink, rootView);
            }
        } else {
            detailLayout.setVisibility(View.GONE);
            noMovieSelected.setText(getString(R.string.no_movie_selected));
        }

        return rootView;
    }

    /**
     * Change UI elements, when necessary
     * @param name
     * @param releaseDate
     * @param rating
     * @param synopsis
     * @param coverLink
     * @param view
     */
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

    /**
     * Go to Review activity
     * @param movieId
     */
    private void startReviewsIntent(int movieId) {
        // If local data
        if(Utilities.isStored(getActivity(),MovieContract.MovieEntry.TABLE_NAME,movieId)) {
            ((Callback) getActivity()).onReviewsSelected(MovieContract.ReviewEntry.buildReviewUri(movieId));
        } else {
            // If fetch data from API
            ((Callback) getActivity()).onReviewsSelected(movieId);
        }
    }

    /**
     * Go to Trailer activity
     * @param movieId
     */
    private void startTrailersIntent(int movieId) {
        // If local data
        if(Utilities.isStored(getActivity(),MovieContract.MovieEntry.TABLE_NAME,movieId)) {
            ((Callback) getActivity()).onTrailersSelected(MovieContract.TrailerEntry.buildTrailerUri(movieId));
        } else {
            // If fetch data from API
            ((Callback) getActivity()).onTrailersSelected(movieId);
        }
    }

    /**
     * Adds Movie and associated reviews/trailers to database
     * @param name
     * @param cover
     * @param synopsis
     * @param rating
     * @param releaseDate
     */
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

    /**
     * Removes Movie and associated reviews/trailers from database
     */
    private void removeMovie() {
        if(Utilities.isStored(getActivity(), MovieContract.MovieEntry.TABLE_NAME, movieId)) {
            int affectedRows = getActivity().getContentResolver().delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)}
            );

            if (affectedRows == 1) {
                Toast.makeText(getActivity(), "Movie deleted from favorites!", Toast.LENGTH_SHORT).show();

                // Remove reviews and trailers as well
                getActivity().getContentResolver().delete(
                        MovieContract.ReviewEntry.CONTENT_URI,
                        MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{String.valueOf(movieId)}
                );

                getActivity().getContentResolver().delete(
                        MovieContract.TrailerEntry.CONTENT_URI,
                        MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[]{String.valueOf(movieId)}
                );

            } else {
                Log.e(LOG_TAG,"Movie id " + String.valueOf(movieId) + " not deleted");
            }

        } else {
            Toast.makeText(getActivity(), "Movie is not favorite", Toast.LENGTH_SHORT).show();
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
        // Get bundle arguments
        Bundle arguments = getArguments();
        // If movie is stored locally
        if(arguments!=null && arguments.containsKey(DETAIL_MOVIE_URI)) {
            uri = arguments.getParcelable(DETAIL_MOVIE_URI);
            movieId =  Integer.valueOf(MovieContract.MovieEntry.getMovieIdFromUri(uri));
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(null != uri) {
            return new CursorLoader(
                    getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    null
            );
        }
        return null;
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
