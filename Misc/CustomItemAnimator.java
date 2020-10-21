package icn.icmyas.Misc;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;

import com.squareup.picasso.Transformation;

/**
 * Author:  Tom Linford
 * Date: 29/08/2017
 * Package: icn.icmyas.Misc
 * Project Name: ICMYAS
 */

public class CustomItemAnimator extends DefaultItemAnimator {

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        return true;
    }

    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
    }

    @Override
    public void onAnimationFinished(RecyclerView.ViewHolder holder) {
        super.onAnimationFinished(holder);
    }


    @Override
    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        return super.animateMove(holder, fromX, fromY, toX, toY);
    }
}
