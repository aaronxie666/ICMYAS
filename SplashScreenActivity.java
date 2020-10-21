package icn.icmyas;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.VideoView;

import com.parse.ParseException;
import com.parse.ParseQuery;

import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.SashidoHelper;
import icn.icmyas.Misc.SharedPreferencesManager;
import icn.icmyas.Misc.Utils;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;


public class SplashScreenActivity extends AppCompatActivity {
    private Context context = this;
    private final int SPLASH_DISPLAY_LENGTH = 1000;    // in milliseconds
    private boolean dialogShowing = true;
    private Utils utils;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        utils = new Utils(SplashScreenActivity.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestInternetIfNecessary();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!dialogShowing) {
            requestInternetIfNecessary();
        }
    }

    private void checkIfFirstRun() {
        final int DOESNT_EXIST = -1;
        final String PREF_VERSION_CODE_KEY = "version_code";

        int currentVersionCode = BuildConfig.VERSION_CODE;
        int savedVersionCode = SharedPreferencesManager.getInt(context, PREF_VERSION_CODE_KEY);

        if (currentVersionCode == savedVersionCode) {
            // normal run
            moveOn();
        } else if (savedVersionCode == DOESNT_EXIST) {
            // new installation
            playIntroVideo();
        } else if (currentVersionCode > savedVersionCode) {
            moveOn();
        }
        SharedPreferencesManager.setInt(context, PREF_VERSION_CODE_KEY, currentVersionCode);
    }

    private void playIntroVideo() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_intro_video);
        dialog.setCancelable(false);
        dialog.show();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        lp.copyFrom(dialog.getWindow().getAttributes());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setAttributes(lp);

        final VideoView videoView = (VideoView) dialog.findViewById(R.id.intro_video);
        String uriPath = null;
        try {
            uriPath = ParseQuery.getQuery(Constants.LATEST_VIDEOS_CLASS_KEY).whereEqualTo(Constants.LATEST_VIDEOS_VIDEO_TITLE_KEY, "ICMYAS: The App").getFirst().getString(Constants.LATEST_VIDEOS_VIDEO_LINK_KEY);
        } catch (ParseException e) {
            Log.e("debug", "failed to get intro video: " + e.getLocalizedMessage());
            moveOn();
        }
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        videoView.setVideoURI(Uri.parse(uriPath));
        videoView.start();
        videoView.setZOrderOnTop(true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                final Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Button skip = dialog.findViewById(R.id.btn_skip);
                        skip.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                moveOn();
                            }
                        });
                        skip.setVisibility(View.VISIBLE);
                    }
                }, 5000);
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                moveOn();
            }
        });
    }

    private void moveOn() {
        if (SashidoHelper.isLogged()) {
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }
        finish();
    }

    private void requestInternetIfNecessary() {
        if (!internetIsAvailable()) {
            final AlertDialog dialog = new AlertDialog.Builder(context, R.style.customAlertDialog).create();
            dialog.setTitle("Internet Required");
            dialog.setMessage("Unfortunately, internet access is required to use this app.");
            dialog.setCancelable(false);
            dialog.setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialog.dismiss();
                    requestInternetIfNecessary();
                }
            });
            dialog.setButton(BUTTON_NEGATIVE, "Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    dialog.dismiss();
                    dialogShowing = false;
                }
            });
            dialog.show();
            dialogShowing = true;
        } else {
            checkIfFirstRun();
        }
    }

    private boolean internetIsAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
