package icn.icmyas.Listeners;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import icn.icmyas.Misc.SashidoHelper;
import icn.icmyas.Misc.Utils;
import icn.icmyas.R;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.Listeners
 * Project Name: ICMYAS
 */

public class DrawerToggleListener extends ActionBarDrawerToggle {

    private Activity activity;
    private Context context;
    private Utils utils;
    private DrawerLayout mDrawerLayout;
    private static final String TAG = DrawerToggleListener.class.getSimpleName();
    private ImageView profilePicture;
    private TextView name, silverCoins, goldCoins, notificationsBadge;

    public DrawerToggleListener(Context context, Activity activity, DrawerLayout drawerLayout, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes, Utils utils,
                                ImageView profilePicture, TextView name, TextView silverCoins, TextView goldCoins) {
        super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        this.context = context;
        this.activity = activity;
        this.utils = utils;
        this.mDrawerLayout = drawerLayout;
        this.profilePicture = profilePicture;
        this.silverCoins = silverCoins;
        this.name = name;
        this.goldCoins = goldCoins;
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        Log.e(TAG, "Drawer Open");
        activity.invalidateOptionsMenu();
        // SashidoHelper.updateNotifications(notificationsBadge);
        SashidoHelper.populateProfileDetails(context, profilePicture, name, silverCoins, goldCoins);
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        super.onDrawerClosed(drawerView);
        Log.e(TAG, "Drawer Closed");
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        super.onDrawerStateChanged(newState);
        utils.hideKeyboard(mDrawerLayout, activity);
    }
}
