package io.github.giusan82.easytrip.NetUtilities;

import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.neovisionaries.i18n.CountryCode;

import java.net.MalformedURLException;
import java.net.URL;

import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.data.WeatherData;
import timber.log.Timber;

/**
 * This Api request was made following these tutorials:
 * https://www.androidhive.info/2014/05/android-working-with-volley-library-1/
 * https://www.sitepoint.com/volley-a-networking-library-for-android/
 * https://stackoverflow.com/questions/28120029/how-can-i-return-value-from-function-onresponse-of-volley
 */

public class ApiRequest {

    private static final String LOG_TAG = ApiRequest.class.getSimpleName();
    private static final int DELAY_MILLIS = 3000;
    //Places
    private static final String BASE_DESTINATION_URL = "https://www.triposo.com/api/20180627/location.json";
    private static final String BASE_PLACES_URL = "https://www.triposo.com/api/20180627/poi.json";
    private static final String ACCOUNT_ID_KEY = "account";
    private static final String TOKEN_KEY = "token";
    private static final String PARAM_ORDER_BY = "order_by";
    private static final String TAG_REQUEST = "api_request";
    private static final String PARAM_ANNOTATE = "annotate";
    private static final String PARAM_TRIGRAM = "trigram";
    private static final String PARAM_DISTANCE = "distance";
    private static final String PARAM_SCORE = "score";
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_LOCATION_ID = "location_id";
    private static final String PARAM_TAG_LABEL = "tag_labels";
    private static final String TAG_LABEL_VALUE = "sightseeing";
    private static final String PARAM_FIELDS = "fields";

    //Weather
    private static final String BASE_WEATHER_URL = "https://api.weatherbit.io/v2.0/current";
    private static final String API_WEATHER_KEY = "key";
    private static final String PARAM_LATITUDE = "lat";
    private static final String PARAM_LOGITUDE = "lon";
    private static final String PARAM_UNIT = "units";

    private Context mContext;
    private RequestQueue requestQueue;
    private SharedPreferences sharedPrefs;

    //Constructor
    public ApiRequest(Context context) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    public interface Callback {
        void onSuccess(String result);
    }

    public void get(String url, boolean hasLoading, final Callback callback) {

        final ProgressDialog loader;
        if (hasLoading) {
            loader = new ProgressDialog(mContext, R.style.alertDialog);
            loader.setTitle(mContext.getString(R.string.loading_title));
            loader.setMessage(mContext.getString(R.string.loading_message));
            loader.show();
        } else {
            loader = null;
        }
        requestQueue = Volley.newRequestQueue(mContext);
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                if (callback != null) {
                    callback.onSuccess(response);
                    if (loader != null) {
                        loader.dismiss();
                    }
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                Toast.makeText(mContext, mContext.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                if (loader != null) {
                    loader.dismiss();
                }
            }
        });
        stringRequest.setTag(TAG_REQUEST);
        requestQueue.add(stringRequest);
    }

    public URL getDestinationUrl(String query) {
        String orderBy = sharedPrefs.getString(mContext.getString(R.string.pref_orderBy_key), mContext.getString(R.string.pref_orderBy_default));
        Uri.Builder builtUri;
        builtUri = Uri.parse(BASE_DESTINATION_URL).buildUpon();
        builtUri.appendQueryParameter(PARAM_TAG_LABEL, "city");
        builtUri.appendQueryParameter(PARAM_ORDER_BY, orderBy);
        return buildUrl(builtUri, query);
    }

    public URL getPlacesUrl(String query, String location_id){
        String orderBy = sharedPrefs.getString(mContext.getString(R.string.pref_orderBy_key), mContext.getString(R.string.pref_orderBy_default));
        Uri.Builder builtUri;
        builtUri = Uri.parse(BASE_PLACES_URL).buildUpon();
        builtUri.appendQueryParameter(PARAM_LOCATION_ID, location_id);
        builtUri.appendQueryParameter(PARAM_ORDER_BY, orderBy);
        return buildUrl(builtUri, query);
    }

    public URL getUrlByLocation(String latitude, String longitude){
        Uri.Builder builtUri;
        builtUri = Uri.parse(BASE_PLACES_URL).buildUpon();
        builtUri.encodedQuery(PARAM_ANNOTATE + "=" + PARAM_DISTANCE + ":" + latitude + "," + longitude + "&" + PARAM_DISTANCE + "=<" + 5000 + "&" + PARAM_ORDER_BY + "=" + "-(" + PARAM_SCORE + "**3/" + PARAM_DISTANCE + ")");
        //builtUri.appendQueryParameter(PARAM_ORDER_BY, "-" + PARAM_DISTANCE);
        builtUri.appendQueryParameter(PARAM_TAG_LABEL, TAG_LABEL_VALUE);
        return buildUrl(builtUri, null);
    }

    private URL buildUrl(Uri.Builder builtUri, String query){
        String count = sharedPrefs.getString(mContext.getString(R.string.pref_count_key), mContext.getString(R.string.pref_count_default));
        if (query != null) {
            if (!query.isEmpty()){
                builtUri.encodedQuery(PARAM_ANNOTATE + "=" + PARAM_TRIGRAM + ":" + query + "&trigram=>=1")
                        .appendQueryParameter(PARAM_ORDER_BY, "-" + PARAM_TRIGRAM);
            }
        }
        builtUri.appendQueryParameter(PARAM_COUNT, count)
                .appendQueryParameter(PARAM_FIELDS, "all")
                .appendQueryParameter(ACCOUNT_ID_KEY, mContext.getString(R.string.triposo_account_id))
                .appendQueryParameter(TOKEN_KEY, mContext.getString(R.string.triposo_token_key))
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
            Timber.d("URL: " + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public URL getWeatherUrl(String latitude, String longitude){
        Uri.Builder builtUri;
        builtUri = Uri.parse(BASE_WEATHER_URL).buildUpon();
        builtUri.appendQueryParameter(PARAM_LATITUDE, latitude);
        builtUri.appendQueryParameter(PARAM_LOGITUDE, longitude);
        builtUri.appendQueryParameter(PARAM_UNIT, "S");
        builtUri.appendQueryParameter(API_WEATHER_KEY, mContext.getString(R.string.weatherbit_api_key));
        builtUri.build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
            Timber.d("URL: " + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


}
