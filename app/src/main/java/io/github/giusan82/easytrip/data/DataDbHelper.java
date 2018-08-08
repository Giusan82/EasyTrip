package io.github.giusan82.easytrip.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.giusan82.easytrip.data.CacheContract.CacheEntry;
import io.github.giusan82.easytrip.data.PlacesContract.PlacesEntry;
import io.github.giusan82.easytrip.data.WeatherContract.WeatherEntry;
import timber.log.Timber;

public class DataDbHelper extends SQLiteOpenHelper {
    //Name of the database file
    private static final String DATABASE_NAME = "easytrip.db";
    //Database version.
    private static final int DATABASE_VERSION = 1;

    // this contains the SQL statement to create the table
    private static final String SQL_CREATE_TABLE_CACHE = "CREATE TABLE " + CacheEntry.TABLE_NAME + " ("
            + CacheEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CacheEntry.COLUMN_CREATION_DATE + " REAL NOT NULL, "
            + CacheEntry.COLUMN_URL + " TEXT NOT NULL, "
            + CacheEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, "
            + CacheEntry.COLUMN_PLACE_NAME + " TEXT, "
            + CacheEntry.COLUMN_PLACE_COUNTRY_NAME + " TEXT, "
            + CacheEntry.COLUMN_PLACE_PARENT_NAME + " TEXT, "
            + CacheEntry.COLUMN_SCORE + " REAL, "
            + CacheEntry.COLUMN_IMAGE_URL + " TEXT"
            + ");";

    private static final String SQL_CREATE_TABLE_PLACES = "CREATE TABLE " + PlacesEntry.TABLE_NAME + " ("
            + PlacesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PlacesEntry.COLUMN_CREATION_DATE + " REAL NOT NULL, "
            + PlacesEntry.COLUMN_URL + " TEXT NOT NULL, "
            + PlacesEntry.COLUMN_LOCATION_ID + " TEXT NOT NULL, "
            + PlacesEntry.COLUMN_PLACE_NAME + " TEXT, "
            + PlacesEntry.COLUMN_PLACE_COUNTRY_NAME + " TEXT, "
            + PlacesEntry.COLUMN_PLACE_PARENT_NAME + " TEXT, "
            + PlacesEntry.COLUMN_SCORE + " REAL, "
            + PlacesEntry.COLUMN_IMAGE_URL + " TEXT, "
            + PlacesEntry.COLUMN_PARENT_IMAGE_URL + " TEXT, "
            + PlacesEntry.COLUMN_INTRO + " TEXT"
            + ");";

    private static final String SQL_CREATE_TABLE_WEATHER = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " ("
            + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WeatherEntry.COLUMN_CREATION_DATE + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_URL + " TEXT NOT NULL, "
            + WeatherEntry.COLUMN_PLACE_NAME + " TEXT, "
            + WeatherEntry.COLUMN_COUNTRY_CODE + " TEXT, "
            + WeatherEntry.COLUMN_ICON + " TEXT, "
            + WeatherEntry.COLUMN_TEMPERATURE + " REAL, "
            + WeatherEntry.COLUMN_DESCRIPTION + " TEXT, "
            + WeatherEntry.COLUMN_FLAG + " TEXT"
            + ");";

    public DataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_TABLE_CACHE);
        db.execSQL(SQL_CREATE_TABLE_PLACES);
        db.execSQL(SQL_CREATE_TABLE_WEATHER);
        Timber.d("Created DataBase: " + db.getPath());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + CacheEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PlacesEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(db);
    }
}
