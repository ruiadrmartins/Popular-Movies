package pt.ruiadrmartins.popularmovies.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import pt.ruiadrmartins.popularmovies.R;

/**
 * Created by ruimartins on 22-12-2015.
 */
public class Utilities {

    public static String currentPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key), context.getString(R.string.pref_sort_default));
    }

    public static boolean sortIsFavorite(String sort, Context context) {
        return sort.equals(context.getString(R.string.pref_sort_favorites));
    }
}
