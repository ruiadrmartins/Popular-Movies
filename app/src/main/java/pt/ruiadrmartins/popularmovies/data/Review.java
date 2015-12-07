package pt.ruiadrmartins.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruimartins on 01-12-2015.
 */
public class Review implements Parcelable {

    public int movieId;
    public String author;
    public String content;
    public String url;

    public Review(int movieId, String author, String content, String url) {
        this.movieId = movieId;
        this.author = author;
        this.content = content;
        this.url = url;
    }

    public Review(Parcel parcel){
        movieId = parcel.readInt();
        author = parcel.readString();
        content = parcel.readString();
        url = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(movieId);
        dest.writeString(author);
        dest.writeString(content);
        dest.writeString(url);
    }

    public final static Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>() {

        @Override
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
