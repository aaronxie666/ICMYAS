package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import icn.icmyas.Models.NewsArticle;
import icn.icmyas.R;

public class RecyclerViewNewsAdapter extends RecyclerView.Adapter<RecyclerViewNewsAdapter.NewsViewHolder> {

    private List<NewsArticle> articles;
    private Context context;
    private ArticleClickListener listener;

    public RecyclerViewNewsAdapter(Context context, List<NewsArticle> articles) {
        this.context = context;
        this.articles = articles;
    }

    public RecyclerViewNewsAdapter.NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) inflater.inflate(R.layout.item_row_articles, parent, false);
        return new NewsViewHolder(mainGroup);
    }

    public void onBindViewHolder(RecyclerViewNewsAdapter.NewsViewHolder holder, int position) {
        NewsArticle article = articles.get(position);
        holder.title.setText(article.getTitle());
    }

    public void update(List<NewsArticle> list) {
        this.articles = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(RecyclerViewNewsAdapter.ArticleClickListener itemClickListener) {
        this.listener = itemClickListener;
    }

    public interface ArticleClickListener {
        void onItemClickListener(View view, int position, NewsArticle article);
    }

    @Override
    public int getItemCount() {
        return (articles != null) ? articles.size() : 0;
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView articleImage;
        TextView title;

        public NewsViewHolder(View itemView) {
            super(itemView);
            this.articleImage = itemView.findViewById(R.id.article_img);
            this.title = itemView.findViewById(R.id.article_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                int position = getAdapterPosition();
                listener.onItemClickListener(view, position, articles.get(position));
            }
        }
    }
}
