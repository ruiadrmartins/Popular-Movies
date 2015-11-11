package pt.ruiadrmartins.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Movie structure to store Movie info
 * Parcelable
 */
public class Movie implements Parcelable {

    String movieName;
    String coverLink;
    String synopsis;
    double rating;
    String releaseDate;

    public Movie(String movieName, String coverLink, String synopsis, double rating, String releaseDate) {
        this.movieName = movieName;
        this.coverLink = coverLink;
        this.synopsis = synopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    /*
    Parcelable specific functions
    Adapted from Udacity Parcelable and onSavedInstance() Webcast
     */
    public Movie(Parcel parcel) {
        movieName = parcel.readString();
        coverLink = parcel.readString();
        synopsis = parcel.readString();
        rating = parcel.readDouble();
        releaseDate = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieName);
        dest.writeString(coverLink);
        dest.writeString(synopsis);
        dest.writeDouble(rating);
        dest.writeString(releaseDate);
    }

    public final static Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
