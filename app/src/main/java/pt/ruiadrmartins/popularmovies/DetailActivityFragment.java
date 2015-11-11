package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Detail Activity Fragment
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Ger all info on a Parcelable Movie object
        Intent intent = getActivity().getIntent();
        Movie movieData = intent.getParcelableExtra("movieData");

        // Input all received information into respective views
        TextView detailTitle = (TextView) rootView.findViewById(R.id.detail_movie_title);
        detailTitle.setText(movieData.movieName);

        TextView detailDate = (TextView) rootView.findViewById(R.id.detail_movie_date);
        detailDate.setText(movieData.releaseDate);

        TextView detailRating = (TextView) rootView.findViewById(R.id.detail_movie_rating);
        detailRating.setText(String.valueOf(movieData.rating));

        TextView detailSynopsis = (TextView) rootView.findViewById(R.id.detail_movie_synopsis);
        detailSynopsis.setText(movieData.synopsis);

        ImageView cover = (ImageView) rootView.findViewById(R.id.detail_movie_poster);
        Picasso.with(rootView.getContext()).load(movieData.coverLink).placeholder(R.mipmap.ic_launcher).into(cover);

        return rootView;
    }
}
