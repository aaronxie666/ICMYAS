package icn.icmyas.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import icn.icmyas.Adapters.RecyclerViewAdapterLatestVideos;
import icn.icmyas.Adapters.RecyclerViewJudgesAdapter;
import icn.icmyas.Adapters.RecyclerViewPicksAdapter;
import icn.icmyas.MainActivity;
import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.SashidoHelper;
import icn.icmyas.Misc.SharedPreferencesManager;
import icn.icmyas.Misc.Utils;
import icn.icmyas.ModelWorkshopActivity;
import icn.icmyas.Models.EpisodeOrEntries;
import icn.icmyas.Models.Judge;
import icn.icmyas.Models.LatestVideos;
import icn.icmyas.Models.Model;
import icn.icmyas.R;
import icn.icmyas.VideoActivity;
import icn.icmyas.WebViewActivity;
import icn.icmyas.Widgets.CustomRobotoCondensedTextView;

import static android.view.View.GONE;
import static android.widget.Toast.LENGTH_LONG;
import static icn.icmyas.Misc.Constants.EMPTY;
import static icn.icmyas.Misc.Constants.JUDGES_CLASS_KEY;
import static icn.icmyas.Misc.Constants.JUDGES_DESCRIPTION_KEY;
import static icn.icmyas.Misc.Constants.JUDGES_IMAGE_KEY;
import static icn.icmyas.Misc.Constants.JUDGES_NAME_KEY;
import static icn.icmyas.Misc.Constants.JUDGES_VOTED_FOR_KEY;
import static icn.icmyas.Misc.Constants.PICKS_CLASS_KEY;
import static icn.icmyas.Misc.Constants.PICKS_MODELS_KEY;
import static icn.icmyas.Misc.Constants.USER_CLASS_KEY;
import static icn.icmyas.Misc.Constants.USER_HAS_SHARED_KEY;
import static icn.icmyas.Misc.Constants.USER_PROFILE_PICTURE_KEY;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.Fragments
 * Project Name: ICMYAS
 */

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.getSimpleName();
    private RecyclerView recyclerView;
    private ArrayList<EpisodeOrEntries> episodesList;
    private ArrayList<EpisodeOrEntries> entriesList;
    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    private FloatingActionButton fab;
    private Utils utils;
    private static boolean viewsLoaded = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("test", "HomeFragment onCreate()");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        utils = new Utils(getActivity());
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initViews(view);
        return view;
    }

    private void checkForHourlyBonus() {
        if (ParseUser.getCurrentUser().getDate(Constants.USER_HOURLY_POINTS_KEY) == null) {
            fab.show();
            return;
        }
        Date expiryDate = ParseUser.getCurrentUser().getDate(Constants.USER_HOURLY_POINTS_KEY);
        if (expiryDate.before(Calendar.getInstance().getTime())) {
            fab.show();
        }
    }

