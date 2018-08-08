package io.github.giusan82.easytrip.utilities;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.ui.MainActivity;

import io.github.giusan82.easytrip.data.CacheContract.CacheEntry;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {
    private static final String COMMA_SEPARATOR = ", ";
    private static final String DOT_SEPARATOR = ". ";
    private Context mContext;
    private Cursor mCursor;

    public final ListAdapter.ItemListOnClickHandler mCallback;

    public interface ItemListOnClickHandler {
        void onClickList(Cursor cursor, int position);
    }

    /**
     * Constructor
     */
    public ListAdapter(Context context, ItemListOnClickHandler onClickHandler) {
        this.mContext = context;
        this.mCallback = onClickHandler;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.grid;

        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        ListViewHolder viewHolder = new ListViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ListViewHolder holder, int position) {

        mCursor.moveToPosition(position);
        StringBuilder builder = new StringBuilder();
        builder.append(position + 1);
        builder.append(DOT_SEPARATOR);
        String place_name = mCursor.getString(mCursor.getColumnIndex(CacheEntry.COLUMN_PLACE_NAME));
        String country_name = mCursor.getString(mCursor.getColumnIndex(CacheEntry.COLUMN_PLACE_COUNTRY_NAME));
        String parent_name = mCursor.getString(mCursor.getColumnIndex(CacheEntry.COLUMN_PLACE_PARENT_NAME));
        builder.append(place_name);
        if(!country_name.equals("")){
            if(!parent_name.equals("")) {
                builder.append(COMMA_SEPARATOR);
                builder.append(parent_name);
                builder.append(COMMA_SEPARATOR);
                builder.append(country_name);

            }else{
                builder.append(COMMA_SEPARATOR);
                builder.append(country_name);
            }
        }
        String name = builder.toString();
        holder.tv_name.setText(name);
        CardView.LayoutParams viewParams= new CardView.LayoutParams(CardView.LayoutParams.MATCH_PARENT, getHeightListImage((MainActivity)mContext));
        int viewMargins = (int) mContext.getResources().getDimension(R.dimen.card_margin);
        viewParams.setMargins(viewMargins, viewMargins, viewMargins, viewMargins);
        holder.list_container.setLayoutParams(viewParams);
        String image_url = mCursor.getString(mCursor.getColumnIndex(CacheEntry.COLUMN_IMAGE_URL));
        if(image_url != null){
            if(image_url.isEmpty()){
                holder.iv_image_list.setImageResource(R.drawable.placeholder);
            }else{
                Glide.with(mContext).load(image_url).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.tv_name.setVisibility(View.VISIBLE);
                        return false;
                    }
                }).crossFade().dontTransform().into(holder.iv_image_list);
            }
        }
        holder.iv_image_list.setContentDescription(mContext.getString(R.string.destination_images_content_description, name));
        holder.setIsRecyclable(false);

    }

    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor newCursor) {
        if(newCursor == mCursor){
            return null;
        }
        this.mCursor = newCursor;
        if(newCursor != null){
            this.notifyDataSetChanged();
        }
        return mCursor;
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_name;
        public ImageView iv_image_list;
        public View list_container;

        public ListViewHolder(View itemView) {
            super(itemView);
            this.tv_name = itemView.findViewById(R.id.tw_name);
            this.iv_image_list = itemView.findViewById(R.id.iv_image_list);
            this.list_container = itemView.findViewById(R.id.list_container);
            this.list_container.setOnClickListener(mViewListener);

        }
        private View.OnClickListener mViewListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                mCallback.onClickList(mCursor, position);

            }
        };
    }

    //source:  Udacity reviewer
    public static int numberOfColumns(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        //if (nColumns < 2) return 2; //to keep the grid aspect
        return nColumns;
    }

    public static int getHeightListImage(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = (width/numberOfColumns(activity)) *2/3;
        return height;
    }
}
