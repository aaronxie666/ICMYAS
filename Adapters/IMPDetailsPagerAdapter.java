package icn.icmyas.Adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import icn.icmyas.Misc.Constants;
import icn.icmyas.R;

public class IMPDetailsPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> photos;
    private LayoutInflater inflater;

    public IMPDetailsPagerAdapter(Context context, ArrayList<String> photos) {
        this.context = context;
        this.photos = photos;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e("PagerAdapter", "instantiateItem()");
        View itemView = inflater.inflate(R.layout.item_imp_photos, container, false);
        ImageView imageView = itemView.findViewById(R.id.photo);
        String photoUrl = photos.get(position);
        if (photoUrl.equals(Constants.NO_PICTURE)) {
            Picasso.with(context).load(R.drawable.no_profile).into(imageView);
        } else {
            Picasso.with(context).load(photos.get(position)).fit().centerCrop().into(imageView);
        }
        container.addView(itemView);
        return itemView;
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }
}
