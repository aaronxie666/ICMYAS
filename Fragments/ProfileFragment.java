package icn.icmyas.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import icn.icmyas.Adapters.RecyclerViewAdapterGallery;
import icn.icmyas.FullscreenActivity;
import icn.icmyas.Misc.CircleTransform;
import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.ExifUtil;
import icn.icmyas.Misc.SashidoHelper;
import icn.icmyas.Misc.SharedPreferencesManager;
import icn.icmyas.Misc.Utils;
import icn.icmyas.R;
import icn.icmyas.Widgets.HSquareImageView;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.facebook.FacebookSdk.getApplicationContext;
import static icn.icmyas.Misc.Constants.EMPTY;
import static icn.icmyas.Misc.Constants.MY_PERMISSIONS_REQUEST_CAMERA;
import static icn.icmyas.Misc.Constants.MY_PERMISSIONS_REQUEST_STORAGE;
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
import static icn.icmyas.Misc.Constants.PROFILE_IS_USER;
import static icn.icmyas.Misc.Constants.PROFILE_USER_OBJECT_ID;
import static icn.icmyas.Misc.Constants.USER_FULL_NAME_KEY;
import static icn.icmyas.Misc.Constants.USER_GOLD_COINS_KEY;
import static icn.icmyas.Misc.Constants.USER_PROFILE_COMPLETED_KEY;
import static icn.icmyas.Misc.Constants.USER_PROFILE_PICTURE_KEY;
import static icn.icmyas.Misc.Constants.USER_SILVER_COINS_KEY;

/**
 * Author:  Bradley Wilson
 * Date: 17/07/2017
 * Package: icn.icmyas.Fragments
 * Project Name: ICMYAS
 */

public class ProfileFragment extends Fragment {

    private View view;
    private RecyclerView cameraRecyclerView, galleryRecyclerView;
    private final int REQUEST_TAKE_PHOTO = 1000;
    private final int SELECT_FILE = 2000;
    private final int SELECT_PROFILE_PICTURE = 3000;
    private int galleryRequestCode;
    private boolean isGallery;
    private double progress = 0;
    private ProgressBar progressBar;
    private Utils utils;
    private Handler progressHandler;
    //add by chang start

    boolean CameraProfile = true;
    SwipeRefreshLayout swipeLayout;


