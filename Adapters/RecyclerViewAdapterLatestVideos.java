package icn.icmyas.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Models.LatestVideos;
import icn.icmyas.R;
import icn.icmyas.VideoActivity;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class RecyclerViewAdapterLatestVideos extends
        RecyclerView.Adapter<RecyclerViewAdapterLatestVideos.RecyclerViewHolderTitleAndImage>{

    // recyclerview adapter
    private ArrayList<LatestVideos> latestVideosList;
    private Context context;

    public RecyclerViewAdapterLatestVideos(Context context,
                                        ArrayList<LatestVideos> latestVideosList) {
        this.context = context;
        this.latestVideosList = latestVideosList;
    }

    @Override
    public int getItemCount() {
        return (null != latestVideosList ? latestVideosList.size() : 0);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolderTitleAndImage holder, int position) {
        final LatestVideos model = latestVideosList.get(position);
        // setting title
        holder.title.setText(model.getTitle());
        Picasso.with(context).load(model.getImage()).into(holder.imageview);
        holder.imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(context, VideoActivity.class);
                myIntent.putExtra("VIDEO_URL", model.getUrl());
                context.startActivity(myIntent);
            }
        });
    }

    @Override
    public RecyclerViewHolderTitleAndImage onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        // This method will inflate the custom layout and return as viewholder
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_latest_news, viewGroup, false);
        RecyclerViewHolderTitleAndImage listHolder = new RecyclerViewHolderTitleAndImage(mainGroup);
        return listHolder;

    }

    public class RecyclerViewHolderTitleAndImage extends RecyclerView.ViewHolder {
        // View holder for gridview recycler view as we used in listview
        public TextView title;
        public ImageView imageview;

        public RecyclerViewHolderTitleAndImage(View view) {
            super(view);
            // Find all views ids
            this.title = (TextView) view
                    .findViewById(R.id.title);
            this.imageview = (ImageView) view
                    .findViewById(R.id.videoImage);
        }
    }
}
