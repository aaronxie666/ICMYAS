package icn.icmyas.Fragments;

import android.content.Intent;
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
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import icn.icmyas.Adapters.RecyclerViewAgenciesAdapter;
import icn.icmyas.Adapters.RecyclerViewNewsAdapter;
import icn.icmyas.IMPTutorialActivity;
import icn.icmyas.MainActivity;
import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.Utils;
import icn.icmyas.Models.Agency;
import icn.icmyas.Models.NewsArticle;
import icn.icmyas.R;
import icn.icmyas.Widgets.CustomDosisTextView;

import static icn.icmyas.Misc.Constants.AGENCIES_BANNER_URL_KEY;
import static icn.icmyas.Misc.Constants.AGENCIES_BIO_KEY;
import static icn.icmyas.Misc.Constants.AGENCIES_CLASS_KEY;
import static icn.icmyas.Misc.Constants.AGENCIES_COUNTRY_KEY;
import static icn.icmyas.Misc.Constants.AGENCIES_LOGO_URL_KEY;
import static icn.icmyas.Misc.Constants.AGENCIES_NAME_KEY;
import static icn.icmyas.Misc.Constants.AGENCIES_URL_KEY;
import static icn.icmyas.Misc.Constants.IMP_COMP_CLASS_KEY;
import static icn.icmyas.Misc.Constants.IMP_COMP_HAS_ENTERED_KEY;
import static icn.icmyas.Misc.Constants.IMP_COMP_USER_KEY;
import static icn.icmyas.Misc.Constants.IMP_COMP_VOTED_ON_KEY;
import static icn.icmyas.Misc.Constants.IMP_COMP_VOTES_KEY;
import static icn.icmyas.Misc.Constants.IMP_TUTORIAL_COMPLETE_KEY;
import static icn.icmyas.Misc.Constants.NEWS_CLASS_KEY;
import static icn.icmyas.Misc.Constants.NEWS_CONTENT_KEY;
import static icn.icmyas.Misc.Constants.NEWS_CREATED_AT_KEY;
import static icn.icmyas.Misc.Constants.NEWS_TITLE_KEY;
import static icn.icmyas.Misc.Constants.NEWS_USER_KEY;

public class IMPHomeFragment extends Fragment {

