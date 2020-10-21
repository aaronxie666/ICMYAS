package icn.icmyas;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.SashidoHelper;
import icn.icmyas.Misc.Utils;
import icn.icmyas.Widgets.CustomTextView;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static icn.icmyas.Misc.Constants.EMAIL;
import static icn.icmyas.Misc.Constants.EMPTY;
import static icn.icmyas.Misc.Constants.FB_EMAIL;
import static icn.icmyas.Misc.Constants.FB_PUBLIC_PROFILE;
import static icn.icmyas.Misc.Constants.FB_USER_BIRTHDAY;
import static icn.icmyas.Misc.Constants.LOGIN_VA_CHILD_ONE;
import static icn.icmyas.Misc.Constants.LOGIN_VA_CHILD_THREE;
import static icn.icmyas.Misc.Constants.LOGIN_VA_CHILD_TWO;
import static icn.icmyas.Misc.Constants.NAME;
import static icn.icmyas.Misc.Constants.PASSWORD;
import static icn.icmyas.Misc.Constants.USERNAME;
import static icn.icmyas.Misc.Constants.USERNAME_INPUT;
import static icn.icmyas.Misc.Constants.USER_FACEBOOK_ID_KEY;
import static icn.icmyas.Misc.Constants.USER_FULL_NAME_KEY;
import static icn.icmyas.Misc.Constants.USER_GOLD_COINS_KEY;
import static icn.icmyas.Misc.Constants.USER_GOOGLE_ID_KEY;
import static icn.icmyas.Misc.Constants.USER_HAS_SHARED_KEY;
import static icn.icmyas.Misc.Constants.USER_INSTA_ID_KEY;
import static icn.icmyas.Misc.Constants.USER_LOGIN_METHOD_KEY;
import static icn.icmyas.Misc.Constants.USER_PROFILE_COMPLETED_KEY;
import static icn.icmyas.Misc.Constants.USER_PROFILE_PICTURE_KEY;
import static icn.icmyas.Misc.Constants.USER_SILVER_COINS_KEY;
import static icn.icmyas.Misc.Constants.USER_UNIQUE_ID_KEY;
import static icn.icmyas.Misc.Constants.USER_VOTES_KEY;
import static icn.icmyas.Misc.Validate.isValid;

public class LoginActivity extends AppCompatActivity {

