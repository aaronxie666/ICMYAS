package icn.icmyas.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import icn.icmyas.Adapters.RecyclerViewAdapterEpisodes;
import icn.icmyas.Adapters.RecyclerViewImageOnlyAdapter;
import icn.icmyas.Adapters.RecyclerViewLeaderboardAdapter;
import icn.icmyas.FullscreenActivity;
import icn.icmyas.MainActivity;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.Utils;
import icn.icmyas.Models.EpisodeOrEntries;
import icn.icmyas.Models.LeaderboardModel;
import icn.icmyas.Models.Model;
import icn.icmyas.R;

import static icn.icmyas.Misc.Constants.EMPTY;
import static icn.icmyas.Misc.Constants.MODELS_VOTED_BY_KEY;

/**
 * Author:  Bradley Wilson
 * Date: 21/07/2017
 * Package: icn.icmyas.Fragments
 * Project Name: ICMYAS
 */

public class EpisodeFragment extends Fragment {

    private ViewAnimator episode_va;
    private ArrayList<EpisodeOrEntries> episodeOrEntries;
    private ArrayList<Model> modelsList;
    private RecyclerView modelsRecyclerView, leaderboardRecyclerView;
    private final String TAG = EpisodeFragment.class.getSimpleName();
    private Model selectedModel;
    private View view;
    private String story, cause, story_thumb, cause_thumb;
    private boolean storySelected, votedOn;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_episode, container, false);
        initToolbar();
        Bundle bundle = this.getArguments();
        initGlobalViews(view, bundle);
        if (bundle.getString(Constants.DETERMINE_LIST).equals(Constants.EPISODE_LIST)) {
            initEpisodeViews(view);
        }
        return view;
    }

    private void initCompetitionViews(View view) {
        Toast.makeText(getActivity(), "competition", Toast.LENGTH_SHORT).show();
    }

    private void initGlobalViews(View view, Bundle bundle) {
        episode_va = view.findViewById(R.id.episode_va);
        Animation inAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        Animation outAnim = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
        episode_va.setInAnimation(inAnim);
        episode_va.setOutAnimation(outAnim);
        episodeOrEntries = (ArrayList<EpisodeOrEntries>) bundle.getSerializable(Constants.EPISODE_LIST_BUNDLE_KEY);
        initEpisodesOrEntriesRecyclerView(view);
    }

    private void initToolbar() {
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
    }

    private Button heartButton;

    private void initEpisodeViews(View view) {
        final TextView selected_model_name = view.findViewById(R.id.selected_model_name);
        final TextView tv_my_story = view.findViewById(R.id.tv_my_story);
        final TextView tv_my_cause = view.findViewById(R.id.tv_my_cause);
        final TextView pledge_button = view.findViewById(R.id.pledge_button);
        final TextView total_votes = view.findViewById(R.id.total_votes);
        final TextView description = view.findViewById(R.id.story_cause_description);
        final ImageView thumbnail = view.findViewById(R.id.video_image);
        heartButton = view.findViewById(R.id.btn_heart);

        tv_my_story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!storySelected) {
                    tv_my_story.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_my_story.setBackgroundResource(R.drawable.rounded_left_selected);
                    tv_my_cause.setTextColor(Color.WHITE);
                    tv_my_cause.setBackgroundResource(R.drawable.rounded_right);
                    pledge_button.setVisibility(View.GONE);
                    description.setText(story);
                    Picasso.with(getActivity()).load(story_thumb).into(thumbnail);
                    storySelected = true;
                }
            }
        });
        tv_my_cause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (storySelected) {
                    tv_my_cause.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
                    tv_my_cause.setBackgroundResource(R.drawable.rounded_right_selected);
                    tv_my_story.setTextColor(Color.WHITE);
                    tv_my_story.setBackgroundResource(R.drawable.rounded_left);
                    pledge_button.setVisibility(View.VISIBLE);
                    description.setText(cause);
                    Picasso.with(getActivity()).load(cause_thumb).into(thumbnail);
                    storySelected = false;
                }
            }
        });
        TextView btn_storycause = view.findViewById(R.id.story_cause_button);
        btn_storycause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                episode_va.setDisplayedChild(2);
                // pull model information
                selected_model_name.setText(selectedModel.getName());
                total_votes.setText(String.format("%,d", selectedModel.getTransformationTotal()));
                try {
                    story = selectedModel.getMyStoryMyCause().fetch().getString(Constants.MY_STORY_MY_CAUSE_STORY_DESC_KEY);
                    cause = selectedModel.getMyStoryMyCause().fetch().getString(Constants.MY_STORY_MY_CAUSE_CAUSE_DESC_KEY);
                    story_thumb = selectedModel.getMyStoryMyCause().fetch().getString(Constants.MY_STORY_MY_CAUSE_STORY_VIDEO_THUMBNAIL_KEY);
                    cause_thumb = selectedModel.getMyStoryMyCause().fetch().getString(Constants.MY_STORY_MY_CAUSE_CAUSE_VIDEO_THUMBNAIL_KEY);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                description.setText(story);
                Picasso.with(getActivity()).load(story_thumb).into(thumbnail);
                storySelected = true;
            }
        });

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!votedOn) {
                    modelObject.add(Constants.MODELS_VOTED_BY_KEY, ParseUser.getCurrentUser().getObjectId());
                    modelObject.put(Constants.MODELS_TRANSFORMATION_TOTAL_KEY, modelObject.getInt(Constants.MODELS_TRANSFORMATION_TOTAL_KEY) + 1);
                    heartButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_heart_2));
                } else {
                    JSONArray newVotedByJSON = new JSONArray();
                    for (int i = 0; i < votedByJSON.length(); i++) {
                        try {
                            String objectId = votedByJSON.get(i).toString();
                            if (!objectId.equals(ParseUser.getCurrentUser().getObjectId())) {
                                newVotedByJSON.put(objectId);
                            }
                        } catch (JSONException e) {
                            Log.e("debug", "failed to add to newVotedByJSON array: " + e.getLocalizedMessage());
                            return;
                        }
                    }
                    modelObject.put(MODELS_VOTED_BY_KEY, newVotedByJSON);
                    modelObject.put(Constants.MODELS_TRANSFORMATION_TOTAL_KEY, modelObject.getInt(Constants.MODELS_TRANSFORMATION_TOTAL_KEY) - 1);
                    heartButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_heart));
                }
                try {
                    modelObject.save();
                } catch (ParseException e) {
                    Log.e("debug", "failed to cast vote on episode model: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private void initEpisodesOrEntriesRecyclerView(View view) {
        RecyclerView episodesRecyclerView = view.findViewById(R.id.episodes_list_recycler_view);
        episodesRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        episodesRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerViewAdapterEpisodes adapter = new RecyclerViewAdapterEpisodes(getActivity(), episodeOrEntries);
        adapter.setOnItemClickListener(new RecyclerViewAdapterEpisodes.onEpisodeItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position, EpisodeOrEntries episode) {
                if (!episode.isEntry()) {
                    episode_va.setDisplayedChild(1);
                    populateModelsWithinEpisodeRecyclerView(position);
                    initLeaderboardRecyclerView();
                } else {
                    Utils.openFragment(ProfileFragment.class.getSimpleName(), null, getFragmentManager(), EMPTY, episode.getObjectId(), null);
                }
            }
        });
        episodesRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void populateModelsWithinEpisodeRecyclerView(int i) {
        modelsList = new ArrayList<>();
        modelsRecyclerView = view.findViewById(R.id.models_images_recycler_view);
        modelsRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager tmLayoutManager = new GridLayoutManager(getActivity(), 5, LinearLayoutManager.VERTICAL, false);
        modelsRecyclerView.setLayoutManager(tmLayoutManager);

        String ep_number = Integer.toString(episodeOrEntries.get(i).getEpisodeNumber());
        String ep_location = episodeOrEntries.get(i).getLocationOrName();
        final TextView episode_title = getView().findViewById(R.id.heading_episode_title);
        episode_title.setText("Episode " + ep_number + " - " + ep_location);
        // get all models in episode
        modelsList = episodeOrEntries.get(i).getModelsInEpisode();

        final RecyclerViewImageOnlyAdapter adapter = new RecyclerViewImageOnlyAdapter(getActivity(), modelsList);
        adapter.setOnItemClickListener(new RecyclerViewImageOnlyAdapter.onItemClickListener() {
            @Override
            public void setOnItemClickListener(View view, int itemPosition, Model model) {
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    if (i == itemPosition) {
                        modelsRecyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.logo).setBackgroundResource(R.drawable.item_bg);
                    } else {
                        modelsRecyclerView.findViewHolderForAdapterPosition(i).itemView.findViewById(R.id.logo).setBackgroundResource(0);
                    }
                }
                updateSelectedModel(model);
            }
        });
        modelsRecyclerView.setAdapter(adapter);
        InitialiseSelectedModel();
        adapter.notifyDataSetChanged();
    }

    private void InitialiseSelectedModel() {
        for (Model model : modelsList) {
            if (model.isBestTransformed()) {
                updateSelectedModel(model);
                break;
            }
        }
    }

    private ArrayList<String> votedBy;
    private JSONArray votedByJSON;
    private ParseObject modelObject;

    private void updateSelectedModel(Model model) {
        // update before and after images
        final String beforeImageUrl = model.getBeforeImage();
        final String afterImageUrl = model.getAfterImage();
        ImageView model_before_image = getView().findViewById(R.id.model_before_image);
        Picasso.with(getActivity()).load(beforeImageUrl).fit().centerCrop().into(model_before_image);
        ImageView model_after_image = getView().findViewById(R.id.model_after_image);
        Picasso.with(getActivity()).load(afterImageUrl).fit().centerCrop().into(model_after_image);
        selectedModel = model;

        model_before_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showFullscreenImage = new Intent(getActivity(), FullscreenActivity.class);
                showFullscreenImage.putExtra(Constants.INTENT_EXTRA_IMAGE_URL, beforeImageUrl);
                getActivity().startActivity(showFullscreenImage);
            }
        });
        model_after_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showFullscreenImage = new Intent(getActivity(), FullscreenActivity.class);
                showFullscreenImage.putExtra(Constants.INTENT_EXTRA_IMAGE_URL, afterImageUrl);
                getActivity().startActivity(showFullscreenImage);
            }
        });

        ParseQuery getVotedOnArray = ParseQuery.getQuery(Constants.MODELS_CLASS_KEY);
        getVotedOnArray.whereEqualTo(Constants.MODELS_OBJECT_ID_KEY, selectedModel.getObjectId());
        try {
            modelObject = getVotedOnArray.getFirst();
            votedByJSON = modelObject.getJSONArray(Constants.MODELS_VOTED_BY_KEY);
            votedBy = new ArrayList<>();
            if(votedByJSON!=null){
                for (int i = 0; i < votedByJSON.length(); i++) {
                    votedBy.add(votedByJSON.get(i).toString());
                }
                // user has already voted on this model
                if (votedBy.contains(ParseUser.getCurrentUser().getObjectId())) {
                    heartButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_heart_2));
                    votedOn = true;
                } else {
                    heartButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_heart));
                    votedOn = false;
                }
            }else{
                votedBy = null;
            }

        } catch (ParseException e) {
            Log.e("debug", "failed to get models votedBy array");
        } catch (JSONException e) {
            Log.e("debug", "failed to pull from JSON array");
        }
    }

    private void initLeaderboardRecyclerView() {
        leaderboardRecyclerView = getView().findViewById(R.id.leaderboard_recycler_view);
        leaderboardRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        leaderboardRecyclerView.setLayoutManager(mLayoutManager);

        populateLeaderboardRecyclerView();
    }

    private void populateLeaderboardRecyclerView() {
        final ArrayList<LeaderboardModel> leaderboardsList = new ArrayList<>();
        for (Model model : modelsList) {
            leaderboardsList.add(new LeaderboardModel(model.getName(), model.getTransformationTotal()));
        }

        RecyclerViewLeaderboardAdapter adapter = new RecyclerViewLeaderboardAdapter(getActivity(), leaderboardsList);
        leaderboardRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
