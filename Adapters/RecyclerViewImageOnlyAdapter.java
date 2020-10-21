package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Models.Model;
import icn.icmyas.R;

/**
 * Author:  Bradley Wilson
 * Date: 24/07/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class RecyclerViewImageOnlyAdapter extends RecyclerView.Adapter<RecyclerViewImageOnlyAdapter.RecyclerViewImageOnlyViewHolder> {

    private ArrayList<Model> imageList;
    private Context context;
    private onItemClickListener mItemClickListener;

    public RecyclerViewImageOnlyAdapter(Context context,
                                           ArrayList<Model> latestVideosList) {
        this.context = context;
        this.imageList = latestVideosList;
    }

    @Override
    public int getItemCount() {
        return (null != imageList ? imageList.size() : 0);
    }

    public void setOnItemClickListener(onItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onItemClickListener {
        void setOnItemClickListener(View view, int position, Model model);
    }

    @Override
    public void onBindViewHolder(RecyclerViewImageOnlyViewHolder holder, int position) {
        final Model model = imageList.get(position);
        Picasso.with(context).load(model.getAfterImage()).into(holder.imageView);

        if (model.isBestTransformed())
            holder.imageView.setBackgroundResource(R.drawable.item_bg);
    }

    @Override
    public RecyclerViewImageOnlyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_image_only, viewGroup, false);
        return new RecyclerViewImageOnlyViewHolder(mainGroup);
    }

    public class RecyclerViewImageOnlyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView imageView;

        public RecyclerViewImageOnlyViewHolder(View itemView) {
            super(itemView);
            this.imageView = itemView.findViewById(R.id.logo);
            this.imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.setOnItemClickListener(view, getAdapterPosition(), imageList.get(getAdapterPosition()));
            }
        }
    }
}