    private Context mContext = this;
    private ViewAnimator login_va;
    private EditText inputUsername, inputPassword, inputRegisterName, inputRegisterUsername, inputRegisterEmail,
            inputRegisterConfirmEmail, inputRegisterPassword, inputRegisterConfirmPassword;
    private long mBackPressed;
    private TextInputLayout inputLayoutUsername, inputLayoutPassword, inputRegisterLayoutName, inputRegisterLayoutUsername,
            inputRegisterLayoutEmail, inputRegisterLayoutConfirmEmail, inputRegisterLayoutPassword, inputRegisterLayoutConfirmPassword;
    private Utils utils;
    private WebView webView;
    private final String TAG = LoginActivity.class.getSimpleName();

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.newer_bg);
        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews() {
        utils = new Utils(this);

        //login page view animator init + assignment of animations
        login_va = (ViewAnimator) findViewById(R.id.activity_login_view_animator);
        Animation inAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation outAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        login_va.setInAnimation(inAnim);
        login_va.setOutAnimation(outAnim);

        TextView loginEmailButton, registerEmailButton, actuallyRegisterButton, actuallyLoginButton, facebookButton, instagramButton, googleButton;
        loginEmailButton = (TextView) findViewById(R.id.activity_login_login_button);
        registerEmailButton = (TextView) findViewById(R.id.activity_login_register_button);
        loginEmailButton.setOnClickListener(customListener);
        registerEmailButton.setOnClickListener(customListener);
        actuallyLoginButton = (TextView) findViewById(R.id.activity_login_email_button_child);
        actuallyLoginButton.setOnClickListener(customListener);
        actuallyRegisterButton = (TextView) findViewById(R.id.activity_register_email_button_child);
        actuallyRegisterButton.setOnClickListener(customListener);
        facebookButton = (TextView) findViewById(R.id.activity_login_facebook_button);
        facebookButton.setOnClickListener(customListener);
//        instagramButton = (TextView) findViewById(R.id.activity_login_instagram_button);
//        instagramButton.setOnClickListener(customListener);
//        googleButton = (TextView) findViewById(R.id.activity_login_google_button);
//        googleButton.setOnClickListener(customListener);

        //all Textinputlayouts and edit texts init
        inputLayoutUsername = (TextInputLayout) findViewById(R.id.login_email_username);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.login_email_password);
        inputRegisterLayoutName = (TextInputLayout)findViewById(R.id.register_email_name);
        inputRegisterLayoutUsername = (TextInputLayout)findViewById(R.id.register_email_username);
        inputRegisterLayoutEmail = (TextInputLayout)findViewById(R.id.register_email_email);
        inputRegisterLayoutConfirmEmail = (TextInputLayout)findViewById(R.id.register_email_confirm_email);
        inputRegisterLayoutPassword = (TextInputLayout)findViewById(R.id.register_email_password);
        inputRegisterLayoutConfirmPassword = (TextInputLayout)findViewById(R.id.register_email_confirm_password);

        inputUsername = (EditText) findViewById(R.id.login_input_username);
        inputUsername.setOnFocusChangeListener(customFocusChangeListener);
        inputPassword = (EditText) findViewById(R.id.login_input_password);
        inputPassword.setOnFocusChangeListener(customFocusChangeListener);
        inputRegisterName = (EditText) findViewById(R.id.register_input_name);
        inputRegisterName.setOnFocusChangeListener(customFocusChangeListener);
        inputRegisterUsername = (EditText) findViewById(R.id.register_input_username);
        inputRegisterUsername.setOnFocusChangeListener(customFocusChangeListener);
        inputRegisterEmail = (EditText) findViewById(R.id.register_input_email);
        inputRegisterEmail.setOnFocusChangeListener(customFocusChangeListener);
        inputRegisterConfirmEmail = (EditText) findViewById(R.id.register_input_confirm_email);
        inputRegisterConfirmEmail.setOnFocusChangeListener(customFocusChangeListener);
        inputRegisterPassword = (EditText) findViewById(R.id.register_input_password);
        inputRegisterPassword.setOnFocusChangeListener(customFocusChangeListener);
        inputRegisterConfirmPassword = (EditText) findViewById(R.id.register_input_confirm_password);
        inputRegisterConfirmPassword.setOnFocusChangeListener(customFocusChangeListener);

        CustomTextView forgotPassword = (CustomTextView) findViewById(R.id.activity_login_email_forgot_password);
        forgotPassword.setOnClickListener(customListener);

        webView = (WebView) findViewById(R.id.web_view_insta);
        goToActivity();
    }

    private final List<String> pPermissions = Arrays.asList(FB_PUBLIC_PROFILE, FB_EMAIL, FB_USER_BIRTHDAY);
    private View.OnClickListener customListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.activity_login_facebook_button:
                    Log.e(TAG, getString(R.string.facebook_login));
                    ParseFacebookUtils.logInWithReadPermissionsInBackground((Activity) mContext, pPermissions, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException err) {
                            if (err == null) {
                                if (user == null) {
                                    Log.e(TAG, getString(R.string.facebook_login_cancel));
                                } else if (user.isNew()) {
                                    Log.e(TAG, getString(R.string.facebook_new_user));
                                    sendGraphRequest(mContext);
                                } else {
                                    Log.e(TAG, getString(R.string.facebook_login_success));
                                    SashidoHelper.goToDashboard(LoginActivity.this);
                                }
                            } else {
                                Log.e(TAG, "Error: " + err.getLocalizedMessage());
                            }
                        }
                    });
                    break;
