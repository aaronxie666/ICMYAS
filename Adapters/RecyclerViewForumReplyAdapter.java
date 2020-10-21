package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Models.ForumPost;
import icn.icmyas.R;

public class RecyclerViewForumReplyAdapter extends RecyclerView.Adapter<RecyclerViewForumReplyAdapter.RecyclerViewForumReplyViewHolder> {

    private Context context;
    private ArrayList<ForumPost> forumReplies;
    private onReplyItemClickListener mItemClickListener;

    public RecyclerViewForumReplyAdapter(Context context, ArrayList<ForumPost> forumReplies) {
        this.context = context;
        this.forumReplies = forumReplies;
    }

    @Override
    public RecyclerViewForumReplyAdapter.RecyclerViewForumReplyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_forum_reply, parent, false);
        return new RecyclerViewForumReplyViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerViewForumReplyAdapter.RecyclerViewForumReplyViewHolder holder, int position) {
        ForumPost reply = forumReplies.get(position);
        if (reply.getProfilePictureUrl().equals(Constants.NO_PICTURE)) {
            Picasso.with(context).load(R.drawable.no_profile).transform(new CircleTransform()).into(holder.profilePicture);
        } else {
            Picasso.with(context).load(reply.getProfilePictureUrl()).transform(new CircleTransform()).into(holder.profilePicture);
        }
        holder.message.setText(reply.getMessage());
        holder.username.setText(reply.getUsername() + " -");
        holder.dateAndTime.setText(reply.getDateAndTime());
    }

    public void setOnItemClickListener(onReplyItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onReplyItemClickListener {
        void onItemClickListener(View view, int position, ForumPost reply, boolean isLongClicked);
    }

    public void insertItem(ArrayList<ForumPost> replies, int position) {
        this.forumReplies = replies;
        notifyItemInserted(position);
    }

    public void removeItem(ArrayList<ForumPost> replies, int position) {
        this.forumReplies = replies;
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return (forumReplies != null ? forumReplies.size() : 0);
    }

    public class RecyclerViewForumReplyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private ImageView profilePicture, reportButton;
        private TextView message, username, dateAndTime;

        public RecyclerViewForumReplyViewHolder(View itemView) {
            super(itemView);
            this.profilePicture = itemView.findViewById(R.id.reply_profile_picture);
            this.message = itemView.findViewById(R.id.reply_message);
            this.username = itemView.findViewById(R.id.reply_user_name);
            this.dateAndTime = itemView.findViewById(R.id.reply_datetime);
            this.reportButton = itemView.findViewById(R.id.report_button);
            reportButton.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickListener(view, getAdapterPosition(), forumReplies.get(getAdapterPosition()), false);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (forumReplies.get(getAdapterPosition()).getUserId().equals(ParseUser.getCurrentUser().getObjectId())) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickListener(view, getAdapterPosition(), forumReplies.get(getAdapterPosition()), true);
                }
            }
            return true;
        }
    }
}