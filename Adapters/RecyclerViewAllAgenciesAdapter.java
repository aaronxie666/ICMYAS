package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import icn.icmyas.Models.Agency;
import icn.icmyas.R;

public class RecyclerViewAllAgenciesAdapter extends RecyclerView.Adapter<RecyclerViewAllAgenciesAdapter.AllAgenciesViewHolder> {

    private List<Agency> agencies;
    private Context context;
    private AgencyClickListener listener;

    public RecyclerViewAllAgenciesAdapter(Context context, List<Agency> agencies) {
        this.context = context;
        this.agencies = agencies;
    }

    public RecyclerViewAllAgenciesAdapter.AllAgenciesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) inflater.inflate(R.layout.item_row_all_agencies, parent, false);
        return new AllAgenciesViewHolder(mainGroup);
    }

    public void onBindViewHolder(RecyclerViewAllAgenciesAdapter.AllAgenciesViewHolder holder, int position) {
        Agency agency = agencies.get(position);
        Picasso.with(context).load(agency.getLogoUrl()).fit().centerCrop().into(holder.image);
        holder.name.setText(agency.getName());
        holder.country.setText(agency.getCountry());
    }

    public void update(List<Agency> list) {
        this.agencies = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(RecyclerViewAllAgenciesAdapter.AgencyClickListener itemClickListener) {
        this.listener = itemClickListener;
    }

    public interface AgencyClickListener {
        void onItemClickListener(View view, int position, Agency agency);
    }

    @Override
    public int getItemCount() {
        return (agencies != null) ? agencies.size() : 0;
    }

    public class AllAgenciesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView image;
        TextView name, country;

        public AllAgenciesViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.logo);
            this.name = itemView.findViewById(R.id.name);
            this.country = itemView.findViewById(R.id.country);
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
