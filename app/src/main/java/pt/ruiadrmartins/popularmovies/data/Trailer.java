package pt.ruiadrmartins.popularmovies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruimartins on 01-12-2015.
 */
public class Trailer implements Parcelable {

    private final String TRAILER_THUMB_PREFIX = "http://img.youtube.com/vi/";
    private final String TRAILER_THUMB_SUFFIX = "/0.jpg";

    public int movieId;
    public String key;
    public String name;
    public String site;
    public String thumbnail;

    public Trailer(int movieId, String key, String name, String site) {
        this.movieId = movieId;
        this.key = key;
        this.name = name;
        this.site = site;
        this.thumbnail = TRAILER_THUMB_PREFIX + key + TRAILER_THUMB_SUFFIX;
    }

    public Trailer(Parcel parcel) {
        movieId = parcel.readInt();
        key = parcel.readString();
        name = parcel.readString();
        site = parcel.readString();
        thumbnail = TRAILER_THUMB_PREFIX + key + TRAILER_THUMB_SUFFIX;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(movieId);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
    }

    public final static Parcelable.Creator<Trailer> CREATOR = new Parcelable.Creator<Trailer>() {

        @Override
        public Trailer createFromParcel(Parcel source) {
            return new Trailer(source);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };
}
