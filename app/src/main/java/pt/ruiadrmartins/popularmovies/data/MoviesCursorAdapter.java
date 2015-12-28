package pt.ruiadrmartins.popularmovies.data;

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
 * Created by ruimartins on 18-12-2015.
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

        int coverIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_COVER_LINK);
        final String coverLink = cursor.getString(coverIndex);

        int nameIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
        final String movieName = cursor.getString(nameIndex);

        // Insert cover into imageview, using Picasso
        // http://square.github.io/picasso/
        Picasso.with(view.getContext()).load(coverLink).placeholder(R.mipmap.ic_launcher).into(viewHolder.cover);
        viewHolder.cover.setContentDescription(movieName);

    }
}
