package io.github.giusan82.easytrip.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import io.github.giusan82.easytrip.data.CacheContract.CacheEntry;
import io.github.giusan82.easytrip.data.PlacesContract.PlacesEntry;
import io.github.giusan82.easytrip.data.WeatherContract.WeatherEntry;
import timber.log.Timber;


public class DataProvider extends ContentProvider {

    public static final int CACHE_DATA = 100;
    public static final int CACHE_DATA_ID = 101;
    public static final int PLACES_DATA = 200;
    public static final int PLACES_DATA_ID = 201;
    public static final int WEATHER_DATA = 300;
    public static final int WEATHER_DATA_ID = 301;

    //UriMatcher object to match a content URI to a corresponding code.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer for sUriMatcher
    static {
        sUriMatcher.addURI(CacheContract.CONTENT_AUTHORITY, CacheContract.PATH_DATA_CACHED, CACHE_DATA);
        sUriMatcher.addURI(CacheContract.CONTENT_AUTHORITY, CacheContract.PATH_DATA_CACHED + "/#", CACHE_DATA_ID);
        sUriMatcher.addURI(PlacesContract.CONTENT_AUTHORITY, PlacesContract.PATH_PLACES_DATA, PLACES_DATA);
        sUriMatcher.addURI(PlacesContract.CONTENT_AUTHORITY, PlacesContract.PATH_PLACES_DATA + "/#", PLACES_DATA_ID);
        sUriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER_DATA, WEATHER_DATA);
        sUriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER_DATA + "/#", WEATHER_DATA_ID);
    }

    //initialize a DataDbHelper object
    private DataDbHelper dataDbHelper;
    private Cursor mCursor;

    @Override
    public boolean onCreate() {
        dataDbHelper = new DataDbHelper(getContext());
        return true;
    }

    //Perform the query for the given URI.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase db = dataDbHelper.getReadableDatabase();

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CACHE_DATA:
                // This queries the values table directly with the given
                // projection, selection, selection arguments, and sort order.
                mCursor = db.query(CacheEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CACHE_DATA_ID:
                // This extracts out the ID from the URI and queries the table at specific id
                selection = CacheEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query at specific id
                mCursor = db.query(CacheEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PLACES_DATA:
                mCursor = db.query(PlacesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PLACES_DATA_ID:
                selection = PlacesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                mCursor = db.query(PlacesEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case WEATHER_DATA:
                mCursor = db.query(WeatherEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case WEATHER_DATA_ID:
                selection = WeatherEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                mCursor = db.query(WeatherEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        //set notification URI on the Cursor. If the data at this URI changes, it knows where need to update the Cursor.
        mCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return mCursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CACHE_DATA:
                return insertValue(uri, contentValues, CacheEntry.COLUMN_URL, CacheEntry.TABLE_NAME);
            case PLACES_DATA:
                return insertValue(uri, contentValues, PlacesEntry.COLUMN_URL, PlacesEntry.TABLE_NAME);
            case WEATHER_DATA:
                return insertValue(uri, contentValues, WeatherEntry.COLUMN_URL, WeatherEntry.TABLE_NAME);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a new task into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertValue(Uri uri, ContentValues values, String column_required, String table_name) {
        // Check that the name is not null
        String name = values.getAsString(column_required);
        if (name == null) {
            throw new IllegalArgumentException("Task requires a name");
        }
        SQLiteDatabase database = dataDbHelper.getWritableDatabase();
        // Insert the new item with the given values
        long id = database.insert(table_name, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Timber.d("Failed to insert row for " + uri);
            return null;
        }
        //return the new URI with the ID append at the end
        // Notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri, null);
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CACHE_DATA:
                return updateItem(uri, contentValues, selection, selectionArgs, CacheEntry.TABLE_NAME, CacheEntry.COLUMN_URL);
            case CACHE_DATA_ID:
                // updates the table at specific id
                selection = CacheEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs, CacheEntry.TABLE_NAME, CacheEntry.COLUMN_URL);
            case PLACES_DATA:
                return updateItem(uri, contentValues, selection, selectionArgs, PlacesEntry.TABLE_NAME, PlacesEntry.COLUMN_URL);
            case PLACES_DATA_ID:
                // updates the table at specific id
                selection = PlacesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs, PlacesEntry.TABLE_NAME, PlacesEntry.COLUMN_URL);
            case WEATHER_DATA:
                return updateItem(uri, contentValues, selection, selectionArgs, WeatherEntry.TABLE_NAME, WeatherEntry.COLUMN_URL);
            case WEATHER_DATA_ID:
                // updates the table at specific id
                selection = WeatherEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs, WeatherEntry.TABLE_NAME, WeatherEntry.COLUMN_URL);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update the database with the given content values.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs, String table_name, String column_required) {
        /** If the {@link column_name} key is present, check that the name value is not null.*/
        if (values.containsKey(column_required)) {
            // Check that the name is not null
            String name = values.getAsString(column_required);
            if (name == null) {
                throw new IllegalArgumentException("Task requires a name");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        SQLiteDatabase database = dataDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(table_name, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        //Return the number of rows that were affected
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase db = dataDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CACHE_DATA:
                // Delete all rows that match the selection and selection args
                rowsDeleted = db.delete(CacheEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CACHE_DATA_ID:
                // Delete a single row given by the ID in the URI
                selection = CacheEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(CacheEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PLACES_DATA:
                // Delete all rows that match the selection and selection args
                rowsDeleted = db.delete(PlacesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PLACES_DATA_ID:
                // Delete a single row given by the ID in the URI
                selection = PlacesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(PlacesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WEATHER_DATA:
                // Delete all rows that match the selection and selection args
                rowsDeleted = db.delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case WEATHER_DATA_ID:
                // Delete a single row given by the ID in the URI
                selection = WeatherEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CACHE_DATA:
                return CacheEntry.CONTENT_LIST_TYPE;
            case CACHE_DATA_ID:
                return CacheEntry.CONTENT_DATA_TYPE;
            case PLACES_DATA:
                return PlacesEntry.CONTENT_LIST_TYPE;
            case PLACES_DATA_ID:
                return PlacesEntry.CONTENT_DATA_TYPE;
            case WEATHER_DATA:
                return WeatherEntry.CONTENT_LIST_TYPE;
            case WEATHER_DATA_ID:
                return WeatherEntry.CONTENT_DATA_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