    //add by chang end
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (progress > 101) {
                progressDisplay.setText(progress / 2 + "%");
            } else {
                progressDisplay.setText(progress + "%");
            }
            progressBar.setProgress((int) progress);
            if (ParseUser.getCurrentUser().getBoolean(USER_PROFILE_COMPLETED_KEY)) {
                progressContainer.setVisibility(View.GONE);
                if (progress < 100 && progress > 0) {
                    progressContainer.setVisibility(View.VISIBLE);
                    ParseUser.getCurrentUser().put(USER_PROFILE_COMPLETED_KEY, false);
                    // ParseUser.getCurrentUser().put(USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(USER_SILVER_COINS_KEY) - 100);
                    ParseUser.getCurrentUser().saveInBackground();
                }
            } else {
                if (progress == 100) {
                    progressContainer.setVisibility(View.GONE);
                    ParseUser.getCurrentUser().put(USER_PROFILE_COMPLETED_KEY, true);
                    // ParseUser.getCurrentUser().put(USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(USER_SILVER_COINS_KEY) + 100);
                    ParseUser.getCurrentUser().saveInBackground();
                } else {
                    if (ParseUser.getCurrentUser().getBoolean(USER_PROFILE_COMPLETED_KEY)) {
                        progressContainer.setVisibility(View.VISIBLE);
                        ParseUser.getCurrentUser().put(USER_PROFILE_COMPLETED_KEY, false);
                        // ParseUser.getCurrentUser().put(USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(USER_SILVER_COINS_KEY) - 100);
                        ParseUser.getCurrentUser().saveInBackground();
                    }
                }
                progressHandler.postDelayed(this, 1000);
            }
        }
    };
    private TextView progressDisplay;
    private LinearLayout progressContainer;
    private ParseObject userObject;
    private RecyclerViewAdapterGallery cameraAdapter, galleryAdapter;
    private JSONArray cameraArray, galleryArray;
    private String chosenItem;
    private boolean isUser;
    private final String TAG = ProfileFragment.class.getSimpleName();
    private final String POSITION = "position";
    private final String CAMERA_URI = "camera_uri";
    private final String IMAGE_LIST = "image_list";
    private final String USER_OBJECT = "user_object";
    private Context context;
    private ParseUser user;
    private HSquareImageView profilePicture;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("test", "ProfileFragment onCreate()");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        //add by chang start
        // Getting SwipeContainerLayout
        swipeLayout = view.findViewById(R.id.swipe_container);
        // Adding Listener
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    String profilePictureURL = ParseUser.getCurrentUser().fetchIfNeeded().getString(USER_PROFILE_PICTURE_KEY);
                    if (profilePictureURL.equals(NO_PICTURE)) {
                        Picasso.with(getContext()).load(R.drawable.no_profile).transform(new CircleTransform()).into(profilePicture);
                    } else {
                        Log.e("progressbar", "progress + 20, currently " + Double.toString(progress));
                        Picasso.with(getContext()).load(profilePictureURL).fit().centerCrop().transform(new CircleTransform()).into(profilePicture);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // Your code here
                Toast.makeText(getApplicationContext(), "Works!", Toast.LENGTH_LONG).show();
                swipeLayout.setRefreshing(false );

            }
        });

        // Scheme colors for animation
        swipeLayout.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );

        //add by chang end
        utils = new Utils(getActivity());
        Bundle bundle = this.getArguments();
        isUser = bundle.getBoolean(PROFILE_IS_USER);
        initViews(view, bundle);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CAMERA_URI)) {
                mCurrentPhotoPath = savedInstanceState.getString(CAMERA_URI);
            }

            if (savedInstanceState.containsKey(POSITION)) {
                finalPosition = savedInstanceState.getInt(POSITION);
            }

            if (savedInstanceState.containsKey(IMAGE_LIST)) {
                try {
                    cameraArray = new JSONArray(savedInstanceState.getString(IMAGE_LIST));
                } catch (JSONException e) {
                    Log.e(TAG, "failed1 " + e.getLocalizedMessage());
                }
            }

            if (savedInstanceState.containsKey(USER_OBJECT)) {
                userObject = savedInstanceState.getParcelable(USER_OBJECT);
            }
        }
        Log.e("test", "ProfileFragment onCreateViews()");
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CAMERA_URI, mCurrentPhotoPath);
        outState.putInt(POSITION, finalPosition);
        if (cameraArray != null) {
            outState.putString(IMAGE_LIST, cameraArray.toString());
        }
        outState.putParcelable(USER_OBJECT, userObject);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

    }

    private void initViews(View view, Bundle bundle) {
        progressBar = view.findViewById(R.id.profile_progress);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressDisplay = view.findViewById(R.id.progress_amount);
        progressContainer = view.findViewById(R.id.progress_container);
        progressHandler = new Handler();
        progressHandler.postDelayed(runnable, 1000);
        if (!isUser) {
            progressContainer.setVisibility(View.GONE);
            TextView sendMessageButton = view.findViewById(R.id.send_message_button);
            sendMessageButton.setVisibility(View.VISIBLE);
            sendMessageButton.setOnClickListener(customListener);
        }
        user = (ParseUser) ParseUser.createWithoutData("_User", bundle.getString(PROFILE_USER_OBJECT_ID));
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"+ParseUser.getCurrentUser().getUsername());
        initProfileContainer(view, user);
        populateCameraRecyclerView(user);
        populateGalleryRecyclerView(user);
    }

    private void initProfileContainer(View view, ParseObject user) {
        profilePicture = view.findViewById(R.id.profile_picture);
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryRequestCode = SELECT_PROFILE_PICTURE;
                CameraProfile = true;
                showPictureDialog();
//                requestStoragePermission();
            }
        });
        TextView userName = view.findViewById(R.id.user_name);
        TextView silverStars = view.findViewById(R.id.silver_stars);
        TextView goldStars = view.findViewById(R.id.gold_stars);
        try {
            String profilePictureURL = ParseUser.getCurrentUser().getString(USER_PROFILE_PICTURE_KEY);
            System.out.println("------------------------------------------------------------------ppppppp"+ParseUser.getCurrentUser().getString("name"));
            if (profilePictureURL.equals(NO_PICTURE)) {
                Picasso.with(getContext()).load(R.drawable.no_profile).transform(new CircleTransform()).into(profilePicture);
            } else {
                progress += 20;
                Log.e("progressbar", "progress + 20, currently " + Double.toString(progress));
                Picasso.with(getContext()).load(profilePictureURL).fit().centerCrop().transform(new CircleTransform()).into(profilePicture);
            }
            userName.setText(ParseUser.getCurrentUser().fetchIfNeeded().getString(USER_FULL_NAME_KEY));
            silverStars.setText(String.valueOf(ParseUser.getCurrentUser().fetchIfNeeded().getInt(USER_SILVER_COINS_KEY)));
            goldStars.setText(String.valueOf(ParseUser.getCurrentUser().fetchIfNeeded().getInt(USER_GOLD_COINS_KEY)));
        } catch (ParseException e) {
            Log.e("debug", "failed to pull user details: " + e.getLocalizedMessage());
        }
    }


