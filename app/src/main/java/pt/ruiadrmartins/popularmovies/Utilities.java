package pt.ruiadrmartins.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import pt.ruiadrmartins.popularmovies.data.MovieContract;

/**
 * Created by ruimartins on 22-12-2015.
 */
public class Utilities {

    // Preferences
    public static String currentPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_default));
    }

    public static boolean sortIsFavorite(String sort, Context context) {
        return sort.equals(context.getString(R.string.pref_sort_favorites));
    }

    public static String getSortingName(String sorting, Context context){
        String popularity = context.getString(R.string.pref_sort_popularity);
        String rating = context.getString(R.string.pref_sort_rating);
        String favorites = context.getString(R.string.pref_sort_favorites);

        if(sorting.equals(popularity))
            return context.getString(R.string.pref_sort_popularity_label);
        else if(sorting.equals(rating))
            return context.getString(R.string.pref_sort_rating_label);
        else if(sorting.equals(favorites))
            return context.getString(R.string.pref_sort_favorites_label);
        return "";
    }

    // DB Queries
    private static Cursor queryStoredMovie(Context context, String movieId) {
        return context.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movieId},
                null);
    }

    private static Cursor queryStoredTrailers(Context context, String movieId) {
        return context.getContentResolver().query(
                MovieContract.TrailerEntry.CONTENT_URI,
                null,
                MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movieId},
                null);
    }

    private static Cursor queryStoredReviews(Context context, String movieId) {
        return context.getContentResolver().query(
                MovieContract.ReviewEntry.CONTENT_URI,
                null,
                MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                new String[]{movieId},
                null);
    }

    public static boolean isStored(Context context, String table, int movieId) {
        Cursor cursor;
        switch(table){
            case MovieContract.MovieEntry.TABLE_NAME:
                cursor = queryStoredMovie(context, String.valueOf(movieId));
                break;
            case MovieContract.ReviewEntry.TABLE_NAME:
                cursor = queryStoredReviews(context, String.valueOf(movieId));
                break;
            case MovieContract.TrailerEntry.TABLE_NAME:
                cursor = queryStoredTrailers(context, String.valueOf(movieId));
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
}
