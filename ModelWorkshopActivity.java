package icn.icmyas;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import icn.icmyas.Misc.Utils;
import icn.icmyas.Widgets.CustomRobotoCondensedTextView;

import static android.widget.Toast.LENGTH_SHORT;

public class ModelWorkshopActivity extends AppCompatActivity {

    private final String TAG = ModelWorkshopActivity.class.getSimpleName();
    private Context context = this;
    private boolean checked = false;

    private ImageView imgCheckbox;
    private ProgressBar progressBar;
    private ScrollView BasePage;
    private CustomRobotoCondensedTextView btnJoinWorkshop;
    private Utils utils;
    private WebView webView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_workshop);
        initToolbar();
        initViews();

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
        webView.loadUrl("https://www.bit.ly/PaulFisherWorkshopAugust12");
    }

    private void initToolbar() {
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(false);
        ab.setElevation(0);
        ab.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(context, R.color.silver)));
        ab.setCustomView(R.layout.toolbar_layout);
        ab.setDisplayShowCustomEnabled(true);
        invalidateOptionsMenu();
    }

    private void initViews() {
        BasePage = (ScrollView) findViewById(R.id.base_page);
        imgCheckbox = (ImageView) findViewById(R.id.img_checkbox);
        imgCheckbox.setOnClickListener(customListener);
        btnJoinWorkshop = (CustomRobotoCondensedTextView) findViewById(R.id.btn_join_workshop);
        btnJoinWorkshop.setOnClickListener(customListener);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private View.OnClickListener customListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_join_workshop:
                    joinWorkshop();
                    break;
                case R.id.img_checkbox:
                    updateCheckbox();
                    break;
            }
        }
    };

    private void joinWorkshop() {
        if (checked) {
            progressBar.setVisibility(View.VISIBLE);
            joinSkypeComp();
            progressBar.setVisibility(View.GONE);
        }else{
            Toast.makeText(getApplicationContext(), "Please check the condition below first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void joinSkypeComp() {
        webView.setVisibility(View.VISIBLE);
        BasePage.setVisibility(View.GONE);

    }

    private void updateCheckbox() {
        checked = !checked;
        if (checked) {
            Picasso.with(context).load(R.drawable.checkbox_2).fit().into(imgCheckbox);
        } else {
            Picasso.with(context).load(R.drawable.checkbox).fit().into(imgCheckbox);
        }
    }
}
