package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import icn.icmyas.Misc.Constants;
import icn.icmyas.Models.IMPModel;
import icn.icmyas.R;

public class IMPVotingRecyclerAdapter extends RecyclerView.Adapter<IMPVotingRecyclerAdapter.IMPVotingViewHolder> {

    private Context context;
    private ArrayList<IMPModel> entrants;
    private final String TAG = "IMPVotingAdapter";
    public AdapterListener onClickListener;

    public IMPVotingRecyclerAdapter(final Context context, final ArrayList<IMPModel> entrants, AdapterListener onClickListener) {
        this.context = context;
        this.entrants = entrants;
        this.onClickListener = onClickListener;
    }

    @Override
    public IMPVotingRecyclerAdapter.IMPVotingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_imp_entrants, parent, false);
        return new IMPVotingRecyclerAdapter.IMPVotingViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(IMPVotingRecyclerAdapter.IMPVotingViewHolder holder, int position) {
        IMPModel model = entrants.get(position);
        holder.heartButton.setVisibility(View.VISIBLE);
        holder.heartButton.setEnabled(true);
        ParseObject user = null;
        ParseObject details = null;
        ArrayList<String> photos = new ArrayList<>();
        try {
            user = model.getUser().fetch();
            String profilePictureUrl = user.getString(Constants.USER_PROFILE_PICTURE_KEY);
            photos.add(profilePictureUrl);
            holder.name.setText(user.getString(Constants.USER_FULL_NAME_KEY));
            details = model.getDetails();
            if (details != null) {
                JSONArray photosJSON = details.getJSONArray(Constants.IMP_DETAILS_IMAGES_KEY);
                for (int i = 0; i < photosJSON.length(); i++) {
                    try {
                        String photo = photosJSON.getString(i);
                        if (photo != null && photo.length() > 0) {
                            photos.add(photo);
                        }
                    } catch (JSONException e) {
                        // Log.e(TAG, "failed to get IMP details from JSON array: " + e.getLocalizedMessage());
                    }
                }
                String videoLink = details.getString(Constants.IMP_DETAILS_VIDEO_LINK_KEY);
                boolean hasVideo = videoLink != null && videoLink.length() > 0;
                if (hasVideo && details.getBoolean(Constants.IMP_DETAILS_VIDEO_APPROVED_KEY)) {
                    holder.viewVideoButton.setVisibility(View.VISIBLE);
                }
            }
            IMPDetailsPagerAdapter pagerAdapter = new IMPDetailsPagerAdapter(context, photos);
            holder.pager.setAdapter(pagerAdapter);
            holder.pager.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Touched!", Toast.LENGTH_SHORT).show();
                }
            });
            pagerAdapter.notifyDataSetChanged();
        } catch (ParseException e) {
            Log.e(TAG, "failed to get user from IMPModel: " + e.getLocalizedMessage());
        }
    }

    public void removeAt(int pos) {
        entrants.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, entrants.size());
    }

    public void remove(String userId) {
        for (int i = 0; i < entrants.size(); i++) {
            if (entrants.get(i).getUser().getObjectId().equals(userId)) {
                entrants.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, entrants.size());
                return;
            }
        }
    }

    public IMPModel getItemAt(int pos) {
        return entrants.get(pos);
    }

    @Override
    public int getItemCount() { return (entrants != null ? entrants.size() : 0); }

    public interface AdapterListener {
        void viewVideoOnClick(int pos);
        void heartOnClick(int pos);
    }

    public class IMPVotingViewHolder extends RecyclerView.ViewHolder {

        private ViewPager pager;
        private TextView name;
        private ImageView heartButton, viewVideoButton;

        public IMPVotingViewHolder(View itemView) {
            super(itemView);
            this.pager = itemView.findViewById(R.id.photos_pager);
            this.name = itemView.findViewById(R.id.name);
            this.heartButton = itemView.findViewById(R.id.btn_heart);
            this.heartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    castVote();
                }
            });
            this.viewVideoButton = itemView.findViewById(R.id.btn_view_video);
            this.viewVideoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.viewVideoOnClick(getAdapterPosition());
                }
            });
        }

        private void castVote() {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.setDuration(250);
            fadeOut.setAnimationListener(new Animation.AnimationListener()
            {
                public void onAnimationEnd(Animation animation)
                {
                    heartButton.setVisibility(View.GONE);
                    heartButton.setEnabled(false);
                    onClickListener.heartOnClick(getAdapterPosition());
                }
                public void onAnimationRepeat(Animation animation) {
                    //
                }
                public void onAnimationStart(Animation animation) {
                    //
                }
            });

            heartButton.startAnimation(fadeOut);
        }
    }
}