//    add by chang start
private void showPictureDialog(){
    AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this.getContext());
    pictureDialog.setTitle("Select Action");
    String[] pictureDialogItems = {
            "Select photo from gallery",
            "Capture photo from camera" };
    pictureDialog.setItems(pictureDialogItems,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            requestStoragePermission();
                            break;
                        case 1:
                            requestCameraPermission();
                            break;
                    }
                }
            });
    pictureDialog.show();
}

//    add by chang end

    private void initGalleryRecycler() {
        galleryRecyclerView = view.findViewById(R.id.gallery_photos_recycler_view);
        RecyclerView.LayoutManager gLayoutManager = new GridLayoutManager(getActivity(), 4, LinearLayoutManager.VERTICAL, false);
        galleryRecyclerView.setLayoutManager(gLayoutManager);
    }

    private void initCameraRecycler() {
        cameraRecyclerView = view.findViewById(R.id.camera_photos_recycler_view);
        RecyclerView.LayoutManager cLayoutManager = new GridLayoutManager(getActivity(), 4, LinearLayoutManager.VERTICAL, false);
        cameraRecyclerView.setLayoutManager(cLayoutManager);
    }

    private void populateCameraRecyclerView(ParseUser user) {
        initCameraRecycler();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(PROFILE_DETAILS_CLASS_KEY);
        query.whereEqualTo(PROFILE_DETAILS_USER_POINTER_KEY, user);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    try {
                        sortRecyclerViews(true, object);
                    } catch (JSONException e1) {
                        Log.e(TAG, "failed" + e1.getLocalizedMessage());
                    }

                    checkET((TextView) view.findViewById(R.id.profile_height_et), object.getString(PROFILE_DETAILS_HEIGHT_KEY), "Feet/Inches", PROFILE_DETAILS_HEIGHT_KEY);
                    checkET((TextView) view.findViewById(R.id.profile_hips_et), object.getString(PROFILE_DETAILS_HIPS_KEY), "Inches", PROFILE_DETAILS_HIPS_KEY);
                    checkET((TextView) view.findViewById(R.id.profile_waist_et), object.getString(PROFILE_DETAILS_WAIST_KEY), "Inches", PROFILE_DETAILS_WAIST_KEY);
                    checkET((TextView) view.findViewById(R.id.profile_chest_et), object.getString(PROFILE_DETAILS_CHEST_KEY), "Inches", PROFILE_DETAILS_CHEST_KEY);
                    checkET((TextView) view.findViewById(R.id.profile_dress_size_et), object.getString(PROFILE_DETAILS_DRESS_SIZE_KEY), "", PROFILE_DETAILS_DRESS_SIZE_KEY);
                    checkET((TextView) view.findViewById(R.id.profile_hair_colour_et), object.getString(PROFILE_DETAILS_HAIR_COLOUR_KEY), "", PROFILE_DETAILS_HAIR_COLOUR_KEY);
                    Log.e(TAG, String.valueOf(progress));
                } else {
                    Log.e(TAG, "failed2 " + e.getLocalizedMessage());
                }
            }
        });
    }

    private void populateGalleryRecyclerView(ParseUser user) {
        initGalleryRecycler();
        ParseQuery<ParseObject> query = ParseQuery.getQuery(PROFILE_DETAILS_CLASS_KEY);
        query.whereEqualTo(PROFILE_DETAILS_USER_POINTER_KEY, user);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    try {
                        sortRecyclerViews(false, object);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "failed3 " + e.getLocalizedMessage());
                }
            }
        });
    }

    private void checkET(TextView et, String profileDetail, String metrics, String key) {
        if( profileDetail == null){
            et.setText(metrics);
        }
        else if (profileDetail.equals(EMPTY) || profileDetail == null) {
            et.setText(metrics);
        } else {
            et.setText(profileDetail);
            progress = progress + 10;
            Log.e("progressbar", "progress + 10, currently " + Double.toString(progress));
        }
        if (isUser) {
            et.setOnClickListener(customListener);
        }
    }

    private View.OnClickListener customListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.profile_height_et:
                    showDialog(PROFILE_DETAILS_HEIGHT_KEY, (TextView) view, getString(R.string.height), getString(R.string.feet));
                    break;
                case R.id.profile_hips_et:
                    showDialog(PROFILE_DETAILS_HIPS_KEY, (TextView) view, getString(R.string.hips), getString(R.string.inches));
                    break;
                case R.id.profile_waist_et:
                    showDialog(PROFILE_DETAILS_WAIST_KEY, (TextView) view, getString(R.string.waist), getString(R.string.inches));
                    break;
                case R.id.profile_chest_et:
                    showDialog(PROFILE_DETAILS_CHEST_KEY, (TextView) view, getString(R.string.chest), getString(R.string.inches));
                    break;
                case R.id.profile_dress_size_et:
                    showDialog(PROFILE_DETAILS_DRESS_SIZE_KEY, (TextView) view, getString(R.string.dress_size), "");
                    break;
                case R.id.profile_hair_colour_et:
                    showDialog(PROFILE_DETAILS_HAIR_COLOUR_KEY, (TextView) view, getString(R.string.hair_colour), "");
                    break;
                case R.id.send_message_button:
                    try {
                        Utils.showSendMessageDialog(getContext(), user);
                    } catch (ParseException e) {
                        Log.e("Failed", "failed" + e.getLocalizedMessage());
                    }
                    break;
            }
        }
    };

    private void showDialog(final String detailsKey, final TextView et, final String title, final String metrics) {
        int arryid = this.getResources().getIdentifier(detailsKey, "array",
                getActivity().getPackageName());
        final String[] items  = this.getResources().getStringArray(arryid);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.customAlertDialog);
        dialogBuilder.setTitle(title);
        chosenItem = items[0];
        dialogBuilder.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (detailsKey) {
                            case PROFILE_DETAILS_CHEST_KEY:
                            case PROFILE_DETAILS_DRESS_SIZE_KEY:
                            case PROFILE_DETAILS_HAIR_COLOUR_KEY:
                            case PROFILE_DETAILS_WAIST_KEY:
                            case PROFILE_DETAILS_HIPS_KEY:
                            case PROFILE_DETAILS_HEIGHT_KEY:
                                if (items[item].equals("Other")) {
                                    showOtherDialog(detailsKey, et, title, metrics);
                                    dialog.dismiss();
                                } else {
                                    chosenItem = items[item];
                                }
                                break;
                        }
                    }
                });
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery(PROFILE_DETAILS_CLASS_KEY);
                        query.whereEqualTo(PROFILE_DETAILS_USER_POINTER_KEY, ParseUser.getCurrentUser());
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                if (e == null) {
                                    final boolean isFirstUpdate = object.getString(detailsKey).equals(EMPTY);
                                    object.put(detailsKey, chosenItem);
                                    object.saveEventually(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                et.setText(chosenItem);
                                                if (isFirstUpdate) {
                                                    progress = progress + 10;
                                                    Log.e("progressbar", "progress + 10, currently " + Double.toString(progress));
                                                }
                                                dialogBuilder.create().dismiss();
                                            } else {
                                                Log.e(TAG, e.getLocalizedMessage());
                                            }
                                        }
                                    });
                                } else {
                                    Log.e(TAG, "failed4 " + e.getLocalizedMessage());
                                }
                            }
                        });

            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogBuilder.create().dismiss();
            }
        });
        dialogBuilder.create().show();

    }

    private void showOtherDialog(final String detailsKey, final TextView et, String title, String metrics) {
        LinearLayout childContainer = new LinearLayout(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins((int) Utils.convertDpToPixels(20, getActivity()), (int) Utils.convertDpToPixels(20, getActivity()), (int) Utils.convertDpToPixels(20, getActivity()), (int) Utils.convertDpToPixels(20, getActivity()));
        childContainer.setLayoutParams(params);
        childContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        EditText editText = null;
        EditText editText1 = null;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.customAlertDialog);
        dialogBuilder.setTitle(title);
        dialogBuilder.setCancelable(false);
        LinearLayout containerLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins((int) Utils.convertDpToPixels(20, getActivity()), 0, 50, (int) Utils.convertDpToPixels(20, getActivity()));
        containerLayout.setLayoutParams(lp);
        switch (detailsKey) {
            case PROFILE_DETAILS_HEIGHT_KEY:
                final View heightView = View.inflate(getActivity(),
                        R.layout.dialog_item_height, null);
                editText = heightView.findViewById(R.id.foot_et);
                editText1 = heightView.findViewById(R.id.inches_et);
                childContainer.addView(heightView);
                break;
            case PROFILE_DETAILS_CHEST_KEY:
            case PROFILE_DETAILS_WAIST_KEY:
            case PROFILE_DETAILS_HIPS_KEY:
                final View kgView = View.inflate(getActivity(),
                        R.layout.dialog_item_kg, null);
                editText = kgView.findViewById(R.id.kg_et);
                TextView tv = kgView.findViewById(R.id.metrics_tv);
                tv.setText(getString(R.string.inches));
                childContainer.addView(kgView);
                break;
            case PROFILE_DETAILS_HAIR_COLOUR_KEY:
            case PROFILE_DETAILS_DRESS_SIZE_KEY:
                final View normalView = View.inflate(getActivity(),
                        R.layout.dialog_item_normal, null);
                editText = normalView.findViewById(R.id.normal_et);
                childContainer.addView(normalView);
                break;
        }
        containerLayout.addView(childContainer);
        final EditText finalEditText = editText1;
        final EditText finalEditText1 = editText;
        dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String[] otherInput = {null};
                ParseQuery<ParseObject> query = ParseQuery.getQuery(PROFILE_DETAILS_CLASS_KEY);
                query.whereEqualTo(PROFILE_DETAILS_USER_POINTER_KEY, ParseUser.getCurrentUser());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            switch (detailsKey) {
                                case PROFILE_DETAILS_HEIGHT_KEY:
                                    if (!finalEditText.getText().toString().trim().equals(EMPTY) && !finalEditText1.getText().toString().trim().equals(EMPTY)){
                                        int feet = Integer.parseInt(finalEditText1.getText().toString().trim());
                                        int inches = Integer.parseInt(finalEditText.getText().toString().trim());
                                        if (feet > 0 && feet < 8) {
                                            if (inches > 0 && inches < 12) {
                                                otherInput[0] = feet + " foot " + inches + " inches";
                                                object.put(detailsKey, otherInput[0]);
                                                object.saveEventually(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if (e == null) {
                                                            et.setText(otherInput[0]);
                                                            progress = progress + 10;
                                                            Log.e("progressbar", "progress + 10, currently " + Double.toString(progress));
                                                            et.clearFocus();
                                                            dialogBuilder.create().dismiss();
                                                        } else {
                                                            Log.e(TAG, e.getLocalizedMessage());
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(getActivity(), "Enter realistic details, please.", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getActivity(), "Enter realistic details, please.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Please fill out the details, before submitting.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case PROFILE_DETAILS_CHEST_KEY:
                                case PROFILE_DETAILS_WAIST_KEY:
                                case PROFILE_DETAILS_HIPS_KEY:
                                    if (!finalEditText1.getText().toString().trim().equals(EMPTY)){
                                        int kg = Integer.parseInt(finalEditText1.getText().toString().trim());
                                        if (kg > 0) {
                                            otherInput[0] = kg + " inches";
                                            object.put(detailsKey, otherInput[0]);
                                            object.saveEventually(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        et.setText(otherInput[0]);
                                                        progress = progress + 10;
                                                        Log.e("progressbar", "progress + 10, currently " + Double.toString(progress));
                                                        et.clearFocus();
                                                        dialogBuilder.create().dismiss();
                                                    } else {
                                                        Log.e(TAG, e.getLocalizedMessage());
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(getActivity(), "Enter realistic details, please.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), "Please fill out the details, before submitting.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case PROFILE_DETAILS_HAIR_COLOUR_KEY:
                                case PROFILE_DETAILS_DRESS_SIZE_KEY:
                                    if (!finalEditText1.getText().toString().trim().equals(EMPTY)){
                                        otherInput[0] = finalEditText1.getText().toString().trim();
                                        object.put(detailsKey, otherInput[0]);
                                        object.saveEventually(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    et.setText(otherInput[0]);
                                                    et.clearFocus();
                                                    dialogBuilder.create().dismiss();
                                                } else {
                                                    Log.e(TAG, e.getLocalizedMessage());
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(getContext(), "Please fill out the details, before submitting.", Toast.LENGTH_SHORT).show();
                                    }
                                    break;

                            }
                        } else {
                            Log.e(TAG, "failed5 " + e.getLocalizedMessage());
                        }
                    }
                });
            }
        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogBuilder.create().dismiss();
            }
        });
        dialogBuilder.setView(containerLayout);
        dialogBuilder.create().show();
    }

    public int finalPosition;
    private void sortRecyclerViews(final boolean isCamera, final ParseObject object) throws JSONException {
        if (isCamera) {
            cameraArray = object.getJSONArray(PROFILE_DETAILS_CAMERA_ARRAY_KEY);
            sortAdapter(true, object, cameraArray);
        } else {
            galleryArray = object.getJSONArray(PROFILE_DETAILS_GALLERY_ARRAY_KEY);
            sortAdapter(false, object, galleryArray);
        }
    }

    private void sortAdapter(final boolean isCamera, final ParseObject object, final JSONArray array) throws JSONException {
        double internalProgress = 0;
        for (int i = 0; i < array.length(); i++) {
            if (!array.getString(i).equals(EMPTY)) {
                progress = progress + 2.5;
                Log.e("progressbar", "progress + 2.5, currently " + Double.toString(progress));
                internalProgress = internalProgress + 2.5;
            }
        }

        if (isCamera) {
            cameraAdapter = new RecyclerViewAdapterGallery(getActivity(), array, true);
            if (isUser) {
                cameraAdapter.setOnItemClickListener(new RecyclerViewAdapterGallery.onItemClickListener() {
                    @Override
                    public void setOnItemClickListener(View view, final int position, String image, boolean isLongClick) {
                        clickResponse(view, position, image, true, isLongClick, array, object);
                    }
                });
            }
            cameraRecyclerView.setAdapter(cameraAdapter);
            if (internalProgress != 10) {
                cameraAdapter.notifyDataSetChanged();
            }
        } else {
            galleryAdapter = new RecyclerViewAdapterGallery(getActivity(), array, false);
            if (isUser) {
                galleryAdapter.setOnItemClickListener(new RecyclerViewAdapterGallery.onItemClickListener() {
                    @Override
                    public void setOnItemClickListener(View view, int position, String image, boolean isLongClick) {
                        clickResponse(view, position, image, false, isLongClick, array, object);
                    }
                });
            }
            galleryRecyclerView.setAdapter(galleryAdapter);
            if (internalProgress != 10) {
                galleryAdapter.notifyDataSetChanged();
            }
        }

    }

    private void clickResponse(View view, final int position, String image, final boolean isCamera, boolean isLongClick, final JSONArray array, final ParseObject object) {
        CameraProfile = false;
        userObject = object;
        finalPosition = position;
        switch (view.getId()) {
            case R.id.modelProfilePicture:
                if (isLongClick) {
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder(getContext(), R.style.customAlertDialog);
                    builder.setTitle(getActivity().getResources().getString(R.string.delete_image));
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setMessage(getActivity().getResources().getString(R.string.are_you_sure_you_want_to_delete_this_image));
                    builder.setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            try {
                                array.put(position, EMPTY);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (isCamera) {
                                object.put(PROFILE_DETAILS_CAMERA_ARRAY_KEY, array);
                            } else {
                                object.put(PROFILE_DETAILS_GALLERY_ARRAY_KEY, array);
                            }
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) - 10);
                                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    if (isCamera) {
                                                        cameraAdapter.updatePosition(array, position);
                                                    } else {
                                                        galleryAdapter.updatePosition(array, position);
                                                    }
                                                    progress = progress - 2.5;
                                                    progressHandler.postDelayed(runnable, 1000);
                                                } else {
                                                    Log.e("failed", "failed" + e.getLocalizedMessage());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e(TAG, "failed6 " + e.getLocalizedMessage());
                                    }
                                }
                            });
                            dialogInterface.cancel();
                        }
                    });
                    builder.setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.show();
                } else {
                    Intent intent = new Intent(getActivity(), FullscreenActivity.class);
                    intent.putExtra(Constants.INTENT_EXTRA_IMAGE_URL, image);
                    getActivity().startActivity(intent);
                }
                break;
            case R.id.lock_container:
                SharedPreferencesManager.setString(getActivity(), IMAGE_LIST, array.toString());
                if (isCamera) {
                    requestCameraPermission();
                } else {
                    galleryRequestCode = SELECT_FILE;
                    requestStoragePermission();
                }
                break;
        }
    }

    private void galleryIntent() {
        Log.e(TAG, "Gallery Intent");
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, galleryRequestCode);
        }
    }

    private void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Storage Permission")
                        .setMessage("To upload a photo, you need to grant us permission to access your photos.")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_STORAGE);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);
            }
        } else {
            galleryIntent();
        }
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA)
                != PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle(R.string.title_camera_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                        Constants.MY_PERMISSIONS_REQUEST_CAMERA);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            cameraIntent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (progressHandler != null) {
            progressHandler.postDelayed(runnable, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (progressHandler != null) {
            progressHandler.removeCallbacks(runnable);
        }
    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.e(TAG, mCurrentPhotoPath);
        return image;
    }

    private void cameraIntent() {
        Log.e(TAG, "Camera Intent");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        Log.e(TAG, getActivity().getPackageManager().toString());
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            Log.e(TAG, "resolves activity");
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, ex.getLocalizedMessage());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri uri = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        "icn.icmyas.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void galleryAddPic(int position) throws IOException {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        Bitmap bitmap = rotateBitmap(f, contentUri, mCurrentPhotoPath);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
        os.close();
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
        if(CameraProfile){
            updateProfilePicture(f);
        }else{
            saveFile(f, false, position);
        }

    }

    private Bitmap rotateBitmap(File f, Uri contentUri, String path) throws IOException {
        // InputStream image_stream = getActivity().getContentResolver().openInputStream(contentUri);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
        bitmap = ExifUtil.rotateBitmap(path, bitmap);
        return bitmap;
    }

    public static ProfileFragment newInstance(int imageNum) {
        final ProfileFragment f = new ProfileFragment();
        final Bundle args = new Bundle();
        args.putInt("Position", imageNum);
        args.putBoolean(Constants.PROFILE_IS_USER, true);
        args.putString(Constants.PROFILE_USER_OBJECT_ID, ParseUser.getCurrentUser().getObjectId());
        f.setArguments(args);
        return f;
    }

    private void handleBigCameraPhoto(int position) throws IOException {
        galleryAddPic(position);
        mCurrentPhotoPath = null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (cameraRecyclerView != null && galleryRecyclerView != null) {
                progress = 0;
                if (progressHandler != null) {
                    progressHandler.postDelayed(runnable, 1000);
                }
                populateCameraRecyclerView(user);
                populateGalleryRecyclerView(user);
                initProfileContainer(view, user);
            }
        } else {
            if (progressHandler != null) {
                progressHandler.removeCallbacks(runnable);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode != Activity.RESULT_CANCELED) {
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            handleBigCameraPhoto(finalPosition);
                        } catch (IOException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                    }
                }
                break;
            case SELECT_FILE:
            case SELECT_PROFILE_PICTURE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    try {
                        Log.e("inside gallery", "it's here");
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };
                        Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                        if (cursor != null) {
                            Log.e("cursor!null", "");
                            cursor.moveToFirst();
                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            String picturePath = cursor.getString(columnIndex);
                            File f = new File(picturePath);
                            Bitmap bitmap = rotateBitmap(f, selectedImage, picturePath);
                            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                            os.close();
                            cursor.close();
                            if (requestCode == SELECT_FILE) {
                                saveFile(f, true, finalPosition);
                            } else {
                                updateProfilePicture(f);
                            }
                        }
                    } catch (IOException e) {
                        Log.e("debug", "failed to initialise cursor: " + e.getLocalizedMessage());
                    }
                }
                break;
        }
    }

    private void updateProfilePicture(File file) {
        final ParseFile parseFile = new ParseFile(file);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    boolean isFirstPicture =  SashidoHelper.getCurrentProfilePicture().equals(Constants.NO_PICTURE);
                    Picasso.with(getContext()).load(parseFile.getUrl()).fit().centerCrop().transform(new CircleTransform()).into(profilePicture);
                    ParseUser.getCurrentUser().put(Constants.USER_PROFILE_PICTURE_KEY, parseFile.getUrl());
                    try {
                        ParseUser.getCurrentUser().save();
                        if (isFirstPicture) {
                            progress += 20;
                            Log.e("progressbar", "progress + 20, currently " + Double.toString(progress));
                        }
                    } catch (ParseException e1) {
                        Log.e("debug", "failed to save profile picture: " + e1.getLocalizedMessage());
                    }
                }
            }
        });
    }

    private void saveFile(final File file, final boolean isGallery, final int position) {
        utils.makeText("Uploading...", Toast.LENGTH_LONG);
        final ParseFile parseFile = new ParseFile(file);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    if (isGallery) {
                        try {
                            galleryArray.put(position, parseFile.getUrl());
                            userObject.put(PROFILE_DETAILS_GALLERY_ARRAY_KEY, galleryArray);
                            userObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) + 10);
                                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    galleryRecyclerView.setAdapter(galleryAdapter);
                                                    galleryAdapter.updatePosition(galleryArray, position);
                                                } else {
                                                    Log.e("debug", "failed1: " + e.getLocalizedMessage());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("debug", "failed2: " + e.getLocalizedMessage());
                                    }
                                }
                            });
                        } catch (JSONException e1) {
                            Log.e("debug", "failed to save gallery image: " + e1.getLocalizedMessage());
                        }
                    } else {
                        try {
                            cameraArray.put(position, parseFile.getUrl());
                            userObject.put(PROFILE_DETAILS_CAMERA_ARRAY_KEY, cameraArray);
                            userObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        // ParseUser.getCurrentUser().put(Constants.USER_SILVER_COINS_KEY, ParseUser.getCurrentUser().getInt(Constants.USER_SILVER_COINS_KEY) + 10);
                                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    cameraRecyclerView.setAdapter(cameraAdapter);
                                                    cameraAdapter.updatePosition(cameraArray, position);
                                                } else {
                                                    Log.e("debug", "failed3: " + e.getLocalizedMessage());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.e("debug", "failed4: " + e.getLocalizedMessage());
                                    }
                                }
                            });
                        } catch (JSONException e1) {
                            Log.e(TAG, e1.getLocalizedMessage());
                        }
                    }
                } else {
                    Log.e("debug", "query failed: " + e.getLocalizedMessage());
                    utils.makeText("Oops, something  went wrong! Try a different photo.", Toast.LENGTH_SHORT);
                }
            }
        });

    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (getActivity() == null) {
            context = activity;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (grantResults[0] == PERMISSION_GRANTED) {
                        cameraIntent();
                        Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA);
                    }
                }
                break;
            case MY_PERMISSIONS_REQUEST_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (grantResults[0] == PERMISSION_GRANTED) {
                        galleryIntent();
                        Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_STORAGE);
                    }
                }
                break;
        }
    }
}
