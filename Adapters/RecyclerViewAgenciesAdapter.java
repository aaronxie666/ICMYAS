package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Models.Agency;
import icn.icmyas.R;

public class RecyclerViewAgenciesAdapter extends RecyclerView.Adapter<RecyclerViewAgenciesAdapter.AgenciesViewHolder> {

    private ArrayList<Agency> agencies;
    private Context context;
    private AgencyClickListener listener;

    public RecyclerViewAgenciesAdapter(Context context, ArrayList<Agency> agencies) {
        this.context = context;
        this.agencies = agencies;
    }

    public RecyclerViewAgenciesAdapter.AgenciesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) inflater.inflate(R.layout.item_row_agencies, parent, false);
        return new AgenciesViewHolder(mainGroup);
    }

    public void onBindViewHolder(RecyclerViewAgenciesAdapter.AgenciesViewHolder holder, int position) {
        Agency agency = agencies.get(position);
        Picasso.with(context).load(agency.getLogoUrl()).fit().centerCrop().into(holder.agencyImage);
    }

    public void setOnItemClickListener(RecyclerViewAgenciesAdapter.AgencyClickListener itemClickListener) {
        this.listener = itemClickListener;
    }

    public interface AgencyClickListener {
        void onItemClickListener(View view, int position, Agency agency);
    }

    @Override
    public int getItemCount() {
        return (agencies != null) ? agencies.size() : 0;
    }

    public class AgenciesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView agencyImage;

        public AgenciesViewHolder(View itemView) {
            super(itemView);
            this.agencyImage = itemView.findViewById(R.id.agency_img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                listener.onItemClickListener(view, position, agencies.get(position));
            }
        }
    }
}
