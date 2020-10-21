package icn.icmyas.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import icn.icmyas.Adapters.IMPVotingRecyclerAdapter;
import icn.icmyas.Adapters.RecyclerViewFModelsAdapter;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.Utils;
import icn.icmyas.Models.IMPModel;
import icn.icmyas.R;
import icn.icmyas.VideoActivity;

public class IMPVotingFragment extends Fragment {

    private ArrayList<String> votedOnArray;
    private RecyclerView featuredRecycler, votingRecycler;
    private final String TAG = "IMPVotingFragment";
    private Utils utils;
    private TextView noMoreEntrants;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imp_voting, container, false);
        initViews(view);
        return view;
    }

    private void initViews(final View view) {
        utils = new Utils(getActivity());
        noMoreEntrants = view.findViewById(R.id.no_more_entrants);
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                view.findViewById(R.id.progress_bar).setVisibility(View.GONE);
            }
        };
        Handler h1 = new Handler();
        h1.postDelayed(r1, 2000);

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                getVotedOnArray();
                initFeaturedRecycler(view);
                initVotingRecycler(view);
                initLeftRightButtons(view);
            }
        };
        Handler h2 = new Handler();
        h2.postDelayed(r2, 500);
    }

    private void initLeftRightButtons(final View view) {
        TextView left = view.findViewById(R.id.btn_left);
        TextView right = view.findViewById(R.id.btn_right);
        left.setOnClickListener(listener);
        right.setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    slideRecycler(false);
                    break;
                case R.id.btn_right:
                    slideRecycler(true);
                    break;
            }
        }
    };

    private int recyclerPos = 0;
    private void slideRecycler(boolean slideRight) {
        if (recyclerPos > (votingAdapter.getItemCount() - 1)) {
            recyclerPos = votingAdapter.getItemCount() - 1;
        }
        boolean canSlide = slideRight ? recyclerPos < (votingAdapter.getItemCount() - 1) : recyclerPos > 0;
        if (canSlide) {
            recyclerPos = slideRight ? recyclerPos + 1 : recyclerPos - 1;
            votingRecycler.scrollToPosition(recyclerPos);
        } else {
            utils.makeText("You have reached the end of the list.", Toast.LENGTH_SHORT);
        }
    }

    private void initVotingRecycler(final View view) {
        votingRecycler = view.findViewById(R.id.recycler_voting);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        votingRecycler.setLayoutManager(layoutManager);
        populateVotingRecycler();
    }

    private IMPVotingRecyclerAdapter votingAdapter;
    private void populateVotingRecycler() {
        final ArrayList<IMPModel> entrants = new ArrayList<>();
        // create the query to pull all entrants that are not the current user
        ParseQuery<ParseObject> getEntrants = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
        getEntrants.whereEqualTo(Constants.IMP_COMP_HAS_ENTERED_KEY, true);
        getEntrants.whereNotEqualTo(Constants.IMP_COMP_USER_KEY, ParseUser.getCurrentUser());
        getEntrants.setLimit(999);
        getEntrants.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (ParseObject row : objects) {
                        // check that the user has not already voted for each entrant
                        if (!votedOnArray.contains(row.getObjectId())) {
                            ParseObject user = row.getParseObject(Constants.IMP_COMP_USER_KEY);
                            // create the query to pull their IMP details (photos and video)
                            ParseQuery<ParseObject> getDetails = ParseQuery.getQuery(Constants.IMP_DETAILS_CLASS_KEY);
                            getDetails.whereEqualTo(Constants.IMP_DETAILS_USER_KEY, user);
                            try {
                                ParseObject details = getDetails.getFirst();
                                entrants.add(new IMPModel(user, details));
                            } catch (ParseException e1) {
                                entrants.add(new IMPModel(user, null));
                                Log.e(TAG, "failed to get IMP details: " + e1.getLocalizedMessage());
                            }
                        }
                    }
                    // randomises order of entrants that are displayed
                    Collections.shuffle(entrants);
                    Log.e(TAG, "# IMP entrants: " + entrants.size());
                    votingAdapter = new IMPVotingRecyclerAdapter(getContext(), entrants, new IMPVotingRecyclerAdapter.AdapterListener() {
                        @Override
                        public void viewVideoOnClick(int pos) {
                            String videoUrl = votingAdapter.getItemAt(pos).getDetails().getString(Constants.IMP_DETAILS_VIDEO_LINK_KEY);
                            Intent intent = new Intent(getActivity(), VideoActivity.class);
                            intent.putExtra("VIDEO_URL", videoUrl);
                            getActivity().startActivity(intent);
                        }

                        @Override
                        public void heartOnClick(int pos) {
                            // heart is clicked, upvote the model
                            ParseObject user = votingAdapter.getItemAt(pos).getUser();
                            upVote(user, pos);
                        }
                    });
                    votingRecycler.setAdapter(votingAdapter);
                    if (votingAdapter.getItemCount() == 0) {
                        noMoreEntrants.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e(TAG, "failed to get current IMP entrants: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private void initFeaturedRecycler(View view) {
        featuredRecycler = view.findViewById(R.id.featured_imp_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        featuredRecycler.setLayoutManager(layoutManager);
        populateFeaturedRecycler();
    }

    private void getVotedOnArray() {
        ParseQuery<ParseObject> getUserRow = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
        getUserRow.whereEqualTo(Constants.IMP_COMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject userRow = getUserRow.getFirst();
            votedOnArray = new ArrayList<>();
            Date dateToRefresh = userRow.getDate(Constants.IMP_COMP_DATE_KEY);
            if (dateToRefresh == null || dateToRefresh.before(Calendar.getInstance().getTime())) {
                // update the time at which the votedOnArray is refreshed
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 1);
                dateToRefresh = calendar.getTime();
                userRow.put(Constants.IMP_COMP_VOTED_ON_KEY, new JSONArray());
                userRow.put(Constants.IMP_COMP_DATE_KEY, dateToRefresh);
                userRow.save();
            } else {
                JSONArray votedOnJSON = userRow.getJSONArray(Constants.IMP_COMP_VOTED_ON_KEY);
                for (int i = 0; i < votedOnJSON.length(); i++) {
                    try {
                        votedOnArray.add(votedOnJSON.getString(i));
                    } catch (JSONException e) {
                        Log.e(TAG, "failed to get votedOn array: " + e.getLocalizedMessage());
                    }
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "failed to get user row: " + e.getLocalizedMessage());
        }
    }

    private RecyclerViewFModelsAdapter featuredAdapter;
    private void populateFeaturedRecycler() {
        featuredAdapter = new RecyclerViewFModelsAdapter(getContext(), getFeaturedModels(), votedOnArray, true);
        featuredAdapter.setOnItemClickListener(new RecyclerViewFModelsAdapter.onTickItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position, ParseObject featuredModel, boolean isLongClicked) {
                try {
                    ParseObject user = featuredModel.getParseObject(Constants.IMP_COMP_USER_KEY).fetchIfNeeded();
                    ParseQuery<ParseObject> getIMPObjId = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
                    getIMPObjId.whereEqualTo(Constants.IMP_COMP_USER_KEY, user);
                    if (!votedOnArray.contains(getIMPObjId.getFirst().getObjectId())) {
                        upVote(user, -1);
                    }
                } catch (ParseException e) {
                    Log.e(TAG, "failed to fetch user ID of featuredModel: " + e.getLocalizedMessage());
                }
            }
        });
        featuredRecycler.setAdapter(featuredAdapter);
        featuredAdapter.notifyDataSetChanged();
    }

    private void upVote(final ParseObject entrant, final int pos) {
        if (votedOnArray.contains(entrant.getObjectId())) {
            return;
        }
        // get IMPComp row for selected user
        ParseQuery<ParseObject> getEntry = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.IMP_COMP_USER_KEY, entrant);
        getEntry.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject entry, ParseException e) {
                try {
                    // add 1 to votes
                    entry.put(Constants.IMP_COMP_VOTES_KEY, entry.getInt(Constants.IMP_COMP_VOTES_KEY) + 1);
                    entry.save();
                    // get IMPComp row for current user
                    ParseQuery<ParseObject> getCurrentUserEntry = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
                    getCurrentUserEntry.whereEqualTo(Constants.IMP_COMP_USER_KEY, ParseUser.getCurrentUser());
                    getCurrentUserEntry.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject user, ParseException e) {
                            try {
                                // add the voted-on entrant to the user's votedOn array
                                user.add(Constants.IMP_COMP_VOTED_ON_KEY, entry.getObjectId());
                                user.save();
                                votedOnArray.add(entry.getObjectId());
                                // refresh the featured adapter
                                featuredAdapter.notifyDataSetChanged();
                                // remove the voted-on entrant from the voting adapter
                                if (pos == -1) {
                                    removeFromVotingAdaper(-1, entrant.getObjectId());
                                } else {
                                    removeFromVotingAdaper(pos, null);
                                }
                            } catch (ParseException e1) {
                                Log.e(TAG, "failed to add user to votedOn: " + e.getLocalizedMessage());
                            }
                        }
                    });
                } catch (ParseException e1) {
                    Log.e(TAG, "failed to upvote: " + e1.getLocalizedMessage());
                }
            }
        });
    }

    private void removeFromVotingAdaper(int pos, String userId) {
        if (pos != -1) {
            votingAdapter.removeAt(pos);
        } else {
            votingAdapter.remove(userId);
        }
        if (votingAdapter.getItemCount() == 0) {
            noMoreEntrants.setVisibility(View.VISIBLE);
        }
    }

    private List<ParseObject> getFeaturedModels() {
        ParseQuery<ParseObject> getFModels = ParseQuery.getQuery(Constants.IMP_FEATURED_CLASS_KEY);
        getFModels.whereNotEqualTo(Constants.IMP_FEATURED_USER_KEY, ParseUser.getCurrentUser());
        try {
            List<ParseObject> fModels = getFModels.find();
            for (ParseObject model : fModels) {
                if (featuredStatusHasExpired(model)) {
                    fModels.remove(model);
                    model.deleteInBackground();
                }
            }
            return fModels;
        } catch (ParseException e) {
            Log.e(TAG, "get featured query failed: " + e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    private boolean featuredStatusHasExpired(ParseObject model) {
        Date expiryDate = model.getDate(Constants.IMP_FEATURED_DATE_KEY);
        return expiryDate.before(Calendar.getInstance().getTime());
    }
}
