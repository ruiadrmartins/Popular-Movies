package pt.ruiadrmartins.popularmovies.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import pt.ruiadrmartins.popularmovies.R;

/**
 * Created by ruimartins on 02-12-2015.
 */
public class ReviewAdapter extends ArrayAdapter<Review>{

    private static final String LOG_TAG = ReviewAdapter.class.getSimpleName();

    public ReviewAdapter(Context context, List<Review> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Review review = getItem(position);

        // Recycle adapter views
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_item, parent, false);
        }

        TextView author = (TextView) convertView.findViewById(R.id.movie_review_author);
        author.setText(review.author);
        TextView content = (TextView) convertView.findViewById(R.id.movie_review_content);
        content.setText(review.content);

        return convertView;
    }
}
