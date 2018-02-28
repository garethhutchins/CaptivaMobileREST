/** -------------------------------------------------------------------------
 * Copyright 2013-2016 EMC Corporation.  All rights reserved.
 ---------------------------------------------------------------------------- */

package otmobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import emc.captiva.mobile.sdk.CaptureImage;

/**
 * A class that provides static helpers for common needs.
 */
public final class CoreHelper {
    
    // Used to help compute a unique image filename.
    private static int _imageCounter = 0;
    
    /**
     * This function attempts to generate a unique filename.
     * @param prefix      The prefix for the filename.
     * @param extension   The extension for the filename.
     * @return            The filename as a string.
     */
    @SuppressLint("SimpleDateFormat")
    public static String getUniqueFilename(String prefix, String extension) {
        
        if (prefix == null) {
            prefix = "";
        }
        
        if (extension == null) {
            extension = "";
        }
        
        String timeStamp = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date());

        // Return the filename.
        return prefix + timeStamp + String.valueOf(++_imageCounter) + extension;
    }
    
    /**
     * This function returns the path to the image gallery.
     * @return    The File representing the path to the gallery.
     */
    public static File getImageGalleryPath() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    }    
        
    /**
     * This function saves a file from an input stream.
     * @param inputStream    The input stream object to use to generate the file.
     * @param fullPath       The path to the location of where to write the file.
     * @throws IOException   The kind of exception that may be thrown from this function.
     */
    public static void saveFile(InputStream inputStream, File fullPath) throws IOException {
        byte[] buffer = new byte[1000];

        // Make the directory structure if it doesn't already exist, otherwise, FileOutputStream instantiation will throw exception if folder doesn't exist.
        File parentPath = fullPath.getParentFile();
        //noinspection ResultOfMethodCallIgnored
        parentPath.mkdirs();

        OutputStream outputStream = new FileOutputStream(fullPath, false);
        int ret;
        while (true) {
            // Note: The offset argument (2nd argument) is the offset into 'buffer' where the
            // data from the stream is being written.
            ret = inputStream.read(buffer, 0, 1000);
            if (ret <= 0) {
                break;
            }

            // Note: The offset argument (2nd argument) is the offset of 'buffer' where the
            // data is coming from that will be written into the outputStream.
            outputStream.write(buffer, 0, ret);
        }
        
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }
    
    /**
     * This function displays a simple error message box from an exception.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param exception  The exception to use to generate the message. 
     */
    public static void displayError(Activity context, Exception exception) {
    	displayError(context, exception.getMessage(), null);
    }
    
    /**
     * This function displays a simple error message box from a String.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param message	 The String to use to generate the message.
     */
    public static void displayError(Activity context, String message) {
    	displayError(context, message, null);
    }
           
    /**
     * This function displays a simple error message box from an exception.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param exception  The exception to use to generate the message.
     * @param listener	 Click listener for BUTTON_POSITIVE.
     */
    public static void displayError(Activity context, Exception exception, DialogInterface.OnClickListener listener) {
    	displayError(context, exception.getMessage(), listener);
    }
    
    /**
     * This function displays a simple error message box from a String.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param message	 The String to use to generate the message.
     * @param listener	 Click listener for BUTTON_POSITIVE. 
     */
    public static void displayError(Activity context, String message, DialogInterface.OnClickListener listener) {
        displayMessage(context, message, "Error", listener);
    }

    /**
     * This function displays a simple message box from a String.
     * @param context    The context to use as the basis for the generation of the message box.
     * @param message	 The String to use to generate the message.
     * @param title	     The String to use for the message box title.
     * @param listener	 Click listener for BUTTON_POSITIVE.
     */
    public static void displayMessage(Activity context, String message, String title, DialogInterface.OnClickListener listener) {
        final Context c = context;
        final DialogInterface.OnClickListener l = listener;
        final String m = message;
        final String t = title;

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog dialog = new AlertDialog.Builder(c)
                        .setTitle(t)
                        .setMessage(m)
                        .setCancelable(false)
                        .create();

                dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", l);
                dialog.show();
            }
        });
    }
    
    /**
     * A simple helper function to generate a String from a content Uri.
     * @param context     The context to use as the basis for the generation of the message box.
     * @param uri     The Uri object to use to generate the String.
     * @return        The String generated from the Uri.
     */
    public static String getFilePathFromContentUri(Context context, Uri uri) {
        String filePath;
        if (uri != null && "content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME }, null, null, null);
            cursor.moveToFirst();
            if (cursor.getString(0) != null && !cursor.getString(0).isEmpty()) {
                // Sometimes, this yields a null value when the Photos app has replaced the Gallery.
                filePath = cursor.getString(0);
            } else {
                // This path assumes that the image is stored in the external (SDCard) gallery path.
                filePath = CoreHelper.getImageGalleryPath() + "/" + cursor.getString(1);
            }
            cursor.close();
            return filePath;
        }

        return uri == null ? "" : uri.getPath();
    }

    /**
     * Attempts to return an extension as a suffix of a file.
     * @param filePath    The file path to use.
     * @return            The extension suffix if one exists or the empty string.
     */
    @SuppressWarnings("unused")
    public static String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        
        int index = filePath.lastIndexOf('.');
        if (index == -1) {
            return "";
        }
        
        return filePath.substring(index);
    }
    
    /**
     * A simple helper function for retrieving resources.
     * @param context    The context object to use for the call.
     * @param resId      The resource ID to fetch.
     * @return           The string representation for the resource or empty string if it doesn't exist.
     */
    public static String getStringResource(Context context, int resId) {
        if (context != null) {
            return context.getString(resId);
        }
        
        return "";
    }
    
    /**
     * Parses string as a signed integer.
     * @param data			String holding the signed integer representation.
     * @param defaultValue	Value to return if conversion fails.
     * @return				Converted value.
     */
    public static int getInteger(String data, int defaultValue) {
    	int returnValue;
    	
    	try {
    		returnValue = Integer.parseInt(data);
    	} catch (NumberFormatException exception) {
    		returnValue = defaultValue;
    	}
    	
    	return returnValue;
    }
    
    /**
     * Parses string as a float.
     * @param data          String holding the float representation.
     * @param defaultValue  Value to return if conversion fails.
     * @return              Converted value.
     */
    public static float getFloat(String data, float defaultValue) {
        float returnValue;
        
        try {
            returnValue = Float.parseFloat(data);
        } catch (NumberFormatException exception) {
            returnValue = defaultValue;
        }
        
        return returnValue;
    }
    
    /**
     * The function serves as a helper function to retrieve all of the take picture preferences.
     * @param ctx    The context object to use for the call.
     * @param appParams Sample App internal use parameters.
     * @return       The parameters retrieved from the settings preferences for taking a picture.
     */
    public static HashMap<String, Object> getTakePictureParametersFromPrefs(Context ctx, Map<String, Object> appParams) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(CaptureImage.PICTURE_CONTEXT, ctx);
        if (ctx != null) {
            // Retrieve user's preferences.
            SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            boolean lightSensor = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_LIGHT), true);
            boolean motionSensor = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_MOTION), true);

            appParams.put(MainActivity.USE_MOTION, motionSensor);

            boolean focusSensor = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_FOCUS), true);
            boolean qualitySensor = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_QUALITY), false);
            boolean guideLines = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_GUIDELINES), false);
            boolean pictureCrop = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_PICTURE_CROP), false);
            String pictureCropColor = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_PICTURE_CROP_COLOR), "green");
            String pictureCropWarningColor = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_PICTURE_CROP_WARNING_COLOR), "red");
            // Note: Preference values are stored as strings, thus, numbers will have to be parsed from these string values.
            String temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_PICTURE_CROP_ASPECT_WIDTH), "8.5");
            float pictureCropAspectWidth = getFloat(temp, 8.5f);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_PICTURE_CROP_ASPECT_HEIGHT), "11");
            float pictureCropAspectHeight = getFloat(temp, 11f);
            boolean optimalCondReq = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_OPTIMALCONDREQ), true);
            boolean cancelBtn = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_CANCEL), false);
            boolean torchBtn = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_TORCH_BUTTON), true);
            boolean torch = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_TORCH), false);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_LIGHT_VALUE), "50");
            Integer lightSensitivity = getInteger(temp, 50);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_MOTION_VALUE), ".30");
            Float motionSensitivity = getFloat(temp, .30f);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_QUALITY_GLARE_VALUE), "0");
            Integer qualityGlare = getInteger(temp, 0);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_QUALITY_PERSPECTIVE_VALUE), "0");
            Integer qualityPerspective = getInteger(temp, 0);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_SENSOR_QUALITY_QUADRILATERAL_VALUE), "0");
            Integer qualityQuadrilateral = getInteger(temp, 0);

            // Timer preferences
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_CAPTUREDELAY), "");
            Integer captureDelayMs = getInteger(temp, 500);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_CONTINUOUSCAPTUREFRAMEDELAY), "");
            Integer continuousCaptureFrameDelayMs = getInteger(temp, 500);
            temp = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_CAPTURETIMEOUT), "");
            Integer captureTimeoutMs = getInteger(temp, 0);
            boolean captureImmediately = gprefs.getBoolean(CoreHelper.getStringResource(ctx, R.string.GPREF_AUTOCAPTURE), false);
            
            // Label preferences.
            String edgeLabel = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_EDGELABEL), "");
            String centerLabel = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_CENTERLABEL), "");
            String captureLabel = gprefs.getString(CoreHelper.getStringResource(ctx, R.string.GPREF_CAPTURINGLABEL), "");
            
            // Set preferences into parameters map.
            // LABELS
            parameters.put(CaptureImage.PICTURE_LABEL_EDGE, edgeLabel);
            parameters.put(CaptureImage.PICTURE_LABEL_CENTER, centerLabel);
            parameters.put(CaptureImage.PICTURE_LABEL_CAPTURE, captureLabel);
            
            // SENSORS:
            temp = "";
            temp = lightSensor ? temp.concat("l") : temp;
            temp = motionSensor ? temp.concat("m") : temp;
            temp = focusSensor ? temp.concat("f") : temp;
            temp = qualitySensor ? temp.concat("q") : temp;
            parameters.put(CaptureImage.PICTURE_SENSORS, temp);

            parameters.put(CaptureImage.PICTURE_SENSITIVITY_LIGHT, lightSensitivity);
            parameters.put(CaptureImage.PICTURE_SENSITIVITY_MOTION, motionSensitivity);

            boolean useQuadView = false;
            if (qualitySensor)
            {
                HashMap<String, Object> measures = new HashMap<>();
                if (qualityGlare > 0 && qualityGlare <= 101) {
                    measures.put(CaptureImage.MEASURE_GLARE, qualityGlare);
                }
                if (qualityPerspective > 0 && qualityPerspective <= 100) {
                    measures.put(CaptureImage.MEASURE_PERSPECTIVE, qualityPerspective);
                    useQuadView = true;
                }

                if (qualityQuadrilateral > 0 && qualityQuadrilateral <= 100) {
                    measures.put(CaptureImage.MEASURE_QUADRILATERAL, qualityQuadrilateral);
                    useQuadView = true;
                }
                parameters.put(CaptureImage.PICTURE_QUALITY_MEASURES, measures);
            }
            appParams.put(MainActivity.USE_QUADVIEW, useQuadView);

            // GUIDELINES
            parameters.put(CaptureImage.PICTURE_GUIDELINES, guideLines);

            // PICTURECROP
            parameters.put(CaptureImage.PICTURE_CROP, pictureCrop);
            parameters.put(CaptureImage.PICTURE_CROP_COLOR, pictureCropColor);
            parameters.put(CaptureImage.PICTURE_CROP_WARNING_COLOR, pictureCropWarningColor);
            parameters.put(CaptureImage.PICTURE_CROP_ASPECT_WIDTH, pictureCropAspectWidth);
            parameters.put(CaptureImage.PICTURE_CROP_ASPECT_HEIGHT, pictureCropAspectHeight);

            // CAPTUREDELAY
            parameters.put(CaptureImage.PICTURE_CAPTURE_DELAY, captureDelayMs);

            // CONTINUOUSCAPTUREFRAMEDELAY
            parameters.put(CaptureImage.PICTURE_CONTINUOUS_CAPTURE_FRAME_DELAY, continuousCaptureFrameDelayMs);

            // CAPTURETIMEOUT
            parameters.put(CaptureImage.PICTURE_CAPTURE_TIMEOUT, captureTimeoutMs);

            // CAPTUREIMMEDIATELY
            parameters.put(CaptureImage.PICTURE_IMMEDIATE, captureImmediately);
            
            // OPTIMALCONDREQ
            parameters.put(CaptureImage.PICTURE_OPTIMAL_CONDITIONS, optimalCondReq);

            // CANCEL BUTTON
            parameters.put(CaptureImage.PICTURE_BUTTON_CANCEL, cancelBtn);

            // TORCH BUTTON
            parameters.put(CaptureImage.PICTURE_BUTTON_TORCH, torchBtn);

            // Initial torch mode.
            parameters.put(CaptureImage.PICTURE_TORCH, torch);
        }
        
        // Return parameters.
        return parameters;
    }

    /**
     * When we retrieve the bitmap, we will attempt to save memory on the device by scaling the
     * image to the size that will allow the image to fit into the image view. This does
     * not affect the image actually loaded into the SDK. This only changes the image used
     * to display. We could skip the scaling and just display the image as it, but large
     * images could use up a lot of the memory on the device.
     * @param view            The view to use to determine the display size.
     * @param heightOffset    The amount to subtract from the height for determining the display height.
     * @return                The new calculated display size for the image.
     */
    public static Rect calcImageSizePx(View view, int heightOffset) {
        
        // Get the size of the image loaded in the SDK.
        Map<String, Object> properties = CaptureImage.getImageProperties();
        float imageWidth = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_WIDTH);
        float imageHeight = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_HEIGHT);
        
        // Get the size of the available display minus the top and bottom bars.
        float availWidth = view.getWidth();
        float availHeight = view.getHeight() - heightOffset; 
        
        float ratioWidth = imageWidth / availWidth;
        float ratioHeight = imageHeight / availHeight;
        
        // Modify the height or width depending on the bigger ratio
        if (ratioWidth > ratioHeight) {
        	availHeight = imageHeight / ratioWidth;        	
        } else {
        	availWidth = imageWidth / ratioHeight;
        }
                
        // Set the new rectangle to represent the new image's size and return it.
        return new Rect(0, 0, (int)availWidth, (int)availHeight);
    }
    
    /**
     * License the application based on the license and application id preferences
     * @param context 	Context object to read the preferences from.
     * @return 			True if the license and application id was successfully applied.
     */
    public static boolean license(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Use a default empty string on the getString() calls to guarantee value and avoid null check.
        //We're just going to hard code these
        //String license = preferences.getString(CoreHelper.getStringResource(context, R.string.GPREF_LICENSE), "");
        //String applicationId = preferences.getString(CoreHelper.getStringResource(context, R.string.GPREF_APPLICATIONID), "");
        String license = "";
        String applicationId = "";
        return !(license.length() <= 0 || applicationId.length() <= 0) && CaptureImage.addLicenseKey(applicationId, license);
    }
}
