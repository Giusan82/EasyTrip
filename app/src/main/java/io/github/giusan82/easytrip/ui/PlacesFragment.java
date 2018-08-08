package io.github.giusan82.easytrip.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.data.PlacesData;
import io.github.giusan82.easytrip.utilities.ListAdapter;
import timber.log.Timber;

public class PlacesFragment extends Fragment implements ListAdapter.ItemListOnClickHandler {

    private String mLocationId;
    private String mParentName;
    private String mCountryName;
    private ListAdapter mAdapter;
    private String mName;

    @BindView(R.id.intro)
    TextView mIntro;
    @BindView(R.id.place_title)
    TextView mPlaceTitle;
    @BindView(R.id.iv_header)
    ImageView mHeader;
    @BindView(R.id.lv_place_content)
    View mContent;


    public PlacesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_places, container, false);
        ButterKnife.bind(this, rootView);
        Timber.d("onCreateView");
        StringBuilder builder = new StringBuilder();
        if (getArguments().containsKey(PlacesData.BUNDLE_KEY_PLACE_NAME)) {
            builder.append(getArguments().getString(PlacesData.BUNDLE_KEY_PLACE_NAME));
            mName = builder.toString();
        }
        if (getArguments().containsKey(PlacesData.BUNDLE_KEY_PARENT_NAME)) {
            builder.append(", ");
            builder.append(getArguments().getString(PlacesData.BUNDLE_KEY_PARENT_NAME));
        }
        if (getArguments().containsKey(PlacesData.BUNDLE_KEY_COUNTRY_NAME)) {
            builder.append(", ");
            builder.append(getArguments().getString(PlacesData.BUNDLE_KEY_COUNTRY_NAME));
        }
        mPlaceTitle.setText(builder.toString());
        if (getArguments().containsKey(PlacesData.BUNDLE_KEY_PLACE_IMAGE_URL)) {
            String image_url = getArguments().getString(PlacesData.BUNDLE_KEY_PLACE_IMAGE_URL);
            Timber.d("Header: " + image_url);

            Glide.with(getContext()).load(image_url).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    //holder.tv_name.setVisibility(View.VISIBLE);
                    return false;
                }
            }).crossFade().dontTransform().into(mHeader);
        } else {
            mHeader.setImageResource(R.drawable.placeholder);
        }
        mHeader.setContentDescription(getString(R.string.destination_images_content_description, mName));
        if (getArguments().containsKey(PlacesData.BUNDLE_KEY_INTRO)) {
            mIntro.setText(getArguments().getString(PlacesData.BUNDLE_KEY_INTRO));
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClickList(Cursor cursor, int position) {

    }
}
