package icn.icmyas.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import icn.icmyas.Adapters.RecyclerViewTopAdapter;
import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.R;

public class IMPRankingFragment extends Fragment {

    final static String TAG = "IMPRankingFragment";
    ArrayList<String> names, images, votes;
    RecyclerView votesRecycler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imp_ranking, container, false);
        Bundle args = getArguments();
        names = args.getStringArrayList("names");
        images = args.getStringArrayList("images");
        votes = args.getStringArrayList("votes");
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        initTopModels(view);
        initVotesRecycler(view);
        initUserRank(view);
    }

    private void initTopModels(View view) {
        ImageView bronzeImage = view.findViewById(R.id.img_bronze);
        ImageView silverImage = view.findViewById(R.id.img_silver);
        ImageView goldImage = view.findViewById(R.id.img_gold);
        TextView bronzeName = view.findViewById(R.id.name_bronze);
        TextView silverName = view.findViewById(R.id.name_silver);
        TextView goldName = view.findViewById(R.id.name_gold);
        TextView bronzeVotes = view.findViewById(R.id.votes_bronze);
        TextView silverVotes = view.findViewById(R.id.votes_silver);
        TextView goldVotes = view.findViewById(R.id.votes_gold);
        Picasso.with(getContext()).load(images.get(2)).transform(new CircleTransform()).into(bronzeImage);
        Picasso.with(getContext()).load(images.get(1)).transform(new CircleTransform()).into(silverImage);
        Picasso.with(getContext()).load(images.get(0)).transform(new CircleTransform()).into(goldImage);
        bronzeName.setText(names.get(2));
        silverName.setText(names.get(1));
        goldName.setText(names.get(0));
        bronzeVotes.setText(votes.get(2));
        silverVotes.setText(votes.get(1));
        goldVotes.setText(votes.get(0));
    }

    private void initVotesRecycler(View view) {
        votesRecycler = view.findViewById(R.id.imp_votes_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        votesRecycler.setLayoutManager(layoutManager);
        populateVotesRecycler();
    }

    private void populateVotesRecycler() {
        ParseQuery<ParseObject> getModels = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
        getModels.whereEqualTo(Constants.IMP_COMP_HAS_ENTERED_KEY, true);
        getModels.addDescendingOrder(Constants.IMP_COMP_VOTES_KEY);
        getModels.setLimit(999);
        getModels.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // get all but the top 3 models
                    List<ParseObject> models = objects.subList(3, 10);
                    RecyclerViewTopAdapter adapter = new RecyclerViewTopAdapter(getContext(), models, true);
                    votesRecycler.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    populateUserRank(objects);
                } else {
                    Log.e(TAG, "Failed to pull entered models: " + e.getLocalizedMessage());
                }
            }
        });
    }

    LinearLayout userRankContainer;
    TextView tvRank, tvName, tvVotes;
    ImageView image;
    private void initUserRank(final View view) {
        userRankContainer = view.findViewById(R.id.user_rank_container);
        tvRank = view.findViewById(R.id.rank);
        tvName = view.findViewById(R.id.name);
        tvVotes = view.findViewById(R.id.votes);
        image = view.findViewById(R.id.logo);
    }

    private void populateUserRank(List<ParseObject> objects) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getParseObject(Constants.IMP_COMP_USER_KEY).getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                ParseObject userEntry = objects.get(i);
                Log.e(TAG, "user rank: #" + Integer.toString(i + 1));
                if (i >= 10) {
                    tvRank.setText("#" + Integer.toString(i + 1));
                    tvName.setText(ParseUser.getCurrentUser().getString(Constants.USER_FULL_NAME_KEY));
                    tvVotes.setText(Integer.toString(userEntry.getInt(Constants.IMP_COMP_VOTES_KEY)));
                    Picasso.with(getContext()).load(ParseUser.getCurrentUser().getString(Constants.USER_PROFILE_PICTURE_KEY)).transform(new CircleTransform()).into(image);
                    userRankContainer.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
