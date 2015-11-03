package pt.ruiadrmartins.popularmovies;

import android.net.Uri;

/**
 * Created by ruimartins on 03-11-2015.
 */
public class Movie {

    String movieName;
    String coverLink;

    public Movie(String movieName, String coverLink) {
        this.movieName = movieName;
        this.coverLink = coverLink;
    }
}
