package io.github.giusan82.easytrip.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.utilities.LocationUtilitis;

public class MainActivity extends AppCompatActivity {

    private Window mWindow;

    @BindView(R.id.toolbar)
    Toolbar mToolBar;
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.iv_actionBar_background)
    ImageView mActionBarBackground;
    @BindView(R.id.content)
    CoordinatorLayout mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind the views
        ButterKnife.bind(this);
        mWindow = this.getWindow();
        setSupportActionBar(mToolBar);

        Palette.from(((BitmapDrawable) mActionBarBackground.getDrawable()).getBitmap()).generate(new Palette.PaletteAsyncListener() {

            @Override
            public void onGenerated(Palette palette) {
                if (Build.VERSION.SDK_INT >= 21) {
                    mWindow.setStatusBarColor(palette.getDarkMutedSwatch().getRgb());
                }
                mCollapsingToolbarLayout.setContentScrimColor(palette.getDarkMutedSwatch().getRgb());
                //source of GradientDrawable: https://stackoverflow.com/a/6116273
                GradientDrawable gradiend = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{Color.BLACK, Color.BLACK, palette.getLightVibrantSwatch().getRgb()});
                gradiend.setCornerRadius(0f);
                mContent.setBackground(gradiend);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                if (settingsIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(settingsIntent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                return true;
            case R.id.action_map:
                openingCurrentLocation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mContent, getString(R.string.need_location_permission_message), Snackbar.LENGTH_LONG).show();
        } else {
            openingCurrentLocation();
        }
    }

    private void openingCurrentLocation() {
        Intent mapIntent = new Intent(this, CurrentLocationActivity.class);
        LocationUtilitis locationUtilitis = new LocationUtilitis(this, mContent);
        boolean isLocationEnable = locationUtilitis.enableAccessFineLocation();

        if (isLocationEnable) {
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }
    }
}
