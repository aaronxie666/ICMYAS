package icn.icmyas.Adapters;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.R;

public class RecyclerViewFModelsAdapter extends RecyclerView.Adapter<RecyclerViewFModelsAdapter.RecyclerViewFModelsViewHolder> {

    private Context context;
    private List<ParseObject> featuredModels;
    private ArrayList<String> votedOnArray;
    private onTickItemClickListener mItemClickListener;
    private boolean isIMP;

    public RecyclerViewFModelsAdapter(Context context, List<ParseObject> featuredModels, ArrayList<String> votedOnArray, boolean isIMP) {
        this.context = context;
        this.featuredModels = featuredModels;
        this.votedOnArray = votedOnArray;
        this.isIMP = isIMP;
    }

    @Override
    public RecyclerViewFModelsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_featured_models, parent, false);
        return new RecyclerViewFModelsViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerViewFModelsViewHolder holder, int position) {
        ParseObject featuredModel = featuredModels.get(position);
        try {
            if (isIMP) {
                ParseObject user = featuredModel.getParseObject(Constants.IMP_FEATURED_USER_KEY).fetchIfNeeded();
                Picasso.with(context).load(user.getString(Constants.USER_PROFILE_PICTURE_KEY)).fit().centerCrop().transform(new CircleTransform()).into(holder.profilePicture);
                ParseQuery<ParseObject> getIMPObjId = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
                getIMPObjId.whereEqualTo(Constants.IMP_COMP_USER_KEY, user);
                if (votedOnArray.contains(getIMPObjId.getFirst().getObjectId())) {
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                    holder.tick.setColorFilter(cf);
                    holder.tick.setEnabled(false);
                } else {
                    holder.tick.clearColorFilter();
                    holder.tick.setEnabled(true);
                }
            } else {
                // get SkypeCompetition ID of featured model
                ParseObject user = featuredModel.getParseObject(Constants.FEATURED_MODELS_USER_KEY).fetchIfNeeded();
                // load user profile picture into ImageView
                Picasso.with(context).load(user.getString(Constants.USER_PROFILE_PICTURE_KEY)).fit().centerCrop().transform(new CircleTransform()).into(holder.profilePicture);
                // check if user has already voted on featured model
                ParseQuery<ParseObject> getSkypeCompObjId = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
                getSkypeCompObjId.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, user);
                if (votedOnArray.contains(getSkypeCompObjId.getFirst().getObjectId())) {
                    // if user has voted on featured model, disable vote button
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                    holder.tick.setColorFilter(cf);
                    holder.tick.setEnabled(false);
                } else {
                    holder.tick.clearColorFilter();
                    holder.tick.setEnabled(true);
                }
            }
        } catch (ParseException e) {
            Log.e("debug", "featured models adapter: " + e.getLocalizedMessage());
        }
    }

    public void setOnItemClickListener(onTickItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onTickItemClickListener {
        void onItemClickListener(View view, int position, ParseObject featuredModel, boolean isLongClicked);
    }

    @Override
    public int getItemCount() {
        return (featuredModels != null ? featuredModels.size() : 0);
    }

    public class RecyclerViewFModelsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView profilePicture, tick;

        public RecyclerViewFModelsViewHolder(View itemView) {
            super(itemView);
            this.profilePicture = itemView.findViewById(R.id.profile_picture);
            this.tick = itemView.findViewById(R.id.featured_tick);
            tick.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickListener(view, getAdapterPosition(), featuredModels.get(getAdapterPosition()), false);
                /*ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0);
                ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
                this.tick.setColorFilter(cf);
                this.tick.setEnabled(false);*/
            }
        }
    }
}
