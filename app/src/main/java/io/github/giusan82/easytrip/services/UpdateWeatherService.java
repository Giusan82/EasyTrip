package io.github.giusan82.easytrip.services;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;
import com.neovisionaries.i18n.CountryCode;

import io.github.giusan82.easytrip.NetUtilities.ApiRequest;
import io.github.giusan82.easytrip.data.WeatherData;
import io.github.giusan82.easytrip.data.WeatherContract.WeatherEntry;
import timber.log.Timber;

public class UpdateWeatherService extends JobService {
    private Context mContext = this;
    public static final String ACTION_UPDATE_FINISHED = "update_finished";

    @Override
    public boolean onStartJob(JobParameters job) {
        final String weather_uri = WeatherData.getUri(mContext);
        String latitude = WeatherData.getLatitude(mContext);
        String longitude = WeatherData.getLongitude(mContext);

        Timber.d("ID: " + weather_uri);
        if (!weather_uri.isEmpty()) {
            ApiRequest apiRequest = new ApiRequest(mContext);
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

                    long creation_time = System.currentTimeMillis();
                    ContentValues cv = new ContentValues();
                    cv.put(WeatherEntry.COLUMN_CREATION_DATE, creation_time);
                    cv.put(WeatherEntry.COLUMN_URL, url);
                    cv.put(WeatherEntry.COLUMN_PLACE_NAME, data[0].getCityName());
                    cv.put(WeatherEntry.COLUMN_COUNTRY_CODE, data[0].getCountryCode());
                    cv.put(WeatherEntry.COLUMN_ICON, data[0].getWeather().getIcon());
                    cv.put(WeatherEntry.COLUMN_TEMPERATURE, data[0].getCelsius());
                    cv.put(WeatherEntry.COLUMN_DESCRIPTION, data[0].getWeather().getDescription());
                    try {
                        // Otherwise this is an EXISTING item, this update the item with content URI
                        int rowsAffected = getContentResolver().update(Uri.parse(weather_uri), cv, null, null);
                        // Show a toast message depending on whether or not the update was successful.
                        if (rowsAffected == 0) {
                            Timber.d("Update Failed");
                        } else {
                            Intent intent = new Intent(ACTION_UPDATE_FINISHED);
                            sendBroadcast(intent);
                            Timber.d("Update successful, row affected: " + rowsAffected);
                        }
                    } catch (IllegalArgumentException e) {
                        Timber.e(e.getClass().getCanonicalName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Timber.d("Service Stopped");
        return true; //it is true, because as soon as the conditions are re-met again, the job should be retried again
    }
}
