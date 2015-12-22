package pt.ruiadrmartins.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by ruimartins on 09-12-2015.
 */
public class MovieProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper movieDbHelper;

    static final int MOVIES = 100; // DIR
    static final int MOVIE_WITH_ID = 101; // ITEM
    static final int TRAILERS = 200; // DIR
    static final int TRAILERS_WITH_MOVIE_ID = 201; // DIR
    static final int REVIEWS = 300; // DIR
    static final int REVIEWS_WITH_MOVIE_ID = 301; // DIR

    static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, MOVIES);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);

        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW, REVIEWS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW + "/#", REVIEWS_WITH_MOVIE_ID);

        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER, TRAILERS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER + "/#", TRAILERS_WITH_MOVIE_ID);

        return uriMatcher;
    }

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        String movieId = MovieContract.MovieEntry.getMovieIdFromUri(uri);

        String selection = MovieContract.MovieEntry.TABLE_NAME+
                "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

        return movieDbHelper.getReadableDatabase().query(
                MovieContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                new String[]{movieId}, //selectionArgs
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrailerByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MovieContract.TrailerEntry.getMovieIdFromUri(uri);

        String selection = MovieContract.TrailerEntry.TABLE_NAME+
                "." + MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

        return movieDbHelper.getReadableDatabase().query(
                MovieContract.TrailerEntry.TABLE_NAME,
                projection,
                selection,
                new String[]{movieId}, //selectionArgs
                null,
                null,
                sortOrder
        );
    }

    private Cursor getReviewByMovieId(Uri uri, String[] projection, String sortOrder) {
        String movieId = MovieContract.ReviewEntry.getMovieIdFromUri(uri);

        String selection = MovieContract.ReviewEntry.TABLE_NAME+
                "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

        return movieDbHelper.getReadableDatabase().query(
                MovieContract.ReviewEntry.TABLE_NAME,
                projection,
                selection,
                new String[]{movieId}, //selectionArgs
                null,
                null,
                sortOrder
        );
    }

    private void normalizeDate(ContentValues values) {
        // normalize the date value
        if (values.containsKey(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)) {
            long dateValue = values.getAsLong(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, MovieContract.normalizeDate(dateValue));
        }
    }

    @Override
    public boolean onCreate() {
        movieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE_WITH_ID:
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            case MOVIES:
                retCursor = movieDbHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case TRAILERS_WITH_MOVIE_ID:
                retCursor = getTrailerByMovieId(uri, projection, sortOrder);
                break;
            case TRAILERS:
                retCursor = movieDbHelper.getReadableDatabase().query(
                        MovieContract.TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case REVIEWS_WITH_MOVIE_ID:
                retCursor = getReviewByMovieId(uri, projection, sortOrder);
                break;
            case REVIEWS:
                retCursor = movieDbHelper.getReadableDatabase().query(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case TRAILERS:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case TRAILERS_WITH_MOVIE_ID:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case REVIEWS:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEWS_WITH_MOVIE_ID:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (rowId != -1)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(values.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILERS: {
                long rowId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if (rowId != -1)
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(values.getAsLong(MovieContract.TrailerEntry.COLUMN_MOVIE_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEWS: {
                long rowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if (rowId != -1)
                    returnUri = MovieContract.ReviewEntry.buildReviewUri(values.getAsLong(MovieContract.ReviewEntry.COLUMN_MOVIE_ID));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int affectedRows = 0;

        if ( null == selection ) selection = "1";

        switch (match) {
            case MOVIES:
                affectedRows = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILERS:
                affectedRows = db.delete(MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEWS:
                affectedRows = db.delete(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (affectedRows != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return affectedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int affectedRows = 0;

        switch (match) {
            case MOVIES:
                affectedRows = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRAILERS:
                affectedRows = db.update(MovieContract.TrailerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REVIEWS:
                affectedRows = db.update(MovieContract.ReviewEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (affectedRows != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return affectedRows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIES: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        normalizeDate(value);
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case TRAILERS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            case REVIEWS: {
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
