package io.github.giusan82.easytrip.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.ui.CurrentLocationActivity;
import timber.log.Timber;

import static android.content.Context.LOCATION_SERVICE;

public class LocationUtilitis {
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private Context mContext;
    private View mContent;
    public LocationUtilitis(Context context, View content){
        mContext = context;
        mContent = content;
    }
    public boolean enableAccessFineLocation() {
        boolean isEnable = false;
        LocationManager mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //this verify if the location permission is granted
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //here it is requested location permission, showing an alert dialog where the user can grant or refuse the permission
                Activity activity = (Activity) mContext;
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_FINE_LOCATION);
            } else {
                Timber.d("Location Permission Granted");
                isEnable = true;
                //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mLocationListener);
            }
        }else{
            isEnable = false;
            Snackbar.make(mContent, mContext.getString(R.string.need_gps_message), Snackbar.LENGTH_LONG).show();
            Timber.d("GPS status is off");
        }

        return isEnable;
    }
}
