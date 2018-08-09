package io.github.giusan82.easytrip.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class PlacesContract {

    //To prevent someone from accidentally instantiating the contract class
    private PlacesContract() {
    }

    //This is the name of the entire content provider expressed as package name
    public static final String CONTENT_AUTHORITY = "io.github.giusan82.easytrip";
    //Base URI used to contact the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //Path appended to base content URI referred to the cache data table
    public static final String PATH_PLACES_DATA = "places_data";

    public static final class PlacesEntry implements BaseColumns {
        //The content URI to access the item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PLACES_DATA);
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of items.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLACES_DATA;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single item.
         */
        public static final String CONTENT_DATA_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PLACES_DATA;
        //Name of database table
        public static final String TABLE_NAME = "places";
        //Unique ID number for the item (only for use in the database table). Type: INTEGER
        public static final String ID = BaseColumns._ID;
        //item's date. Type: REAL
        public static final String COLUMN_CREATION_DATE = "creation_date";
        //Type: TEXT
        public static final String COLUMN_URL = "url";
        //Type: TEXT
        public static final String COLUMN_LOCATION_ID = "location_id";
        //Type: TEXT
        public static final String COLUMN_PLACE_NAME = "place_name";
        //Type: TEXT
        public static final String COLUMN_PLACE_PARENT_NAME = "place_parent_name";
        //Type: TEXT
        public static final String COLUMN_PLACE_COUNTRY_NAME = "place_country_name";
        //Type: TEXT
        public static final String COLUMN_IMAGE_URL = "image_url";
        //Type: TEXT
        public static final String COLUMN_PARENT_IMAGE_URL = "parent_image_url";
        //Type: REAL
        public static final String COLUMN_SCORE = "score";
        //Type: TEXT
        public static final String COLUMN_INTRO = "intro";
    }
}
