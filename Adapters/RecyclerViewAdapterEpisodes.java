package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Models.EpisodeOrEntries;
import icn.icmyas.R;

import static icn.icmyas.Misc.Constants.NO_PICTURE;

/**
 * Author:  Bradley Wilson
 * Date: 21/07/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class RecyclerViewAdapterEpisodes extends RecyclerView.Adapter<RecyclerViewAdapterEpisodes.RecyclerViewEpisodesViewHolder> {

    private Context context;
    private ArrayList<EpisodeOrEntries> episodesList;
    private onEpisodeItemClickListener mItemClickListener;

    public RecyclerViewAdapterEpisodes(Context context, ArrayList<EpisodeOrEntries> episodesList) {
        this.context = context;
        this.episodesList = episodesList;
    }

    @Override
    public RecyclerViewEpisodesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater pInflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) pInflater.inflate(R.layout.item_row_episodes, parent, false);
        return new RecyclerViewEpisodesViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerViewEpisodesViewHolder holder, int position) {
        EpisodeOrEntries episode = episodesList.get(position);
        if (episode.isEntry()) {
            if (episode.getThumbnailUrl().equals(NO_PICTURE)) {
                Picasso.with(context).load(R.drawable.no_profile).transform(new CircleTransform()).into(holder.episodeImage);
            } else {
                Picasso.with(context).load(episode.getThumbnailUrl()).fit().centerCrop().transform(new CircleTransform()).into(holder.episodeImage);
            }
            holder.episodeImage.setBackground(null);
            holder.episodeTitle.setText(episode.getLocationOrName());
            holder.viewTransformationButton.setText(context.getString(R.string.view_profile));
            holder.viewTransformationButton.setBackgroundResource(R.drawable.btn_bg);
            if (episode.isAiredOrViewed()) {
                holder.episodeDate.setText(episode.getAirDate() + " (Viewed)");
            } else {
                holder.episodeDate.setText(episode.getAirDate());
            }
        } else {
            Picasso.with(context).load(episode.getThumbnailUrl()).fit().centerCrop().into(holder.episodeImage);
            holder.episodeTitle.setText(context.getString(R.string.episode) + " " + episode.getEpisodeNumber() + " - " + episode.getLocationOrName());
            if (episode.isAiredOrViewed()) {
                holder.viewTransformationButton.setBackgroundResource(R.drawable.btn_bg);
                holder.episodeDate.setText(context.getString(R.string.aired) + " " + episode.getAirDate());
                Picasso.with(context).load(episode.getThumbnailUrl()).fit().centerCrop().into(holder.episodeImage);
            } else {
                holder.viewTransformationButton.setBackgroundResource(R.drawable.btn_bg_silver);
                holder.episodeDate.setText(context.getString(R.string.airs) + " " + episode.getAirDate());
                Picasso.with(context).load(R.drawable.ic_noepisode).into(holder.episodeImage);
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != episodesList ? episodesList.size() : 0);
    }

    public void setOnItemClickListener(onEpisodeItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onEpisodeItemClickListener {
        void onItemClickListener(View view, int position, EpisodeOrEntries episodeOrEntries);
    }

    public class RecyclerViewEpisodesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView episodeImage;
        private TextView episodeTitle, episodeDate, viewTransformationButton;

        public RecyclerViewEpisodesViewHolder(View itemView) {
            super(itemView);
            this.episodeImage = itemView.findViewById(R.id.episode_image);
            this.episodeTitle = itemView.findViewById(R.id.episode_title);
            this.episodeDate = itemView.findViewById(R.id.episode_date);
            this.viewTransformationButton = itemView.findViewById(R.id.view_transformation_button);
            this.viewTransformationButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (!episodesList.get(getAdapterPosition()).isEntry()) {
                if (episodesList.get(getAdapterPosition()).isAiredOrViewed()) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClickListener(view, getAdapterPosition(), episodesList.get(getAdapterPosition()));
                    }
                }
            } else {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickListener(view, getAdapterPosition(), episodesList.get(getAdapterPosition()));
                }
            }
        }
    }
}
