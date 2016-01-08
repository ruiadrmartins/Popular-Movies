package pt.ruiadrmartins.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ruimartins on 07-12-2015.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "pt.ruiadrmartins.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_REVIEW = "review";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        // Table Name
        public static final String TABLE_NAME = "movie";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movieid";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COVER_LINK = "coverlink";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "releasedate";
        public static final String COLUMN_COVER_BLOB = "coverblob";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static final class TrailerEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        // Table Name
        public static final String TABLE_NAME = "trailer";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movieid";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";


        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }

    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        // Table Name
        public static final String TABLE_NAME = "review";

        // Columns
        public static final String COLUMN_MOVIE_ID = "movieid";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";


        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}
