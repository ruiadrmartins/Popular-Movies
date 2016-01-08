package pt.ruiadrmartins.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Movie structure to store Movie info
 * Parcelable
 */
public class Movie implements Parcelable {

    public int movieId;
    public String movieName;
    public String coverLink;
    public String synopsis;
    public double rating;
    public String releaseDate;
    public byte[] coverBlob;

    public Movie(int movieId, String movieName, String coverLink, String synopsis, double rating, String releaseDate, byte[] coverBlob) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.coverLink = coverLink;
        this.synopsis = synopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.coverBlob = coverBlob;
    }

    /*
    Parcelable specific functions
    Adapted from Udacity Parcelable and onSavedInstance() Webcast
     */
    public Movie(Parcel parcel) {
        movieId = parcel.readInt();
        movieName = parcel.readString();
        coverLink = parcel.readString();
        synopsis = parcel.readString();
        rating = parcel.readDouble();
        releaseDate = parcel.readString();
        coverBlob = new byte[parcel.readInt()];
        parcel.readByteArray(coverBlob);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(movieId);
        dest.writeString(movieName);
        dest.writeString(coverLink);
        dest.writeString(synopsis);
        dest.writeDouble(rating);
        dest.writeString(releaseDate);
        if(coverBlob!=null) {
            dest.writeInt(coverBlob.length);
            dest.writeByteArray(coverBlob);
        }
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
