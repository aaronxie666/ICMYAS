package icn.icmyas;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import icn.icmyas.Adapters.IMPPagerAdapter;
import icn.icmyas.Misc.Constants;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class IMPTutorialActivity extends AppCompatActivity {

    private final String TAG = IMPTutorialActivity.class.getSimpleName();
    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        initViews();
    }

    private void initViews() {
        final TextView doneButton = (TextView) findViewById(R.id.btn_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTutorialComplete();
                finish();
            }
        });
        final int images[] = {R.drawable.tut1, R.drawable.tut2, R.drawable.tut3, R.drawable.tut4, R.drawable.tut5, R.drawable.tut6, R.drawable.tut7};
        final ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
        IMPPagerAdapter adapter = new IMPPagerAdapter(context, images);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //
            }

            @Override
            public void onPageSelected(int position) {
                if (position == (images.length - 1)) {
                    doneButton.setVisibility(VISIBLE);
                } else {
                    doneButton.setVisibility(GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //
            }
        });
    }

    private void setTutorialComplete() {
        ParseQuery<ParseObject> getEntry = ParseQuery.getQuery(Constants.IMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.IMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            ParseObject entry = getEntry.getFirst();
            entry.put(Constants.IMP_TUTORIAL_COMPLETE_KEY, true);
            entry.saveInBackground();
        } catch (ParseException e) {
            Log.e(TAG, "Could not get IMP entry for current user: " + e.getLocalizedMessage());
        }
    }
}
