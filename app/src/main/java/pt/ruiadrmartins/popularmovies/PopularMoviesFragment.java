package pt.ruiadrmartins.popularmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PopularMoviesFragment extends Fragment {

    private MoviesAdapter adapter;
    private GridView gridView;

    public PopularMoviesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        gridView = (GridView) rootView.findViewById(R.id.gridview);

        adapter = new MoviesAdapter(
                getActivity(),
                new ArrayList<Movie>());

        gridView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        update();
    }

    public void update() {
        FetchMoviesTask fmt = new FetchMoviesTask();
        fmt.execute("cenas");

    }

    public class FetchMoviesTask extends AsyncTask<String,Void,List<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected List<Movie> doInBackground(String... params) {
            Movie interstellar = new Movie("Interstellar","http://image.tmdb.org/t/p/w500//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg");
            List<Movie> example = new ArrayList<>();
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            example.add(interstellar);
            return example;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            adapter.clear();
            adapter = new MoviesAdapter(getActivity(),movies);
            gridView.setAdapter(adapter);
        }
    }
}
