package icn.icmyas.Fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import icn.icmyas.Adapters.RecyclerViewForumPostAdapter;
import icn.icmyas.Adapters.RecyclerViewForumReplyAdapter;
import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.ProfanityFilter;
import icn.icmyas.Misc.Utils;
import icn.icmyas.Models.ForumPost;
import icn.icmyas.R;

import static android.view.View.GONE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static icn.icmyas.Fragments.HomeFragment.MILLIS_PER_DAY;
import static icn.icmyas.Misc.Constants.USER_LAST_ACTIVE;

public class ForumsFragment extends Fragment  {

    private boolean forumSelected;
    private Utils utils;
    private Dialog dialog;
    private RecyclerView postRecyclerView, replyRecyclerView;
    private RecyclerViewForumPostAdapter pAdapter;
    private RecyclerViewForumReplyAdapter rAdapter;
    private ArrayList<ForumPost> forumPosts, forumReplies;
    private ParseObject paulFisher;
    private ViewAnimator forum_va;
    private FloatingActionButton fab;
    private boolean viewingReplies;
    private ParseObject originalPost;
    private ImageView op_profilePicture;
    private TextView op_username, op_dateTime, op_title, op_message;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("test", "ForumsFragment onCreate()");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forums, container, false);
        utils = new Utils(getActivity());
        new AsyncProfanity().execute();
        paulFisher = initPaulFisher();
        initViews(view);
        populateForumPosts();
        Log.e("test", "ForumsFragment onCreateViews()");
        return view;
    }

    private ParseObject initPaulFisher() {
        ParseQuery<ParseObject> getPaulFisher = ParseQuery.getQuery(Constants.USER_CLASS_KEY);
        getPaulFisher.whereEqualTo("objectId", Constants.PAUL_FISHER_ID);
        try {
            return getPaulFisher.getFirst();
        } catch (ParseException e) {
            Log.e("debug", e.getLocalizedMessage());
            return null;
        }
    }

    private void initViews(View view) {
        initPostRecyclerView(view);
        initReplyRecyclerView(view);
        forumSelected = true;
        viewingReplies = false;
        forum_va = view.findViewById(R.id.forum_va);
        Animation inAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left);
        Animation outAnim = AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right);
        // forum_va.setInAnimation(inAnim);
        // forum_va.setOutAnimation(outAnim);

        op_profilePicture = view.findViewById(R.id.op_profile_picture);
        op_username = view.findViewById(R.id.op_user_name);
        op_title = view.findViewById(R.id.op_title);
        op_message = view.findViewById(R.id.op_message);
        op_dateTime = view.findViewById(R.id.op_datetime);
        ImageView op_reportButton = view.findViewById(R.id.op_report_button);

        op_reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReportDialog(null);
            }
        });
        final TextView tv_forum = view.findViewById(R.id.tv_forum);
        final TextView tv_live = view.findViewById(R.id.tv_live);
        fab = view.findViewById(R.id.add_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewingReplies) {
                    showAddReplyDialog();
                } else {
                    showAddPostDialog();
                }
            }
        });

        tv_forum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!forumSelected) {
                    tv_forum.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_forum.setBackgroundResource(R.drawable.rounded_left_selected);
                    tv_live.setTextColor(Color.WHITE);
                    tv_live.setBackgroundResource(R.drawable.rounded_right);
                    forumSelected = true;
                    populateForumPosts();
                }
            }
        });

        tv_live.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (forumSelected) {
                    tv_live.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_live.setBackgroundResource(R.drawable.rounded_right_selected);
                    tv_forum.setTextColor(Color.WHITE);
                    tv_forum.setBackgroundResource(R.drawable.rounded_left);
                    forumSelected = false;
                    populateForumPosts();
                }
            }
        });
    }

    private void showReportDialog(final ParseObject reply) {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_report_post);

        final EditText reportReason = dialog.findViewById(R.id.input_report_reason);
        final TextView cancelButton = dialog.findViewById(R.id.cancel_report_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        final TextView submitReportButton = dialog.findViewById(R.id.submit_report_button);
        submitReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reportReason.getText().toString().length() == 0) {
                    reportReason.setError("Please enter a reason.");
                } else {
                    if (reply == null) {
                        saveNewReport(reportReason.getText().toString(), null, false);
                        dialog.cancel();
                    } else {
                        saveNewReport(reportReason.getText().toString(), reply, true);
                        dialog.cancel();
                    }
                }
            }
        });
        dialog.show();
    }

    private void initPostRecyclerView(View view) {
        postRecyclerView = view.findViewById(R.id.forum_recycler_view);
        postRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager pLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        postRecyclerView.setLayoutManager(pLayoutManager);
    }

    private void initReplyRecyclerView(View view) {
        replyRecyclerView = view.findViewById(R.id.replies_recycler_view);
        replyRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager pLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        replyRecyclerView.setLayoutManager(pLayoutManager);
    }

    private void showAddReplyDialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_reply);
        final EditText replyMessage = dialog.findViewById(R.id.input_reply_message);
        final TextView cancelButton = dialog.findViewById(R.id.cancel_reply_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        final TextView postReplyButton = dialog.findViewById(R.id.post_reply_button);
        postReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (replyMessage.getText().toString().length() == 0) {
                    replyMessage.setError("Please enter a message.");
                } else {
                    boolean isBadWordMessage = ProfanityFilter.filterText(replyMessage.getText().toString());
                    if (isBadWordMessage) {
                        utils.makeText("Please remove all profanity from your message and try again.", LENGTH_LONG);
                    } else {
                        saveNewReply(replyMessage.getText().toString());
                    }
                }
            }
        });
        dialog.show();
    }

    private void showAddPostDialog() {
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_post);
        final EditText postTitle = dialog.findViewById(R.id.input_title);
        final EditText postMessage = dialog.findViewById(R.id.input_message);
        final TextView cancelButton = dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        final TextView postButton = dialog.findViewById(R.id.post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (postTitle.getText().toString().length() == 0) {
                    postTitle.setError("Please enter a title.");
                } else if (postMessage.getText().toString().length() == 0) {
                    postMessage.setError("Please enter a message.");
                } else {
                    boolean isBadWordTitle = ProfanityFilter.filterText(postTitle.getText().toString());
                    boolean isBadWordMessage = ProfanityFilter.filterText(postMessage.getText().toString());
                    if (isBadWordTitle && isBadWordMessage) {
                        utils.makeText("Please remove all profanity from your title and message and try again.", LENGTH_LONG);
                    } else if (isBadWordTitle) {
                        utils.makeText("Please remove all profanity from your title and try again.", LENGTH_LONG);
                    } else if (isBadWordMessage) {
                        utils.makeText("Please remove all profanity from your message and try again.", LENGTH_LONG);
                    } else {
                        saveNewPost(postTitle.getText().toString(), postMessage.getText().toString());
                    }
                }
            }
        });
        dialog.show();
    }

    private void saveNewReport(final String reason, ParseObject reply, boolean reportingReply) {
        final ParseObject newReport = new ParseObject(Constants.REPORTED_POSTS_CLASS_KEY);
        newReport.put(Constants.REPORTED_POSTS_MODERATED_KEY, false);
        newReport.put(Constants.REPORTED_POSTS_REASON_KEY, reason);
        newReport.put(Constants.REPORTED_POSTS_POST_KEY, originalPost);
        newReport.put(Constants.REPORTED_POSTS_USER_KEY, ParseUser.getCurrentUser());
        if (!reportingReply) {
            newReport.put(Constants.REPORTED_POSTS_CONTENT_KEY, op_message.getText().toString());
        } else {
            newReport.put(Constants.REPORTED_POSTS_CONTENT_KEY, reply.getString(Constants.FORUM_POSTS_REPLIES_MESSAGE_KEY));
            newReport.put(Constants.REPORTED_POSTS_REPLY_KEY, reply);
        }
        newReport.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // ParseUser.getCurrentUser().put(USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(USER_SILVER_COINS_KEY) + 25);
                    // ParseUser.getCurrentUser().saveInBackground();
                    utils.makeText("Successfully submitted report. We will review it shortly.", LENGTH_SHORT);
                } else {
                    Log.e("debug", e.getLocalizedMessage());
                    utils.makeText("Unable to submit report at this time. Please try again later.", LENGTH_SHORT);
                }
            }
        });
    }

    private void saveNewReply(final String message) {
        final ParseObject newReply = new ParseObject(Constants.FORUM_POST_REPLIES_CLASS_KEY);
        newReply.put(Constants.FORUM_POSTS_REPLIES_POSTER_KEY, ParseUser.getCurrentUser());
        newReply.put(Constants.FORUM_POSTS_REPLIES_MESSAGE_KEY, message);
        newReply.put(Constants.FORUM_POSTS_REPLIES_ORIGINAL_POST_KEY, originalPost);
        newReply.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) + 5);
                    // ParseUser.getCurrentUser().saveEventually();
                    utils.makeText("Successfully posted reply.", LENGTH_SHORT);
                    replyRecyclerView.setVisibility(View.VISIBLE);
                    if (rAdapter != null) {
                        forumReplies.add(forumReplies.size(), new ForumPost(newReply.getObjectId(), message, ParseUser.getCurrentUser().getString(Constants.USER_PROFILE_PICTURE_KEY), ParseUser.getCurrentUser().getObjectId(), ParseUser.getCurrentUser().getUsername(), Utils.getDateAndTime(new Date())));
                        rAdapter.insertItem(forumReplies, rAdapter.getItemCount());
                    } else {
                        ArrayList<ForumPost> replies = new ArrayList<>();
                        replies.add(new ForumPost(newReply.getObjectId(), message, ParseUser.getCurrentUser().getString(Constants.USER_PROFILE_PICTURE_KEY), ParseUser.getCurrentUser().getObjectId(), ParseUser.getCurrentUser().getUsername(), Utils.getDateAndTime(new Date())));
                        rAdapter = new RecyclerViewForumReplyAdapter(getContext(), replies);
                        replyRecyclerView.setAdapter(rAdapter);
                        rAdapter.notifyDataSetChanged();
                    }
                    updatePostRepliesArray(newReply.getObjectId(), true);
                    populateReplies();
                    dialog.cancel();
                } else {
                    utils.makeText("Unable to post at this time. Please try again later.", LENGTH_SHORT);
                }
            }
        });
    }

    private void updatePostRepliesArray(String replyId, boolean needToAddReply) {
        JSONArray replies = originalPost.getJSONArray(Constants.FORUM_POSTS_REPLIES_KEY);
        if (needToAddReply) {
            replies.put(replyId);
        } else {
            String tempId;
            for (int i = 0; i < replies.length(); i++) {
                try {
                    tempId = replies.get(i).toString();
                    if (tempId.equals(replyId)) {
                        replies.remove(i);
                    }
                } catch (JSONException e) {
                    Log.e("debug", "failed to delete reply");
                    return;
                }
            }
        }
        originalPost.put(Constants.FORUM_POSTS_REPLIES_KEY, replies);
        originalPost.saveInBackground();
    }

    private void saveNewPost(final String title, final String message) {
        final ParseObject newPost = new ParseObject(Constants.FORUM_POSTS_CLASS_KEY);
        newPost.put(Constants.FORUM_POSTS_POSTER_KEY, ParseUser.getCurrentUser());
        newPost.put(Constants.FORUM_POSTS_TITLE_KEY, title);
        newPost.put(Constants.FORUM_POSTS_MESSAGE_KEY, message);
        newPost.put(Constants.FORUM_POSTS_REPLIES_KEY, new JSONArray());
        newPost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) + 5);
                    ParseUser.getCurrentUser().saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Date activeDate = ParseUser.getCurrentUser().getDate(USER_LAST_ACTIVE);
                                if (activeDate != null) {
                                    boolean moreThanDay = Math.abs(activeDate.getTime() - new Date().getTime()) > MILLIS_PER_DAY;
                                    if (moreThanDay) {
                                        // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(USER_SILVER_COINS_KEY) + 25);
                                        // ParseUser.getCurrentUser().saveInBackground();
                                    } else {
                                        ParseUser.getCurrentUser().put(Constants.USER_LAST_FORUM_POST, new Date());
                                        ParseUser.getCurrentUser().saveInBackground();
                                    }
                                } else {
                                    ParseUser.getCurrentUser().put(Constants.USER_LAST_FORUM_POST, new Date());
                                    ParseUser.getCurrentUser().saveInBackground();
                                }
                            } else {
                                Log.e("Failed", "failed" + e.getLocalizedMessage());
                            }
                        }
                    });
                    utils.makeText("Successfully posted to forum.", LENGTH_SHORT);
                    postRecyclerView.setVisibility(View.VISIBLE);
                    if (pAdapter != null) {
                        forumPosts.add(0, new ForumPost(newPost.getObjectId(), title, message, ParseUser.getCurrentUser().getString(Constants.USER_PROFILE_PICTURE_KEY), ParseUser.getCurrentUser().getObjectId(), ParseUser.getCurrentUser().getUsername(), Utils.getDateAndTime(new Date()), 0, new ArrayList<String>()));
                        pAdapter.insertItem(forumPosts, 0);
                    } else {
                        ArrayList<ForumPost> posts = new ArrayList<>();
                        posts.add(new ForumPost(newPost.getObjectId(), title, message, ParseUser.getCurrentUser().getString(Constants.USER_PROFILE_PICTURE_KEY), ParseUser.getCurrentUser().getObjectId(), ParseUser.getCurrentUser().getUsername(), Utils.getDateAndTime(new Date()), 0, new ArrayList<String>()));
                        pAdapter = new RecyclerViewForumPostAdapter(getContext(), posts);
                        postRecyclerView.setAdapter(pAdapter);
                        pAdapter.notifyDataSetChanged();
                    }
                    dialog.cancel();
                } else {
                    utils.makeText("Unable to post at this time. Please try again later.", LENGTH_SHORT);
                }
            }
        });
    }

    private void populateOP(ForumPost post) {
        if (post.getProfilePictureUrl().equals(Constants.NO_PICTURE)) {
            Picasso.with(getContext()).load(R.drawable.no_profile).transform(new CircleTransform()).into(op_profilePicture);
        } else {
            Picasso.with(getContext()).load(post.getProfilePictureUrl()).fit().centerCrop().transform(new CircleTransform()).into(op_profilePicture);
        }
        op_username.setText(post.getUsername() + " -");
        op_title.setText(post.getTitle());
        op_message.setText(post.getMessage());
        op_dateTime.setText(post.getDateAndTime());
    }

    private ParseObject getPost(ForumPost post, boolean isReply) {
        ParseQuery<ParseObject> getOP;
        if (!isReply) {
            getOP = ParseQuery.getQuery(Constants.FORUM_POSTS_CLASS_KEY);
            getOP.whereEqualTo(Constants.FORUM_POSTS_OBJECT_ID_KEY, post.getObjectId());
        } else {
            getOP = ParseQuery.getQuery(Constants.FORUM_POST_REPLIES_CLASS_KEY);
            getOP.whereEqualTo(Constants.FORUM_POSTS_REPLIES_OBJECT_ID_KEY, post.getObjectId());
        }
        ParseObject obj;
        try {
            obj = getOP.getFirst();
            return obj;
        } catch (ParseException e) {
            Log.e("debug", e.getLocalizedMessage());
            utils.makeText("An unexpected error occurred. Please try again.", Toast.LENGTH_SHORT);
            return null;
        }
    }

    // display the original post and fill the RecyclerView with replies
    private void populateReplies() {
        forum_va.setDisplayedChild(1);
        fab.setImageResource(R.drawable.ic_reply);
        viewingReplies = true;
        forumReplies = new ArrayList<>();
        final ParseQuery<ParseObject> getReplies = ParseQuery.getQuery(Constants.FORUM_POST_REPLIES_CLASS_KEY);
        getReplies.whereEqualTo(Constants.FORUM_POSTS_REPLIES_ORIGINAL_POST_KEY, originalPost);
        getReplies.orderByAscending("createdAt");
        getReplies.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        Log.e("debug", "populateReplies objects.size > 0");
                        if (replyRecyclerView.getVisibility() != View.VISIBLE) {
                            replyRecyclerView.setVisibility(View.VISIBLE);
                        }
                        for (ParseObject r : objects) {
                            String message = r.getString(Constants.FORUM_POSTS_REPLIES_MESSAGE_KEY);
                            String opUserId, opProfilePicUrl, opUsername;
                            try {
                                opUserId = r.getParseObject(Constants.FORUM_POSTS_REPLIES_POSTER_KEY).fetchIfNeeded().getObjectId();
                                opProfilePicUrl = r.getParseObject(Constants.FORUM_POSTS_REPLIES_POSTER_KEY).fetchIfNeeded().getString(Constants.USER_PROFILE_PICTURE_KEY);
                                opUsername = r.getParseObject(Constants.FORUM_POSTS_REPLIES_POSTER_KEY).fetchIfNeeded().getString(Constants.USER_USERNAME_KEY);
                            } catch (ParseException e1) {
                                continue;
                            }
                            String dateAndTime = Utils.getDateAndTime(r.getCreatedAt());
                            forumReplies.add(new ForumPost(r.getObjectId(), message, opProfilePicUrl, opUserId, opUsername, dateAndTime));
                        }
                        rAdapter = new RecyclerViewForumReplyAdapter(getContext(), forumReplies);
                        rAdapter.setOnItemClickListener(new RecyclerViewForumReplyAdapter.onReplyItemClickListener() {
                            @Override
                            public void onItemClickListener(View view, final int position, final ForumPost reply, boolean isLongClicked) {
                                if (view.getId() == R.id.report_button) {
                                    // Log.e("debug", "report button clicked");
                                    showReportDialog(getPost(reply, true));
                                    return;
                                }
                                if (isLongClicked) {
                                    Log.e("debug", "position " + Integer.toString(position));
                                    Log.e("debug", "reply ID " + reply.getObjectId());
                                    if (reply.getUserId().equals(ParseUser.getCurrentUser().getObjectId())) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.customAlertDialog);
                                        builder.setTitle("Delete Reply");
                                        builder.setMessage("Are you sure you want to delete this reply?");
                                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ParseQuery<ParseObject> getPostToDelete = ParseQuery.getQuery(Constants.FORUM_POST_REPLIES_CLASS_KEY);
                                                getPostToDelete.whereEqualTo(Constants.FORUM_POSTS_REPLIES_OBJECT_ID_KEY, reply.getObjectId());
                                                getPostToDelete.getFirstInBackground(new GetCallback<ParseObject>() {
                                                    @Override
                                                    public void done(ParseObject object, ParseException e) {
                                                        if (e == null) {
                                                            object.deleteInBackground(new DeleteCallback() {
                                                                // TODO delete from Sashido replies array
                                                                @Override
                                                                public void done(ParseException e) {
                                                                    if (e == null) {
                                                                        // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) - 5);
                                                                        // ParseUser.getCurrentUser().saveEventually();
                                                                        updatePostRepliesArray(reply.getObjectId(), false);
                                                                        forumReplies.remove(position);
                                                                        rAdapter.removeItem(forumReplies, position);
                                                                    } else {
                                                                        Log.e("debug", "query failed " + e.getLocalizedMessage());
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            Log.e("debug", e.getLocalizedMessage());
                                                        }
                                                    }
                                                });
                                            }
                                        })
                                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.cancel();
                                                    }
                                                })
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .show();
                                    }
                                }
                            }
                        });
                        replyRecyclerView.setAdapter(rAdapter);
                        rAdapter.notifyDataSetChanged();
                    } else {
                        replyRecyclerView.setVisibility(GONE);
                        Log.e("debug", "visibility set to gone");
                    }
                } else {
                    Log.e("debug", "query failed");
                }
            }
        });
    }

    // fill the RecyclerView with forum posts
    private void populateForumPosts() {
        forum_va.setDisplayedChild(0);
        fab.setImageResource(R.drawable.ic_btn_newthread);
        viewingReplies = false;
        forumPosts = new ArrayList<>();
        final ParseQuery<ParseObject> getPosts = ParseQuery.getQuery(Constants.FORUM_POSTS_CLASS_KEY);
        if (forumSelected) {
            getPosts.whereNotEqualTo(Constants.FORUM_POSTS_POSTER_KEY, paulFisher);
            Log.e("debug", "forum is selected");
        } else {
            getPosts.whereEqualTo(Constants.FORUM_POSTS_POSTER_KEY, paulFisher);
        }
        getPosts.orderByDescending("createdAt");
        getPosts.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        if (postRecyclerView.getVisibility() != View.VISIBLE) {
                            postRecyclerView.setVisibility(View.VISIBLE);
                        }
                        try {
                            for (ParseObject p : objects) {
                                String title = p.getString(Constants.FORUM_POSTS_TITLE_KEY);
                                String message = p.getString(Constants.FORUM_POSTS_MESSAGE_KEY);
                                String opUserId, opProfilePicUrl, opUsername;
                                try {
                                    opUserId = p.getParseObject(Constants.FORUM_POSTS_POSTER_KEY).fetchIfNeeded().getObjectId();
                                    opProfilePicUrl = p.getParseObject(Constants.FORUM_POSTS_POSTER_KEY).fetchIfNeeded().getString(Constants.USER_PROFILE_PICTURE_KEY);
                                    opUsername = p.getParseObject(Constants.FORUM_POSTS_POSTER_KEY).fetchIfNeeded().getString(Constants.USER_USERNAME_KEY);
                                } catch (ParseException e1) {
                                    continue;
                                }
                                String dateAndTime = Utils.getDateAndTime(p.getCreatedAt());
                                int totalReplies = 0;
                                JSONArray repliesArray = p.getJSONArray(Constants.FORUM_POSTS_REPLIES_KEY);
                                if (repliesArray.length() > 0) {
                                    totalReplies = repliesArray.length();
                                }
                                forumPosts.add(new ForumPost(p.getObjectId(), title, message, opProfilePicUrl, opUserId, opUsername, dateAndTime, totalReplies, Utils.convertJSONtoArrayList(repliesArray)));
                            }
                            pAdapter = new RecyclerViewForumPostAdapter(getContext(), forumPosts);
                            pAdapter.setOnItemClickListener(new RecyclerViewForumPostAdapter.onPostItemClickListener() {
                                @Override
                                public void onItemClickListener(View view, final int position, final ForumPost post, boolean isLongClicked) {
                                    if (isLongClicked) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.customAlertDialog);
                                        builder.setTitle("Delete Post");
                                        if (post.getTotalReplies() > 0) {
                                            builder.setMessage("Are you sure you want to delete this post and all of its replies?");
                                        } else {
                                            builder.setMessage("Are you sure you want to delete this post?");
                                        }
                                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ParseQuery<ParseObject> getPostToDelete = ParseQuery.getQuery(Constants.FORUM_POSTS_CLASS_KEY);
                                                getPostToDelete.whereEqualTo(Constants.FORUM_POSTS_OBJECT_ID_KEY, post.getObjectId());
                                                getPostToDelete.whereEqualTo(Constants.FORUM_POSTS_POSTER_KEY, ParseUser.getCurrentUser());
                                                getPostToDelete.getFirstInBackground(new GetCallback<ParseObject>() {
                                                    @Override
                                                    public void done(ParseObject object, ParseException e) {
                                                        object.deleteInBackground(new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    forumPosts.remove(position);
                                                                    pAdapter.removeItem(forumPosts, position);
                                                                    if (post.getTotalReplies() > 0) {
                                                                        ParseQuery<ParseObject> getRepliesToDelete = ParseQuery.getQuery(Constants.FORUM_POST_REPLIES_CLASS_KEY);
                                                                        getRepliesToDelete.whereContainedIn(Constants.FORUM_POSTS_REPLIES_OBJECT_ID_KEY, post.getRepliesArray());
                                                                        getRepliesToDelete.findInBackground(new FindCallback<ParseObject>() {
                                                                            @Override
                                                                            public void done(List<ParseObject> objects, ParseException e) {
                                                                                if (e == null) {
                                                                                    for (ParseObject r : objects) {
                                                                                        r.deleteInBackground(new DeleteCallback() {
                                                                                            @Override
                                                                                            public void done(ParseException e) {
                                                                                                if (e == null) {
                                                                                                    // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) - 5);
                                                                                                    // ParseUser.getCurrentUser().saveEventually();
                                                                                                } else {
                                                                                                    Log.e("debug", "query failed " + e.getLocalizedMessage());
                                                                                                }
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                } else {
                                                                                    Log.e("debug", "query failed " + e.getLocalizedMessage());
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                } else {
                                                                    Log.e("debug", "query failed " + e.getLocalizedMessage());
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                                    } else {
                                        // TODO handle null return value
                                        originalPost = getPost(post, false);
                                        populateOP(post);
                                        populateReplies();
                                    }
                                }
                            });
                            postRecyclerView.setAdapter(pAdapter);
                            pAdapter.notifyDataSetChanged();
                        } catch (Exception e2) {
                            Log.e("debug", e2.getLocalizedMessage());
                            e2.printStackTrace();
                        }
                    } else {
                        postRecyclerView.setVisibility(GONE);
                        Log.e("debug", "visibility set to gone");
                    }
                } else {
                    Log.e("debug", "query failed");
                }
            }
        });
    }

    public static ForumsFragment newInstance(int imageNum) {
        final ForumsFragment f = new ForumsFragment();
        final Bundle args = new Bundle();
        args.putInt("Position", imageNum);
        f.setArguments(args);
        return f;
    }

    private class AsyncProfanity extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            ProfanityFilter.loadConfigs();
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    if (forum_va.getDisplayedChild() > 0) {
                        forum_va.setDisplayedChild(0);
                        fab.setImageResource(R.drawable.ic_btn_newthread);
                    } else {
                        getActivity().onBackPressed();
                    }
                    return true;
                }
                return false;
            }
        });
    }
}