package io.github.giusan82.easytrip.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public class WeatherContract {

    //To prevent someone from accidentally instantiating the contract class
    private WeatherContract() {
    }

    //This is the name of the entire content provider expressed as package name
    public static final String CONTENT_AUTHORITY = "io.github.giusan82.easytrip";
    //Base URI used to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //Path appended to base content URI referred to the cache data table
    public static final String PATH_WEATHER_DATA = "weather_data";

    public static final class WeatherEntry implements BaseColumns {
        //The content URI to access the item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_WEATHER_DATA);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER_DATA;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_DATA_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER_DATA;
        //Name of database table
        public static final String TABLE_NAME = "weather";
        //Unique ID number for the item (only for use in the database table). Type: INTEGER
        public static final String ID = BaseColumns._ID;
        //item's date. Type: REAL
        public static final String COLUMN_CREATION_DATE = "creation_date";
        //Type: TEXT
        public static final String COLUMN_URL = "url";
        //Type: TEXT
        public static final String COLUMN_PLACE_NAME = "place_name";
        //Type: TEXT
        public static final String COLUMN_COUNTRY_CODE = "country_code";
        //Type: TEXT
        public static final String COLUMN_ICON = "icon";
        //Type: REAL
        public static final String COLUMN_TEMPERATURE = "temperature";
        //Type: TEXT
        public static final String COLUMN_DESCRIPTION = "description";
        //Type: TEXT
        public static final String COLUMN_FLAG = "flag";
    }
}