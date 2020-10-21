package icn.icmyas.Widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Author:  Tom Linford
 * Date: 29/08/2017
 * Package: icn.icmyas.Widgets
 * Project Name: ICMYAS
 */

public class WSquareImageView extends android.support.v7.widget.AppCompatImageView {

    public WSquareImageView(Context context) {
        super(context);
    }

    public WSquareImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WSquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }
}
