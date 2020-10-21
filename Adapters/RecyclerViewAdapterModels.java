package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Models.FeaturedModel;
import icn.icmyas.R;

import static icn.icmyas.Misc.Constants.NO_PICTURE;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class RecyclerViewAdapterModels extends RecyclerView.Adapter<RecyclerViewAdapterModels.RecyclerViewModelListViewHolder> {

    private Context context;
    private ArrayList<FeaturedModel> modelsList;

    public RecyclerViewAdapterModels(Context context, ArrayList<FeaturedModel> modelsList) {
        this.context = context;
        this.modelsList = modelsList;
    }

    @Override
    public int getItemCount() {
        return (null != modelsList ? modelsList.size() : 0);

    }

    @Override
    public void onBindViewHolder(RecyclerViewModelListViewHolder holder, int position) {
        final FeaturedModel model = modelsList.get(position);
        if (position <= 3) {
            holder.positionContainer.setVisibility(View.VISIBLE);
            holder.userPosition.setText(String.valueOf(position + 1));
        } else {
            holder.positionContainer.setVisibility(View.GONE);
        }

        holder.userVotes.setText(String.valueOf(model.getUserVotes()));
        if (model.getProfilePictureUrl().equals(NO_PICTURE)) {
            Picasso.with(context).load(R.drawable.no_profile).into(holder.profileImage);
        } else {
            Picasso.with(context).load(model.getProfilePictureUrl()).into(holder.profileImage);
        }

    }

    @Override
    public RecyclerViewModelListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_model_leaderboard, viewGroup, false);
        return new RecyclerViewModelListViewHolder(mainGroup);

    }

    public class RecyclerViewModelListViewHolder extends RecyclerView.ViewHolder {

        private ImageView profileImage;
        private TextView userVotes, userPosition;
        private FrameLayout positionContainer;

        public RecyclerViewModelListViewHolder(View itemView) {
            super(itemView);
            this.userVotes = itemView.findViewById(R.id.modelVotes);
            this.profileImage = itemView.findViewById(R.id.modelProfilePicture);
            this.userPosition = itemView.findViewById(R.id.leaderboard_number);
            this.positionContainer = itemView.findViewById(R.id.position_container);

        }
    }
}
