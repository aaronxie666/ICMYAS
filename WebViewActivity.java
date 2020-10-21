package icn.icmyas;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private Context context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initToolbar();
        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl("http://www.paulfishershop.com");
    }

    private void initToolbar() {
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayShowTitleEnabled(false);
//        ab.setDisplayHomeAsUpEnabled(true);
        ab.setElevation(0);
        ab.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.silver)));
        ab.setCustomView(R.layout.toolbar_layout);
        ab.setDisplayShowCustomEnabled(true);
        invalidateOptionsMenu();
    }

}
