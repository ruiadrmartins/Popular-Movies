package pt.ruiadrmartins.popularmovies.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import pt.ruiadrmartins.popularmovies.R;

/**
 * Cursor Adapter to load locally stored Movie info
 */
public class MoviesCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = MoviesCursorAdapter.class.getSimpleName();

    private Context mContext;
    private int sLoaderID;

    public static class ViewHolder {

        ImageView cover;

        public ViewHolder(View view){
            cover = (ImageView) view.findViewById(R.id.grid_item_movie_image);
        }
    }

    public MoviesCursorAdapter(Context context, Cursor c, int flags, int loaderID) {
        super(context, c, flags);
        mContext = context;
        sLoaderID = loaderID;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_movie, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int nameIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
        final String movieName = cursor.getString(nameIndex);

        int coverBlobIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_COVER_BLOB);
        if(coverBlobIndex!=-1) {
            byte[] cover = cursor.getBlob(coverBlobIndex);

            Bitmap bm = BitmapFactory.decodeByteArray(cover, 0, cover.length);

            viewHolder.cover.setImageBitmap(bm);
            viewHolder.cover.setContentDescription(movieName);
        } else {
            int coverIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_COVER_LINK);
            final String coverLink = cursor.getString(coverIndex);

            // Insert cover into imageview, using Picasso
            // http://square.github.io/picasso/
            Picasso.with(view.getContext())
            .load(coverLink)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(viewHolder.cover);

            viewHolder.cover.setContentDescription(movieName);
        }

    }
}
