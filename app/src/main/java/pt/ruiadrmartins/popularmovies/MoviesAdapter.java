package pt.ruiadrmartins.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ruimartins on 03-11-2015.
 */
public class MoviesAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();
    
    public MoviesAdapter(Context context, List<Movie> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);

        // Recycle adapter views
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        ImageView cover = (ImageView) convertView.findViewById(R.id.grid_item_movie_image);
        Picasso.with(convertView.getContext()).load(movie.coverLink).into(cover);
        cover.setContentDescription(movie.movieName);

        return convertView;
    }
}
