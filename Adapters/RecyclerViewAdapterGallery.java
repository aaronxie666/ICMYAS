package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import icn.icmyas.R;

import static icn.icmyas.Misc.Constants.EMPTY;

/**
 * Author:  Bradley Wilson
 * Date: 18/07/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class RecyclerViewAdapterGallery extends RecyclerView.Adapter<RecyclerViewAdapterGallery.RecyclerViewGalleryViewHolder> {

    private Context context;
    private onItemClickListener mItemClickListener;
    private JSONArray imageList;
    private boolean isCamera;
    private final String TAG = RecyclerViewAdapterGallery.class.getSimpleName();

    public RecyclerViewAdapterGallery(Context context, JSONArray imageList, boolean isCamera) {
        this.context = context;
        this.imageList = imageList;
        this.isCamera = isCamera;
    }

    @Override
    public int getItemCount() {
        return (null != imageList ? imageList.length() : 0);
    }

    @Override
    public void onBindViewHolder(RecyclerViewGalleryViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder");
        try {
            if (getItemCount() > 0) {
                String imageUrl = imageList.getString(position);
                if (!imageUrl.equals(EMPTY)) {
                    holder.profileImage.setVisibility(View.VISIBLE);
                    holder.lockContainer.setVisibility(View.GONE);
                    Picasso.with(context).load(imageUrl).resize(200, 200).centerCrop().into(holder.profileImage);
                } else {
                    holder.profileImage.setVisibility(View.GONE);
                    holder.lockContainer.setVisibility(View.VISIBLE);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed" + e.getLocalizedMessage());
        }
    }

    public void setOnItemClickListener(RecyclerViewAdapterGallery.onItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void updatePosition(JSONArray array, int position) {
        Log.e(TAG, "updatePositionCalled");
        try {
            imageList = new JSONArray(array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getImageList() {
        return imageList;
    }

    public interface onItemClickListener {
        void setOnItemClickListener(View view, int position, String image, boolean isLong);
    }

    @Override
    public RecyclerViewGalleryViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_gallery, viewGroup, false);
        return new RecyclerViewGalleryViewHolder(mainGroup);

    }

    public class RecyclerViewGalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ImageView profileImage;
        private LinearLayout lockContainer;

        public RecyclerViewGalleryViewHolder(View itemView) {
            super(itemView);
            this.profileImage = itemView.findViewById(R.id.modelProfilePicture);
            this.lockContainer = itemView.findViewById(R.id.lock_container);
            profileImage.setOnClickListener(this);
            lockContainer.setOnClickListener(this);
            profileImage.setOnLongClickListener(this);
            lockContainer.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                try {
                    mItemClickListener.setOnItemClickListener(view, getAdapterPosition(), imageList.getString(getAdapterPosition()), false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (mItemClickListener != null) {
                try {
                    mItemClickListener.setOnItemClickListener(view, getAdapterPosition(), imageList.getString(getAdapterPosition()), true);
                    return true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }
}
