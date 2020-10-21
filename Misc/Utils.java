package icn.icmyas.Misc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import icn.icmyas.Fragments.AboutFragment;
import icn.icmyas.Fragments.EpisodeFragment;
import icn.icmyas.Fragments.IMPHomeFragment;
import icn.icmyas.Fragments.IMPProfileFragment;
import icn.icmyas.Fragments.IMPRankingFragment;
import icn.icmyas.Fragments.IMPVotingFragment;
import icn.icmyas.Fragments.ListArticlesFragment;
import icn.icmyas.Fragments.ModelGuideFragment;
import icn.icmyas.Fragments.ProfileFragment;
import icn.icmyas.Fragments.TermsConditionsFragment;
import icn.icmyas.Fragments.ViewArticleFragment;
import icn.icmyas.LoginActivity;
import icn.icmyas.Models.EpisodeOrEntries;
import icn.icmyas.R;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.widget.Toast.LENGTH_LONG;
import static icn.icmyas.Misc.Constants.EMAIL;
import static icn.icmyas.Misc.Constants.LOGOUT_ALERT_ID;
import static icn.icmyas.Misc.Constants.RESET_PASSWORD;
import static icn.icmyas.Misc.Constants.USERNAME;
import static icn.icmyas.Misc.Constants.USERNAME_INPUT;
import static icn.icmyas.Misc.Constants.USER_CLASS_KEY;
import static icn.icmyas.Misc.Constants.USER_FULL_NAME_KEY;
import static icn.icmyas.Misc.Constants.USER_USERNAME_KEY;
import static icn.icmyas.Misc.Validate.isValid;

/**
 * Author:  Bradley Wilson
 * Date: 14/07/2017
 * Package: icn.icmyas.Misc
 * Project Name: ICMYAS
 */

public class Utils {

    private Activity activity;
    private Context context;
    private AlertDialog dialog;

    public Utils(Object activity) {
        if (activity instanceof Activity) {
            this.activity = (Activity) activity;
        } else {
            this.activity = null;
        }
    }

