package pt.ruiadrmartins.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import pt.ruiadrmartins.popularmovies.data.Trailer;
import pt.ruiadrmartins.popularmovies.data.TrailerAdapter;
import pt.ruiadrmartins.popularmovies.helper.TrailersFetchHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrailersActivityFragment extends Fragment {

    final String TRAILER_PARCELABLE_KEY = "trailerData";

    ListView trailerListView;
    TrailerAdapter adapter;
    TextView noTrailersFound;
    ArrayList<Trailer> trailerList;

    public TrailersActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_trailers, container, false);

        noTrailersFound = (TextView) rootView.findViewById(R.id.no_trailers_found);

        trailerListView = (ListView) rootView.findViewById(R.id.trailer_list);
        trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startVideoIntent(adapter.getItem(position));
            }
        });
        adapter = new TrailerAdapter(getActivity(),new ArrayList<Trailer>());
        trailerListView.setAdapter(adapter);

        if(savedInstanceState == null || !savedInstanceState.containsKey(TRAILER_PARCELABLE_KEY)) {
            Intent intent = getActivity().getIntent();
            int movieId = intent.getIntExtra("movieId", 0);
            fetchTrailers(movieId);
        } else {
            trailerList = savedInstanceState.getParcelableArrayList(TRAILER_PARCELABLE_KEY);
            updateTrailerList(trailerList);
        }

        return rootView;
    }

    public void startVideoIntent(Trailer trailer) {

        final String YOUTUBE_VIDEO_LINK_PREFIX = "https://www.youtube.com/watch?v=";

        if(trailer.site.toLowerCase().equals("youtube")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(YOUTUBE_VIDEO_LINK_PREFIX + trailer.key));

            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private void fetchTrailers(int movieId) {
        FetchTrailerTask ftt = new FetchTrailerTask();
        ftt.execute(movieId);
    }

    private void updateTrailerList(ArrayList<Trailer> list) {
        if(list != null && list.size()>0) {
            adapter = new TrailerAdapter(getActivity(),list);
            trailerListView.setAdapter(adapter);
            noTrailersFound.setText("");
        } else {
            adapter = new TrailerAdapter(getActivity(),new ArrayList<Trailer>());
            trailerListView.setAdapter(adapter);
            noTrailersFound.setText(getResources().getText(R.string.no_trailers_found));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save data in saveInstanceState to access after
        // whatever triggered this
        outState.putParcelableArrayList(TRAILER_PARCELABLE_KEY, trailerList);
        super.onSaveInstanceState(outState);
    }

    public class FetchTrailerTask extends AsyncTask<Integer,Void,ArrayList<Trailer>> {

        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        TrailersFetchHelper trailersFetchHelper = new TrailersFetchHelper(LOG_TAG);

        @Override
        protected ArrayList<Trailer> doInBackground(Integer... params) {
            return trailersFetchHelper.doInBackground(params);
        }

        @Override
        protected void onPostExecute(ArrayList<Trailer> list) {
            super.onPostExecute(list);
            adapter.clear();
            if(list!=null && list.size()>0) {
                trailerList = list;
                noTrailersFound.setText("");
                for (Trailer trailer: list) {
                    adapter.add(trailer);
                }
            }
            //updateTrailerList(list);
        }
    }
}
