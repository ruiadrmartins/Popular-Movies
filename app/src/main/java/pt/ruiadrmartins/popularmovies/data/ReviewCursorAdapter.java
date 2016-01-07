package pt.ruiadrmartins.popularmovies.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.ruiadrmartins.popularmovies.R;

/**
 * Cursor Adapter to load locally stored Review info
 */
public class ReviewCursorAdapter extends CursorAdapter {

    private Context mContext;
    private int sLoaderID;

    public static class ViewHolder {

        TextView author;
        TextView content;

        public ViewHolder(View view){
            author = (TextView) view.findViewById(R.id.movie_review_author);
            content = (TextView) view.findViewById(R.id.movie_review_content);
        }
    }

    public ReviewCursorAdapter(Context context, Cursor c, int flags, int loaderID) {
        super(context, c, flags);
        mContext = context;
        sLoaderID = loaderID;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int authorIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR);
        String author = cursor.getString(authorIndex);

        int contentIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT);
        String content = cursor.getString(contentIndex);

        viewHolder.author.setText(author);
        viewHolder.content.setText(content);

    }
}