    final static String TAG = "IMPHomeFragment";
    Utils utils;
    RecyclerView agenciesRecycler, newsRecycler;
    RecyclerViewAgenciesAdapter agenciesAdapter;
    RecyclerViewNewsAdapter newsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imp_home, container, false);
        utils = new Utils(getActivity());
        initToolbar();
        initViews(view);
        return view;
    }

    private void initToolbar() {
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
    }

    private void initViews(View view) {
        TextView profileButton = view.findViewById(R.id.profile_button);
        TextView viewAgencies = view.findViewById(R.id.view_agencies);
        TextView viewArticles = view.findViewById(R.id.view_articles);
        TextView voting = view.findViewById(R.id.voting);
        TextView ranking = view.findViewById(R.id.ranking);
        profileButton.setOnClickListener(listener);
        viewAgencies.setOnClickListener(listener);
        viewArticles.setOnClickListener(listener);
        voting.setOnClickListener(listener);
        ranking.setOnClickListener(listener);

        initFanFavourites(view);
        initAgenciesRecycler(view);
        initNewsRecycler(view);

        checkForTableEntry(view);
    }

    private boolean tutorialComplete = false;
    private boolean isEntered() {
        ParseQuery<ParseObject> getEntry = ParseQuery.getQuery(Constants.IMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.IMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject entry = getEntry.getFirst();
            tutorialComplete = entry.getBoolean(IMP_TUTORIAL_COMPLETE_KEY);
            return true;
        } catch (ParseException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return false;
        }
    }

    private void checkForTableEntry(final View view) {
        if (!isEntered()) {
            enterIntoIMPTable();
            enterIntoIMPCompTable();
            showTutorial();
        } else if (!tutorialComplete) {
            showTutorial();
        }
    }

    private void showTutorial() {
        Intent intent = new Intent(getContext(), IMPTutorialActivity.class);
        startActivity(intent);
    }

    private void enterIntoIMPTable() {
        final ParseObject newEntry = new ParseObject(Constants.IMP_CLASS_KEY);
        newEntry.put(Constants.IMP_TUTORIAL_COMPLETE_KEY, true);
        newEntry.put(Constants.IMP_USER_KEY, ParseUser.getCurrentUser());
        newEntry.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.e(TAG, "entered into IMP");
                } else {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        });
    }

    private void enterIntoIMPCompTable() {
        final ParseObject newEntry = new ParseObject(Constants.IMP_COMP_CLASS_KEY);
        newEntry.put(IMP_COMP_VOTES_KEY, 0);
        newEntry.put(IMP_COMP_USER_KEY, ParseUser.getCurrentUser());
        newEntry.put(IMP_COMP_VOTED_ON_KEY, new JSONArray());
        newEntry.put(IMP_COMP_HAS_ENTERED_KEY, false);
        newEntry.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.e(TAG, "entered into IMPComp");
                } else {
                    Log.e(TAG, e.getLocalizedMessage());
                }
            }
        });
    }

    private ImageView bronzeImage, silverImage, goldImage;
    private CustomDosisTextView bronzeName, silverName, goldName;
    private CustomDosisTextView bronzeVotes, silverVotes, goldVotes;

    private void initFanFavourites(View view) {
        bronzeImage = view.findViewById(R.id.img_bronze);
        silverImage = view.findViewById(R.id.img_silver);
        goldImage = view.findViewById(R.id.img_gold);
        bronzeName = view.findViewById(R.id.name_bronze);
        silverName = view.findViewById(R.id.name_silver);
        goldName = view.findViewById(R.id.name_gold);
        bronzeVotes = view.findViewById(R.id.votes_bronze);
        silverVotes = view.findViewById(R.id.votes_silver);
        goldVotes = view.findViewById(R.id.votes_gold);
        fetchFanFavourites();
    }

    private ArrayList<String> names, images, votes;
    private void fetchFanFavourites() {
        names = new ArrayList<>();
        images = new ArrayList<>();
        votes = new ArrayList<>();
        ParseQuery<ParseObject> getFanFavourites = ParseQuery.getQuery(IMP_COMP_CLASS_KEY);
        // getFanFavourites.whereEqualTo(IMP_COMP_HAS_ENTERED_KEY, true);
        getFanFavourites.addDescendingOrder(IMP_COMP_VOTES_KEY);
        getFanFavourites.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    // will have to ensure that at least 3 users are in the database
                    for (int i = 0; i < 3; i++) {
                        try {
                            names.add(objects.get(0).getParseObject(Constants.IMP_COMP_USER_KEY).fetchIfNeeded().getString(Constants.USER_FULL_NAME_KEY));
                            images.add(objects.get(0).getParseObject(Constants.IMP_COMP_USER_KEY).fetchIfNeeded().getString(Constants.USER_PROFILE_PICTURE_KEY));
                            votes.add(Integer.toString(objects.get(0).getInt(Constants.IMP_COMP_VOTES_KEY)));
                            objects.remove(0);
                        } catch (ParseException e1) {
                            e1.getLocalizedMessage();
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
                } else {
                    Log.e("debug", "get favourites query failed: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle args = new Bundle();
            switch (v.getId()) {
                case R.id.profile_button:
                    Utils.overlayFragment(IMPProfileFragment.class.getSimpleName(), getFragmentManager(), args);
                    break;
                case R.id.view_agencies:
                    args.putBoolean("isArticlesList", false);
                    args.putSerializable("list", agencies);
                    Utils.overlayFragment(ListArticlesFragment.class.getSimpleName(), getFragmentManager(), args);
                    break;
                case R.id.view_articles:
                    args.putBoolean("isArticlesList", true);
                    args.putSerializable("list", articles);
                    Utils.overlayFragment(ListArticlesFragment.class.getSimpleName(), getFragmentManager(), args);
                    break;
                case R.id.voting:
                    Utils.overlayFragment(IMPVotingFragment.class.getSimpleName(), getFragmentManager(), args);
                    break;
                case R.id.ranking:
                    args.putStringArrayList("names", names);
                    args.putStringArrayList("images", images);
                    args.putStringArrayList("votes", votes);
                    Utils.overlayFragment(IMPRankingFragment.class.getSimpleName(), getFragmentManager(), args);
                    break;
            }
        }
    };

    private void initAgenciesRecycler(View view) {
        agenciesRecycler = view.findViewById(R.id.recycler_agencies);
        agenciesRecycler.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        agenciesRecycler.setLayoutManager(layoutManager);
        populateAgenciesRecycler();
    }

    private ArrayList<Agency> agencies, displayedAgencies;
    private void populateAgenciesRecycler() {
        agencies = new ArrayList<>();
        displayedAgencies = new ArrayList<>();
        ParseQuery<ParseObject> getAgencies = ParseQuery.getQuery(AGENCIES_CLASS_KEY);
        getAgencies.setLimit(999);
        getAgencies.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        String name = objects.get(i).getString(AGENCIES_NAME_KEY);
                        String bio = objects.get(i).getString(AGENCIES_BIO_KEY);
                        String country = objects.get(i).getString(AGENCIES_COUNTRY_KEY);
                        String logoUrl = objects.get(i).getString(AGENCIES_LOGO_URL_KEY);
                        String bannerUrl = objects.get(i).getString(AGENCIES_BANNER_URL_KEY);
                        String url = objects.get(i).getString(AGENCIES_URL_KEY);
                        if (imageExists(logoUrl) && imageExists(bannerUrl)) {
                            Agency agency = new Agency(name, bio, country, logoUrl, bannerUrl, url);
                            agencies.add(agency);
                            if (i < 6) {
                                displayedAgencies.add(agency);
                            }
                        }
                    }
                    agenciesAdapter = new RecyclerViewAgenciesAdapter(getContext(), displayedAgencies);
                    agenciesAdapter.setOnItemClickListener(new RecyclerViewAgenciesAdapter.AgencyClickListener() {
                        @Override
                        public void onItemClickListener(View view, int position, Agency agency) {
                            Bundle args = new Bundle();
                            args.putBoolean("isAgency", true);
                            args.putString("imageUrl", agency.getBannerUrl());
                            args.putString("title", agency.getName());
                            args.putString("country", agency.getCountry());
                            args.putString("content", agency.getBio());
                            args.putString("url", agency.getUrl());
                            Utils.overlayFragment(ViewArticleFragment.class.getSimpleName(), getFragmentManager(), args);
                        }
                    });
                    agenciesRecycler.setAdapter(agenciesAdapter);
                } else {
                    Log.e("IMPHomeFragment", "failed to get agencies: " + e.getLocalizedMessage());
                }
            }
        });
    }

    private boolean imageExists(String imageUrl) {
        return (imageUrl != null) && (imageUrl.length() > 0);
    }

    private void initNewsRecycler(View view) {
        newsRecycler = view.findViewById(R.id.recycler_news);
        newsRecycler.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        newsRecycler.setLayoutManager(layoutManager);
        populateNewsRecycler();
    }

    private ArrayList<NewsArticle> articles, displayedArticles;
    private void populateNewsRecycler() {
        articles = new ArrayList<>();
        displayedArticles = new ArrayList<>();
        ParseQuery<ParseObject> getArticles = ParseQuery.getQuery(NEWS_CLASS_KEY);
        getArticles.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < objects.size(); i++) {
                        String title = objects.get(i).getString(NEWS_TITLE_KEY);
                        String content = objects.get(i).getString(NEWS_CONTENT_KEY);
                        String user = objects.get(i).getString(NEWS_USER_KEY);
                        String createdAt = objects.get(i).getString(NEWS_CREATED_AT_KEY);
                        NewsArticle article = new NewsArticle(title, content, user, createdAt);
                        articles.add(article);
                        if (i < 3) {
                            displayedArticles.add(article);
                        }
                    }
                    newsAdapter = new RecyclerViewNewsAdapter(getContext(), displayedArticles);
                    newsAdapter.setOnItemClickListener(new RecyclerViewNewsAdapter.ArticleClickListener() {
                        @Override
                        public void onItemClickListener(View view, int position, NewsArticle article) {
                            Bundle args = new Bundle();
                            args.putBoolean("isAgency", false);
                            args.putString("imageUrl", null);
                            args.putString("title", article.getTitle());
                            args.putString("content", article.getContent());
                            Utils.overlayFragment(ViewArticleFragment.class.getSimpleName(), getFragmentManager(), args);
                        }
                    });
                    newsRecycler.setAdapter(newsAdapter);
                } else {
                    Log.e("IMPHomeFragment", "failed to get news: " + e.getLocalizedMessage());
                }
            }
        });
    }
}
