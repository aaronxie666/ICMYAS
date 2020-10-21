package icn.icmyas.ViewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import icn.icmyas.R;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.ViewHolders
 * Project Name: ICMYAS
 */

public class RecyclerViewHolderTitleAndImage extends RecyclerView.ViewHolder {
    // View holder for gridview recycler view as we used in listview
    public TextView title;
    public ImageView imageview;

    public RecyclerViewHolderTitleAndImage(View view) {
        super(view);
        // Find all views ids
        this.title = (TextView) view
                .findViewById(R.id.title);
        this.imageview = (ImageView) view
                .findViewById(R.id.videoImage);
    }

}