//    TextView judgeName, judgeDesc;
//    ImageView judgeImg, pickImg;
//    ViewAnimator animator;

    private void initViews(View rootView) {
        initEpisodeImage(rootView);
        initLatestVideos(rootView);
        CustomRobotoCondensedTextView enterSkypeButton = rootView.findViewById(R.id.enter_skype_button);
//        add by chang start
        CustomRobotoCondensedTextView enterPaulFisherWeb = rootView.findViewById(R.id.enter_PaulFisher_website_button);
        enterPaulFisherWeb.setOnClickListener(customListener);

        TextView WeeklyCompetitionDate = rootView.findViewById(R.id.Weekly_competition_date);
        WeeklyCompetitionDate.setText(getCompetitionTime());

        // Finding the facebook share button
        final ShareButton shareButton = (ShareButton)rootView.findViewById(R.id.fb_share_button);
// Sharing the content to facebook
        ShareLinkContent content = new ShareLinkContent.Builder()
                // Setting the title that will be shared
                .setContentTitle("Want to be a Super Model?")
                // Setting the description that will be shared
                .setContentDescription("How can I become a model? Could I make it as a model? For which section of the industry am I best suited? Download the ICMYAS to find out!")
                // Setting the URL that will be shared
                .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=icn.icmyas&hl=en_GB"))
                // Setting the image that will be shared
                .setImageUrl(Uri.parse("android.resource://icn.ICMYAS/drawable/logo"))
                .build();
        shareButton.setShareContent(content);

//        add by chang end
        enterSkypeButton.setOnClickListener(customListener);


        CustomRobotoCondensedTextView impButton = rootView.findViewById(R.id.imp_button);
        impButton.setOnClickListener(customListener);
        ImageView shareFbButton = rootView.findViewById(R.id.btn_share_fb);
        //add by chang start
        shareFbButton.setOnClickListener(new View.OnClickListener() {
            // The code in this method will be executed when the Share  on Facebook custom button is clicked on
            @Override
            public void onClick(View view) {
                shareButton.performClick();
                updateHasSharedApp();

            }
        });
        //add by chang end
//        shareFbButton.setOnClickListener(customListener);
        CustomRobotoCondensedTextView workshopButton = rootView.findViewById(R.id.workshop_button);
        workshopButton.setOnClickListener(customListener);

        initJudges(rootView);
        initPicks(rootView);
//        judgeName = rootView.findViewById(R.id.judge_name);
//        judgeDesc = rootView.findViewById(R.id.judge_desc);
//        judgeImg = rootView.findViewById(R.id.judge_image);
//        animator = rootView.findViewById(R.id.view_animator);
        if (fab == null) {
            initHourlyFab(rootView);
            checkForHourlyBonus();
        }
    }

    private  String getCompetitionTime(){
        String NextTime = "dd/MM/yyyy";
        ParseQuery<ParseObject> query = ParseQuery.getQuery("CompetitionTime");
        try {
            NextTime = query.getFirst().getString("nextTime");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return NextTime;
    }


    private void initHourlyFab(View view) {
        fab = (FloatingActionButton) view.findViewById(R.id.hourly_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int silverCoins = ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY);
                // award 100 silver stars
                ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, silverCoins + 100);
                // update hourlyPoints date
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.HOUR_OF_DAY, 1);
                Date newDate = calendar.getTime();
                ParseUser.getCurrentUser().put(Constants.USER_HOURLY_POINTS_KEY, newDate);
                ParseUser.getCurrentUser().saveInBackground();
                fab.hide();
                utils.makeText("You received an hourly bonus of 100 silver stars!", Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (fab == null) {
                return;
            }
            if (fab.getVisibility() == GONE) {
                checkForHourlyBonus();
            }
        }
    }

    private void initLatestVideos(View rootView) {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        populateRecyclerView();
    }

    private String videoLink;
    private void initEpisodeImage(View view) {
        episodesList = new ArrayList<>();
        final boolean[] activeEpisodeExists = {false};
        final LinearLayout episodeContainer = view.findViewById(R.id.episode_container);
        final TextView episodeTitleTV = view.findViewById(R.id.episode_title);
        final ImageView episodeThumbnail = view.findViewById(R.id.episode_thumbnail);
//        final TextView modelTransformationTitle = view.findViewById(R.id.best_transformation_name);
//        final ImageView modelTransformationImage = view.findViewById(R.id.best_transformation_image);
        final ImageView rightOrWrongVideoImage = view.findViewById(R.id.rightOrWrongVideoImage);
//        final TextView viewAllTransformationsButton = view.findViewById(R.id.view_model_button);
        final TextView dislikeWrongOrRightTV = view.findViewById(R.id.dislike_votes);
        final FrameLayout dislikeContainer = view.findViewById(R.id.dislike_container);
        final FrameLayout likeContainer = view.findViewById(R.id.like_container);
        final TextView likeWrongOrRightTv = view.findViewById(R.id.like_votes);
        final ProgressBar progressRightOrWrong = view.findViewById(R.id.right_or_wrong_progress);


        /*Date activeDate = ParseUser.getCurrentUser().getDate(USER_LAST_ACTIVE);
        if (activeDate != null) {
            boolean moreThanDay = Math.abs(activeDate.getTime() - new Date().getTime()) > MILLIS_PER_DAY;
            if (moreThanDay) {
                // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(USER_SILVER_COINS_KEY) + 25);
                // ParseUser.getCurrentUser().saveInBackground();
            } else {
                ParseUser.getCurrentUser().put(Constants.USER_LAST_ACTIVE, new Date());
                ParseUser.getCurrentUser().saveInBackground();
            }
        } else {
            ParseUser.getCurrentUser().put(Constants.USER_LAST_ACTIVE, new Date());
            ParseUser.getCurrentUser().saveInBackground();
        }*/

        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.EPISODES_CLASS_KEY);
        query.orderByAscending(Constants.EPISODES_EPISODE_NUMBER_KEY);
        try {
            List<ParseObject> objects = query.find();
            Log.e("isactive", Integer.toString(objects.size()));
            for (ParseObject j : objects) {
                // pull details of episode
                String episodeTitle = getString(R.string.episode) + " " + j.getInt(Constants.EPISODES_EPISODE_NUMBER_KEY) + " " + getString(R.string.highlights);
                int episodeNumber = j.getInt(Constants.EPISODES_EPISODE_NUMBER_KEY);
                boolean isActive = j.getBoolean(Constants.EPISODES_IS_ACTIVE_KEY);
                boolean isAired = j.getBoolean(Constants.EPISODES_IS_AIRED_KEY);
                String airDate = getAirDateString(j.getString(Constants.EPISODES_AIR_DATE_KEY));
                String locationTitle = j.getString(Constants.EPISODES_LOCATION_KEY);
                ParseObject bestModelTransformation = j.getParseObject(Constants.EPISODES_BEST_TRANSFORMATION_MODEL_KEY);
                String thumbnailURL = j.getString(Constants.EPISODES_THUMBNAIL_KEY);
                String squareThumbnailImage = j.getString(Constants.EPISODES_SQUARE_THUMBNAIL_KEY);
                try {
                    ArrayList<Model> modelsList = getModelsList(j.getJSONArray(Constants.EPISODES_MODELS_IN_EPISODE_ARRAY_KEY), bestModelTransformation);
                    if (isActive) {
                        Log.e("isactive", "isactive");
                        final ParseObject rightOrWrongObject = j.getParseObject(Constants.EPISODES_RIGHT_OR_WRONG_KEY);
                        dislikeWrongOrRightTV.setText(String.valueOf(rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY)));
                        likeWrongOrRightTv.setText(String.valueOf(rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY)));
                        setProgress(progressRightOrWrong, rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY), rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY));
                        Picasso.with(getContext()).load(rightOrWrongObject.fetchIfNeeded().getString(Constants.RIGHT_OR_WRONG_THUMBNAIL_KEY)).fit().centerCrop().into(rightOrWrongVideoImage);
                        rightOrWrongVideoImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent myIntent = new Intent(getActivity(), VideoActivity.class);
                                try {
                                    myIntent.putExtra("VIDEO_URL", rightOrWrongObject.fetchIfNeeded().getString(Constants.RIGHT_OR_WRONG_VIDEO_LINK_KEY));
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                }
                                getActivity().startActivity(myIntent);
                            }
                        });

                        likeContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES).equals(EMPTY)) {
                                    Log.e("debugHome", "likes is empty");
                                    JSONArray likesArray = new JSONArray();
                                    try {
                                        // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) + 50);
                                        // ParseUser.getCurrentUser().saveEventually();
                                        SashidoHelper.giveStars(250, false);
                                        likesArray.put(0, rightOrWrongObject.fetchIfNeeded().getObjectId());
                                        SharedPreferencesManager.setString(getActivity(), Constants.SHARED_PREFS_LIKES, likesArray.toString());
                                        Log.e("debugHome", "likes total " + rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY) + 1);
                                        rightOrWrongObject.put(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY, rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY) + 1);
                                        Log.e("debugHome", "likes total " + rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY) + 1);
                                        rightOrWrongObject.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    try {
                                                        likeWrongOrRightTv.setText(String.valueOf(rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY)));
                                                        if (SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES).contains(rightOrWrongObject.fetchIfNeeded().getObjectId())) {
                                                            JSONArray dislikesArray = new JSONArray(SharedPreferencesManager.getString(getContext(), Constants.SHARED_PREFS_DISLIKES));
                                                            for (int i = 0; i < dislikesArray.length(); i++) {
                                                                if (dislikesArray.get(i).equals(rightOrWrongObject.fetchIfNeeded().getObjectId())) {
                                                                    dislikesArray.remove(i);
                                                                    String disString = dislikesArray.toString().replaceAll(".*\": null(,)?\\r\\n", "");
                                                                    SharedPreferencesManager.setString(getActivity(), Constants.SHARED_PREFS_DISLIKES, disString);
                                                                    final int dislikeVotes = rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY);
                                                                    rightOrWrongObject.put(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY, dislikeVotes - 1);
                                                                    rightOrWrongObject.saveInBackground(new SaveCallback() {
                                                                        @Override
                                                                        public void done(ParseException e) {
                                                                            if (e == null) {
                                                                                // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) - 50);
                                                                                // ParseUser.getCurrentUser().saveEventually();
                                                                                SashidoHelper.giveStars(-250, false);
                                                                                dislikeWrongOrRightTV.setText(String.valueOf(dislikeVotes - 1));
                                                                                try {
                                                                                    setProgress(progressRightOrWrong, rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY), rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY));
                                                                                } catch (ParseException e1) {
                                                                                    e1.printStackTrace();
                                                                                }
                                                                                Log.e("debugHome", "LikesArray " + SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES));
                                                                                Log.e("debugHome", "DislikesArray " + SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES));
                                                                            } else {
                                                                                Log.e("debugHome", "Failed" + e.getLocalizedMessage());
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }
                                                    } catch (JSONException | ParseException e1) {
                                                        Log.e("debugHome", e1.getLocalizedMessage());
                                                    }
                                                } else {
                                                    Log.e("debugHome", "failed" + e.getLocalizedMessage());
                                                }
                                            }
                                        });
                                    } catch (JSONException | ParseException e1) {
                                        Log.e("debugHome", e1.getLocalizedMessage());
                                    }
                                } else {
                                    Log.e("debugHome", "likes isn't empty");
                                    try {
                                        JSONArray likesArray = new JSONArray(SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES));
                                        if (!SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES).contains(rightOrWrongObject.getObjectId())) {
                                            // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) + 50);
                                            // ParseUser.getCurrentUser().saveEventually();
                                            SashidoHelper.giveStars(250, false);
                                            likesArray.put(likesArray.length(), rightOrWrongObject.fetchIfNeeded().getObjectId());
                                            SharedPreferencesManager.setString(getActivity(), Constants.SHARED_PREFS_LIKES, likesArray.toString());
                                            rightOrWrongObject.put(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY, rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY) + 1);
                                            rightOrWrongObject.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        try {
                                                            likeWrongOrRightTv.setText(String.valueOf(rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY)));
                                                            if (SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES).contains(rightOrWrongObject.fetchIfNeeded().getObjectId())) {
                                                                JSONArray dislikesArray = new JSONArray(SharedPreferencesManager.getString(getContext(), Constants.SHARED_PREFS_DISLIKES));
                                                                for (int i = 0; i < dislikesArray.length(); i++) {
                                                                    if (dislikesArray.get(i).equals(rightOrWrongObject.fetchIfNeeded().getObjectId())) {
                                                                        dislikesArray.remove(i);
                                                                        String disString = dislikesArray.toString().replaceAll(".*\": null(,)?\\r\\n", "");
                                                                        SharedPreferencesManager.setString(getActivity(), Constants.SHARED_PREFS_DISLIKES, disString);
                                                                        final int dislikeVotes = rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY);
                                                                        rightOrWrongObject.put(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY, dislikeVotes - 1);
                                                                        rightOrWrongObject.saveInBackground(new SaveCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {
                                                                                if (e == null) {
                                                                                    // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) - 50);
                                                                                    // ParseUser.getCurrentUser().saveEventually();
                                                                                    SashidoHelper.giveStars(-250, false);
                                                                                    dislikeWrongOrRightTV.setText(String.valueOf(dislikeVotes - 1));
                                                                                    try {
                                                                                        setProgress(progressRightOrWrong, rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY), rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY));
                                                                                        // SashidoHelper.giveStars(250, false);
                                                                                    } catch (ParseException e1) {
                                                                                        e1.printStackTrace();
                                                                                    }
                                                                                    Log.e("debugHome", "LikesArray " + SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES));
                                                                                    Log.e("debugHome", "DislikesArray " + SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES));
                                                                                } else {
                                                                                    Log.e("debugHome", "Failed" + e.getLocalizedMessage());
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }
                                                        } catch (JSONException | ParseException e1) {
                                                            Log.e("debugHome", e1.getLocalizedMessage());
                                                        }
                                                    } else {
                                                        Log.e("debugHome", "failed" + e.getLocalizedMessage());
                                                    }
                                                }
                                            });
                                        } else {
                                            ((MainActivity) getActivity()).utils.makeText("You have already agreed with Paul on this.", LENGTH_LONG);
                                        }
                                    } catch (JSONException | ParseException e1) {
                                        Log.e("debugHome", e1.getLocalizedMessage());
                                    }
                                }
                            }
                        });
                        dislikeContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES).equals(EMPTY)) {
                                    Log.e("debugHome", "dislikes is empty");
                                    JSONArray dislikesArray = new JSONArray();
                                    try {
                                        // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) + 50);
                                        // ParseUser.getCurrentUser().saveEventually();
                                        SashidoHelper.giveStars(250, false);
                                        dislikesArray.put(0, rightOrWrongObject.fetchIfNeeded().getObjectId());
                                        SharedPreferencesManager.setString(getActivity(), Constants.SHARED_PREFS_DISLIKES, dislikesArray.toString());
                                        Log.e("debugHome", "dislikes total " + rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY) + 1);
                                        rightOrWrongObject.put(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY, rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY) + 1);
                                        Log.e("debugHome", "dislikes total " + rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY) + 1);
                                        rightOrWrongObject.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    try {
                                                        dislikeWrongOrRightTV.setText(String.valueOf(rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY)));
                                                        if (SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES).contains(rightOrWrongObject.fetchIfNeeded().getObjectId())) {
                                                            JSONArray likesArray = new JSONArray(SharedPreferencesManager.getString(getContext(), Constants.SHARED_PREFS_LIKES));
                                                            for (int i = 0; i < likesArray.length(); i++) {
                                                                if (likesArray.get(i).equals(rightOrWrongObject.fetchIfNeeded().getObjectId())) {
                                                                    // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) - 50);
                                                                    // ParseUser.getCurrentUser().saveEventually();
                                                                    SashidoHelper.giveStars(-250, false);
                                                                    likesArray.remove(i);
                                                                    String likesString = likesArray.toString().replaceAll(".*\": null(,)?\\r\\n", "");
                                                                    SharedPreferencesManager.setString(getActivity(), Constants.SHARED_PREFS_LIKES, likesString);
                                                                    final int likeVotes = rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY);
                                                                    rightOrWrongObject.put(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY, likeVotes - 1);
                                                                    rightOrWrongObject.saveInBackground(new SaveCallback() {
                                                                        @Override
                                                                        public void done(ParseException e) {
                                                                            if (e == null) {
                                                                                likeWrongOrRightTv.setText(String.valueOf(likeVotes - 1));
                                                                                try {
                                                                                    setProgress(progressRightOrWrong, rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY), rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY));
                                                                                    // SashidoHelper.giveStars(250, false);
                                                                                } catch (ParseException e1) {
                                                                                    e1.printStackTrace();
                                                                                }
                                                                                Log.e("debugHome", "LikesArray " + SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES));
                                                                                Log.e("debugHome", "DislikesArray " + SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES));
                                                                            } else {
                                                                                Log.e("debugHome", "Failed" + e.getLocalizedMessage());
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }
                                                    } catch (JSONException | ParseException e1) {
                                                        Log.e("debugHome", e1.getLocalizedMessage());
                                                    }
                                                } else {
                                                    Log.e("debugHome", "failed" + e.getLocalizedMessage());
                                                }
                                            }
                                        });
                                    } catch (JSONException | ParseException e1) {
                                        Log.e("debugHome", e1.getLocalizedMessage());
                                    }
                                } else {
                                    Log.e("debugHome", "dislikes isn't empty");
                                    try {
                                        JSONArray dislikesArray = new JSONArray(SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES));
                                        if (!SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES).contains(rightOrWrongObject.getObjectId())) {
                                            // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) + 50);
                                            // ParseUser.getCurrentUser().saveEventually();
                                            SashidoHelper.giveStars(250, false);
                                            dislikesArray.put(dislikesArray.length(), rightOrWrongObject.fetchIfNeeded().getObjectId());
                                            SharedPreferencesManager.setString(getActivity(), Constants.SHARED_PREFS_DISLIKES, dislikesArray.toString());
                                            rightOrWrongObject.put(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY, rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY) + 1);
                                            rightOrWrongObject.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        try {
                                                            dislikeWrongOrRightTV.setText(String.valueOf(rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY)));
                                                            if (SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES).contains(rightOrWrongObject.fetchIfNeeded().getObjectId())) {
                                                                JSONArray likesArray = new JSONArray(SharedPreferencesManager.getString(getContext(), Constants.SHARED_PREFS_LIKES));
                                                                for (int i = 0; i < likesArray.length(); i++) {
                                                                    if (likesArray.get(i).equals(rightOrWrongObject.fetchIfNeeded().getObjectId())) {
                                                                        // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) - 50);
                                                                        // ParseUser.getCurrentUser().saveEventually();
                                                                        SashidoHelper.giveStars(-250, false);
                                                                        likesArray.remove(i);
                                                                        String likesString = likesArray.toString().replaceAll(".*\": null(,)?\\r\\n", "");
                                                                        SharedPreferencesManager.setString(getActivity(), Constants.SHARED_PREFS_LIKES, likesString);
                                                                        final int likeVotes = rightOrWrongObject.fetchIfNeeded().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY);
                                                                        rightOrWrongObject.put(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY, likeVotes - 1);
                                                                        rightOrWrongObject.saveInBackground(new SaveCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {
                                                                                if (e == null) {
                                                                                    likeWrongOrRightTv.setText(String.valueOf(likeVotes - 1));
                                                                                    try {
                                                                                        setProgress(progressRightOrWrong, rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_AGREE_VOTES_KEY), rightOrWrongObject.fetch().getInt(Constants.RIGHT_OR_WRONG_DISAGREE_VOTES_KEY));
                                                                                        // SashidoHelper.giveStars(250, false);
                                                                                    } catch (ParseException e1) {
                                                                                        Log.e("debugHome", "failed " + e1.getLocalizedMessage());
                                                                                    }
                                                                                    Log.e("debugHome", "LikesArray " + SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_LIKES));
                                                                                    Log.e("debugHome", "DislikesArray " + SharedPreferencesManager.getString(getActivity(), Constants.SHARED_PREFS_DISLIKES));
                                                                                } else {
                                                                                    Log.e("debugHome", "Failed" + e.getLocalizedMessage());
                                                                                }
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }
                                                        } catch (JSONException | ParseException e1) {
                                                            Log.e("debugHome", e1.getLocalizedMessage());
                                                        }
                                                    } else {
                                                        Log.e("debugHome", "failed" + e.getLocalizedMessage());
                                                    }
                                                }
                                            });
                                        } else {
                                            ((MainActivity) getActivity()).utils.makeText("You have already disagreed with Paul on this.", LENGTH_LONG);
                                        }
                                    } catch (JSONException | ParseException e1) {
                                        Log.e("debugHome", e1.getLocalizedMessage());
                                    }
                                }
                            }
                        });

                        //removed to add 'season 1' image

