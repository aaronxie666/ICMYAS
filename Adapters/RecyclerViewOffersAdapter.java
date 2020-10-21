package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Models.Offer;
import icn.icmyas.R;
import icn.icmyas.Widgets.WSquareImageView;

import static android.graphics.Typeface.BOLD;

/**
 * Author:  Bradley Wilson
 * Date: 14/08/2017
 * Package: icn.icmyas.Adapters
 * Project Name: ICMYAS
 */

public class RecyclerViewOffersAdapter extends RecyclerView.Adapter<RecyclerViewOffersAdapter.RecyclerViewOffersViewHolder> {

    private Context context;
    private ArrayList<Offer> offersList;
    private onOfferItemClickListener mItemClickListener;

    public RecyclerViewOffersAdapter(Context context, ArrayList<Offer> offersList) {
        this.context = context;
        this.offersList = offersList;
    }

    @Override
    public RecyclerViewOffersViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());
        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.item_row_offers, viewGroup, false);
        return new RecyclerViewOffersViewHolder(mainGroup);
    }

    @Override
    public void onBindViewHolder(RecyclerViewOffersViewHolder holder, int position) {
        Offer offer = offersList.get(position);
        if (offer.isOffer()) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.itemView.setLayoutParams(params);
            if (offer.isGold()) {
                holder.price.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_goldstar), null, null, null);
            } else {
                holder.price.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_silverstar), null, null, null);
            }
            holder.price.setText(String.valueOf(offer.getPrice()));
            Picasso.with(context).load(offer.getImageUrl()).fit().centerCrop().into(holder.image);
            holder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.bonus.setVisibility(View.GONE);
        } else {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            holder.itemView.setLayoutParams(params);
            holder.price.setText(String.valueOf("£" + offer.getPrice() + ".99"));
            holder.description.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, R.drawable.ic_goldstar), null, null, null);
            holder.description.setGravity(Gravity.CENTER);
            holder.description.setTypeface(holder.description.getTypeface(), BOLD);
            Picasso.with(context).load(offer.getImageUrl()).fit().centerCrop().into(holder.image);
            if (offer.getExtra() != 0) {
                int bonusImage;
                switch (offer.getExtra()) {
                    case 25:
                        bonusImage = R.drawable.extra25;
                        break;
                    case 50:
                        bonusImage = R.drawable.extra50;
                        break;
                    case 75:
                        bonusImage = R.drawable.extra75;
                        break;
                    default:
                        bonusImage = R.drawable.extra100;
                        break;
                }
                Picasso.with(context).load(bonusImage).into(holder.bonus);
                holder.bonus.setVisibility(View.VISIBLE);
            } else {
                holder.bonus.setVisibility(View.GONE);
            }
        }

        if (offer.isFeatured()) {
            String featuredDesc = "Featured " + offer.getText();
            Spannable spannable = new SpannableString(featuredDesc);
            spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, android.R.color.holo_red_dark)), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.description.setText(spannable, TextView.BufferType.SPANNABLE);
        } else {
            holder.description.setText(offer.getText());
        }
    }

    public void setOnItemClickListener(onOfferItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onOfferItemClickListener {
        void onItemClickListener(View view, int position, Offer offer);
    }

    @Override
    public int getItemCount() {
        return (null != offersList ? offersList.size() : 0);
    }

    public class RecyclerViewOffersViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView price, description;
        private WSquareImageView image;
        private ImageView bonus;

        public RecyclerViewOffersViewHolder(View itemView) {
            super(itemView);
            this.image = (WSquareImageView) itemView.findViewById(R.id.offers_image);
            this.price = (TextView) itemView.findViewById(R.id.offers_price);
            this.description = (TextView) itemView.findViewById(R.id.offers_description);
            this.bonus = itemView.findViewById(R.id.bonus_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClickListener(view, getAdapterPosition(), offersList.get(getAdapterPosition()));
            }
        }
    }
}
