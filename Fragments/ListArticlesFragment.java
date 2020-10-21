package icn.icmyas.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import icn.icmyas.Adapters.RecyclerViewAllAgenciesAdapter;
import icn.icmyas.Adapters.RecyclerViewNewsAdapter;
import icn.icmyas.Misc.Utils;
import icn.icmyas.Models.Agency;
import icn.icmyas.Models.NewsArticle;
import icn.icmyas.R;

public class ListArticlesFragment extends Fragment {

    private RecyclerView recycler;
    private Bundle bundle;
    private boolean isArticlesList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_articles, container, false);
        initViews(view);
        return view;
    }

    TextView pageNum;
    private void initViews(View view) {
        recycler = view.findViewById(R.id.recycler_all);
        recycler.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recycler.setLayoutManager(layoutManager);

        bundle = this.getArguments();
        isArticlesList = bundle.getBoolean("isArticlesList");
        if (isArticlesList) {
            populateArticles();
        } else {
            populateAgencies();
        }

        pageNum = view.findViewById(R.id.page_num);
        TextView nextPage = view.findViewById(R.id.next);
        TextView prevPage = view.findViewById(R.id.prev);
        nextPage.setOnClickListener(listener);
        prevPage.setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.next:
                    changePage(true);
                    break;
                case R.id.prev:
                    changePage(false);
                    break;
            }
        }
    };

    int currentPage = 1, fromIndex = 0, toIndex = 10;
    private void changePage(final boolean nextPage) {
        if (nextPage) {
            if (isArticlesList) {
                int length = articles.size();
                if (length > (currentPage * 10)) {
                    fromIndex = toIndex;
                    toIndex = (length > (currentPage + 1) * 10) ? toIndex + 10 : length;
                    visibleArticles = articles.subList(fromIndex, toIndex);
                    newsAdapter.update(visibleArticles);
                    currentPage++;
                    pageNum.setText(Integer.toString(currentPage));
                }
            } else {
                int length = agencies.size();
                if (length > (currentPage * 10)) {
                    fromIndex = toIndex;
                    toIndex = (length > (currentPage + 1) * 10) ? toIndex + 10 : length;
                    visibleAgencies = agencies.subList(fromIndex, toIndex);
                    agenciesAdapter.update(visibleAgencies);
                    currentPage++;
                    pageNum.setText(Integer.toString(currentPage));
                }
            }
        } else {
            if (isArticlesList) {
                if (currentPage > 1) {
                    toIndex = fromIndex;
                    fromIndex -= 10;
                    visibleArticles = articles.subList(fromIndex, toIndex);
                    newsAdapter.update(visibleArticles);
                    currentPage--;
                    pageNum.setText(Integer.toString(currentPage));
                }
            } else {
                if (currentPage > 1) {
                    toIndex = fromIndex;
                    fromIndex -= 10;
                    visibleAgencies = agencies.subList(fromIndex, toIndex);
                    agenciesAdapter.update(visibleAgencies);
                    currentPage--;
                    pageNum.setText(Integer.toString(currentPage));
                }
            }
        }
    }

    ArrayList<NewsArticle> articles;
    List<NewsArticle> visibleArticles;
    RecyclerViewNewsAdapter newsAdapter;
    private void populateArticles() {
        articles = (ArrayList<NewsArticle>) bundle.getSerializable("list");
        visibleArticles = (articles.size() > 10) ? articles.subList(0, 10) : articles;
        newsAdapter = new RecyclerViewNewsAdapter(getContext(), visibleArticles);
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
        recycler.setAdapter(newsAdapter);
    }

    ArrayList<Agency> agencies;
    List<Agency> visibleAgencies;
    RecyclerViewAllAgenciesAdapter agenciesAdapter;
    private void populateAgencies() {
        agencies = (ArrayList<Agency>) bundle.getSerializable("list");
        visibleAgencies = (agencies.size() > 10) ? agencies.subList(0, 10) : agencies;
        agenciesAdapter = new RecyclerViewAllAgenciesAdapter(getContext(), visibleAgencies);
        agenciesAdapter.setOnItemClickListener(new RecyclerViewAllAgenciesAdapter.AgencyClickListener() {
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
        recycler.setAdapter(agenciesAdapter);
    }
}
