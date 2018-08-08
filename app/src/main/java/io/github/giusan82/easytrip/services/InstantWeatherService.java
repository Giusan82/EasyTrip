package io.github.giusan82.easytrip.services;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.neovisionaries.i18n.CountryCode;

import java.net.URI;

import io.github.giusan82.easytrip.NetUtilities.ApiRequest;
import io.github.giusan82.easytrip.data.WeatherContract.WeatherEntry;
import io.github.giusan82.easytrip.data.WeatherData;
import io.github.giusan82.easytrip.ui.WeatherWidget;
import timber.log.Timber;

public class InstantWeatherService extends IntentService{
    public static final String ACTION_GET_DATA = "getting_data";
    private String mLatitude;
    private String mLongitude;
    public InstantWeatherService(){super("InstantWeatherService");}
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent != null){
            if(intent.getAction().equals(ACTION_GET_DATA)){
                mLatitude = intent.getStringExtra(WeatherData.EXTRA_WEATHERI_LATITUDE);
                mLongitude = intent.getStringExtra(WeatherData.EXTRA_WEATHERI_LONGITUDE);
                catchingWeatherData(mLatitude, mLongitude);
            }
        }
    }

    private void catchingWeatherData(String latitude, String longitude){
        if(latitude != null && longitude != null){
            final ApiRequest apiRequest = new ApiRequest(this);
            final String url = apiRequest.getWeatherUrl(latitude, longitude).toString();
            apiRequest.get(url, false, new ApiRequest.Callback() {
                @Override
                public void onSuccess(String result) {
                    Gson gson = new Gson();
                    WeatherData weatherData = gson.fromJson(result, WeatherData.class);
                    WeatherData.Data[] data = weatherData.getData();
                    Timber.d("Temperature: " + data[0].getCelsius());
                    CountryCode country = CountryCode.getByCode(data[0].getCountryCode());
                    Timber.d("Location: " + data[0].getCityName() + ", " + country.getName());

                    int rows = 0;

                    long creation_time = System.currentTimeMillis();
                    ContentValues cv = new ContentValues();
                    cv.put(WeatherEntry.COLUMN_CREATION_DATE, creation_time);
                    cv.put(WeatherEntry.COLUMN_URL, url);
                    cv.put(WeatherEntry.COLUMN_PLACE_NAME, data[0].getCityName());
                    cv.put(WeatherEntry.COLUMN_COUNTRY_CODE, data[0].getCountryCode());
                    cv.put(WeatherEntry.COLUMN_ICON, data[0].getWeather().getIcon());
                    cv.put(WeatherEntry.COLUMN_TEMPERATURE, data[0].getCelsius());
                    cv.put(WeatherEntry.COLUMN_DESCRIPTION, data[0].getWeather().getDescription());
                    cv.put(WeatherEntry.COLUMN_FLAG, WeatherData.KEY_CURRENT_FLAG);

                    try{
                        // Otherwise this is an EXISTING item, this update the item with content URI
                        if(!WeatherData.getUri(getApplicationContext()).isEmpty()){
                            int rowsAffected = getContentResolver().update(Uri.parse(WeatherData.getUri(getApplicationContext())), cv, null, null);
                            // Show a toast message depending on whether or not the update was successful.
                            if (rowsAffected == 0) {
                                Timber.d("Update Failed");
                            }
                        }else{
                            Uri newUri = getContentResolver().insert(WeatherEntry.CONTENT_URI, cv);
                            WeatherData.setUri(getApplicationContext(), newUri.toString());
                            if(newUri != null){
                                rows++;
                            }
                        }

                    }catch (IllegalArgumentException e){
                        Timber.e(e.getClass().getCanonicalName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                    getData();
                    Timber.d(rows + " new items saved");
                }
            });
        }

    }
    private void getData(){
        Uri uri = WeatherEntry.CONTENT_URI;
        String sortOrder = WeatherEntry.ID + " ASC";
        String selection = WeatherEntry.COLUMN_FLAG + " = ?";
        String[] selectionArgs = {WeatherData.KEY_CURRENT_FLAG};
        Cursor cursor = getContentResolver().query(
                uri,
                null,
                selection,
                selectionArgs,
                sortOrder
        );
        String description = "";
        String temperature = "";
        String city = "";
        String country_code = "";
        String icon = "";
        if(cursor != null){

            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                //get data here
                description = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_DESCRIPTION));
                temperature = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_TEMPERATURE));
                city = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_PLACE_NAME));
                country_code = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_COUNTRY_CODE));
                icon = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_ICON));
                cursor.close();
            }
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, WeatherWidget.class));
        //update all widgets
        WeatherWidget.onUpdateWidget(this, appWidgetManager, appWidgetIds, description, temperature, city, country_code, icon);

        String weather_id = WeatherData.getUri(this);
        if(!weather_id.isEmpty()){
            Tasks.sheduleUpdateWeather(this, true);
        }
    }
}