//                        activeEpisodeExists[0] = true;
//                        episodeTitleTV.setText(episodeTitle);
//                        Picasso.with(getActivity()).load(thumbnailURL).fit().centerCrop().into(episodeThumbnail);
//                        videoLink = j.getString(Constants.EPISODES_THUMBNAIL_KEY);
//                        episodeThumbnail.setOnClickListener(customListener);
//                        modelTransformationTitle.setText(bestModelTransformation.fetchIfNeeded().getString(Constants.MODELS_NAME_KEY));
//                        Picasso.with(getActivity()).load(bestModelTransformation.fetchIfNeeded().getString(Constants.MODELS_FEATURED_IMAGE_KEY)).fit().centerCrop().into(modelTransformationImage);
                        //changed by chang
//                        viewAllTransformationsButton.setOnClickListener(customListener);
                    }
                    EpisodeOrEntries episode = new EpisodeOrEntries();
                    episode.setActive(isActive);
                    episode.setAirDate(airDate);
                    episode.setIsAiredOrViewed(isAired);
                    episode.setEpisodeNumber(episodeNumber);
                    episode.setLocationOrName(locationTitle);
                    episode.setModelsInEpisode(modelsList);
                    episode.setThumbnailUrl(squareThumbnailImage);
                    episode.setTransformedModel(bestModelTransformation);
                    episode.setEntry(false);
                    episodesList.add(episode);
                } catch (ParseException | JSONException e1) {
                    Log.e(TAG, e1.getLocalizedMessage());
                }
            }
            if (activeEpisodeExists[0]) {
                episodeContainer.setVisibility(View.VISIBLE);
            } else {
                episodeContainer.setVisibility(GONE);
            }
        } catch (ParseException e) {
            Log.e("debug", "failed to get episode list:" + e.getLocalizedMessage());
        }
    }

    private void setProgress(ProgressBar progressRightOrWrong, int agreeVotes, int disagreeVotes) {
        progressRightOrWrong.setMax(100);
        double total = disagreeVotes + agreeVotes;
        double progressValue = (disagreeVotes / total) * 100.0f;
        progressRightOrWrong.setProgress((int) progressValue);
    }

    private String getAirDateString(String dateString) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        try {
            Date date = df.parse(dateString);
            dateString = df.format(date);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        Log.e(TAG, dateString);
        return dateString;
    }

    private ArrayList<Model> getModelsList(JSONArray jsonArray, ParseObject bestModelTransformation) throws JSONException, ParseException {
        ArrayList<Model> modelsList = new ArrayList<>();
        ArrayList<ParseObject> objectsList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            objectsList.add(ParseObject.createWithoutData(Constants.MODELS_CLASS_KEY, jsonArray.getString(i)));
        }
        ParseObject.fetchAll(objectsList);

        for (ParseObject j : objectsList) {
            Model model = new Model();
            model.setObjectId(j.getObjectId());
            model.setAfterImage(j.getString(Constants.MODELS_AFTER_IMAGE_KEY));
            model.setBeforeImage(j.getString(Constants.MODELS_BEFORE_IMAGE_KEY));
            model.setName(j.getString(Constants.MODELS_NAME_KEY));
            model.setMyStoryMyCause(j.getParseObject(Constants.MODELS_MY_STORY_MY_CAUSE_POINTER_KEY));
            model.setPledgesTotal(j.getInt(Constants.MODELS_PLEDGE_TOTAL_KEY));
            model.setTransformationTotal(j.getInt(Constants.MODELS_TRANSFORMATION_TOTAL_KEY));
            if (j.getObjectId().equals(bestModelTransformation.fetch().getObjectId())) {
                model.setBestTransformed(true);
            } else {
                model.setBestTransformed(false);
            }
            modelsList.add(model);
        }

        return modelsList;
    }

    private void populateRecyclerView() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(Constants.LATEST_VIDEOS_CLASS_KEY);
        query.orderByDescending("createdAt");
        try {
            List<ParseObject> objects = query.find();
            ArrayList<LatestVideos> latestVideoList = new ArrayList<>();
            for (ParseObject j : objects) {
                ParseFile image = (ParseFile) j.get(Constants.LATEST_VIDEOS_THUMBNAIL_KEY);
                String video_link = (String) j.get(Constants.LATEST_VIDEOS_VIDEO_LINK_KEY);
                latestVideoList.add(new LatestVideos(j.getString(Constants.LATEST_VIDEOS_VIDEO_TITLE_KEY), image.getUrl(), video_link));
            }
            RecyclerViewAdapterLatestVideos adapter = new RecyclerViewAdapterLatestVideos(getActivity(), latestVideoList);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (ParseException e) {
            Log.e("debug", "failed to get latest videos: " + e.getLocalizedMessage());
        }
    }

    private View.OnClickListener customListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.episode_thumbnail:
                    Intent myIntent = new Intent(getActivity(), VideoActivity.class);
                    myIntent.putExtra("VIDEO_URL", videoLink);
                    getActivity().startActivity(myIntent);
                    break;
                    //changed by chang
