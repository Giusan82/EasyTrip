package io.github.giusan82.easytrip.ui;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.neovisionaries.i18n.CountryCode;

import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.services.InstantWeatherService;

/**
 * Implementation of App Widget functionality.
 */
public class WeatherWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String description, String temperature, String city, String country_code, String icon) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        Intent open = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, open, 0);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
        if(description != null){
            views.setTextViewText(R.id.tv_widget_weather_state, description);
        }
        if(temperature != null){
            views.setTextViewText(R.id.tv_widget_temperature, temperature);
            views.setViewVisibility(R.id.tv_widget_temperature, View.VISIBLE);
            views.setViewVisibility(R.id.tv_widget_unit, View.VISIBLE);
        }
        if(city != null){
            views.setTextViewText(R.id.tv_widget_location, city);
            if(country_code != null){
                CountryCode countryCode = CountryCode.getByCode(country_code);
                if(countryCode != null){
                    String location = city + ", " + countryCode.getName();
                    views.setTextViewText(R.id.tv_widget_location, location);
                }
            }
            views.setViewVisibility(R.id.tv_widget_location, View.VISIBLE);
        }

        if(icon != null){
            String uriIcons = "@drawable/" + icon;
            views.setImageViewResource(R.id.iv_widget_weather_icon, context.getResources().getIdentifier(uriIcons, null, context.getPackageName()));
            views.setViewVisibility(R.id.iv_widget_weather_icon, View.VISIBLE);
        }


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        Intent intent = new Intent(context, InstantWeatherService.class);
        intent.setAction(InstantWeatherService.ACTION_GET_DATA);
        context.startService(intent);

    }

    public static void onUpdateWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, String description, String temperature, String city, String country_code, String icon){
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, description, temperature, city, country_code, icon);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

