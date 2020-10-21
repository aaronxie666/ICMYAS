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
import icn.icmyas.Models.Messages;
import icn.icmyas.R;

import static android.graphics.Typeface.BOLD;
import static android.graphics.Typeface.NORMAL;
import static icn.icmyas.Misc.Constants.NO_PICTURE;

/**
 * Author:  Bradley Wilson
 * Date: 22/08/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class RecyclerViewAdapterMessages extends RecyclerView.Adapter<RecyclerViewAdapterMessages.RecyclerViewHolderMessages>{

    private Context context;
    private ArrayList<Messages> messagesList;
    private onMessageItemClickListener mItemClickListener;

    public RecyclerViewAdapterMessages (Context context, ArrayList<Messages> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    @Override
    public RecyclerViewHolderMessages onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_episodes, viewGroup, false);
        return new RecyclerViewHolderMessages(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolderMessages holder, int position) {
        Messages message = messagesList.get(position);
        holder.viewTransformationButton.setVisibility(View.INVISIBLE);
        holder.episodeImage.setBackground(null);
        if (message.getProfileURL().equals(NO_PICTURE)) {
            Picasso.with(context).load(R.drawable.no_profile).transform(new CircleTransform()).into(holder.episodeImage);
        } else {
            Picasso.with(context).load(message.getProfileURL()).fit().centerCrop().transform(new CircleTransform()).into(holder.episodeImage);
        }
        holder.episodeTitle.setText(message.getSenderName());
        holder.episodeDate.setText(message.getMessage());

        if (!message.isRead()) {
            holder.episodeTitle.setTypeface(holder.episodeTitle.getTypeface(), BOLD);
            holder.episodeDate.setTypeface(holder.episodeDate.getTypeface(), BOLD);
        } else{
            holder.episodeTitle.setTypeface(holder.episodeTitle.getTypeface(), NORMAL);
            holder.episodeDate.setTypeface(holder.episodeDate.getTypeface(), NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return (null != messagesList ? messagesList.size() : 0);
    }

    public void setOnItemClickListener(onMessageItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void updateList(ArrayList<Messages> messagesList) {
        this.messagesList = messagesList;
        notifyDataSetChanged();
    }

    public interface onMessageItemClickListener {
        void onItemClickListener(View view, int position, Messages message);
    }

    public class RecyclerViewHolderMessages extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView episodeImage;
        private TextView episodeTitle, episodeDate, viewTransformationButton;

        public RecyclerViewHolderMessages(View itemView) {
            super(itemView);
            this.episodeImage = itemView.findViewById(R.id.episode_image);
            this.episodeTitle = itemView.findViewById(R.id.episode_title);
            this.episodeDate = itemView.findViewById(R.id.episode_date);
            this.viewTransformationButton = itemView.findViewById(R.id.view_transformation_button);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickListener(view, getAdapterPosition(), messagesList.get(getAdapterPosition()));
            }
        }
    }
}
