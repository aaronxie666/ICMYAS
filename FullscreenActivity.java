package icn.icmyas;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import icn.icmyas.Misc.Constants;

public class FullscreenActivity extends AppCompatActivity {

    private final String TAG = FullscreenActivity.class.getSimpleName();
    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ImageView imageView = (ImageView) findViewById(R.id.fullscreen_image);
        setImage(imageView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setImage((ImageView) findViewById(R.id.fullscreen_image));
    }

    public int getRotation(Context context){
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
    }

    public void setImage(ImageView imageView) {
        Picasso.with(context).load(getIntent().getExtras().getString(Constants.INTENT_EXTRA_IMAGE_URL)).fit().rotate(getRotation(context)).centerInside().into(imageView, new Callback() {
            @Override
            public void onSuccess() {
                Log.e(TAG, "Image loaded successfully");
            }

            @Override
            public void onError() {
                Log.e(TAG, "Image load failed");
            }
        });
    }
}