//                case R.id.activity_login_instagram_button:
//                    utils.makeText("Instagram login coming soon!", LENGTH_SHORT);
//                    // openWebViewForInstagramAuthentication();
//                    break;
//                case R.id.activity_login_google_button:
//                    signInGoogle();
//                    // utils.makeText("Google+ Button", LENGTH_LONG);
//                    break;
                case R.id.activity_register_email_button_child:
                    String rName = toTitleCase(inputRegisterName.getText().toString().trim());
                    String rUsername = inputRegisterUsername.getText().toString().trim();
                    String rEmail = inputRegisterEmail.getText().toString().trim();
                    String rConfirmEmail = inputRegisterConfirmEmail.getText().toString().trim();
                    String rPassword = inputRegisterPassword.getText().toString().trim();
                    String rConfirmPassword = inputRegisterConfirmPassword.getText().toString().trim();

                    if (!validateRegistrationDetails(NAME, rName, EMPTY)) {
                        break;
                    }

                    if (!validateRegistrationDetails(USERNAME, rUsername, EMPTY)) {
                        break;
                    }

                    if (!validateRegistrationDetails(EMAIL, rEmail, rConfirmEmail)) {
                        break;
                    }

                    if (!validateRegistrationDetails(PASSWORD, rPassword, rConfirmPassword)) {
                        break;
                    }
                    SashidoHelper.register(mContext, LoginActivity.this, rName, rUsername, rConfirmEmail, rConfirmPassword);
                    break;
                case R.id.activity_login_email_button_child:
                    String lUsername = inputUsername.getText().toString().trim();
                    String lPassword = inputPassword.getText().toString().trim();

                    if (!validateLoginDetails(USERNAME, lUsername, lPassword)){
                        break;
                    }
                    if (!validateLoginDetails(PASSWORD, lUsername, lPassword)) {
                        break;
                    }
                    SashidoHelper.logIn(mContext, LoginActivity.this, lUsername, lPassword);
                    break;
                case R.id.activity_login_login_button:
                    login_va.setDisplayedChild(LOGIN_VA_CHILD_TWO);
                    break;
                case R.id.activity_login_register_button:
                    login_va.setDisplayedChild(LOGIN_VA_CHILD_THREE);
                    break;
                case R.id.activity_login_email_forgot_password:
                    utils.showInputDialog(false, mContext, LoginActivity.this, utils);
                    break;
            }
        }
    };

    private final int GOOGLE_SIGN_IN = 6000;

