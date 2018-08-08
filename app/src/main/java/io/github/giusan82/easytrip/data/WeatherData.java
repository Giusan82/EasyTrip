package io.github.giusan82.easytrip.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.annotations.SerializedName;

public class WeatherData {
    private static final String KEY_DATA = "data";
    private static final String KEY_CITY_NAME = "city_name";
    private static final String KEY_WEATHER = "weather";
    private static final String KEY_ICON = "icon";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_TEMP = "temp";
    private static final String KEY_COUNTRY_CODE = "country_code";

    public static final String KEY_CURRENT_FLAG = "current";
    private static final String PREF_WEATHERI_URI = "pref_weather_uri";
    private static final String PREF_WEATHERI_LATITUDE = "pref_weather_latitude";
    private static final String PREF_WEATHERI_LONGITUDE = "pref_weather_longitude";

    public static final String EXTRA_WEATHERI_ID = "weather_id";
    public static final String EXTRA_WEATHERI_LATITUDE = "weather_latitude";
    public static final String EXTRA_WEATHERI_LONGITUDE = "weather_longitude";

    public static final String BUNDLE_KEY_TEMPERATURE = "temperature";
    public static final String BUNDLE_KEY_IMAGE_ICON = "image_icon";

    @SerializedName(KEY_DATA)
    public Data[] mData;

    public WeatherData(){}
    public Data[] getData(){return mData;}

    public class Data{
        @SerializedName(KEY_CITY_NAME)
        public String mCityName;
        @SerializedName(KEY_WEATHER)
        public Weather mWeather;
        @SerializedName(KEY_TEMP)
        public double mTemp;
        @SerializedName(KEY_COUNTRY_CODE)
        public String mCountryCode;


        public String getCityName(){return mCityName;}

        public String getCountryCode() {
            return mCountryCode;
        }

        public Weather getWeather() {
            return mWeather;
        }

        public double getTemp() {
            return mTemp;
        }
        public double getCelsius(){
            double celsius = mTemp - 273.15;
            return celsius;
        }
    }

    public class Weather{
        @SerializedName(KEY_ICON)
        public String mIcon;
        @SerializedName(KEY_DESCRIPTION)
        public String mDescription;

        public String getIcon() {
            return mIcon;
        }

        public String getDescription() {
            return mDescription;
        }
    }

    public static void setUri(Context context, String value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_WEATHERI_URI, value);
        editor.apply();
    }

    public static String getUri(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(PREF_WEATHERI_URI, "");
    }

    public static void setLatitude(Context context, String value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_WEATHERI_LATITUDE, value);
        editor.apply();
    }

    public static String getLatitude(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(PREF_WEATHERI_LATITUDE, "");
    }

    public static void setLongitude(Context context, String value) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_WEATHERI_LONGITUDE, value);
        editor.apply();
    }

    public static String getLongitude(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(PREF_WEATHERI_LONGITUDE, "");
    }
}
