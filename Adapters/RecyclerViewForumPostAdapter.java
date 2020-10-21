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

public class RecyclerViewForumPostAdapter extends RecyclerView.Adapter<RecyclerViewForumPostAdapter.RecyclerViewForumPostViewHolder> {

    private Context context;
    private ArrayList<ForumPost> forumPosts;
    private onPostItemClickListener mItemClickListener;

    public RecyclerViewForumPostAdapter(Context context, ArrayList<ForumPost> forumPosts) {
        this.context = context;
        this.forumPosts = forumPosts;
    }

    @Override
    public RecyclerViewForumPostAdapter.RecyclerViewForumPostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_forum_post, parent, false);
        return new RecyclerViewForumPostViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerViewForumPostAdapter.RecyclerViewForumPostViewHolder holder, int position) {
        ForumPost post = forumPosts.get(position);
        if (post.getProfilePictureUrl().equals(Constants.NO_PICTURE)) {
            Picasso.with(context).load(R.drawable.no_profile).transform(new CircleTransform()).into(holder.profilePicture);
        } else {
            Picasso.with(context).load(post.getProfilePictureUrl()).transform(new CircleTransform()).into(holder.profilePicture);
        }
        holder.title.setText(post.getTitle());
        holder.username.setText(post.getUsername() + " -");
        holder.dateAndTime.setText(post.getDateAndTime());
        holder.totalReplies.setText(String.valueOf(post.getTotalReplies()));
    }

    public void setOnItemClickListener(onPostItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onPostItemClickListener {
        void onItemClickListener(View view, int position, ForumPost post, boolean isLongClicked);
    }

    public void insertItem(ArrayList<ForumPost> posts, int position) {
        this.forumPosts = posts;
        notifyItemInserted(position);
    }

    public void removeItem(ArrayList<ForumPost> posts, int position) {
        this.forumPosts = posts;
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return (forumPosts != null ? forumPosts.size() : 0);
    }

    public class RecyclerViewForumPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private ImageView profilePicture, repliesImage;
        private TextView title, username, dateAndTime, totalReplies;

        public RecyclerViewForumPostViewHolder(View itemView) {
            super(itemView);
            this.profilePicture = itemView.findViewById(R.id.profile_picture);
            this.title = itemView.findViewById(R.id.post_title);
            this.username = itemView.findViewById(R.id.user_name);
            this.dateAndTime = itemView.findViewById(R.id.post_datetime);
            this.totalReplies = itemView.findViewById(R.id.total_replies);
            repliesImage = itemView.findViewById(R.id.replies_image);
            repliesImage.setOnClickListener(this);
            totalReplies.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickListener(view, getAdapterPosition(), forumPosts.get(getAdapterPosition()), false);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (forumPosts.get(getAdapterPosition()).getUserId().equals(ParseUser.getCurrentUser().getObjectId())) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickListener(view, getAdapterPosition(), forumPosts.get(getAdapterPosition()), true);
                }
            }
            return true;
        }
    }
}