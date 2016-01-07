package pt.ruiadrmartins.popularmovies.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import pt.ruiadrmartins.popularmovies.R;

/**
 * ArrayAdapter to insert Trailer info into View
 */
public class TrailerAdapter extends ArrayAdapter<Trailer> {
    private static final String LOG_TAG = TrailerAdapter.class.getSimpleName();

    public TrailerAdapter(Context context, List<Trailer> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Trailer trailer = getItem(position);

        // Recycle adapter views
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.trailer_item, parent, false);
        }

        ImageView cover = (ImageView) convertView.findViewById(R.id.trailer_item_cover);
        Picasso.with(convertView.getContext()).load(trailer.thumbnail).placeholder(R.mipmap.ic_launcher).into(cover);

        TextView trailerName = (TextView) convertView.findViewById(R.id.trailer_name);
        trailerName.setText(trailer.name);

        return convertView;
    }
}
