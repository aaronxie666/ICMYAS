package icn.icmyas;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import icn.icmyas.Adapters.RecyclerViewAdapterMessages;
import icn.icmyas.Adapters.ViewPagerAdapter;
import icn.icmyas.Fragments.AboutFragment;
import icn.icmyas.Fragments.ForumsFragment;
import icn.icmyas.Fragments.HomeFragment;
import icn.icmyas.Fragments.OffersFragment;
import icn.icmyas.Fragments.ProfileFragment;
import icn.icmyas.Listeners.DrawerToggleListener;
import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.Utils;
import icn.icmyas.Models.Messages;

import static android.view.Window.FEATURE_NO_TITLE;
import static android.widget.Toast.LENGTH_LONG;
import static icn.icmyas.Misc.Constants.EMPTY;
import static icn.icmyas.Misc.Constants.LOGOUT_ALERT_ID;
import static icn.icmyas.Misc.Constants.MESSAGES_CLASS_KEY;
import static icn.icmyas.Misc.Constants.MESSAGES_IS_READ_KEY;
import static icn.icmyas.Misc.Constants.MESSAGES_MESSAGE_KEY;
import static icn.icmyas.Misc.Constants.MESSAGES_RECEIVING_USER_KEY;
import static icn.icmyas.Misc.Constants.MESSAGES_SENDING_USER_KEY;
import static icn.icmyas.Misc.Constants.NO_PICTURE;
import static icn.icmyas.Misc.Constants.USER_FULL_NAME_KEY;
import static icn.icmyas.Misc.Constants.USER_PROFILE_PICTURE_KEY;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas
 * Project Name: ICMYAS
 */

public class MainActivity extends AppCompatActivity {

