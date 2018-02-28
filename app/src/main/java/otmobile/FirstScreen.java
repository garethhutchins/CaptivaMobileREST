package otmobile;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import emc.captiva.mobile.sdk.CaptureException;
import emc.captiva.mobile.sdk.CaptureImage;
import emc.captiva.mobile.sdk.CaptureWindow;
import emc.captiva.mobile.sdk.PictureCallback;


public class FirstScreen extends Activity implements PictureCallback {
    static String USE_QUADVIEW = "UseQuadView";
    static boolean _newLoad = true;
    private static String TAG = MainActivity.class.getSimpleName();
    private final int CHOOSE_IMAGE = 1;
    private static  final int PERMISSION_REQUEST_CAMERA = 0;
    private static  final int PERMISSION_REQUEST_GALLERY = 0;
    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(null);
        setContentView(otmobile.R.layout.activity_first_screen);
        //License the Application
        CoreHelper.license(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Add a listener for the take picture
        FloatingActionButton takepicture = (FloatingActionButton) findViewById(R.id.floatingCamera);
        takepicture.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                onTakePictureClick(v);
            }
        });
        //Settings
        FloatingActionButton settings = (FloatingActionButton) findViewById(R.id.floatSettings);
        settings.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                onSettingsClick(v);
            }
        });
        //Gallery
        FloatingActionButton gallery = (FloatingActionButton) findViewById(R.id.floatingGallery);
        gallery.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                onGalleryClick(v);
            }
        });
        mLayout = findViewById(R.id.firstscreenRL);

    }
    public void onGalleryClick(View view) {
        //First check the gallery permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            //Permission is OK
            Intent galleryIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent , CHOOSE_IMAGE );
        }
        else {
            //Request Permission
            requestGalleryPermission();
        }


    }

    private void requestGalleryPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //Show detailed prompt
            Snackbar.make(mLayout,"Gallery access is required to select images.",Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Request the permission
                    ActivityCompat.requestPermissions(FirstScreen.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_GALLERY);
                }
            }).show();
        } else {
            Snackbar.make(mLayout,
                    "Permission is not available. Requesting gallery permissions.",
                    Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_GALLERY);
        }
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)) {
            //Show detailed prompt
            Snackbar.make(mLayout,"Gallery access is required to select images.",Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //Request the permission
                    ActivityCompat.requestPermissions(FirstScreen.this,
                            new String[]{Manifest.permission.CAMERA},
                            PERMISSION_REQUEST_CAMERA);
                }
            }).show();
        } else {
            Snackbar.make(mLayout,
                    "Permission is not available. Requesting gallery permissions.",
                    Snackbar.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        }
    }

    public void onTakePictureClick(View view) {
        //First check the Camera permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            //Permission is OK

            // Use a separate HashMap to hold non-TakePicture parameter values from preferences.
            HashMap<String, Object> appParams = new HashMap<>();
            // Obtain our picture parameters from the preferences. Only supported SDK keys should go into parameters.
            HashMap<String, Object> parameters = CoreHelper.getTakePictureParametersFromPrefs(this, appParams);

            // Get the preference for CaptureWindow
            SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
            String capWndNone = CoreHelper.getStringResource(this, R.string.GPREF_CAPTURE_CUSTOM_OPTIONS_NONE);
            String capWndPref = gprefs.getString(CoreHelper.getStringResource(this, R.string.GPREF_CAPTURE_CUSTOM_OPTIONS), capWndNone);

            if (capWndPref.compareToIgnoreCase(capWndNone) != 0) {
                // Assign a custom CaptureWindow if specified by the prefs.
                CaptureWindow wnd = new CustomWindow(this, capWndPref, appParams);
                parameters.put(CaptureImage.PICTURE_CAPTUREWINDOW, wnd);
            }
            else if ((boolean) appParams.get(USE_QUADVIEW))
            {
                CaptureWindow wnd = new CustomWindow(this, capWndPref, appParams);
                parameters.put(CaptureImage.PICTURE_CAPTUREWINDOW, wnd);
            }

            // Launch the camera to take a picture.
            CaptureImage.takePicture(this, parameters);
        }
        else {
            //Request Permission
            requestCameraPermission();
        }

    }

    public void onSettingsClick(View view) {
        // Launch the preference settings activity.
        Log.d("This","Called");
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPictureTaken(byte[] imageData) {
        // Use our utility functions to obtain a unique filename to store into the image gallery.
        File fullpath = new File(CoreHelper.getImageGalleryPath(), CoreHelper.getUniqueFilename("Img", ".JPG"));
        Log.d(TAG, "save to path: " + fullpath);

        try {
            // Use our utility function to save this JPG encoded byte array to storage.
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
            CoreHelper.saveFile(inputStream, fullpath);

            // Get a URI to broadcast and let Android know there is a new image in the gallery.
            Uri uri = Uri.fromFile(fullpath);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

            // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
            gotoEnhanceImage(uri);
        }
        catch (IOException e) {
            // Log a message and display an error using our utility function.
            Log.e(TAG, e.getMessage(), e);
            CoreHelper.displayError(this, "Could not save the image to the gallery.");
        }
    }

    @Override
    public void onPictureCanceled(int reason) {
        captureCanceled(reason);
    }

    private void captureCanceled(int reason) {
        // This callback will be called if the take picture operation was canceled.
        if (reason == PictureCallback.REASON_OPTIMAL_CONDITIONS) {
            CoreHelper.displayError(this, "The optimal conditions were not met and the picture was canceled.");
        } else if (reason == PictureCallback.REASON_CAMERA_ERROR) {
            StringBuilder errorReport = new StringBuilder();
            CaptureException ex = CaptureImage.getLastError();
            if (ex != null)
            {
                errorReport.append("\n\n************ Stack Trace ************\n\n");
                errorReport.append(Log.getStackTraceString(ex));
            }

            CoreHelper.displayError(this, "An error occurred while accessing the camera." + errorReport);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle results for activities launched by this activity.
        try {
            if(requestCode == CHOOSE_IMAGE && data != null && data.getData() != null) {
                // The user picked an image from the gallery.
                // Send the new picture taken to the enhancement screen so that users can modify it if necessary.
                Uri uri = data.getData();
                gotoEnhanceImage(uri);
            }
            // Note: The broadcast of the image change is already done in this.onPictureTaken for original picture and
            // in EnhanceImageActivity for images that get enhanced/modified.
        }
        catch (Exception e) {
            // Log a message and display the error to the user using our utility function.
            Log.e(TAG, e.getMessage(), e);
            CoreHelper.displayError(this, e);
        }
    }

    private void gotoEnhanceImage(Uri uri) {

        // If we have a file, then send it to enhancement.
        if (uri != null) {
            // Send the file path to enhancement so that it can load in the screen.
            String filepath = CoreHelper.getFilePathFromContentUri(this, uri);
            Intent intent = new Intent(this, EnhanceImageActivity.class);
            intent.putExtra("Filename", filepath);
            _newLoad = true;
            startActivity(intent);
            finish();
        }
    }


}
