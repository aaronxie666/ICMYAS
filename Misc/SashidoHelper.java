package icn.icmyas.Misc;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import icn.icmyas.MainActivity;
import icn.icmyas.R;

import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static icn.icmyas.Misc.Constants.EMAIL;
import static icn.icmyas.Misc.Constants.EMPTY;
import static icn.icmyas.Misc.Constants.NO_PICTURE;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_CAMERA_ARRAY_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_CHEST_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_CLASS_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_DRESS_SIZE_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_GALLERY_ARRAY_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_HAIR_COLOUR_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_HEIGHT_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_HIPS_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_USER_POINTER_KEY;
import static icn.icmyas.Misc.Constants.PROFILE_DETAILS_WAIST_KEY;
import static icn.icmyas.Misc.Constants.USER_FACEBOOK_ID_KEY;
import static icn.icmyas.Misc.Constants.USER_FRIENDS_KEY;
import static icn.icmyas.Misc.Constants.USER_FULL_NAME_KEY;
import static icn.icmyas.Misc.Constants.USER_GOLD_COINS_KEY;
import static icn.icmyas.Misc.Constants.USER_HAS_SHARED_KEY;
import static icn.icmyas.Misc.Constants.USER_LOGIN_METHOD_KEY;
import static icn.icmyas.Misc.Constants.USER_PROFILE_COMPLETED_KEY;
import static icn.icmyas.Misc.Constants.USER_PROFILE_PICTURE_KEY;
import static icn.icmyas.Misc.Constants.USER_SILVER_COINS_KEY;
import static icn.icmyas.Misc.Constants.USER_UNIQUE_ID_KEY;
import static icn.icmyas.Misc.Constants.USER_VOTES_KEY;

/**
 * Author:  Bradley Wilson
 * Date: 14/07/2017
 * Package: icn.icmyas.Misc
 * Project Name: ICMYAS
 */

public class SashidoHelper {

    private static String TAG = SashidoHelper.class.getSimpleName();
    private static Utils utils;

    public static boolean isLogged() {
        return ParseUser.getCurrentUser() != null;
    }