//                case R.id.view_model_button:
//                    Utils.openFragment(EpisodeFragment.class.getSimpleName(), episodesList, getFragmentManager(), Constants.EPISODE_LIST, EMPTY, null);
//                    break;

//                    add by chang start
                case R.id.enter_PaulFisher_website_button:
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    startActivity(intent);
                    break;

//                add by chang end
                case R.id.enter_skype_button:
                    Utils.openFragment(ModelGuideFragment.class.getSimpleName(), null, getFragmentManager(), null, null, null);
                    break;
//                case R.id.btn_share_fb:
//                    if (utils.showAppInviteDialog(getContext(), HomeFragment.this)) {
//                        updateHasSharedApp();
//                    }
//                    break;
                case R.id.imp_button:
                    Utils.openFragment(IMPHomeFragment.class.getSimpleName(), null, getFragmentManager(), null, null, null);
                    break;
                case R.id.workshop_button:
                    Intent workshopIntent = new Intent(getActivity(), ModelWorkshopActivity.class);
                    startActivity(workshopIntent);
                    break;
            }
        }
    };

    private void updateHasSharedApp() {
        if (!ParseUser.getCurrentUser().getBoolean(USER_HAS_SHARED_KEY)) {
            ParseUser.getCurrentUser().put(USER_HAS_SHARED_KEY, true);
            ParseUser.getCurrentUser().saveInBackground();
            SashidoHelper.giveStars(250, false);
            utils.makeText("Thanks! You've been awarded 250 silver stars.", Toast.LENGTH_SHORT);
        } else {
            utils.makeText("Thanks!", Toast.LENGTH_SHORT);
        }
    }

    public static HomeFragment newInstance(int imageNum) {
        final HomeFragment f = new HomeFragment();
        final Bundle args = new Bundle();
        args.putInt("Position", imageNum);
        f.setArguments(args);
        return f;
    }



    //Pauls weekly picks - recycler view



    RecyclerView picksRecycler;

    private void initPicks (View view) {
        picksRecycler = view.findViewById(R.id.picks_recycler);
        RecyclerView.LayoutManager aLayoutManager = new GridLayoutManager(getActivity(), 3);
        picksRecycler.setLayoutManager(aLayoutManager);
        new PopulatePicks().execute();
    }

    private ArrayList<String> picks;

    private class PopulatePicks extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            picks = new ArrayList<>();
            ParseQuery<ParseObject> getPicks = ParseQuery.getQuery(PICKS_CLASS_KEY);

            getPicks.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        JSONArray json = object.getJSONArray(PICKS_MODELS_KEY);
                        ArrayList<String> models = new ArrayList<>();
                        for (int i = 0; i < json.length(); i++) {
                            try {
                                models.add(json.getString(i));
                            } catch (JSONException e1) {
                                Log.e("HomeFragment ", e.getLocalizedMessage());
                            }
                        }
                        ParseQuery<ParseObject> getUserPics = ParseQuery.getQuery(USER_CLASS_KEY);
                        getUserPics.whereContainedIn("objectId", models);
                        getUserPics.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> objects, ParseException e) {
                                for (ParseObject object : objects) {
                                    picks.add(object.getString(USER_PROFILE_PICTURE_KEY));
                                }
                                RecyclerViewPicksAdapter adapter = new RecyclerViewPicksAdapter(getContext(), picks);
                                picksRecycler.setAdapter(adapter);
                            }
                        });
                    } else {
                        Log.e("HomeFragment ", e.getLocalizedMessage());
                    }
                }
            });
            return null;
        }
    }

    //expert weekly votes section

    RecyclerView judgesRecycler;

    private void initJudges (View view) {
        judgesRecycler = view.findViewById(R.id.judges_recycler);
        RecyclerView.LayoutManager oLayoutManager = new GridLayoutManager(getActivity(), 3);
        judgesRecycler.setLayoutManager(oLayoutManager);
        new PopulateJudges().execute();
    }

    private ArrayList<Judge> judges;

    private class PopulateJudges extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            judges = new ArrayList<>();
            ParseQuery<ParseObject> getJudges = ParseQuery.getQuery(JUDGES_CLASS_KEY);
            getJudges.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        for (ParseObject p : objects) {
                            String name = p.getString(JUDGES_NAME_KEY);
                            String votedFor = p.getString(JUDGES_VOTED_FOR_KEY);
                            String description = p.getString(JUDGES_DESCRIPTION_KEY);
                            String image = p.getString(JUDGES_IMAGE_KEY);
                            judges.add(new Judge(name, votedFor, description, image));
                        }
                        RecyclerViewJudgesAdapter adapter = new RecyclerViewJudgesAdapter(getContext(), judges);
                        adapter.setOnItemClickListener(new RecyclerViewJudgesAdapter.JudgeClickListener() {
                            @Override
                            public void onItemClickListener(View view, int position, Judge judge) {
                                final Dialog dialog = new Dialog(getContext(), R.style.customAlertDialog);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView(R.layout.dialog_judges);
                                TextView judgeName, judgeDesc;
                                ImageView judgeImg, pickImg;
                                judgeName = (TextView) dialog.findViewById(R.id.judge_name);
                                judgeDesc = (TextView) dialog.findViewById(R.id.judge_desc);
                                judgeImg = (ImageView) dialog.findViewById(R.id.judge_image);

                                judgeName.setText(judge.getName());
                                judgeDesc.setText(judge.getDescription());
                                Picasso.with(getContext()).load(judge.getImage()).fit().centerCrop().transform(new CircleTransform()).into(judgeImg);
                                dialog.show();
//                                animator.setDisplayedChild(1);
                            }
                        });
                        judgesRecycler.setAdapter(adapter);
                    } else {
                        Log.e("Home Fragment", "failed to get posts: " + e.getLocalizedMessage());
                    }
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}
