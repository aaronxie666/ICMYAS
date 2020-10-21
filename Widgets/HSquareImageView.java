package icn.icmyas.Widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import icn.icmyas.R;

/**
 * Author:  Tom Linford
 * Date: 29/08/2017
 * Package: icn.icmyas.Widgets
 * Project Name: ICMYAS
 */

public class HSquareImageView extends android.support.v7.widget.AppCompatImageView {

    public HSquareImageView(Context context) {
        super(context);
    }

    public HSquareImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HSquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        setMeasuredDimension(height, height);
    }
}
