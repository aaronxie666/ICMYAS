package icn.icmyas.Fragments;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import icn.icmyas.Adapters.RecyclerViewFModelsAdapter;
import icn.icmyas.Adapters.RecyclerViewTopAdapter;
import icn.icmyas.Adapters.RecyclerViewVotingAdapter;
import icn.icmyas.MainActivity;
import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.SashidoHelper;
import icn.icmyas.Misc.Utils;
import icn.icmyas.R;
import icn.icmyas.Widgets.CustomDosisTextView;
import icn.icmyas.Widgets.CustomRobotoCondensedTextView;

public class ModelGuideFragment extends Fragment {

    RecyclerView votingRecyclerView, topRecyclerView, fModelsRecyclerView;
    RecyclerViewVotingAdapter vAdapter;
    RecyclerViewTopAdapter tAdapter;
    RecyclerViewFModelsAdapter fAdapter;
    ViewAnimator va_entrants, va_tabs;
    ImageView tick, cross;
    Utils utils;
    boolean hasEntered = false;
    ArrayList<String> votedOnArray;
    ShareButton shareButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_model_guide, container, false);
        utils = new Utils(getActivity());
        initToolbar();
        checkIfExistsInSkypeCompTable();
        getVotedOnArray();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        initFeaturedRecyclerView(view);
        initViewsAbout(view);
        initViewsTop(view);
        initViewsVotes(view);
    }

    private void initFeaturedRecyclerView(View view) {
        fModelsRecyclerView = view.findViewById(R.id.featured_models_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        fModelsRecyclerView.setLayoutManager(mLayoutManager);
        populateFeaturedRecyclerView(view);
    }

    private void populateFeaturedRecyclerView(View view) {
        fAdapter = new RecyclerViewFModelsAdapter(getContext(), getFeaturedModels(), votedOnArray, false);
        fAdapter.setOnItemClickListener(new RecyclerViewFModelsAdapter.onTickItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position, ParseObject featuredModel, boolean isLongClicked) {
                try {
                    ParseObject user = featuredModel.getParseObject(Constants.FEATURED_MODELS_USER_KEY).fetchIfNeeded();
                    ParseQuery<ParseObject> getSkypeCompObjId = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
                    getSkypeCompObjId.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, user);
                    if (!votedOnArray.contains(getSkypeCompObjId.getFirst().getObjectId())) {
                        upVote(user, true);
                    }
                } catch (ParseException e) {
                    Log.e("debug", "failed to fetch user ID of featuredModel: " + e.getLocalizedMessage());
                }
            }
        });
        fModelsRecyclerView.setAdapter(fAdapter);
        fAdapter.notifyDataSetChanged();
    }

    private List<ParseObject> getFeaturedModels() {
        ParseQuery<ParseObject> getFModels = ParseQuery.getQuery(Constants.FEATURED_MODELS_CLASS_KEY);
        try {
            List<ParseObject> FModels = getFModels.find();
            for (ParseObject model : FModels) {
                if (featuredStatusHasExpired(model)) {
                    FModels.remove(model);
                    model.deleteInBackground();
                }
            }
            return FModels;
        } catch (ParseException e) {
            Log.e("debug", "get featured query failed: " + e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    private boolean featuredStatusHasExpired(ParseObject model) {
        Date expiryDate = model.getDate(Constants.FEATURED_MODELS_DATE_KEY);
        Log.e("test", "expiry date: " + expiryDate.toString() + ", current date: " + Calendar.getInstance().getTime().toString());
        return expiryDate.before(Calendar.getInstance().getTime());
    }

    private void initViewsAbout(View view) {
        ImageView enterCompButton = view.findViewById(R.id.btn_enter_skype_comp);
        ImageView shareFbButton = view.findViewById(R.id.btn_share_fb);
        CustomRobotoCondensedTextView termsConditionsButton = view.findViewById(R.id.btn_terms_conditions);
        enterCompButton.setOnClickListener(customListener);
        shareFbButton.setOnClickListener(customListener);
        //add by chang
        // Finding the facebook share button
        shareButton = (ShareButton)view.findViewById(R.id.fb_share_button);
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
        //add by chang
        termsConditionsButton.setOnClickListener(customListener);
    }

    private void checkIfExistsInSkypeCompTable() {
        Log.e("checkComp", "enters");
        ParseQuery<ParseObject> getEntry = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject user = getEntry.getFirst();
            hasEntered = (user.getBoolean(Constants.SKYPE_COMP_HAS_ENTERED_KEY));
        } catch (ParseException e) {
            Log.e("checkComp", "saves");
            ParseObject newEntry = new ParseObject(Constants.SKYPE_COMP_CLASS_KEY);
            newEntry.put(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
            newEntry.put(Constants.SKYPE_COMP_VOTES_KEY, 0);
            newEntry.put(Constants.SKYPE_COMP_HAS_ENTERED_KEY, false);
            newEntry.put(Constants.SKYPE_COMP_HAS_SHARED_KEY, false);
            try {
                newEntry.save();
                JSONArray initVotedOnArray = new JSONArray();
                initVotedOnArray.put(newEntry.getObjectId());
                newEntry.put(Constants.SKYPE_COMP_VOTED_ON_KEY, initVotedOnArray);
                newEntry.save();
            } catch (ParseException e1) {
                Log.e("checkComp", "failed to save: " + e1.getLocalizedMessage());
            }
            hasEntered = false;
        }
    }

    private void initToolbar() {
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
    }

    boolean aboutSelected = false, topSelected = false, votesSelected = false;

    private void initViewsVotes(View view) {
        va_tabs = view.findViewById(R.id.va_tabs);
        va_tabs.setDisplayedChild(0);

        va_entrants = view.findViewById(R.id.va_entrants);
        va_entrants.setDisplayedChild(0);

        final TextView tv_about = view.findViewById(R.id.tv_about);
        final TextView tv_top = view.findViewById(R.id.tv_top);
        final TextView tv_votes = view.findViewById(R.id.tv_votes);

        aboutSelected = true;
        tv_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!aboutSelected) {
                    va_tabs.setDisplayedChild(0);
                    tv_about.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_about.setBackgroundResource(R.drawable.rounded_left_selected);
                    tv_top.setTextColor(Color.WHITE);
                    tv_top.setBackgroundResource(R.drawable.middle_tab);
                    tv_votes.setTextColor(Color.WHITE);
                    tv_votes.setBackgroundResource(R.drawable.rounded_right);
                    aboutSelected = true;
                    topSelected = false;
                    votesSelected = false;
                }
            }
        });
        tv_top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!topSelected) {
                    va_tabs.setDisplayedChild(1);
                    tv_top.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_top.setBackgroundResource(R.drawable.middle_tab_selected);
                    tv_about.setTextColor(Color.WHITE);
                    tv_about.setBackgroundResource(R.drawable.rounded_left);
                    tv_votes.setTextColor(Color.WHITE);
                    tv_votes.setBackgroundResource(R.drawable.rounded_right);
                    aboutSelected = false;
                    topSelected = true;
                    votesSelected = false;
                }
            }
        });
        tv_votes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!votesSelected) {
                    va_tabs.setDisplayedChild(2);
                    tv_votes.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_votes.setBackgroundResource(R.drawable.rounded_right_selected);
                    tv_top.setTextColor(Color.WHITE);
                    tv_top.setBackgroundResource(R.drawable.middle_tab);
                    tv_about.setTextColor(Color.WHITE);
                    tv_about.setBackgroundResource(R.drawable.rounded_left);
                    aboutSelected = false;
                    topSelected = false;
                    votesSelected = true;
                }
            }
        });

        tick = view.findViewById(R.id.tick);
        cross = view.findViewById(R.id.cross);
        tick.setOnClickListener(customListener);
        cross.setOnClickListener(customListener);

        initVotingRecyclerView(view);
    }

    private void initVotingRecyclerView(View view) {
        votingRecyclerView = view.findViewById(R.id.voting_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        votingRecyclerView.setLayoutManager(mLayoutManager);
        populateVotingRecyclerView(view);
    }

    private void getVotedOnArray() {
        ParseQuery<ParseObject> getUserRow = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getUserRow.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject userRow = getUserRow.getFirst();
            votedOnArray = new ArrayList<>();
            JSONArray votedOnJSON = userRow.getJSONArray(Constants.SKYPE_COMP_VOTED_ON_KEY);
            for (int i = 0; i < votedOnJSON.length(); i++) {
                try {
                    votedOnArray.add(votedOnJSON.getString(i));
                } catch (JSONException e) {
                    Log.e("debug", "failed to get votedOn array: " + e.getLocalizedMessage());
                }
            }
        } catch (ParseException e) {
            Log.e("debug", "failed to get user row: " + e.getLocalizedMessage());
        }
    }

    private void populateVotingRecyclerView(View view) {
        final ArrayList<ParseObject> entrants = new ArrayList<>();
        ParseQuery<ParseObject> getEntrants = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getEntrants.whereEqualTo(Constants.SKYPE_COMP_HAS_ENTERED_KEY, true);
        // getEntrants.whereNotEqualTo(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
        getEntrants.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject row : objects) {
                        if (!votedOnArray.contains(row.getObjectId())) {
                            entrants.add(row.getParseObject(Constants.SKYPE_COMP_USER_KEY));
                        }
                    }
                    // randomises order of entrants that are displayed
                    Collections.shuffle(entrants);
                    vAdapter = new RecyclerViewVotingAdapter(getContext(), entrants);
                    votingRecyclerView.setAdapter(vAdapter);
                    vAdapter.notifyDataSetChanged();
                    if (vAdapter.getItemCount() == 0) {
                        va_entrants.setDisplayedChild(1);
                        disableButtons();
                    }
                } else {
                    Log.e("debug", "query failed: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private void disableButtons() {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        tick.setColorFilter(cf);
        tick.setEnabled(false);
        cross.setColorFilter(cf);
        cross.setEnabled(false);
    }

    private void upVote(final ParseObject user, final boolean fromFeatured) {
        // get SkypeComp row for user whose votes will be increased
        ParseQuery<ParseObject> getEntry = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, user);
        try {
            final ParseObject entry = getEntry.getFirst();
            // increment this entrant's votes by one
            entry.put(Constants.SKYPE_COMP_VOTES_KEY, entry.getInt(Constants.SKYPE_COMP_VOTES_KEY) + 1);
            entry.save();
            // get SkypeComp row for current user
            ParseQuery<ParseObject> getCurrentUserEntry = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
            getCurrentUserEntry.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
            final ParseObject currentUserEntry = getCurrentUserEntry.getFirst();
            // add entrant to current user's votedOn array
            currentUserEntry.add(Constants.SKYPE_COMP_VOTED_ON_KEY, entry.getObjectId());
            currentUserEntry.save();
            votedOnArray.add(entry.getObjectId());
            SashidoHelper.giveStars(10, false);
            if (fromFeatured) {
                vAdapter.remove(user.getObjectId());
                if (vAdapter.getItemCount() == 0) {
                    va_entrants.setDisplayedChild(1);
                    disableButtons();
                }
                // grey out the user's tick
                fAdapter.notifyDataSetChanged();
            } else {
                // check if user is a featured user, and grey out their tick if so
                fAdapter.notifyDataSetChanged();
            }
            setTopImagesAndNames(getView());
        } catch (ParseException e) {
            Log.e("debug", "failed to upvote: " + e.getLocalizedMessage());
        }
    }

    private void downVote(final ParseObject user) {
        // get SkypeComp row for user who will be added to votedOnArray
        ParseQuery<ParseObject> getEntry = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, user);
        try {
            final ParseObject entry = getEntry.getFirst();
            // get SkypeComp row for current user
            ParseQuery<ParseObject> getCurrentUserEntry = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
            getCurrentUserEntry.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
            final ParseObject currentUserEntry = getCurrentUserEntry.getFirst();
            // add entrant to current user's votedOn array
            currentUserEntry.add(Constants.SKYPE_COMP_VOTED_ON_KEY, entry.getObjectId());
            currentUserEntry.save();
            votedOnArray.add(entry.getObjectId());
            SashidoHelper.giveStars(10, false);
            fAdapter.notifyDataSetChanged();
        } catch (ParseException e) {
            Log.e("debug", "failed to downvote: " + e.getLocalizedMessage());
        }
    }
    
    private View.OnClickListener customListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.tick:
                    upVote(vAdapter.getItemAt(0), false);
                    vAdapter.removeAt(0);
                    if (vAdapter.getItemCount() == 0) {
                        va_entrants.setDisplayedChild(1);
                        disableButtons();
                    }
                    break;
                case R.id.cross:
                    downVote(vAdapter.getItemAt(0));
                    vAdapter.removeAt(0);
                    if (vAdapter.getItemCount() == 0) {
                        va_entrants.setDisplayedChild(1);
                        disableButtons();
                    }
                    break;
                case R.id.btn_enter_skype_comp:
                    if (!hasEntered) {
                        if (userProfileIsComplete()) {
                            enterSkypeComp();
                            utils.makeText("Competition entered!", Toast.LENGTH_SHORT);
                        } else {
                            utils.makeText("Your profile must be 100% complete to enter!", Toast.LENGTH_SHORT);
                        }
                    } else {
                        utils.makeText("You're already entered into the competition!", Toast.LENGTH_SHORT);
                    }
                    break;
                case R.id.btn_share_fb:
                    if (utils.showAppInviteDialog(getContext(), ModelGuideFragment.this)) {
                        shareButton.performClick();
                        updateHasSharedCompetition();
                    }
                    break;
                case R.id.btn_terms_conditions:
                    Utils.openFragment(TermsConditionsFragment.class.getSimpleName(), null, getFragmentManager(), null, null, null);
                    break;
            }
        }
    };

    private void updateHasSharedCompetition() {
        ParseQuery getUserEntry = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getUserEntry.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject entry = getUserEntry.getFirst();
            if (!entry.getBoolean(Constants.SKYPE_COMP_HAS_SHARED_KEY)) {
                entry.put(Constants.SKYPE_COMP_HAS_SHARED_KEY, true);
                entry.saveInBackground();
                SashidoHelper.giveStars(50, true);
                utils.makeText("You've been awarded 50 gold stars for sharing!", Toast.LENGTH_SHORT);
            }
        } catch (ParseException e) {
            Log.e("debug", "failed to get hasShared key: " + e.getLocalizedMessage());
        }
    }

    private boolean userProfileIsComplete() {
        return ParseUser.getCurrentUser().getBoolean(Constants.USER_PROFILE_COMPLETED_KEY);
    }

    private void enterSkypeComp() {
        ParseQuery<ParseObject> getUserEntry = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getUserEntry.whereEqualTo(Constants.SKYPE_COMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject userEntry = getUserEntry.getFirst();
            userEntry.put(Constants.SKYPE_COMP_HAS_ENTERED_KEY, true);
            userEntry.save();
            SashidoHelper.giveStars(20, true);
        } catch (ParseException e) {
            Log.e("skypeComp", "failed to change hasEntered: " + e.getLocalizedMessage());
        }
    }

    private ArrayList<String> names, images, votes;
    private ImageView bronzeImage, silverImage, goldImage;
    private CustomDosisTextView bronzeName, silverName, goldName;
    private CustomDosisTextView bronzeVotes, silverVotes, goldVotes;

    private void initViewsTop(View view) {
        bronzeImage = view.findViewById(R.id.img_bronze);
        silverImage = view.findViewById(R.id.img_silver);
        goldImage = view.findViewById(R.id.img_gold);
        bronzeName = view.findViewById(R.id.name_bronze);
        silverName = view.findViewById(R.id.name_silver);
        goldName = view.findViewById(R.id.name_gold);
        bronzeVotes = view.findViewById(R.id.votes_bronze);
        silverVotes = view.findViewById(R.id.votes_silver);
        goldVotes = view.findViewById(R.id.votes_gold);
        setTopImagesAndNames(view);
    }

    private void initTopRecyclerView(View view, List<ParseObject> orderedEntries) {
        topRecyclerView = view.findViewById(R.id.top_recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        topRecyclerView.setLayoutManager(mLayoutManager);
        populateTopRecyclerView(orderedEntries);
    }

    private void populateTopRecyclerView(List<ParseObject> orderedEntries) {
        tAdapter = new RecyclerViewTopAdapter(getContext(), orderedEntries, false);
        topRecyclerView.setAdapter(tAdapter);
        tAdapter.notifyDataSetChanged();
    }

    private void setTopImagesAndNames(final View view) {
        names = new ArrayList<>();
        images = new ArrayList<>();
        votes = new ArrayList<>();
        ParseQuery<ParseObject> getTopUsers = ParseQuery.getQuery(Constants.SKYPE_COMP_CLASS_KEY);
        getTopUsers.whereEqualTo(Constants.SKYPE_COMP_HAS_ENTERED_KEY, true);
        getTopUsers.addDescendingOrder(Constants.SKYPE_COMP_VOTES_KEY);
        getTopUsers.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // will have to ensure that at least 3 users are in the database on launch
                    for (int i = 0; i < 3; i++) {
                        try {
                            names.add(objects.get(0).getParseObject(Constants.SKYPE_COMP_USER_KEY).fetchIfNeeded().getString(Constants.USER_FULL_NAME_KEY));
                            images.add(objects.get(0).getParseObject(Constants.SKYPE_COMP_USER_KEY).fetchIfNeeded().getString(Constants.USER_PROFILE_PICTURE_KEY));
                            votes.add(Integer.toString(objects.get(0).getInt(Constants.SKYPE_COMP_VOTES_KEY)));
                            objects.remove(0);
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                    }
                    Picasso.with(getContext()).load(images.get(2)).transform(new CircleTransform()).into(bronzeImage);
                    Picasso.with(getContext()).load(images.get(1)).transform(new CircleTransform()).into(silverImage);
                    Picasso.with(getContext()).load(images.get(0)).transform(new CircleTransform()).into(goldImage);
                    bronzeName.setText(names.get(2));
                    silverName.setText(names.get(1));
                    goldName.setText(names.get(0));
                    bronzeVotes.setText(votes.get(2));
                    silverVotes.setText(votes.get(1));
                    goldVotes.setText(votes.get(0));
                    // use query result to populate recyclerview
                    initTopRecyclerView(view, objects);
                } else {
                    Log.e("debug", "get top query failed: " + e.getLocalizedMessage());
                }
            }
        });
    }
}
