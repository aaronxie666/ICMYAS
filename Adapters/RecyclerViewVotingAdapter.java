package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Misc.Constants;
import icn.icmyas.R;
import icn.icmyas.Widgets.HSquareImageView;

public class RecyclerViewVotingAdapter extends RecyclerView.Adapter<RecyclerViewVotingAdapter.RecyclerViewVotingViewHolder> {

    private Context context;
    private ArrayList<ParseObject> entrants;

    public RecyclerViewVotingAdapter(Context context, ArrayList<ParseObject> entrants) {
        this.context = context;
        this.entrants = entrants;
    }

    @Override
    public RecyclerViewVotingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_entrants_image, parent, false);
        return new RecyclerViewVotingViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerViewVotingViewHolder holder, int position) {
        ParseObject entrant = entrants.get(position);
        try {
            Picasso.with(context).load(entrant.fetchIfNeeded().getString(Constants.USER_PROFILE_PICTURE_KEY)).fit().centerCrop().into(holder.image);
            holder.name.setText(entrant.fetchIfNeeded().getString(Constants.USER_FULL_NAME_KEY));
        } catch (ParseException e) {
            Log.e("debug", "voting adapter: " + e.getLocalizedMessage());
        }
    }

    public void removeAt(int pos) {
        entrants.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, entrants.size());
    }

    public void remove(String userId) {
        for (int i = 0; i < entrants.size(); i++) {
            if (entrants.get(i).getObjectId().equals(userId)) {
                entrants.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, entrants.size());
                return;
            }
        }
    }

    public ParseObject getItemAt(int pos) {
        return entrants.get(pos);
    }

    @Override
    public int getItemCount() { return (entrants != null ? entrants.size() : 0); }

    public class RecyclerViewVotingViewHolder extends RecyclerView.ViewHolder {

        private HSquareImageView image;
        private TextView name;

        public RecyclerViewVotingViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.logo);
            this.name = itemView.findViewById(R.id.name);
        }
    }
}