     public void hideKeyboard(View view, Context mContext) {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void makeText(String text, int length) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout_root));

        TextView description = (TextView) layout.findViewById(R.id.toastMessage);
        description.setText(text);

        Toast toast = new Toast(activity.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 50);
        toast.setDuration(length);
        toast.setView(layout);
        toast.show();
    }

    public void showInputDialog(boolean username, Context mContext, Activity activity, Utils utils) {
        String type;
        if (username) {
            type = USERNAME;
        } else {
            type = RESET_PASSWORD;
        }
        this.activity = activity;
        this.context = mContext;
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext, R.style.customAlertDialog);
        TextInputLayout til = new TextInputLayout(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int px = convertDpToPixel(25, mContext);
        lp.setMargins(px, 0, px, 0);
        til.setLayoutParams(lp);

        TextInputEditText et = new TextInputEditText(mContext);
        et.setLayoutParams(lp);
        px = convertDpToPixel(7, mContext);
        et.setPadding(et.getPaddingLeft(), et.getPaddingTop() + px, et.getPaddingRight(), et.getPaddingBottom());
        et.setBackground(ContextCompat.getDrawable(mContext, R.drawable.text_et_bg));
        et.setHintTextColor(ContextCompat.getColor(context, R.color.red));
        et.setMaxLines(1);
        et.setTextColor(ContextCompat.getColor(context, R.color.red));
        et.setHint(getInputHint(username));
        et.setInputType(getInputType(username));
        til.addView(et);

        alert.setMessage(getAlertMessage(type));
        alert.setTitle(getAlertTitle(type));
        alert.setView(til);
        if (username) {
            alert.setCancelable(false);
        }
        alert.setPositiveButton(getPositiveButton(type), null);
        showAlert(alert, et, type, mContext);
    }

    private int convertDpToPixel(int dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private int getPositiveButton(String type) {
        switch (type) {
            case USERNAME:
                return R.string.ok;
            case RESET_PASSWORD:
                return R.string.Reset;
            default:
                return R.string.ok;
        }
    }

    private int getAlertTitle(String type) {
        switch (type) {
            case USERNAME:
                return R.string.hint_username;
            case RESET_PASSWORD:
                return R.string.reset_password;
            default:
                return R.string.error;
        }
    }

    private int getAlertMessage(String type) {
        switch (type) {
            case USERNAME:
                return R.string.enter_username;
            case RESET_PASSWORD:
                return R.string.enter_signed_up_email;
            default:
                return R.string.alert_dialog_error;
        }
    }

    private int getInputType(boolean username) {
        if (!username) {
            return InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        } else {
            return InputType.TYPE_CLASS_TEXT;
        }
    }

    private int getInputHint(boolean username) {
        if (!username) {
            return R.string.hint_email;
        } else {
            return R.string.hint_username;
        }
    }

    private void showAlert(final AlertDialog.Builder alert, final TextInputEditText et, final String type, final Context context) {
        dialog = alert.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog1) {
                Button reset = dialog.getButton(BUTTON_POSITIVE);
                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String inputText;
                        switch (type) {
                            case USERNAME:
                                inputText = et.getText().toString();
                                if (inputText.isEmpty()) {
                                    et.setError(context.getString(R.string.empty_err_msg_username));
                                } else if (!isValid(USERNAME_INPUT, et.getText().toString())) {
                                    et.setError(context.getString(R.string.invalid_username));
                                } else {
                                    ParseQuery<ParseObject> query = ParseQuery.getQuery(USER_CLASS_KEY);
                                    try {
                                        boolean isUsername = query.whereEqualTo(USER_USERNAME_KEY, inputText).find().size() > 0;
                                        if (!isUsername) {
                                            dialog.dismiss();
                                            SashidoHelper.addUsername(inputText);
                                            SashidoHelper.goToDashboard(activity);
                                            SashidoHelper.createUserDetails(ParseUser.getCurrentUser());
                                        } else {
                                            et.setError(context.getResources().getString(R.string.username_exists));
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            case RESET_PASSWORD:
                                inputText = et.getText().toString();
                                if (inputText.isEmpty()) {
                                    et.setError(context.getString(R.string.empty_err_msg_email));
                                } else if (!isValid(EMAIL, et.getText().toString())) {
                                    et.setError(context.getString(R.string.invalid_email));
                                } else {
                                    ParseUser.requestPasswordResetInBackground(inputText, new RequestPasswordResetCallback() {
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                makeText(context.getString(R.string.email_sent), LENGTH_LONG);
                                                dialog.dismiss();
                                            } else {
                                                et.setError(context.getString(R.string.email_doesnt_exist));
                                            }
                                        }
                                    });
                                }
                                break;
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    public void requestFocus(View view, Activity mContext) {
        if (view.requestFocus()) {
            mContext.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public void showStandardDialog(Context mContext, String dialogTitle, String dialogMessage, String pButtonTitle, String nButtonTitle, boolean positiveButton, boolean negativeButton, int actionID, Activity activity) {
        this.actionID = actionID;
        this.activity = activity;
        dialog = new AlertDialog.Builder(mContext, R.style.customAlertDialog).create();
        dialog.setTitle(dialogTitle);
        dialog.setMessage(dialogMessage);
        dialog.setCanceledOnTouchOutside(false);
        if (positiveButton) {
            dialog.setButton(BUTTON_POSITIVE, pButtonTitle, customDialogListener);
        }
        if (negativeButton) {
            dialog.setButton(BUTTON_NEGATIVE, nButtonTitle, customDialogListener);
        }
        dialog.show();
    }

    private int actionID;
    private DialogInterface.OnClickListener customDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int which) {
            switch (which) {
                case BUTTON_POSITIVE:
                    switch (actionID) {
                        case LOGOUT_ALERT_ID:
                            SashidoHelper.logOut();
                            Intent i = new Intent(activity, LoginActivity.class);
                            activity.startActivity(i);
                            activity.finish();
                            break;
                    }
                    break;
                case BUTTON_NEGATIVE:
                    switch (actionID) {
                        case LOGOUT_ALERT_ID:
                            default:
                            dialogInterface.dismiss();
                            break;
                    }
                    break;
            }
        }
    };

    public static float convertDpToPixels(int dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String getDateAndTime(Date createdAt) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy kk:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdAt);
        return df.format(calendar.getTime());
    }

    public static ArrayList<String> convertJSONtoArrayList(JSONArray jsonArray) {
        ArrayList<String> tempList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tempList.add(jsonArray.optString(i));
        }
        return tempList;
    }

    public static void openFragment(String fragment, ArrayList<EpisodeOrEntries> episodesList, FragmentManager manager, String bundleKey, String objectID, Bundle args) {
        FragmentTransaction trans;
        Fragment frag = null;
        Bundle arguments = (args == null) ? new Bundle() : args;
        switch (fragment) {
            case "EpisodeFragment":
                frag = new EpisodeFragment();
                arguments.putSerializable(Constants.EPISODE_LIST_BUNDLE_KEY, episodesList);
                arguments.putString(Constants.DETERMINE_LIST, bundleKey);
                frag.setArguments(arguments);
                break;
            case "AboutFragment":
                frag = new AboutFragment();
                break;
            case "ProfileFragment":
                frag = new ProfileFragment();
                arguments.putBoolean(Constants.PROFILE_IS_USER, false);
                arguments.putString(Constants.PROFILE_USER_OBJECT_ID, objectID);
                frag.setArguments(arguments);
                break;
            case "ModelGuideFragment":
                frag = new ModelGuideFragment();
                break;
            case "TermsConditionsFragment":
                frag = new TermsConditionsFragment();
                break;
            case "IMPHomeFragment":
                frag = new IMPHomeFragment();
                break;
        }
        trans = manager.beginTransaction();
        trans.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right);
        trans.addToBackStack(Constants.MISC_FRAGMENT_TAG);
        trans.replace(R.id.dashboard_fragment_container, frag);
        trans.commit();
    }

    public static void overlayFragment(String fragment, FragmentManager manager, Bundle args) {
        FragmentTransaction trans;
        Fragment frag = null;
        switch (fragment) {
            case "ViewArticleFragment":
                frag = new ViewArticleFragment();
                frag.setArguments(args);
                break;
            case "ListArticlesFragment":
                frag = new ListArticlesFragment();
                frag.setArguments(args);
                break;
            case "IMPProfileFragment":
                frag = new IMPProfileFragment();
                break;
            case "IMPVotingFragment":
                frag = new IMPVotingFragment();
                break;
            case "IMPRankingFragment":
                frag = new IMPRankingFragment();
                frag.setArguments(args);
                break;
        }
        trans = manager.beginTransaction();
        trans.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right);
        trans.addToBackStack(Constants.MISC_FRAGMENT_TAG);
        trans.add(R.id.dashboard_fragment_container, frag);
        trans.commit();
    }

    public static void showSendMessageDialog(final Context context, final ParseUser user) throws ParseException {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_post);
        final EditText postTitle = dialog.findViewById(R.id.input_title);
        postTitle.setText(user.fetch().getString(USER_FULL_NAME_KEY));
        postTitle.setEnabled(false);
        final EditText postMessage = dialog.findViewById(R.id.input_message);
        final TextView cancelButton = dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        final TextView postButton = dialog.findViewById(R.id.post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseObject message = new ParseObject(Constants.MESSAGES_CLASS_KEY);
                message.put(Constants.MESSAGES_SENDING_USER_KEY, ParseUser.getCurrentUser());
                message.put(Constants.MESSAGES_RECEIVING_USER_KEY, user);
                message.put(Constants.MESSAGES_IS_READ_KEY, false);
                message.put(Constants.MESSAGES_MESSAGE_KEY, postMessage.getText().toString().trim());
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(context, "Message Sent.", Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                        } else {
                            Log.e("failed", "failed" + e.getLocalizedMessage());
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    public boolean showAppInviteDialog(Context context, Fragment fragment) {
        String appLinkUrl = "https://fb.me/1948325375425829";
        // TODO get a new preview image
        // String previewImageUrl = "http://i.imgur.com/b5NHiLU.png";
        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
                    .build();
            AppInviteDialog.show(fragment, content);
            return true;
        } else {
            Toast.makeText(context, "Please ensure you are logged in to Facebook.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