//    private void signInGoogle() {
//        configureGoogleApi();
//        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
//        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
//    }

    public void configureGoogleApi() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(Constants.WEB_APP_CLIENT_ID)
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, null /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private String toTitleCase(String str) {
        String[] arr = str.toLowerCase().split(" ");
        StringBuffer sb = new StringBuffer();
        for (String s : arr) {
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private String name, email, facebookID, profilePicture;
    public void sendGraphRequest(final Context mContext) {
        final GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    name = object.getString("name");
                    Log.e(TAG, name);
                    email = object.getString("email");
                    Log.e(TAG, email);
                    facebookID = object.getString("id");
                    Log.e(TAG, facebookID);
                    profilePicture = "https://graph.facebook.com/"+facebookID+"/picture?type=large";
                    Log.e(TAG, profilePicture);
                } catch(JSONException e){
                    e.printStackTrace();
                }
                saveFacebookUser(mContext, name, email, facebookID, profilePicture);
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, email, age_range, picture");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void saveFacebookUser(final Context context, String name, String email, String facebookID, String profilePicture) {
        ParseUser user = ParseUser.getCurrentUser();
        user.setUsername(facebookID);
        user.put(USER_FACEBOOK_ID_KEY, facebookID);
        user.put(USER_UNIQUE_ID_KEY, facebookID);
        user.setEmail(email);
        user.put(USER_FULL_NAME_KEY, name);
        user.put(USER_LOGIN_METHOD_KEY, Constants.FACEBOOK);
        user.put(USER_PROFILE_COMPLETED_KEY, false);
        user.put(USER_GOLD_COINS_KEY, 0);
        user.put(USER_SILVER_COINS_KEY, 500);
        user.put(USER_VOTES_KEY, 0);
        user.put(USER_PROFILE_PICTURE_KEY, profilePicture);
        user.put(USER_HAS_SHARED_KEY, false);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    utils.showInputDialog(true, context, LoginActivity.this, utils);
                    Log.e(TAG, context.getString(R.string.facebook_login));
                } else {
                    Log.e(TAG, "failed" + e.getMessage());
                    SashidoHelper.showErrorDialog(context, e);
                }
            }
        });
    }

    private void saveInstaUser(String id, String username, String name, String profilePicture) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(id + "pass");
        user.put(USER_UNIQUE_ID_KEY, id);
        user.put(USER_INSTA_ID_KEY, id);
        user.put(USER_FULL_NAME_KEY, name);
        user.put(USER_LOGIN_METHOD_KEY, Constants.INSTAGRAM);
        user.put(USER_PROFILE_COMPLETED_KEY, false);
        user.put(USER_GOLD_COINS_KEY, 0);
        user.put(USER_SILVER_COINS_KEY, 500);
        user.put(USER_VOTES_KEY, 0);
        user.put(USER_PROFILE_PICTURE_KEY, profilePicture);
        user.put(USER_HAS_SHARED_KEY, false);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    utils.showInputDialog(true, mContext, LoginActivity.this, utils);
                    Log.e(TAG, "Instagram login");
                } else {
                    Log.e(TAG, "failed" + e.getMessage());
                    SashidoHelper.showErrorDialog(mContext, e);
                }
            }
        });
    }

    private void saveGoogleUser(GoogleSignInAccount account) {
        ParseUser user = new ParseUser();
        user.setUsername(account.getId());
        user.setPassword(account.getId() + "pass");
        user.put(USER_UNIQUE_ID_KEY, account.getId());
        user.put(USER_GOOGLE_ID_KEY, account.getId());
        user.setEmail(account.getEmail());
        user.put(USER_FULL_NAME_KEY, account.getDisplayName());
        user.put(USER_LOGIN_METHOD_KEY, Constants.GOOGLE);
        user.put(USER_PROFILE_COMPLETED_KEY, false);
        user.put(USER_GOLD_COINS_KEY, 0);
        user.put(USER_SILVER_COINS_KEY, 500);
        user.put(USER_VOTES_KEY, 0);
        user.put(USER_PROFILE_PICTURE_KEY, account.getPhotoUrl().toString());
        user.put(USER_HAS_SHARED_KEY, false);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    utils.showInputDialog(true, mContext, LoginActivity.this, utils);
                    Log.e(TAG, "Google login");
                } else {
                    Log.e(TAG, "failed" + e.getMessage());
                    SashidoHelper.showErrorDialog(mContext, e);
                }
            }
        });
    }

    private boolean validateLoginDetails(String type, String username, String password) {
        switch (type) {
            case USERNAME:
                if (username.isEmpty()) {
                    inputLayoutUsername.setError(getString(R.string.empty_err_msg_username));
                    utils.requestFocus(inputUsername, LoginActivity.this);
                    return false;
                } else {
                    inputLayoutUsername.setErrorEnabled(false);
                }
                break;
            case PASSWORD:
                if (password.isEmpty()) {
                    inputLayoutPassword.setError(getString(R.string.empty_err_msg_password));
                    utils.requestFocus(inputPassword, LoginActivity.this);
                    return false;
                } else {
                    inputLayoutPassword.setErrorEnabled(false);
                }
                break;
        }
        return true;
    }

    private boolean validateRegistrationDetails(String type, String input, String confirmInput) {
        switch(type) {
            case NAME:
                if (input.isEmpty()){
                    inputRegisterLayoutName.setError(getString(R.string.empty_err_msg_name));
                    utils.requestFocus(inputRegisterName, LoginActivity.this);
                    return false;
                } else {
                    inputRegisterLayoutName.setErrorEnabled(false);
                }
                break;
            case USERNAME:
                if (input.isEmpty()){
                    inputRegisterLayoutUsername.setError(getString(R.string.empty_err_msg_username));
                    utils.requestFocus(inputRegisterUsername, LoginActivity.this);
                    return false;
                } else {
                    inputRegisterLayoutUsername.setErrorEnabled(false);
                }
                break;
            case EMAIL:
                if (!input.equals(confirmInput)) {
                    inputRegisterLayoutConfirmEmail.setError(getString(R.string.emails_must_match_err));
                    utils.requestFocus(inputRegisterConfirmEmail, LoginActivity.this);
                    return false;
                } else {
                    inputRegisterLayoutConfirmEmail.setErrorEnabled(false);
                }
                break;
            case PASSWORD:
                if (!input.equals(confirmInput)) {
                    inputRegisterLayoutConfirmPassword.setError(getString(R.string.passwords_must_match_err));
                    utils.requestFocus(inputRegisterConfirmPassword, LoginActivity.this);
                    return false;
                } else {
                    inputRegisterLayoutConfirmPassword.setErrorEnabled(false);
                }
                break;
        }
        return true;
    }

    private View.OnFocusChangeListener customFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (!hasFocus) {
                switch (view.getId()) {
                    case R.id.register_input_name:
                        if (!isValid(NAME, inputRegisterName.getText().toString().trim())) {
                            inputRegisterLayoutName.setError(getString(R.string.invalid_name));
                        } else {
                            inputRegisterLayoutName.setErrorEnabled(false);
                        }
                        break;
                    case R.id.register_input_username:
                        if (!isValid(USERNAME_INPUT, inputRegisterUsername.getText().toString().trim())) {
                            inputRegisterLayoutUsername.setError(getString(R.string.invalid_username));
                        } else {
                            inputRegisterLayoutUsername.setErrorEnabled(false);
                        }
                        break;
                    case R.id.register_input_email:
                        if (!isValid(EMAIL, inputRegisterEmail.getText().toString().trim())) {
                            inputRegisterLayoutEmail.setError(getString(R.string.invalid_email));
                        } else {
                            inputRegisterLayoutEmail.setErrorEnabled(false);
                        }
                        break;
                    case R.id.register_input_confirm_email:
                        if (!isValid(EMAIL, inputRegisterConfirmEmail.getText().toString().trim())) {
                            inputRegisterLayoutConfirmEmail.setError(getString(R.string.invalid_email));
                        } else {
                            inputRegisterLayoutConfirmEmail.setErrorEnabled(false);
                        }
                        break;
                    case R.id.register_input_password:
                        if (!isValid(PASSWORD, inputRegisterPassword.getText().toString().trim())) {
                            inputRegisterLayoutPassword.setError(getString(R.string.invalid_password));
                        } else {
                            inputRegisterLayoutPassword.setErrorEnabled(false);
                        }
                        break;
                    case R.id.register_input_confirm_password:
                        if (!isValid(PASSWORD, inputRegisterConfirmPassword.getText().toString().trim())) {
                            inputRegisterLayoutConfirmPassword.setError(getString(R.string.invalid_password));
                        } else {
                            inputRegisterLayoutConfirmPassword.setErrorEnabled(false);
                        }
                        utils.hideKeyboard(view, mContext);
                        break;
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getGoogleUsername(GoogleSignInAccount account) {
        ParseQuery getGoogleUser = ParseQuery.getQuery(Constants.USER_CLASS_KEY);
        getGoogleUser.whereEqualTo(Constants.USER_GOOGLE_ID_KEY, account.getId());
        try {
            ParseUser user = (ParseUser) getGoogleUser.getFirst();
            return user.getUsername();
        } catch (ParseException e) {
            return null;
        }
    }

    private String getInstaUsername(String id) {
        ParseQuery getInstaUser = ParseQuery.getQuery(Constants.USER_CLASS_KEY);
        getInstaUser.whereEqualTo(Constants.USER_INSTA_ID_KEY, id);
        try {
            ParseUser user = (ParseUser) getInstaUser.getFirst();
            return user.getUsername();
        } catch (ParseException e) {
            return null;
        }
    }

    private void handleInstaResult(String id, String username, String name, String profilePicture) {
        String parseUsername = getInstaUsername(id);
        if (parseUsername != null) {
            SashidoHelper.logIn(mContext, LoginActivity.this, parseUsername, id + "pass");
            Log.e("instagram", "logged in through instagram");
        } else {
            saveInstaUser(id, username, name, profilePicture);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            String username = getGoogleUsername(acct);
            if (username != null) {
                SashidoHelper.logIn(mContext, LoginActivity.this, username, acct.getId() + "pass");
                Log.e("google", "logged in through google");
            } else {
                saveGoogleUser(acct);
            }
        } else {
            utils.makeText("Oops, something went wrong! Try another login method.", Toast.LENGTH_SHORT);
        }
    }

    private void goToActivity() {
        if (SashidoHelper.isLogged()) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            initAnimation();
        }
    }

    private void initAnimation() {
        Animation bottomUp = AnimationUtils.loadAnimation(mContext,
                R.anim.login_button_anim);
        login_va.startAnimation(bottomUp);
        login_va.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (login_va.getDisplayedChild() != LOGIN_VA_CHILD_ONE) {
            if (login_va.getDisplayedChild() == LOGIN_VA_CHILD_THREE) {
                removeErrorLabels();
            }
            login_va.setDisplayedChild(LOGIN_VA_CHILD_ONE);
        } else {
            if (mBackPressed + 2000 > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                utils.makeText(getString(R.string.back_button_exit), LENGTH_LONG);
            }
            mBackPressed = System.currentTimeMillis();
        }
    }

    private void removeErrorLabels() {
        inputRegisterLayoutConfirmEmail.setErrorEnabled(false);
        inputRegisterConfirmEmail.setText(EMPTY);
        inputRegisterLayoutConfirmPassword.setErrorEnabled(false);
        inputRegisterConfirmPassword.setText(EMPTY);
        inputRegisterLayoutEmail.setErrorEnabled(false);
        inputRegisterEmail.setText(EMPTY);
        inputRegisterLayoutName.setErrorEnabled(false);
        inputRegisterName.setText(EMPTY);
        inputRegisterLayoutPassword.setErrorEnabled(false);
        inputRegisterPassword.setText(EMPTY);
        inputRegisterLayoutUsername.setErrorEnabled(false);
        inputRegisterUsername.setText(EMPTY);
    }

    private void openWebViewForInstagramAuthentication() {
        String authUrl = "https://api.instagram.com/oauth/authorize/";
        // used for authentication
        String authURLString = authUrl + "?client_id=" + Constants.INSTA_CLIENT_ID + "&redirect_uri=" + Constants.INSTA_CALLBACK_URL + "&response_type=token";
        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(Constants.INSTA_CALLBACK_URL)) {
                    view.setVisibility(View.GONE);
                    String token = url.substring(url.indexOf("=") + 1);
                    RetrieveFeedTask task = new RetrieveFeedTask();
                    task.execute(token);
                    return true;
                }
                return false;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith(Constants.INSTA_CALLBACK_URL)) {
                    view.setVisibility(View.GONE);
                    String token = url.substring(url.indexOf("=") + 1);
                    RetrieveFeedTask task = new RetrieveFeedTask();
                    task.execute(token);
                    return true;
                }
                return false;
            }
        });
        webView.loadUrl(authURLString);
        webView.setVisibility(View.VISIBLE);
    }

    // pulls instagram user data
    private class RetrieveFeedTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        protected void onPreExecute() {
            //
        }

        protected String doInBackground(String... urls) {
            try {
                URL url = new URL("https://api.instagram.com/v1/users/self/?access_token=" + urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally {
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("error: ", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if (response == null) {
                utils.makeText("Oops, something went wrong! Try another login method.", Toast.LENGTH_SHORT);
            } else {
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    JSONObject data = object.getJSONObject("data");
                    Log.e("data", data.toString());
                    handleInstaResult(data.getString("id"), data.getString("username"), data.getString("full_name"), data.getString("profile_picture"));
                } catch (JSONException e) {
                    Log.e("failed", e.getLocalizedMessage());
                }
            }
        }
    }

}
