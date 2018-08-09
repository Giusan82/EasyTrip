package io.github.giusan82.easytrip.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.transition.Slide;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.giusan82.easytrip.NetUtilities.ApiRequest;
import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.data.PlacesData;
import io.github.giusan82.easytrip.data.WeatherData;
import io.github.giusan82.easytrip.services.InstantWeatherService;
import io.github.giusan82.easytrip.services.UpdateWeatherService;
import io.github.giusan82.easytrip.utilities.TabsAdapter;
import io.github.giusan82.easytrip.data.WeatherContract.WeatherEntry;
import timber.log.Timber;

public class CurrentLocationActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int DATA_LOADER_ID = 7;
    private static final String BUNDLE_EXPANSION_KEY = "expansion_key";
    private static final String BUNDLE_EMPTY_VIEW_KEY = "empty_view";

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;

    private GoogleApiClient mClient;
    private LocationManager mLocationManager;
    private PlaceDetectionClient mPlaceDetectionClient;
    private GeoDataClient mGeoDataClient;
    private FusedLocationProviderClient mLocationClient;
    private GoogleMap mMap;
    private Location mLastKnownLocation;
    private TabsAdapter mTabsAdapter;
    private ArrayList<PlacesData.Results> mPlaces;
    private ApiRequest mApiRequest;
    private boolean isExpanded;
    private boolean isEmpty;
    private boolean isFirst;
    private String mLatitude;
    private String mLongitude;
    private IntentFilter mIntentFilter;
    private Window mWindow;
    private Slide mSlide;
    private ConstraintSet mConstrainSet;

    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.location_content)
    ConstraintLayout mContent;
    @BindView(R.id.vp_current_location)
    ViewPager mViewPager;
    @BindView(R.id.tabs_current_location)
    TabLayout mTabLayout;
    @BindView(R.id.map)
    View mMapView;
    @BindView(R.id.fab)
    FloatingActionButton mExpander;
    @BindView(R.id.iv_weather_icon)
    ImageView mWeatherIcon;
    @BindView(R.id.tv_temperature)
    TextView mTemperature;
    @BindView(R.id.empty_view)
    View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_location);
        // Bind the views
        ButterKnife.bind(this);
        Timber.d("savedInstanceState: " + savedInstanceState);

        setSupportActionBar(mToolBar);
        if (mToolBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mWindow = this.getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            mWindow.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        mApiRequest = new ApiRequest(this);
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        //FusedLocationApi is now deprecated, so I obtained the location using FusedLocationProviderClient - source: https://stackoverflow.com/a/46482065
        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mPlaces = new ArrayList<>();
        // Create an adapter that knows which fragment should be shown on each page
        mTabsAdapter = new TabsAdapter(this, getSupportFragmentManager(), PlacesData.ACTION_KEY_LOCATION);
        // Set the adapter onto the view pager
        mViewPager.setAdapter(mTabsAdapter);
        mViewPager.setPageMargin(10);
        mTabLayout.setupWithViewPager(mViewPager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTabLayout.setElevation(3);
        }
        if (savedInstanceState == null) {
            isFirst = true;
        } else {
            mPlaces = (ArrayList<PlacesData.Results>) Parcels.unwrap(savedInstanceState.getParcelable(PlacesData.BUNDLE_KEY_PLACES_LIST));
            mTabsAdapter.setChangingList(mPlaces);
            isExpanded = savedInstanceState.getBoolean(BUNDLE_EXPANSION_KEY);
            isEmpty = savedInstanceState.getBoolean(BUNDLE_EMPTY_VIEW_KEY);
        }
        mSlide = new Slide();
        mConstrainSet = new ConstraintSet();
        if (!isExpanded) {
            actionExpanding();
        } else {
            actionCollapsing();
        }
        if (isEmpty) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
        mExpander.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isExpanded) {
                    actionCollapsing();
                    isExpanded = true;
                } else {
                    actionExpanding();
                    isExpanded = false;
                }
            }
        });
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(UpdateWeatherService.ACTION_UPDATE_FINISHED);
        getSupportLoaderManager().initLoader(DATA_LOADER_ID, null, this);
    }

    private void actionExpanding() {
        Timber.d("is Expanded? " + isExpanded);
        mExpander.setImageResource(R.drawable.ic_expand_less_24dp);
        mExpander.setContentDescription(getString(R.string.collapse_content));

        mSlide.setSlideEdge(Gravity.TOP);
        TransitionManager.beginDelayedTransition((ViewGroup) mMapView, mSlide);
        mMapView.setVisibility(View.VISIBLE);

        TransitionManager.beginDelayedTransition(mViewPager);
        // source: https://stackoverflow.com/a/45264822
        mConstrainSet.clone(mContent);
        mConstrainSet.clear(mViewPager.getId(), ConstraintSet.TOP);
        mConstrainSet.connect(mViewPager.getId(), ConstraintSet.TOP, mMapView.getId(), ConstraintSet.BOTTOM, 8);
        mConstrainSet.applyTo(mContent);
    }

    private void actionCollapsing() {
        Timber.d("is Expanded? " + isExpanded);
        mExpander.setImageResource(R.drawable.ic_expand_more_24dp);
        mExpander.setContentDescription(getString(R.string.expand_content));
        mSlide.setSlideEdge(Gravity.TOP);
        TransitionManager.beginDelayedTransition((ViewGroup) mMapView, mSlide);
        mMapView.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition(mViewPager);
        mConstrainSet.clone(mContent);
        mConstrainSet.clear(mViewPager.getId(), ConstraintSet.TOP);
        mConstrainSet.connect(mViewPager.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 72);
        mConstrainSet.applyTo(mContent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mUpdateReceiver, mIntentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mUpdateReceiver);
    }

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UpdateWeatherService.ACTION_UPDATE_FINISHED.equals(action)) {
                refresh();
            }
        }
    };

    private void refresh() {
        Timber.d("Refreshing data");
        getSupportLoaderManager().restartLoader(DATA_LOADER_ID, null, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.d("Google API connection successful");
        if (isFirst) {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mContent, getString(R.string.need_location_permission_message), Snackbar.LENGTH_LONG).show();
        } else {
            mLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mLatitude = String.valueOf(location.getLatitude());
                        mLongitude = String.valueOf(location.getLongitude());
                        Timber.d("Google Client: " + " Longitude: " + mLongitude + " | Latitude: " + mLatitude);
                        catchingData(mLatitude, mLongitude);
                        catchingWeatherData(mLatitude, mLongitude);
                        refresh();
                        WeatherData.setLatitude(getApplicationContext(), mLatitude);
                        WeatherData.setLongitude(getApplicationContext(), mLongitude);

                    } else {
                        Timber.d("No GPS data");
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Timber.d("Google API connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.d("Google API connection failed");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            Timber.d("Map is null");
            return;
        }
        try {
            mMap.setPadding(0, 0, 0, 50);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int i) {
                    Timber.d("Map is moved");
                }
            });
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    getLocation();
                    return false;
                }
            });
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    mTabLayout.getTabAt((int) marker.getZIndex()).select();
                    return false;
                }
            });
            if (mPlaces != null) {
                for (int i = 0; i < mPlaces.size(); i++) {
                    double latitude = Double.parseDouble(mPlaces.get(i).getCoordinates().getLatitude());
                    double longitude = Double.parseDouble(mPlaces.get(i).getCoordinates().getLogintude());
                    mMap.addMarker(new MarkerOptions().title(mPlaces.get(i).getName()).position(new LatLng(latitude, longitude)).zIndex(i));
                }
            }
        } catch (SecurityException e) {
            Timber.e(e.getMessage());
        }
        getOnMapLocation();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getOnMapLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         *
         * current place location on map was made following this tutorial: https://developers.google.com/places/android-sdk/current-place-tutorial
         * and this example: https://github.com/googlemaps/android-samples/tree/master/tutorials/CurrentPlaceDetailsOnMap
         */
        Timber.d("getting device location...");
        try {

            Timber.d("Permission granted");
            Task<Location> locationResult = mLocationClient.getLastLocation();
            locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.getResult();
                        if (mLastKnownLocation != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            Timber.d("Latitude: " + mLastKnownLocation.getAltitude() + " | Longitude: " + mLastKnownLocation.getLongitude());
                        }
                    } else {
                        Timber.d("Current location is null. Using defaults.");
                        Timber.e("Exception: %s", task.getException());
                        mMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void catchingData(String latitude, String longitude) {
        final String url = mApiRequest.getUrlByLocation(latitude, longitude).toString();
        Timber.d("url: " + url);
        mApiRequest.get(url, true, new ApiRequest.Callback() {
            @Override
            public void onSuccess(String result) {
                Gson gson = new Gson();
                PlacesData placesData = gson.fromJson(result, PlacesData.class);
                PlacesData.Results[] results = placesData.getResults();
                mPlaces.clear();
                for (int i = 0; i < results.length; i++) {
                    mPlaces.add(results[i]);
                    double latitude = Double.parseDouble(results[i].getCoordinates().getLatitude());
                    double longitude = Double.parseDouble(results[i].getCoordinates().getLogintude());
                    mMap.addMarker(new MarkerOptions().title(results[i].getName()).position(new LatLng(latitude, longitude)).zIndex(i));
                }
                if (mPlaces.size() == 0) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    isEmpty = true;
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    isEmpty = false;
                }
                mTabsAdapter.setChangingList(mPlaces);
                //refresh();
                Timber.d("items found: " + results.length);
            }
        });
    }

    private void catchingWeatherData(String latitude, String longitude) {
        Intent intent = new Intent(this, InstantWeatherService.class);
        intent.setAction(InstantWeatherService.ACTION_GET_DATA);
        intent.putExtra(WeatherData.EXTRA_WEATHERI_LATITUDE, latitude);
        intent.putExtra(WeatherData.EXTRA_WEATHERI_LONGITUDE, longitude);
        startService(intent);
        Timber.d("Downloading weather data");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(PlacesData.BUNDLE_KEY_PLACES_LIST, Parcels.wrap(mPlaces));
        outState.putBoolean(BUNDLE_EXPANSION_KEY, isExpanded);
        outState.putBoolean(BUNDLE_EMPTY_VIEW_KEY, isEmpty);
        Timber.d("Saving state " + isExpanded);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Timber.d("Restoring state " + isExpanded);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case DATA_LOADER_ID:

                Uri uri = WeatherEntry.CONTENT_URI;
                String sortOrder = WeatherEntry.ID + " ASC";
                String selection = WeatherEntry.COLUMN_FLAG + " = ?";
                String[] selectionArgs = {WeatherData.KEY_CURRENT_FLAG};
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
        if (cursor.getCount() == 0) {
            catchingWeatherData(mLatitude, mLongitude);
            return;
        }
        if (cursor.moveToFirst()) {
            String temperature = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_TEMPERATURE));
            String image = "@drawable/" + cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_ICON));
            int imageId = getResources().getIdentifier(image, null, getPackageName());
            mTemperature.setText(temperature);
            mWeatherIcon.setImageResource(imageId);
            String state = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DESCRIPTION));
            mWeatherIcon.setContentDescription(state);
            Timber.d("ID: " + cursor.getString(cursor.getColumnIndex(WeatherEntry._ID)));
            Timber.d("Weather updated");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Timber.d("Loader reset");
    }
}
