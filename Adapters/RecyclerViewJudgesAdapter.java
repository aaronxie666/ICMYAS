package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Models.Judge;
import icn.icmyas.R;

public class RecyclerViewJudgesAdapter extends RecyclerView.Adapter<RecyclerViewJudgesAdapter.JudgesViewHolder> {

    private ArrayList<Judge> judges;
    private Context context;
    private JudgeClickListener listener;

    public RecyclerViewJudgesAdapter(Context context, ArrayList<Judge> judges) {
        this.context = context;
        this.judges = judges;
    }

    public RecyclerViewJudgesAdapter.JudgesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewGroup mainGroup = (ViewGroup) inflater.inflate(R.layout.item_row_judges, parent, false);
        return new JudgesViewHolder(mainGroup);
    }

    public void onBindViewHolder(RecyclerViewJudgesAdapter.JudgesViewHolder holder, int position) {
        Judge judge = judges.get(position);
        holder.votedFor.setText("Voted For:\n" + judge.getVotedFor());
        Picasso.with(context).load(judge.getImage()).fit().centerCrop()/*.transform(new CircleTransform())*/.into(holder.judgeImage);
    }

    public void setOnItemClickListener(RecyclerViewJudgesAdapter.JudgeClickListener mItemClickListener) {
        this.listener = mItemClickListener;
    }

    public interface JudgeClickListener {
        void onItemClickListener(View view, int position, Judge judge);
    }

    @Override
    public int getItemCount() {
        return (judges != null) ? judges.size() : 0;
    }

    public class JudgesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView votedFor;
        ImageView judgeImage;

        public JudgesViewHolder(View itemView) {
            super(itemView);
            this.judgeImage = itemView.findViewById(R.id.judge_img);
            this.votedFor = itemView.findViewById(R.id.voted_for);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onItemClickListener(view, getAdapterPosition(), judges.get(getAdapterPosition()));
            }
        }
    }

}
