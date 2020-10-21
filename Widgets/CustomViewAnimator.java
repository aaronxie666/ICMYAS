package icn.icmyas.Widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewAnimator;

import icn.icmyas.R;

/**
 * Author:  Bradley Wilson
 * Date: 14/07/2017
 * Package: icn.icmyas.Widgets
 * Project Name: ICMYAS
 */

public class CustomViewAnimator extends ViewAnimator {

    private Context context;

    public CustomViewAnimator(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec+((int)context.getResources().getDimension(R.dimen.login_va_height)));
    }
}
