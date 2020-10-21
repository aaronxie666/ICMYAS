package icn.icmyas.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import icn.icmyas.BrowserActivity;
import icn.icmyas.R;

public class ViewArticleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_article, container, false);
        initViews(view);
        return view;
    }

    private void initViews(final View view) {
        final Bundle bundle = this.getArguments();
        final Boolean isAgency = bundle.getBoolean("isAgency");
        final String imageUrl = bundle.getString("imageUrl");
        final String title = bundle.getString("title");
        final String country = bundle.getString("country");
        final String content = bundle.getString("content");
        final String url = bundle.getString("url");

        if (isAgency) {
            // display agency country
            TextView tv_country = view.findViewById(R.id.country);
            tv_country.setText(country);
            tv_country.setVisibility(View.VISIBLE);
        }

        int id = isAgency ? R.id.banner : R.id.logo;
        ImageView iv_image = view.findViewById(id);

        // if there is an attached URL
        if (url != null && url.length() > 0) {
            iv_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBrowser(url);
                }
            });

            TextView tv_url = view.findViewById(R.id.url);
            tv_url.setText(url);
            tv_url.setVisibility(View.VISIBLE);

            TextView btn_website = view.findViewById(R.id.visit_website);
            btn_website.setVisibility(View.VISIBLE);
            btn_website.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBrowser(url);
                }
            });
        }

        TextView tv_title = view.findViewById(R.id.title);
        TextView tv_content = view.findViewById(R.id.content);

        if (imageUrl == null) {
            iv_image.setImageResource(R.drawable.paulfisher_round);
        } else {
            Picasso.with(getContext()).load(imageUrl).into(iv_image);
        }
        tv_title.setText(title);
        tv_content.setText(content);
    }

    private void openBrowser(String url) {
        // open the URL in a browser window
        Intent openBrowser = new Intent(getActivity(), BrowserActivity.class);
        openBrowser.putExtra("url", url);
        startActivity(openBrowser);
    }
}
