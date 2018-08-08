package io.github.giusan82.easytrip.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.MenuItem;

import io.github.giusan82.easytrip.R;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        //Show the back arrow
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //Go back in the previous activity without reloading
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            //NavUtils.navigateUpFromSameTask(this);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return super.onOptionsItemSelected(item);
    }

    public static class ResultsPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        SharedPreferences sharedPreferences;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings_main);

            sharedPreferences = getPreferenceScreen().getSharedPreferences();
            PreferenceScreen prefScreen = getPreferenceScreen();
            refreshSummary(prefScreen);
        }

        //this update the listpreferences summary when the setting activity is launched
        private void refreshSummary(PreferenceGroup prefGroup) {
            int count = prefGroup.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference p = prefGroup.getPreference(i);
                if (p instanceof PreferenceGroup) {
                    refreshSummary((PreferenceGroup) p);
                } else if (!(p instanceof SwitchPreferenceCompat)) {
                    String value = sharedPreferences.getString(p.getKey(), "");
                    setPreferenceSummary(p, value);
                }
            }
        }

        //This method set the preference summary
        private void setPreferenceSummary(Preference preference, String value) {
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(value);
                if (prefIndex >= 0) {
                    listPreference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            }
//            if(preference instanceof EditTextPreference){
//                EditTextPreference editTextPreference = (EditTextPreference) preference;
//                editTextPreference.setSummary(value);
//            }
        }

        //Called when a shared preference is changed.
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            if (null != preference) {
                if (!(preference instanceof SwitchPreferenceCompat)) {
                    String value = sharedPreferences.getString(preference.getKey(), "");
                    setPreferenceSummary(preference, value);
                }
            }
        }

        //Registers a callback to be invoked when a change happens to a preference.
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        //Unregisters a previous callback.
        @Override
        public void onDestroy() {
            super.onDestroy();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
