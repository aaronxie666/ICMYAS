package icn.icmyas.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.squareup.picasso.Picasso;

import java.net.URI;
import java.net.URL;

import icn.icmyas.MainActivity;
import icn.icmyas.R;

public class AboutFragment extends Fragment {



    private ViewAnimator about_va;
    private TextView tv_about;
    private TextView textAboutSelected;
    private String selected_about;
    private ImageView img_selected_about;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        initViews(view);
        return view;
    }




    private void initViews(View view) {
        initToolbar();

        about_va = view.findViewById(R.id.about_va);
        tv_about = view.findViewById(R.id.tv_about);
        img_selected_about = view.findViewById(R.id.img_about_selected);
        textAboutSelected = view.findViewById(R.id.text_about_selected);

        // initialise ImageViews
        final ImageView img_paul = view.findViewById(R.id.img_paul);
        final ImageView img_experts = view.findViewById(R.id.img_experts);
        final ImageView img_lifetime = view.findViewById(R.id.img_lifetime);
        final ImageView img_icn = view.findViewById(R.id.img_icn);

        // set image resources for ImageViews
        Picasso.with(getActivity()).load(R.drawable.paul_fisher).into(img_paul);
        Picasso.with(getActivity()).load(R.drawable.img_experts).into(img_experts);
        Picasso.with(getActivity()).load(R.drawable.life_time).into(img_lifetime);
        Picasso.with(getActivity()).load(R.drawable.icn).into(img_icn);

        // set onClickListeners for frames
        img_paul.setOnClickListener(customListener);
        img_experts.setOnClickListener(customListener);
        img_lifetime.setOnClickListener(customListener);
        img_icn.setOnClickListener(customListener);
    }

    private View.OnClickListener customListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            about_va.setInAnimation(getContext(), R.anim.slide_in);
            about_va.setOutAnimation(getContext(), R.anim.slide_out);
            about_va.setDisplayedChild(1);

            switch (view.getId()) {
                case R.id.img_paul:
                    selected_about = "Paul Fisher";
                    Picasso.with(getActivity()).load(R.drawable.paul_fisher).into(img_selected_about);
                    textAboutSelected.setText("Paul Fisher is an internationally recognised modelling manager with over 20 year’s industry experience in talent management. He has represented supermodels such as Naomi Campbell, Monica Bellucci, Stephanie Seymour and more. Paul’s latest show ‘I Can Make You a Supermodel’ is set to be an international success as it launches in more countries, with the world’s next supermodels waiting to be discovered by the show.");
                    break;
                case R.id.img_experts:
                    selected_about = "The Experts";
                    Picasso.with(getActivity()).load(R.drawable.img_experts).into(img_selected_about);
                    textAboutSelected.setText("Top model Jen Dawson has been in the fashion industry since she was 15, and has extensive knowledge having worked with some of the most famous names in the industry including Marc Jacobs and Givenchy. Sophie McMullen is a pro in the beauty industry and supports the search as the Team PA to make sure that they stay on track in finding the best new faces. Ruby Tindall tests out the potential models the team discover as a photographer, and knows what it takes to get fantastic fashion shots.");
                    break;
                case R.id.img_lifetime:
                    selected_about = "Lifetime";
                    Picasso.with(getActivity()).load(R.drawable.life_time).into(img_selected_about);
                    textAboutSelected.setText("Lifetime is an international television network which is airing 'I Can Make You A Supermodel'. The UK channel launches in late 2013 and boasts a huge variety of big TV names such as Britian's Next Top Model, Project Runway, Judge Judy, Little Women, Storage Wars, Pawn Stars and much much more. You can watch ICMYAS on Lifetime on Thursdays at 9PM.");
                    break;
                case R.id.img_icn:
                    selected_about = "ICN";
                    Picasso.with(getActivity()).load(R.drawable.icn).into(img_selected_about);
                    textAboutSelected.setText("International Celebrity Networks is a digital media publisher based in Nottingham’s Creative Quarter which offers a range of digital services, including Apps. ICN has a strong portfolio both for its in-house Apps and for 3rd party App creation. ICN enjoys excellent relationships with Appstores, phone carriers, talent agents, publicists, brands and media owners to distribute their products- offering a specialist service for all parties. ");
                    break;
            }

            tv_about.setText("About " + selected_about);
        }
    };

    private void initToolbar() {
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).getSupportActionBar().setElevation(0);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((MainActivity) getActivity()).setDrawerIndicatorEnabled(false);
    }

    @Override
    public void onResume() {

        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){
                    if (about_va.getDisplayedChild() > 0) {
                        about_va.setDisplayedChild(0);
                    } else {
                        getActivity().onBackPressed();
                    }
                    return true;
                }
                return false;
            }
        });
    }
}