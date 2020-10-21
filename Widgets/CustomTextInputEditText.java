package icn.icmyas.Widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;

/**
 * Author:  Bradley Wilson
 * Date: 14/07/2017
 * Package: icn.icmyas.Widgets
 * Project Name: ICMYAS
 */

public class CustomTextInputEditText extends TextInputEditText {
    private Typeface customTypeface;

    public CustomTextInputEditText(Context context) {
        super(context);
    }

    public CustomTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextInputEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private Typeface getCustomFont(Context context) {
        if (customTypeface == null) {
            customTypeface = Typeface.createFromAsset(
                    context.getAssets(), "fonts/HelveticaNeueLTStd-HvCn.ttf");
        }
        return customTypeface;
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            setTypeface(getCustomFont(getContext()));
        }
    }
}
