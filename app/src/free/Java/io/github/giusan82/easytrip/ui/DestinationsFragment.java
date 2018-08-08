package io.github.giusan82.easytrip.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.Gson;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.giusan82.easytrip.NetUtilities.ApiRequest;
import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.data.PlacesData;
import io.github.giusan82.easytrip.utilities.ListAdapter;
import timber.log.Timber;

import io.github.giusan82.easytrip.data.CacheContract.CacheEntry;

public class DestinationsFragment extends Fragment implements ListAdapter.ItemListOnClickHandler, SharedPreferences.OnSharedPreferenceChangeListener, LoaderManager.LoaderCallbacks {
    private static final int DATA_LOADER_ID = 3;

    private ArrayList<PlacesData.Results> mItems;
    private ListAdapter adapter;
    private String mQuery;
    private ApiRequest mApiRequest;
    private InterstitialAd mInterstitialAd;
    private Cursor mCursor;
    @BindView(R.id.rv_list)
    RecyclerView mList;
    @BindView(R.id.empty_view)
    View mEmptyView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View rootView = inflater.inflate(R.layout.fragment_destinations, container, false);
        // Bind the views
        ButterKnife.bind(this, rootView);
        mApiRequest = new ApiRequest(getContext());

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), ListAdapter.numberOfColumns(getActivity()));

        mList.setLayoutManager(layoutManager);
        mList.setHasFixedSize(true);

        //the ArrayList is initialized
        mItems = new ArrayList<>();
        adapter = new ListAdapter(getContext(), this);
        mList.setAdapter(adapter);

        //Register MainActivity as an OnSharedPreferenceChangedListener to receive a callback when a SharedPreference has changed.
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
        getActivity().getSupportLoaderManager().initLoader(DATA_LOADER_ID, null, this);
        mInterstitialAd =  new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        Timber.d("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
    }

    @Override
    public void onClickList(final Cursor cursor, final int position) {

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }else{
            Timber.d("The interstitial wasn't loaded yet.");
        }
        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                cursor.moveToPosition(position);
                String place_id = cursor.getString(cursor.getColumnIndex(CacheEntry.COLUMN_PLACE_ID));
                String parent_name = cursor.getString(cursor.getColumnIndex(CacheEntry.COLUMN_PLACE_PARENT_NAME));
                String country_name = cursor.getString(cursor.getColumnIndex(CacheEntry.COLUMN_PLACE_COUNTRY_NAME));
                String image_url = cursor.getString(cursor.getColumnIndex(CacheEntry.COLUMN_IMAGE_URL));
                String name = cursor.getString(cursor.getColumnIndex(CacheEntry.COLUMN_PLACE_NAME));
                Intent intent = new Intent(getContext(), PlacesActivity.class);
                intent.putExtra(PlacesData.EXTRA_POSITION, position);
                intent.putExtra(PlacesData.EXTRA_LOCATION_ID, place_id);
                intent.putExtra(PlacesData.EXTRA_PARENT_NAME, parent_name);
                intent.putExtra(PlacesData.EXTRA_COUNTRY_NAME, country_name);
                intent.putExtra(PlacesData.EXTRA_IMAGE_URL, image_url);
                intent.putExtra(PlacesData.EXTRA_NAME, name);
                if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                    getContext().startActivity(intent);
                    getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
        Timber.d("Clicked item: " + position);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.main, menu);
        MenuItem search = menu.findItem(R.id.search_view);
        final SearchView searchField;
        //this add the searchView to actionBar
        searchField = (SearchView) search.getActionView();
        //set the hint text on searchView
        //searchField.setQueryHint(getString(R.string.searchHint));
        //this expand the searchView without click on it
        searchField.setIconified(false);
        searchField.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                /**this active the instant search*/
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String input) {
                //this prevent that the query is called twice
                searchField.clearFocus();
                mQuery = input;
                searchField.setQuery(input, false);
                refresh();
                Timber.d("Query: " + input);

                return true;
            }
        });
        ImageView close = searchField.findViewById(R.id.search_close_btn);//source: https://stackoverflow.com/a/24844944
        final EditText search_query = searchField.findViewById(R.id.search_src_text);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_query.setText("");
                searchField.setQuery("", false);
                mQuery = null;
                refresh();
                Timber.d("Query deleted");
            }
        });
        searchField.setOnSearchClickListener(new View.OnClickListener() { //source: https://stackoverflow.com/a/30038082
            @Override
            public void onClick(View v) {
                searchField.setQuery(mQuery, false);
                Timber.d("Expanded");
            }
        });
    }
    private void catchingData(final String query) {
        mApiRequest.get(mApiRequest.getDestinationUrl(query).toString(), true, new ApiRequest.Callback() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                PlacesData placesData = gson.fromJson(result, PlacesData.class);
                PlacesData.Results[] results = placesData.getResults();
                int rows = 0;
                    for (int i = 0; i < results.length; i++) {
                            try{
                                long creation_time = System.currentTimeMillis();
                                ContentValues cv = new ContentValues();
                                cv.put(CacheEntry.COLUMN_CREATION_DATE, creation_time);
                                cv.put(CacheEntry.COLUMN_URL, mApiRequest.getDestinationUrl(query).toString());
                                cv.put(CacheEntry.COLUMN_PLACE_ID, results[i].getID());
                                cv.put(CacheEntry.COLUMN_PLACE_NAME, results[i].getName());
                                PlacesData.Results.Images[] images = results[i].getImages();
                                cv.put(CacheEntry.COLUMN_IMAGE_URL, images[0].getSizes().getMedium().getImageUrl());
                                cv.put(CacheEntry.COLUMN_PLACE_COUNTRY_NAME, results[i].getCountryName());
                                cv.put(CacheEntry.COLUMN_PLACE_PARENT_NAME, results[i].getParentName());
                                cv.put(CacheEntry.COLUMN_SCORE, results[i].getScore());
                                Uri newUri = getContext().getContentResolver().insert(CacheEntry.CONTENT_URI, cv);
                                if(newUri != null){
                                    rows++;
                                }
                            }catch (IllegalArgumentException e){
                                Timber.e(e.getClass().getCanonicalName() + ": " + e.getMessage());
                                e.printStackTrace();
                            }
                }
                if(results.length == 0){
                    mEmptyView.setVisibility(View.VISIBLE);
                }else{
                    mEmptyView.setVisibility(View.GONE);
                }
                adapter.swapCursor(mCursor);
                Timber.d(rows + " items saved");
            }
        });
    }

    private void refresh(){
        Timber.d("Refreshing data");
        getActivity().getSupportLoaderManager().restartLoader(DATA_LOADER_ID, null, this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.contains(getString(R.string.pref_count_key)) || key.contains(getString(R.string.pref_orderBy_key))){
            refresh();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        Timber.d("OnDestroy");
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case DATA_LOADER_ID:

                Uri uri = CacheEntry.CONTENT_URI;
                String sortOrder = CacheEntry.ID + " ASC";
                String selection = CacheEntry.COLUMN_URL + " = ?";
                String[] selectionArgs = {mApiRequest.getDestinationUrl(mQuery).toString()};
                Timber.d(selectionArgs[0]);
                return new CursorLoader(getContext(),
                        uri,
                        null,
                        selection,
                        selectionArgs,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
            mCursor = (Cursor) data;
            if(mCursor.getCount() == 0){
                catchingData(mQuery);
                Timber.d("Downloading data");
                return;
            }else{
                mEmptyView.setVisibility(View.GONE);
            }
        adapter.swapCursor(mCursor);
        //mList.smoothScrollToPosition(0);
        Timber.d("Number of Items: " + mCursor.getCount());
        Timber.d("Load finished");
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        adapter.swapCursor(null);
        Timber.d("Loader reset");
    }
}
