package pt.ruiadrmartins.popularmovies.data;

import android.support.v4.widget.CursorAdapter;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import pt.ruiadrmartins.popularmovies.R;

/**
 * Created by ruimartins on 28-12-2015.
 */
public class TrailerCursorAdapter extends CursorAdapter {

    private final String TRAILER_THUMB_PREFIX = "http://img.youtube.com/vi/";
    private final String TRAILER_THUMB_SUFFIX = "/0.jpg";

    private Context mContext;
    private int sLoaderID;

    public static class ViewHolder {

        ImageView cover;
        TextView trailerName;


        public ViewHolder(View view){
            cover = (ImageView) view.findViewById(R.id.trailer_item_cover);
            trailerName = (TextView) view.findViewById(R.id.trailer_name);
        }
    }

    public TrailerCursorAdapter(Context context, Cursor c, int flags, int loaderID) {
        super(context, c, flags);
        mContext = context;
        sLoaderID = loaderID;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.trailer_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int keyIndex = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_KEY);
        String key = cursor.getString(keyIndex);
        String thumbnail = TRAILER_THUMB_PREFIX + key + TRAILER_THUMB_SUFFIX;

        int nameIndex = cursor.getColumnIndex(MovieContract.TrailerEntry.COLUMN_NAME);
        String name = cursor.getString(nameIndex);

        Picasso.with(view.getContext()).load(thumbnail).placeholder(R.mipmap.ic_launcher).into(viewHolder.cover);
        viewHolder.trailerName.setText(name);

    }
}
