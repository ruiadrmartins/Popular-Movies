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

import pt.ruiadrmartins.popularmovies.data.Review;
import pt.ruiadrmartins.popularmovies.data.ReviewAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReviewsActivityFragment extends Fragment {

    final String REVIEW_PARCELABLE_KEY = "reviewData";

    ListView reviewListView;
    ReviewAdapter adapter;
    TextView noReviewsFound;
    ArrayList<Review> reviewList;

    public ReviewsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        noReviewsFound = (TextView) rootView.findViewById(R.id.no_reviews_found);

        reviewListView = (ListView) rootView.findViewById(R.id.review_list);
        adapter = new ReviewAdapter(getActivity(),new ArrayList<Review>());

        if(savedInstanceState == null || !savedInstanceState.containsKey(REVIEW_PARCELABLE_KEY)) {
            Intent intent = getActivity().getIntent();
            int movieId = intent.getIntExtra("movieId", 0);
            fetchReviews(movieId);
        } else {
            reviewList = savedInstanceState.getParcelableArrayList(REVIEW_PARCELABLE_KEY);
            updateReviewList(reviewList);
        }

        return rootView;
    }

    private void fetchReviews(int movieId) {
        new FetchMovieDataTask().execute(String.valueOf(movieId));
    }

    private void updateReviewList(ArrayList<Review> list) {
        if(list!= null && list.size()>0) {
            adapter = new ReviewAdapter(getActivity(),list);
            reviewListView.setAdapter(adapter);
            noReviewsFound.setText("");
        } else {
            adapter = new ReviewAdapter(getActivity(),new ArrayList<Review>());
            reviewListView.setAdapter(adapter);
            noReviewsFound.setText(getResources().getText(R.string.no_reviews_found));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // save data in saveInstanceState to access after
        // whatever triggered this
        outState.putParcelableArrayList(REVIEW_PARCELABLE_KEY, reviewList);
        super.onSaveInstanceState(outState);
    }

    public class FetchMovieDataTask extends AsyncTask<String,Void,ArrayList<Review>> {

        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/";
        final String REVIEWS_SUFFIX =  "/reviews?";
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
        protected ArrayList<Review> doInBackground(String... params) {

            String dataJson;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieId = params[0];

            try {
                Uri uri = Uri.parse(MOVIES_BASE_URL + movieId + REVIEWS_SUFFIX).buildUpon()
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

                return getReviewDataFromJson(dataJson);

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

        private ArrayList<Review> getReviewDataFromJson(String reviewJson) throws JSONException {

            final String REVIEW_AUTHOR = "author";
            final String REVIEW_CONTENT = "content";
            final String REVIEW_URL = "url";

            JSONObject mJson = new JSONObject(reviewJson);
            JSONArray resultsArray = mJson.getJSONArray("results");

            ArrayList<Review> result = new ArrayList<>();

            for(int i=0;i<resultsArray.length();i++) {
                JSONObject reviewData = resultsArray.getJSONObject(i);

                String author= reviewData.getString(REVIEW_AUTHOR);
                String content = reviewData.getString(REVIEW_CONTENT);
                String url = reviewData.getString(REVIEW_URL);

                Review review = new Review(1,author,content,url);

                result.add(review);
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> list) {
            super.onPostExecute(list);
            reviewList = list;
            updateReviewList(list);
        }
    }
}
