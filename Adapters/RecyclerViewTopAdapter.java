package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;

import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.R;

/**
 * Author: Tom Linford
 * Date: 31/08/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class RecyclerViewTopAdapter extends RecyclerView.Adapter<RecyclerViewTopAdapter.RecyclerViewTopViewHolder> {

    private Context context;
    private List<ParseObject> entries;
    private boolean isIMP;

    public RecyclerViewTopAdapter(Context context, List<ParseObject> entries, boolean isIMP) {
        this.context = context;
        this.entries = entries;
        this.isIMP = isIMP;
    }

    @Override
    public RecyclerViewTopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_top_entrants, parent, false);
        return new RecyclerViewTopViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerViewTopViewHolder holder, int position) {
        ParseObject entry = entries.get(position);
        try {
            String votes = "";
            if (isIMP) {
                String profilePictureUrl = entry.getParseObject(Constants.IMP_COMP_USER_KEY).fetchIfNeeded().getString(Constants.USER_PROFILE_PICTURE_KEY);
                if (profilePictureUrl.equals(Constants.NO_PICTURE)) {
                    Picasso.with(context).load(R.drawable.no_profile).into(holder.image);
                } else {
                    Picasso.with(context).load(profilePictureUrl).fit().centerCrop().transform(new CircleTransform()).into(holder.image);
                }
                holder.name.setText(entry.getParseObject(Constants.IMP_COMP_USER_KEY).fetchIfNeeded().getString(Constants.USER_FULL_NAME_KEY));
                votes = Integer.toString(entry.getInt(Constants.IMP_COMP_VOTES_KEY));
            } else {
                Picasso.with(context).load(entry.getParseObject(Constants.SKYPE_COMP_USER_KEY).fetchIfNeeded().getString(Constants.USER_PROFILE_PICTURE_KEY)).fit().centerCrop().transform(new CircleTransform()).into(holder.image);
                holder.name.setText(entry.getParseObject(Constants.SKYPE_COMP_USER_KEY).fetchIfNeeded().getString(Constants.USER_FULL_NAME_KEY));
                votes = Integer.toString(entry.getInt(Constants.SKYPE_COMP_VOTES_KEY));
            }
            holder.votes.setText(votes);
            String rank = "#" + Integer.toString(position + 4);
            holder.rank.setText(rank);
        } catch (ParseException e) {
            Log.e("debug", "top adapter: " + e.getLocalizedMessage());
        }
    }

    @Override
    public int getItemCount() { return (entries != null ? entries.size() : 0); }

    public class RecyclerViewTopViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView name, rank, votes;

        public RecyclerViewTopViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.logo);
            this.name = itemView.findViewById(R.id.name);
            this.rank = itemView.findViewById(R.id.rank);
            this.votes = itemView.findViewById(R.id.votes);
        }
    }
}