    public static void logIn(final Context mContext, final Activity activity, String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    Log.e(TAG, "Logging in");
                    goToDashboard(activity);
                } else {
                    Log.e("failed", "failed" + e.getMessage());
                    showErrorDialog(mContext, e);
                }
            }
        });
    }

    public static void register(final Context mContext, final Activity activity, String rName, String rUsername, String rConfirmEmail, String rConfirmPassword) {
        final ParseUser user = new ParseUser();
        user.put(USER_FULL_NAME_KEY, rName);
        user.put(USER_LOGIN_METHOD_KEY, EMAIL);
        user.put(USER_GOLD_COINS_KEY, 0);
        user.put(USER_SILVER_COINS_KEY, 500);
        user.put(USER_VOTES_KEY, 0);
        user.put(USER_UNIQUE_ID_KEY, rConfirmEmail);
        user.put(USER_PROFILE_COMPLETED_KEY, false);
        user.put(USER_PROFILE_PICTURE_KEY, NO_PICTURE);
        user.put(USER_HAS_SHARED_KEY, false);
        user.setUsername(rUsername);
        user.setEmail(rConfirmEmail);
        user.setPassword(rConfirmPassword);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    goToDashboard(activity);
                    createUserDetails(user);
                } else {
                    showErrorDialog(mContext, e);
                }
            }
        });
    }

    private static DialogInterface.OnClickListener customDialogInterfaceListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int buttonID) {
            switch (buttonID) {
                case BUTTON_NEUTRAL:
                    dialogInterface.dismiss();
                    break;
            }
        }
    };

    public static void showErrorDialog(Context mContext, ParseException e) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle(mContext.getString(R.string.something_went_wrong));
        alertDialog.setMessage(determineError(mContext, e));
        alertDialog.setButton(BUTTON_NEUTRAL, mContext.getString(R.string.ok), customDialogInterfaceListener);
        alertDialog.show();
    }

    private static String determineError(Context mContext, ParseException e) {
        switch (e.getCode()) {
            case ParseException.USERNAME_TAKEN:
                return mContext.getString(R.string.username_taken_err);
            case ParseException.OBJECT_NOT_FOUND:
                return mContext.getString(R.string.invalid_creds_err);
            case ParseException.CONNECTION_FAILED:
                return mContext.getString(R.string.connection_needed_err);
            case ParseException.EMAIL_NOT_FOUND:
                return mContext.getString(R.string.empty_err_msg_email);
            case ParseException.EMAIL_TAKEN:
                return mContext.getString(R.string.email_taken_err);
            default:
                return mContext.getString(R.string.technical_err_msg);
        }
    }

    public static void logOut() {
        ParseUser.logOutInBackground();
        LoginManager.getInstance().logOut();
        // SignOut();
    }

    /*private static void googleSignOut() {
        mGoogleApiClient.connect();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // success
                    }
                });
    }*/

    public static void goToDashboard(Activity activity) {
        if (utils == null) {
            utils = new Utils(activity);
        }
        utils.makeText("Logging in...", Toast.LENGTH_SHORT);
        Intent i = new Intent(activity, MainActivity.class);
        activity.startActivity(i);
        activity.finish();
    }

    public static void createUserDetails(ParseUser user){
        try {
            JSONArray empty = new JSONArray();
            empty.put(0, EMPTY);
            empty.put(1, EMPTY);
            empty.put(2, EMPTY);
            empty.put(3, EMPTY);
            ParseObject userDetails = new ParseObject(PROFILE_DETAILS_CLASS_KEY);
            userDetails.put(PROFILE_DETAILS_CHEST_KEY, EMPTY);
            userDetails.put(PROFILE_DETAILS_HEIGHT_KEY, EMPTY);
            userDetails.put(PROFILE_DETAILS_HIPS_KEY, EMPTY);
            userDetails.put(PROFILE_DETAILS_WAIST_KEY, EMPTY);
            userDetails.put(PROFILE_DETAILS_DRESS_SIZE_KEY, EMPTY);
            userDetails.put(PROFILE_DETAILS_HAIR_COLOUR_KEY, EMPTY);
            userDetails.put(PROFILE_DETAILS_CAMERA_ARRAY_KEY, empty);
            userDetails.put(PROFILE_DETAILS_GALLERY_ARRAY_KEY, empty);
            userDetails.put(PROFILE_DETAILS_USER_POINTER_KEY, user);
            userDetails.saveInBackground();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentName() {
        return ParseUser.getCurrentUser().getString("name");
    }

    public static void setProfilePicture(Context mContext, ImageView profilePicture) {
        Picasso.with(mContext).load(getCurrentProfilePicture()).placeholder(R.mipmap.ic_circle).transform(new CircleTransform()).into(profilePicture);
    }

    public static String getCurrentProfilePicture() {
        return ParseUser.getCurrentUser().getString(USER_PROFILE_PICTURE_KEY);
    }

    public static String getCurrentLoginMethod() {
        return ParseUser.getCurrentUser().getString(USER_LOGIN_METHOD_KEY);
    }

    public static String getCurrentFacebookID() {
        return ParseUser.getCurrentUser().getString(USER_FACEBOOK_ID_KEY);
    }

    public static JSONArray getCurrentFriends() {
        return ParseUser.getCurrentUser().getJSONArray(USER_FRIENDS_KEY);
    }

    static void addUsername(String username) {
        ParseUser.getCurrentUser().setUsername(username);
        ParseUser.getCurrentUser().saveInBackground();
    }

    public static void populateProfileDetails(Context context, ImageView profilePicture, TextView name, TextView silverCoins, TextView goldCoins) {
        String profilePictureURL = ParseUser.getCurrentUser().getString(USER_PROFILE_PICTURE_KEY);
        if (profilePictureURL.equals(NO_PICTURE)) {
            Picasso.with(context).load(R.drawable.no_profile).transform(new CircleTransform()).into(profilePicture);
        } else {
            Picasso.with(context).load(profilePictureURL).fit().centerCrop().transform(new CircleTransform()).into(profilePicture);
        }
        name.setText(ParseUser.getCurrentUser().getString(USER_FULL_NAME_KEY));
        silverCoins.setText(String.valueOf(ParseUser.getCurrentUser().getInt(USER_SILVER_COINS_KEY)));
        goldCoins.setText(String.valueOf(ParseUser.getCurrentUser().getInt(USER_GOLD_COINS_KEY)));
    }

    public static void updateNotifications(TextView notificationsBadge) {
        try {
            notificationsBadge.setText(String.valueOf(ParseQuery.getQuery(Constants.MESSAGES_CLASS_KEY).whereEqualTo(Constants.MESSAGES_RECEIVING_USER_KEY, ParseUser.getCurrentUser()).whereEqualTo(Constants.MESSAGES_IS_READ_KEY, false).find().size()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void giveStars(int number, boolean isGold) {
        String column = isGold ? Constants.USER_GOLD_COINS_KEY : Constants.USER_SILVER_COINS_KEY;
        int total = ParseUser.getCurrentUser().getInt(column) + number;
        ParseUser.getCurrentUser().put(column, total);
        try {
            ParseUser.getCurrentUser().save();
        } catch (ParseException e) {
            Log.e("debug", "failed to give stars: " + e.getLocalizedMessage());
        }
    }
}
