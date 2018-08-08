package io.github.giusan82.easytrip.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.giusan82.easytrip.NetUtilities.ApiRequest;
import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.data.PlacesData;
import io.github.giusan82.easytrip.utilities.TabsAdapter;
import io.github.giusan82.easytrip.data.PlacesContract.PlacesEntry;
import timber.log.Timber;


public class PlacesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int DATA_LOADER_ID = 4;
    private String mLocationId;
    private String mParentName;
    private String mCountryName;
    private String mImageUrl;
    private ApiRequest mApiRequest;
    private TabsAdapter mTabsAdapter;
    private Window mWindow;
    private Cursor mCursor;
    private String mTitle;
    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.vp_places)
    ViewPager mViewPager;
    @BindView(R.id.tabs_places)
    TabLayout mTabLayout;
    @BindView(R.id.iv_actionBar_background)
    ImageView mActionBarBackground;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.content)
    CoordinatorLayout mContent;
    @BindView(R.id.empty_view)
    View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        ButterKnife.bind(this);
        mWindow = this.getWindow();
        mApiRequest = new ApiRequest(this);

        setSupportActionBar(mToolBar);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



        Intent intent = getIntent();

        if (intent.hasExtra(PlacesData.EXTRA_NAME)){
            mTitle = intent.getStringExtra(PlacesData.EXTRA_NAME);
            setTitle(mTitle);
        }
        if(intent.hasExtra(PlacesData.EXTRA_POSITION)){
            Timber.d("Position: " + intent.getIntExtra(PlacesData.EXTRA_POSITION, 0));
        }
        if(intent.hasExtra(PlacesData.EXTRA_LOCATION_ID)){
            mLocationId = intent.getStringExtra(PlacesData.EXTRA_LOCATION_ID);
            Timber.d("Position: " + mLocationId);
        }
        if(intent.hasExtra(PlacesData.EXTRA_PARENT_NAME)){
            mParentName = intent.getStringExtra(PlacesData.EXTRA_PARENT_NAME);
        }
        if (intent.hasExtra(PlacesData.EXTRA_COUNTRY_NAME)){
            mCountryName = intent.getStringExtra(PlacesData.EXTRA_COUNTRY_NAME);
        }
        if (intent.hasExtra(PlacesData.EXTRA_IMAGE_URL)){
            mImageUrl = intent.getStringExtra(PlacesData.EXTRA_IMAGE_URL);
            Glide.with(this).load(mImageUrl).asBitmap().dontAnimate().dontTransform().into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (resource != null) {
                        mActionBarBackground.setImageBitmap(resource);
                        mActionBarBackground.setContentDescription(getString(R.string.destination_images_content_description, mTitle));
                        Palette.from(((BitmapDrawable) mActionBarBackground.getDrawable()).getBitmap()).generate(new Palette.PaletteAsyncListener() {

                            @Override
                            public void onGenerated(Palette palette) {
                                if(palette != null){
                                    int darkMutedColor;
                                    if(palette.getDarkMutedSwatch() != null){
                                        darkMutedColor = palette.getDarkMutedSwatch().getRgb();
                                    }else{
                                        Timber.d("Dark Muted Color not found");
                                        darkMutedColor = Color.BLACK;
                                    }
                                    int lightVibrantColor;
                                    if (palette.getLightVibrantSwatch() != null){
                                        lightVibrantColor = palette.getLightVibrantSwatch().getRgb();
                                    }else{
                                        Timber.d("Light Vibrant Color not found");
                                        lightVibrantColor = Color.BLACK;
                                    }

                                    if(Build.VERSION.SDK_INT >= 21){
                                        mWindow.setStatusBarColor(darkMutedColor);
                                    }
                                    mCollapsingToolbarLayout.setContentScrimColor(darkMutedColor);
                                    //source of GradientDrawable: https://stackoverflow.com/a/6116273
                                    GradientDrawable gradiend = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                            new int[]{Color.BLACK, Color.BLACK, lightVibrantColor});
                                    gradiend.setCornerRadius(0f);
                                    mContent.setBackground(gradiend);
                                }
                            }
                        });
                    }else{
                        Timber.d("No Resource ready");
                    }

                }
            });

        }

        // Create an adapter that knows which fragment should be shown on each page
        mTabsAdapter = new TabsAdapter(this, getSupportFragmentManager(), PlacesData.ACTION_KEY_PLACES);
        // Set the adapter onto the view pager
        mViewPager.setAdapter(mTabsAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTabLayout.setElevation(3);
        }
        getSupportLoaderManager().initLoader(DATA_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.only_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case DATA_LOADER_ID:
                Uri uri = PlacesEntry.CONTENT_URI;
                String sortOrder = PlacesEntry.ID + " ASC";
                String selection = PlacesEntry.COLUMN_URL + " = ?";
                String[] selectionArgs = {mApiRequest.getPlacesUrl(null, mLocationId).toString()};
                Timber.d(selectionArgs[0]);
                return new CursorLoader(this,
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
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if(cursor.getCount() == 0){
            catchingData(null);
            Timber.d("Downloading data");
            return;
        }else{
            mEmptyView.setVisibility(View.GONE);
        }
        mTabsAdapter.swapCursor(cursor);

        Timber.d("Number of Items: " + cursor.getCount());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mTabsAdapter.swapCursor(null);
    }

    private void catchingData(String query){

        final String url = mApiRequest.getPlacesUrl(query, mLocationId).toString();
        Timber.d("url: " + url);
        mApiRequest.get(url, true, new ApiRequest.Callback() {
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
                        cv.put(PlacesEntry.COLUMN_CREATION_DATE, creation_time);
                        cv.put(PlacesEntry.COLUMN_URL, url);
                        cv.put(PlacesEntry.COLUMN_LOCATION_ID, results[i].getID());
                        cv.put(PlacesEntry.COLUMN_PLACE_NAME, results[i].getName());
                        PlacesData.Results.Images[] images = results[i].getImages();
                        if(images.length != 0){
                            cv.put(PlacesEntry.COLUMN_IMAGE_URL, images[0].getSizes().getMedium().getImageUrl());
                        }
                        cv.put(PlacesEntry.COLUMN_PLACE_COUNTRY_NAME, mCountryName);
                        cv.put(PlacesEntry.COLUMN_PLACE_PARENT_NAME, mParentName);
                        cv.put(PlacesEntry.COLUMN_SCORE, results[i].getScore());
                        cv.put(PlacesEntry.COLUMN_INTRO, results[i].getIntro());
                        Uri newUri = getContentResolver().insert(PlacesEntry.CONTENT_URI, cv);
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
                mTabsAdapter.swapCursor(mCursor);
                Timber.d(rows + " items saved");
            }
        });
    }
}
