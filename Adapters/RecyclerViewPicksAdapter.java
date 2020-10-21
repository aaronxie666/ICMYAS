package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.R;

public class RecyclerViewPicksAdapter extends RecyclerView.Adapter<RecyclerViewPicksAdapter.PicksViewHolder> {

    private ArrayList<String> picks;
    private Context context;
    private PickClickListener listener;

    public RecyclerViewPicksAdapter(Context context, ArrayList<String> picks) {
        this.context = context;
        this.picks = picks;
    }

    public RecyclerViewPicksAdapter.PicksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) inflater.inflate(R.layout.item_row_picks, parent, false);
        return new PicksViewHolder(mainGroup);
    }

    public void onBindViewHolder(RecyclerViewPicksAdapter.PicksViewHolder holder, int position) {
        String pick = picks.get(position);
        Picasso.with(context).load(pick).fit().centerCrop().transform(new CircleTransform()).into(holder.pickImage);
    }

    //model click listener, should be removed
    public void setOnItemClickListener(RecyclerViewPicksAdapter.PicksViewHolder mItemClickListener) {
        this.listener = (PickClickListener) mItemClickListener;
    }

    public interface PickClickListener {
        void onItemClickListener(View view, int position, String pick);
    }

    @Override
    public int getItemCount() { return (picks != null) ? picks.size() : 0; }

    public class PicksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView pickImage;

        public PicksViewHolder(View itemView) {
            super(itemView);
            this.pickImage = itemView.findViewById(R.id.pick_img);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(listener != null) {
                listener.onItemClickListener(view, getAdapterPosition(), picks.get(getAdapterPosition()));
            }
        }
    }


}
