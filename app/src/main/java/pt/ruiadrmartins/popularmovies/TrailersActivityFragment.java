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
        new FetchMovieDataTask().execute(String.valueOf(movieId));
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

    public class FetchMovieDataTask extends AsyncTask<String,Void,ArrayList<Trailer>> {

        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
        final String TRAILER_SUFFIX = "/videos?";
        final String API_PARAM = "api_key";

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Adapted from Udacity Sunshine App example
         * <a href="https://github.com/udacity/Sunshine-Version-2">Sunshine</a>
         * Changed
         * @param params
         * @return
         */
        @Override
        protected ArrayList<Trailer> doInBackground(String... params) {

            String dataJson;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieId = params[0];

            try {
                Uri uri = Uri.parse(MOVIES_BASE_URL + movieId + TRAILER_SUFFIX).buildUpon()
                        .appendQueryParameter(API_PARAM, BuildConfig.TMDB_API_KEY)
                        .build();

                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                dataJson = buffer.toString();

                Log.v("TR",dataJson);

                return getTrailerDataFromJson(dataJson);

            }  catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }

        private ArrayList<Trailer> getTrailerDataFromJson(String trailerJson) throws JSONException {

            final String TRAILER_KEY = "key";
            final String TRAILER_NAME = "name";
            final String TRAILER_SITE = "site";

            JSONObject mJson = new JSONObject(trailerJson);
            JSONArray resultsArray = mJson.getJSONArray("results");

            ArrayList<Trailer> result = new ArrayList<>();

            for(int i=0;i<resultsArray.length();i++) {
                JSONObject trailerData = resultsArray.getJSONObject(i);

                String key = trailerData.getString(TRAILER_KEY);
                String name = trailerData.getString(TRAILER_NAME);
                String site = trailerData.getString(TRAILER_SITE);

                Trailer trailer = new Trailer(1,key,name,site);

                result.add(trailer);
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Trailer> list) {
            super.onPostExecute(list);
            trailerList = list;
            updateTrailerList(list);
        }
    }
}