    private static Boolean drawerOpen = false;
    private DrawerLayout mDrawerLayout;
    private DrawerToggleListener mDrawerToggle;
    private Context mContext = this;
    public Utils utils;
    private long mBackPressed;
    public ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;
    private FragmentManager fManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.drawable.newer_bg);
        initToolbar();
        setupParseInstallation();
        initViews();
    }

    private void initViews() {
        utils = new Utils(mContext);
        initNavItems();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(4);
        if (viewPager != null) {
            setupViewPager();
        }

        fManager = getSupportFragmentManager();
        tabLayout = (TabLayout) findViewById(R.id.pTabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_profile);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_offers);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_btn_newthread);
        tabLayout.addOnTabSelectedListener(customTabSelecterListener);
    }

    private void initNavItems() {
        ImageView profilePicture;
        TextView home, workshop, profile, offers, forums, about, logOut, name, silverCoins, goldCoins;
        home = (TextView) findViewById(R.id.nav_home);
        home.setOnClickListener(customClickListener);
        workshop = (TextView) findViewById(R.id.nav_workshop);
        workshop.setOnClickListener(customClickListener);
        profile = (TextView) findViewById(R.id.nav_profile);
        profile.setOnClickListener(customClickListener);
        offers = (TextView) findViewById(R.id.nav_offers);
        offers.setOnClickListener(customClickListener);
        forums = (TextView) findViewById(R.id.nav_forums);
        forums.setOnClickListener(customClickListener);
        about = (TextView) findViewById(R.id.nav_about);
        about.setOnClickListener(customClickListener);
        logOut = (TextView) findViewById(R.id.nav_logout);
        logOut.setOnClickListener(customClickListener);

        name = (TextView) findViewById(R.id.side_menu_user_username);
        silverCoins = (TextView) findViewById(R.id.side_menu_user_points_silver);
        goldCoins = (TextView) findViewById(R.id.side_menu_user_point_gold);
        profilePicture = (ImageView) findViewById(R.id.side_menu_profile_avatar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.dashboard_drawer_layout);
        mDrawerToggle = new DrawerToggleListener(mContext, this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close, utils, profilePicture, name, silverCoins, goldCoins);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private Stack<Integer> stack = new Stack<>();
    private TabLayout.OnTabSelectedListener customTabSelecterListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            int tabPosition = tab.getPosition();
            if (tabPosition == 0) {
                setDrawerIndicatorEnabled(true);
            } else {
                setDrawerIndicatorEnabled(false);
            }

            viewPager.setCurrentItem(tab.getPosition());
            if (stack.empty()) {
                stack.push(0);
            }

            if (stack.contains(tabPosition)) {
                stack.remove(stack.indexOf(tabPosition));
                stack.push(tabPosition);
            } else {
                stack.push(tabPosition);
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            if (tab.getPosition() == 0) {
                setDrawerIndicatorEnabled(true);
            } else {
                setDrawerIndicatorEnabled(false);
            }
        }
    };

    private View.OnClickListener customClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.nav_home:
                    viewPager.setCurrentItem(0);
                    mDrawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.nav_workshop:
                    Intent workshopIntent = new Intent(MainActivity.this, ModelWorkshopActivity.class);
                    startActivity(workshopIntent);
                    mDrawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.nav_profile:
                    viewPager.setCurrentItem(1);
                    mDrawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.nav_offers:
                    viewPager.setCurrentItem(2);
                    mDrawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.nav_forums:
                    viewPager.setCurrentItem(3);
                    mDrawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.nav_about:
                    Utils.openFragment(AboutFragment.class.getSimpleName(), null, getSupportFragmentManager(), EMPTY, EMPTY, null);
                    mDrawerLayout.closeDrawer(Gravity.START);
                    break;
                case R.id.nav_logout:
                    mDrawerLayout.closeDrawer(Gravity.START);
                    utils.showStandardDialog(mContext, getString(R.string.logout), getString(R.string.logout_alert_dialog_message), getString(R.string.logout),
                            getString(R.string.cancel), true, true, LOGOUT_ALERT_ID, MainActivity.this);
                    break;
            }
        }
    };

    ArrayList<Messages> messagesList;
    private void showMessagesDialog() {
        mDrawerLayout.closeDrawer(Gravity.START);
        messagesList = new ArrayList<>();
        final Dialog dialog = new Dialog(this, R.style.customAlertDialog);
        dialog.setCancelable(true);
        dialog.requestWindowFeature(FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_recycler_view);
        updateMessages(dialog, true);
    }

    private void updateMessages(final Dialog dialog, final boolean firstTime) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery(MESSAGES_CLASS_KEY);
        query.orderByAscending(MESSAGES_IS_READ_KEY);
        query.whereEqualTo(MESSAGES_RECEIVING_USER_KEY, ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                TextView dialogTitle = dialog.findViewById(R.id.dialog_title);
                dialogTitle.setText(getString(R.string.inbox));
                int messageCount = 0;
                try {
                    for (ParseObject j : objects) {
                        if (!j.getBoolean(MESSAGES_IS_READ_KEY)) {
                            messageCount++;
                        }
                        Messages message = new Messages();
                        message.setObjectId(j.getObjectId());
                        message.setSenderName(j.getParseObject(MESSAGES_SENDING_USER_KEY).fetchIfNeeded().getString(USER_FULL_NAME_KEY));
                        message.setProfileURL(j.getParseObject(MESSAGES_SENDING_USER_KEY).fetchIfNeeded().getString(USER_PROFILE_PICTURE_KEY));
                        message.setMessage(j.getString(MESSAGES_MESSAGE_KEY));
                        message.setRead(j.getBoolean(MESSAGES_IS_READ_KEY));
                        messagesList.add(message);
                    }
                } catch (ParseException e1) {
                    e.getLocalizedMessage();
                }
                dialogTitle.setText(getString(R.string.inbox) + " (" + messageCount + ")");

                final ViewAnimator message_va = dialog.findViewById(R.id.messages_va);
                Animation inAnim = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
                Animation outAnim = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_out_right);
                message_va.setInAnimation(inAnim);
                message_va.setOutAnimation(outAnim);

                final TextView senderName = dialog.findViewById(R.id.sender_name);
                final TextView senderMessage = dialog.findViewById(R.id.sender_message);
                final ImageView senderImage = dialog.findViewById(R.id.sender_image);
                TextView cancelReplyButton = dialog.findViewById(R.id.cancel_reply_button);
                cancelReplyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        messagesList.clear();
                        updateMessages(dialog, false);
                    }
                });

                RecyclerView messagesRecyclerView = dialog.findViewById(R.id.dialog_recycler_view);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                messagesRecyclerView.setLayoutManager(mLayoutManager);
                final RecyclerViewAdapterMessages adapter = new RecyclerViewAdapterMessages(mContext, messagesList);
                messagesRecyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(new RecyclerViewAdapterMessages.onMessageItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, final int position, final Messages message) {
                        message_va.setDisplayedChild(1);
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(MESSAGES_CLASS_KEY);
                        query.whereEqualTo("objectId", message.getObjectId());
                        query.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(final List<ParseObject> objects, ParseException e) {
                                if (e == null) {
                                    if (message.getProfileURL().equals(NO_PICTURE)) {
                                        Picasso.with(mContext).load(R.drawable.no_profile).transform(new CircleTransform()).into(senderImage);
                                    } else {
                                        Picasso.with(mContext).load(message.getProfileURL()).fit().centerCrop().transform(new CircleTransform()).into(senderImage);
                                    }
                                    objects.get(0).put(MESSAGES_IS_READ_KEY, true);
                                    objects.get(0).saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            messagesList.get(position).setRead(true);
                                            adapter.updateList(messagesList);
                                        }
                                    });
                                    senderName.setText(message.getSenderName());
                                    senderMessage.setText(message.getMessage());
                                    TextView replyButton = dialog.findViewById(R.id.send_message_button);
                                    replyButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            try {
                                                Utils.showSendMessageDialog(mContext, (ParseUser) objects.get(0).getParseObject(MESSAGES_SENDING_USER_KEY));
                                            } catch (ParseException e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    });
                                } else {
                                    Log.e("Failed", "Failed" + e.getLocalizedMessage());
                                }
                            }
                        });
                    }
                });
                adapter.notifyDataSetChanged();
                if (firstTime) {
                    dialog.show();
                } else {
                    message_va.setDisplayedChild(0);
                }
            }
        });
    }

    private void initToolbar() {
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayShowTitleEnabled(false);
        setDrawerIndicatorEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setElevation(0);
        ab.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(mContext, R.color.silver)));
        ab.setCustomView(R.layout.toolbar_layout);
        ab.setDisplayShowCustomEnabled(true);
        invalidateOptionsMenu();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        initToolbar();
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                if (fManager.getBackStackEntryCount() != 0 || getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    fManager.popBackStack();
                    getSupportFragmentManager().popBackStack();
                    setDrawerIndicatorEnabled(true);
                } else {
                    if (stack.size() > 1) {
                        stack.pop();
                        viewPager.setCurrentItem(stack.lastElement());
                    }
                }
                break;
            case R.id.about:
                Utils.openFragment(AboutFragment.class.getSimpleName(), null, getSupportFragmentManager(), EMPTY, EMPTY, null);
                break;
            case R.id.logout:
                utils.showStandardDialog(this, getString(R.string.logout), getString(R.string.logout_alert_dialog_message), getString(R.string.logout),
                        getString(R.string.cancel), true, true, LOGOUT_ALERT_ID, MainActivity.this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (!mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                if (viewPager.getCurrentItem() != 0) {
                    if (stack.size() > 1) {
                        stack.pop();
                        viewPager.setCurrentItem(stack.lastElement());
                    }
                } else {
                    if (mBackPressed + 2000 > System.currentTimeMillis()) {
                        super.onBackPressed();
                        return;
                    } else {
                        utils.makeText("Press back again to exit app.", LENGTH_LONG);
                    }
                    mBackPressed = System.currentTimeMillis();
                }
                mBackPressed = System.currentTimeMillis();
            } else {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // get current fragment in container
        Fragment fragment = adapter.getActiveFragment(viewPager.getCurrentItem());
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public void setDrawerIndicatorEnabled(boolean drawerIndicatorEnabled) {
        if (mDrawerToggle != null) {
            mDrawerToggle.setDrawerIndicatorEnabled(drawerIndicatorEnabled);
        }
    }

    public void setupViewPager() {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(HomeFragment.newInstance(0));
        adapter.addFrag(ProfileFragment.newInstance(1));
        adapter.addFrag(OffersFragment.newInstance(2));
        adapter.addFrag(ForumsFragment.newInstance(3));
        viewPager.setAdapter(adapter);
    }
    private void setupParseInstallation() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        // installation.put("email", email);
        installation.put("GCMSenderId", Constants.GCM_SENDER_ID);
        installation.saveInBackground();
    }

}
