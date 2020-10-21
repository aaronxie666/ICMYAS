package icn.icmyas.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.iceteck.silicompressorr.SiliCompressor;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import icn.icmyas.Misc.Constants;
import icn.icmyas.Misc.ExifUtil;
import icn.icmyas.Misc.Utils;
import icn.icmyas.R;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class IMPProfileFragment extends Fragment {

    final static String TAG = "IMPProfileFragment";
    final static int PERMISSIONS_REQUEST_CAMERA = 1;
    final static int PERMISSIONS_REQUEST_STORAGE = 2;
    final static int REQUEST_PHOTO_CAPTURE = 3;
    final static int REQUEST_VIDEO_CAPTURE = 4;
    final static int REQUEST_SELECT = 5;
    private View rootView;
    ViewSwitcher switcher;
    Utils utils;
    ParseObject compEntry, userDetails;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_imp_profile, container, false);
        utils = new Utils(getActivity());
        initViews();
        return rootView;
    }

    private void initViews() {
        switcher = rootView.findViewById(R.id.viewswitcher);
        TextView userName = rootView.findViewById(R.id.user_name);
        userName.setText(ParseUser.getCurrentUser().getUsername());
        if (!isEntered()) {
            switcher.setDisplayedChild(0);
            initJoinIMPComp();
        } else {
            switcher.setDisplayedChild(1);
            initViewProfile();
        }
    }

    private boolean isEntered() {
        ParseQuery<ParseObject> getEntry = ParseQuery.getQuery(Constants.IMP_COMP_CLASS_KEY);
        getEntry.whereEqualTo(Constants.IMP_COMP_USER_KEY, ParseUser.getCurrentUser());
        try {
            compEntry = getEntry.getFirst();
            return compEntry.getBoolean(Constants.IMP_COMP_HAS_ENTERED_KEY);
        } catch (ParseException e) {
            Log.e(TAG, e.getLocalizedMessage());
            return false;
        }
    }

    private void initJoinIMPComp() {
        TextView joinButton = rootView.findViewById(R.id.join_imp);
        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterIntoIMPComp();
            }
        });
    }

    private void enterIntoIMPComp() {
        utils.makeText("Entering...", Toast.LENGTH_SHORT);
        ParseObject newEntry = new ParseObject(Constants.IMP_DETAILS_CLASS_KEY);
        newEntry.put(Constants.IMP_DETAILS_USER_KEY, ParseUser.getCurrentUser());
        newEntry.put(Constants.IMP_DETAILS_IMAGES_KEY, new JSONArray());
        newEntry.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    compEntry.put(Constants.IMP_COMP_HAS_ENTERED_KEY, true);
                    compEntry.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                // success
                                utils.makeText("Success! You are now entered in IMP.", Toast.LENGTH_LONG);
                                initViewProfile();
                                switcher.setDisplayedChild(1);
                            } else {
                                Log.e(TAG, e.getLocalizedMessage());
                                utils.makeText("Failed to enter. Try again or get in touch!", Toast.LENGTH_LONG);
                            }
                        }
                    });
                } else {
                    Log.e(TAG, e.getLocalizedMessage());
                    utils.makeText("Failed to enter. Try again or get in touch!", Toast.LENGTH_LONG);
                }
            }
        });
    }

    ImageView uploadFree, uploadPaid1, uploadPaid2, uploadPaid3;
    TextView uploadVideo, videoApproved;
    private void initViewProfile() {
        uploadFree = rootView.findViewById(R.id.upload1);
        uploadPaid1 = rootView.findViewById(R.id.upload2);
        uploadPaid2 = rootView.findViewById(R.id.upload3);
        uploadPaid3 = rootView.findViewById(R.id.upload4);
        uploadVideo = rootView.findViewById(R.id.upload_video);
        videoApproved = rootView.findViewById(R.id.video_approved);
        uploadFree.setOnClickListener(listener);
        uploadPaid1.setOnClickListener(listener);
        uploadPaid2.setOnClickListener(listener);
        uploadPaid3.setOnClickListener(listener);
        uploadVideo.setOnClickListener(listener);
        userDetails = getUserDetails();
        if (userDetails != null) {
            fetchPhotos();
            // showThumbnailIfExists();
            String yesNo = userDetails.getBoolean(Constants.IMP_DETAILS_VIDEO_APPROVED_KEY) ? "Yes" : "No";
            videoApproved.setText("Video approved: " + yesNo);
        }
    }

    private ParseObject getUserDetails() {
        ParseQuery<ParseObject> getUserDetails = ParseQuery.getQuery(Constants.IMP_DETAILS_CLASS_KEY);
        getUserDetails.whereEqualTo(Constants.IMP_DETAILS_USER_KEY, ParseUser.getCurrentUser());
        try {
            return getUserDetails.getFirst();
        } catch (ParseException e) {
            Log.e(TAG, e.getLocalizedMessage());
            utils.makeText("Failed to load photos. Please try again or get in touch.", Toast.LENGTH_LONG);
            return null;
        }
    }

    JSONArray images;
    private void fetchPhotos() {
        images = userDetails.getJSONArray(Constants.IMP_DETAILS_IMAGES_KEY);
        ImageView[] buttons = {uploadFree, uploadPaid1, uploadPaid2, uploadPaid3};
        for (int i = 0; i < images.length(); i++) {
            try {
                String photoUrl = images.getString(i);
                if (photoUrl != null && photoUrl.length() > 0) {
                    Picasso.with(getContext()).load(photoUrl).fit().centerCrop().into(buttons[i]);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to fetch photo " + i);
            }
        }
    }

    private void showThumbnailIfExists() {
        String videoUrl = userDetails.getString(Constants.IMP_DETAILS_VIDEO_LINK_KEY);
        if (videoUrl != null && videoUrl.length() > 0) {
            Bitmap thumbnail = retrieveThumbnail(videoUrl);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG,75, baos);
            byte[] bytes = baos.toByteArray();
            thumbnail = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
            int height = thumbnail.getHeight();
            int width = thumbnail.getWidth();
            if (height >= width) {
                thumbnail = ThumbnailUtils.extractThumbnail(thumbnail, width, width);
            } else {
                thumbnail = ThumbnailUtils.extractThumbnail(thumbnail, height, height);
            }
            // TODO put the thumbnail somewhere!
        }
    }

    private Bitmap retrieveThumbnail(String videoPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14) {
                mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            } else {
                mediaMetadataRetriever.setDataSource(videoPath);
            }
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    private void selectImage() {
        isVideo = false;
        final CharSequence[] items = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Upload Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    checkCameraPermissions();
                } else if (items[item].equals("Choose from Gallery")) {
                    checkGalleryPermissions();
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void selectVideo() {
        isVideo = true;
        final CharSequence[] items = {"Record Video", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Upload Video");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Record Video")) {
                    checkCameraPermissions();
                } else if (items[item].equals("Choose from Gallery")) {
                    checkGalleryPermissions();
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private boolean isVideo;
    private void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            // do we need to show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                // explanation needed
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Camera Permission");
                builder.setMessage("To upload a photo/video, you need to grant the app permission to access your camera.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CAMERA);
                    }
                });
                builder.create();
                builder.show();
            } else {
                // no explanation needed
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            if (isVideo) {
                videoIntent();
            } else {
                cameraIntent();
            }
        }
    }

    private void checkGalleryPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            // do we need to show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // explanation needed
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Storage Permission");
                builder.setMessage("To upload a photo/video, you need to grant the app permission to access your gallery.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_STORAGE);
                    }
                });
                builder.create();
                builder.show();
            } else {
                // no explanation needed
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_STORAGE);
            }
        } else {
            galleryIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PERMISSION_GRANTED) {
                        if (isVideo) {
                            videoIntent();
                        } else {
                            cameraIntent();
                        }
                    } else {
                        utils.makeText("You need to grant this permission to continue.", Toast.LENGTH_SHORT);
                    }
                }
                break;
            case PERMISSIONS_REQUEST_STORAGE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PERMISSION_GRANTED) {
                        galleryIntent();
                    } else {
                        utils.makeText("You need to grant this permission to continue.", Toast.LENGTH_SHORT);
                    }
                }
                break;
        }
    }

    private void videoIntent() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            File file = null;
            try {
                file = createImageFile();
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            if (file != null) {
                Uri uri = FileProvider.getUriForFile(getActivity().getApplicationContext(), "icn.icmyas.fileprovider", file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                Log.e(TAG, "startActivity cameraIntent");
                startActivityForResult(intent, REQUEST_PHOTO_CAPTURE);
            }
        }
    }

    String currentPhotoPath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(fileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (isVideo) {
            intent.setType("video/*");
        } else {
            intent.setType("image/*");
        }
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Log.e(TAG, "onActivityResult OK");
            if (requestCode == REQUEST_PHOTO_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE) {
                onCaptureResult(data);
            } else if (requestCode == REQUEST_SELECT && data != null) {
                onSelectResult(data);
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContext().getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } catch (Exception e) {
            utils.makeText("Oops, something went wrong. Try again or get in touch!", Toast.LENGTH_LONG);
            Log.e(TAG, e.getLocalizedMessage());
            return null;
        }
    }

    private void onCaptureResult(Intent data) {
        if (isVideo) {
            Snackbar.make(getView(), "Uploading video. This may take a minute or two.", Snackbar.LENGTH_INDEFINITE).show();
            Uri videoUri = data.getData();
            String path = getPathFromUri(videoUri);
            if (path == null) {
                return;
            }
            File file = new File(path);
            saveFile(file);
        } else {
            Snackbar.make(getView(), "Uploading photo...", Snackbar.LENGTH_INDEFINITE).show();
            try {
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File file = new File(currentPhotoPath);
                Uri contentUri = Uri.fromFile(file);
                Bitmap bitmap = rotateBitmap(file, contentUri, currentPhotoPath);
                OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();
                intent.setData(contentUri);
                getActivity().sendBroadcast(intent);
                saveFile(file);
            } catch (IOException e) {
                Snackbar.make(getView(), "Oops! Something went wrong. Try again or get in touch!", Snackbar.LENGTH_LONG).show();
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private Bitmap rotateBitmap(File file, Uri contentUri, String path) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;    // seems to be the magic number!
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        bitmap = ExifUtil.rotateBitmap(path, bitmap);
        return bitmap;
    }

    private void onSelectResult(Intent data) {
        if (isVideo) {
            Snackbar.make(getView(), "Uploading video. This may take a minute or two.", Snackbar.LENGTH_INDEFINITE).show();
        } else {
            Snackbar.make(getView(), "Uploading photo...", Snackbar.LENGTH_INDEFINITE).show();
        }
        try {
            Uri selectedFile = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedFile, filePathColumn, null, null, null);
            if (cursor != null) {
                Log.e(TAG, "Cursor not null");
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                File file = new File(filePath);
                if (!isVideo) {
                    Bitmap bitmap = rotateBitmap(file, selectedFile, filePath);
                    OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                }
                cursor.close();
                Log.e(TAG, "About to save file");
                saveFile(file);
            }
        } catch (IOException e) {
            Snackbar.make(getView(), "Oops! Something went wrong. Try again or get in touch!", Snackbar.LENGTH_LONG).show();
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private ParseFile parseFile;
    private void saveFile(final File file) {
        if (isVideo) {
            try {
                // compress video
                String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                new VideoCompressTask(getContext()).execute(file.getAbsolutePath(), destinationPath);
            } catch (Exception e) {
                Snackbar.make(getView(), "Oops! Something went wrong. Try again or get in touch!", Snackbar.LENGTH_LONG).show();
                Log.e(TAG, e.getLocalizedMessage());
            }
        } else {
            parseFile = new ParseFile(file);
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Log.e(TAG, "Error is null");
                        final String url = parseFile.getUrl();
                        try {
                            JSONArray details = userDetails.getJSONArray(Constants.IMP_DETAILS_IMAGES_KEY);
                            details.put(selectedButton, url);
                            userDetails.put(Constants.IMP_DETAILS_IMAGES_KEY, details);
                            userDetails.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Snackbar.make(getView(), "Photo successfully uploaded!", Snackbar.LENGTH_SHORT).show();
                                            switch (selectedButton) {
                                                case 0:
                                                    Picasso.with(getContext()).load(url).fit().centerCrop().into(uploadFree);
                                                    break;
                                                case 1:
                                                    Picasso.with(getContext()).load(url).fit().centerCrop().into(uploadPaid1);
                                                    break;
                                                case 2:
                                                    Picasso.with(getContext()).load(url).fit().centerCrop().into(uploadPaid2);
                                                    break;
                                                case 3:
                                                    Picasso.with(getContext()).load(url).fit().centerCrop().into(uploadPaid3);
                                                    break;
                                            }
                                            if (shouldDeductStars) {
                                                deductStars();
                                                shouldDeductStars = false;
                                            }
                                        } else {
                                            Snackbar.make(getView(), "Oops! Something went wrong. Try again or get in touch!", Snackbar.LENGTH_LONG).show();
                                            Log.e(TAG, e.getLocalizedMessage());
                                        }
                                    }
                                });
                            } catch (JSONException e1) {
                                Log.e(TAG, e1.getLocalizedMessage());
                            }
                    } else {
                        Log.e(TAG, "Error is not null");
                        Snackbar.make(getView(), "Oops! Something went wrong. Try again or get in touch!", Snackbar.LENGTH_LONG).show();
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    private void deductStars() {
        ParseUser user = ParseUser.getCurrentUser();
        int goldStars = user.getInt(Constants.USER_GOLD_COINS_KEY);
        user.put(Constants.USER_GOLD_COINS_KEY, goldStars - 50);
        try {
            user.save();
        } catch (ParseException e) {
            Log.e(TAG, "failed to update gold stars: " + e.getLocalizedMessage());
        }
    }

    int selectedButton = -1;
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.upload1:
                    selectedButton = 0;
                    selectImage();
                    break;
                case R.id.upload2:
                    selectedButton = 1;
                    checkIfEnoughStars(uploadPaid1);
                    break;
                case R.id.upload3:
                    selectedButton = 2;
                    checkIfEnoughStars(uploadPaid2);
                    break;
                case R.id.upload4:
                    selectedButton = 3;
                    checkIfEnoughStars(uploadPaid3);
                    break;
                case R.id.upload_video:
                    selectVideo();
                    break;
            }
        }
    };

    boolean shouldDeductStars = false;
    private void checkIfEnoughStars(ImageView iv) {
        try {
            String photoUrl = images.getString(selectedButton);
            if (photoUrl.length() == 0) {   // user has not previously uploaded a photo
                ParseUser user = ParseUser.getCurrentUser();
                int goldStars = user.getInt(Constants.USER_GOLD_COINS_KEY);
                if (goldStars >= 50) {
                    shouldDeductStars = true;
                    selectImage();
                } else {
                    utils.makeText("You need at least 50 gold stars to upload a photo.", Toast.LENGTH_SHORT);
                }
            } else {
                selectImage();
            }
        } catch (JSONException e) {
            ParseUser user = ParseUser.getCurrentUser();
            int goldStars = user.getInt(Constants.USER_GOLD_COINS_KEY);
            if (goldStars >= 50) {
                shouldDeductStars = true;
                selectImage();
            } else {
                utils.makeText("You need at least 50 gold stars to upload a photo.", Toast.LENGTH_SHORT);
            }
        }
    }

    class VideoCompressTask extends AsyncTask<String, String, String> {

        Context context;

        public VideoCompressTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... paths) {
            String path = null;
            try {
                path = SiliCompressor.with(context).compressVideo(paths[0], paths[1]);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return path;
        }

        @Override
        protected void onPostExecute(String compressedPath) {
            super.onPostExecute(compressedPath);
            File compressedFile = new File(compressedPath);
            final ParseFile parseFile = new ParseFile(compressedFile);
            // upload video
            parseFile.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        userDetails.put(Constants.IMP_DETAILS_VIDEO_LINK_KEY, parseFile.getUrl());
                        userDetails.put(Constants.IMP_DETAILS_VIDEO_APPROVED_KEY, true);
                        userDetails.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Snackbar.make(getView(), "Video successfully uploaded!", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(getView(), "Oops, something went wrong. Try again or get in touch!", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        Snackbar.make(getView(), "Oops, something went wrong. Try again or get in touch!", Snackbar.LENGTH_LONG).show();
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                }
            });
        }
    }
}
