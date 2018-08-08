package io.github.giusan82.easytrip.utilities;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;

import io.github.giusan82.easytrip.data.PlacesData;
import io.github.giusan82.easytrip.ui.PlacesFragment;
import io.github.giusan82.easytrip.data.PlacesContract.PlacesEntry;
import timber.log.Timber;

/**
 * Provides the appropriate {@link Fragment} for a view pager.
 */
public class TabsAdapter extends FragmentStatePagerAdapter {
    /**
     * Context of the app
     */
    private Context mContext;
    private String mAction;
    private Cursor mCursor;
    private ArrayList<PlacesData.Results> mItems;
    private PlacesFragment mFragment;
    private FragmentManager mFragmentManager;

    /**
     * Create a new {@link TabsAdapter} object.
     *
     * @param context is the context of the app
     * @param fm      is the fragment manager that will keep each fragment's state in the adapter
     *                across swipes.
     */
    public TabsAdapter(Context context, FragmentManager fm, String action) {
        super(fm);
        mContext = context;
        mAction = action;
        mFragmentManager = fm;
    }
    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }
    //this set the fragment for each position
    @Override
    public Fragment getItem(int position) {
        Timber.d("Get Item");
        mFragment = new PlacesFragment();
        Bundle arguments = new Bundle();
        if (mAction == PlacesData.ACTION_KEY_PLACES) {
            mCursor.moveToPosition(position);
            String name = mCursor.getString(mCursor.getColumnIndex(PlacesEntry.COLUMN_PLACE_NAME));
            arguments.putString(PlacesData.BUNDLE_KEY_PLACE_NAME, name);
            String country = mCursor.getString(mCursor.getColumnIndex(PlacesEntry.COLUMN_PLACE_COUNTRY_NAME));
            arguments.putString(PlacesData.BUNDLE_KEY_COUNTRY_NAME, country);
            String parent = mCursor.getString(mCursor.getColumnIndex(PlacesEntry.COLUMN_PLACE_PARENT_NAME));
            arguments.putString(PlacesData.BUNDLE_KEY_PARENT_NAME, parent);
            String image_url = mCursor.getString(mCursor.getColumnIndex(PlacesEntry.COLUMN_IMAGE_URL));
            arguments.putString(PlacesData.BUNDLE_KEY_PLACE_IMAGE_URL, image_url);
            String image_parent_url = mCursor.getString(mCursor.getColumnIndex(PlacesEntry.COLUMN_PARENT_IMAGE_URL));
            arguments.putString(PlacesData.BUNDLE_KEY_PARENT_PLACE_IMAGE_URL, image_parent_url);
            String intro = mCursor.getString(mCursor.getColumnIndex(PlacesEntry.COLUMN_INTRO));
            arguments.putString(PlacesData.BUNDLE_KEY_INTRO, intro);
            mFragment.setArguments(arguments);
            return mFragment;
        } else if (mAction == PlacesData.ACTION_KEY_LOCATION) {
            PlacesData.Results current = mItems.get(position);
            arguments.putString(PlacesData.BUNDLE_KEY_PLACE_NAME, current.getName());
            if(current.getImages().length >0){
                arguments.putString(PlacesData.BUNDLE_KEY_PLACE_IMAGE_URL, current.getImages()[0].getSizes().getMedium().getImageUrl());
            }
            arguments.putString(PlacesData.BUNDLE_KEY_INTRO, current.getIntro());
            mFragment.setArguments(arguments);
            return mFragment;
        } else {
            return null;
        }
    }

    //get the total number of fragment
    @Override
    public int getCount() {
        if (mAction == PlacesData.ACTION_KEY_PLACES) {
            if (null == mCursor) return 0;
            return mCursor.getCount();
        } else if (mAction == PlacesData.ACTION_KEY_LOCATION) {
            if (mItems == null) return 0;
            return mItems.size();
        } else {
            return 0;
        }

    }

    //this set the label of the tabs
    @Override
    public CharSequence getPageTitle(int position) {
        if (mAction == PlacesData.ACTION_KEY_PLACES) {
            String label = "";
            for (int i = 0; i < mCursor.getCount(); i++) {
                label = String.valueOf(position + 1);
            }
            return label;
        } else if (mAction == PlacesData.ACTION_KEY_LOCATION) {
            String label = "";
            for (int i = 0; i < mItems.size(); i++) {
                label = String.valueOf(position + 1);
            }
            return label;
        } else {
            return "";
        }
    }



    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        this.mCursor = newCursor;
        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
        return mCursor;
    }

    public ArrayList<PlacesData.Results> setChangingList(ArrayList<PlacesData.Results> newItems) {
        this.mItems = newItems;
        if(mItems != null){
            Timber.d("Notify data changed");
            this.notifyDataSetChanged();
        }
        return mItems;
    }
}