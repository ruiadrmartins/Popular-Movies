package pt.ruiadrmartins.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import pt.ruiadrmartins.popularmovies.data.MovieContract.MovieEntry;
import pt.ruiadrmartins.popularmovies.data.MovieContract.TrailerEntry;
import pt.ruiadrmartins.popularmovies.data.MovieContract.ReviewEntry;

/**
 * Created by ruimartins on 07-12-2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER," +
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_COVER_LINK + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_RATING + " REAL NOT NULL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER NOT NULL, " + // Julian Days
                MovieEntry.COLUMN_COVER_BLOB + " BLOB " +
                " );";

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY," +
                TrailerEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                TrailerEntry.COLUMN_KEY + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                TrailerEntry.COLUMN_SITE + " TEXT NOT NULL, " +
                " FOREIGN KEY ("  + TrailerEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + ") " +
                " );";

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY," +
                ReviewEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ReviewEntry.COLUMN_URL + " TEXT NOT NULL, " +
                " FOREIGN KEY ("  + ReviewEntry.COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry.COLUMN_MOVIE_ID + ") " +
                " );";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILER_TABLE);
        db.execSQL(SQL_CREATE_REVIEW_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TrailerEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
        onCreate(db);

    }
}
