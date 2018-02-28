/** -------------------------------------------------------------------------
 * Copyright 2013-2016 EMC Corporation.  All rights reserved.
 ---------------------------------------------------------------------------- */

package otmobile;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import emc.captiva.mobile.sdk.CaptureException;
import emc.captiva.mobile.sdk.CaptureImage;
import emc.captiva.mobile.sdk.QuadrilateralCropCallback;

/**
 * This activity provides the ability to enhance the image.
 */
public class EnhanceImageActivity extends Activity implements QuadrilateralCropCallback {
	private static String TAG = EnhanceImageActivity.class.getSimpleName();
	private static boolean _imgEdited = false;
	private PZImageView _imageView = null;
	private boolean _displayed = false;
	private String _filename = null;
    private boolean _enableUndo = false;
	private Button _undoButton = null;
	private Button _undoAllButton = null;
	private ProgressBar _progressBar = null;
	private Menu _menu = null;
	private RelativeLayout _enhanceLayout = null;
	private int _numOfFiltersApplied = 0;
	private String _newFileName = "";
	private List<String> filenames = new ArrayList<>();
	private int picturecount = 0;

	/**
	 * Called when the quadrilateral crop operation is complete.
	 * @param cropped    True if the image was cropped, false if the operation was canceled.
	 */
    @Override
    public void cropComplete(boolean cropped) {
		// If the image is cropped, display the 'UndoAll' button.
		if (cropped) {
			startEdit();
		}
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
	private void QCrop(){
		// Get the parameters to set up the quadrilateral crop.
		SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
		String quadCropColor = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_QUAD_CROP_COLOR), "blue");
		String temp = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_QUAD_CROP_LINE_WIDTH), "4");
		Integer quadCropLineWidth = CoreHelper.getInteger(temp, 4);
		temp = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_QUAD_CROP_CIRCLE_RADIUS), "24");
		Integer quadCropCircleRadius = CoreHelper.getInteger(temp, 24);
		Boolean quadCropShadeBackground = gprefs.getBoolean(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_QUAD_CROP_SHADE_BACKGROUND), true);

		// Add the parameters to a HashMap for passing into the show call.
		HashMap<String, Object> quadCropParams = new HashMap<>();
		quadCropParams.put(CaptureImage.CROP_CONTEXT, this);
		quadCropParams.put(CaptureImage.CROP_COLOR, quadCropColor);
		quadCropParams.put(CaptureImage.CROP_LINE_WIDTH, quadCropLineWidth);
		quadCropParams.put(CaptureImage.CROP_CIRCLE_RADIUS, quadCropCircleRadius);
		quadCropParams.put(CaptureImage.CROP_SHADE_BACKGROUND, quadCropShadeBackground);

		// Start the Quadrilateral Crop activity.
		CaptureImage.showQuadrilateralCrop(this, quadCropParams);

	}
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        
        // Handle the menu item selections.
    	Log.v (TAG, "Enhance Image Operation - " + item.getTitle());
        int menuID = item.getItemId();
		try {
			switch (menuID) {
			    case otmobile.R.id.ABInfo: {
				   // Launch the image info activity.
                    Intent intent = new Intent(this, ImageInfoActivity.class);
                    intent.putExtra("FilterError", CaptureImage.getLastError());
                    startActivity(intent);
                    break;
				}

				case otmobile.R.id.ABBarcode: {
					// Run detection based on preferences.
					SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
					int barcodeMax = CoreHelper.getInteger(gprefs.getString(getString(otmobile.R.string.GPREF_BARCODE_COUNT), ""), 5);
                    Set<String> barcodeTypesSet = gprefs.getStringSet(getString(otmobile.R.string.GPREF_BARCODE_TYPE), null);
					String[] barcodeTypes = barcodeTypesSet == null ? new String[] {CaptureImage.BARCODE_TYPE_ALL} : barcodeTypesSet.toArray(new String[0]);
					Map<String, Object> barcodes[] = CaptureImage.detectBarcodes(barcodeTypes, barcodeMax);
					StringBuilder message = new StringBuilder();

                    // Number of barcodes detected
                    message.append(String.format("Detected %d barcode(s).\n\n", barcodes.length));

					// Display detected barcode properties
					for (int i = 0; i < barcodes.length; i++) {
						Map<String, Object> barcode = barcodes[i];
						Point[] position = (Point[]) barcode.get(CaptureImage.BARCODE_POSITION);
						message.append(String.format("#%d %s [%s, %d, {%s, %s, %s, %s}]\n\n",
								i + 1,
								barcode.get(CaptureImage.BARCODE_TEXT),
								barcode.get(CaptureImage.BARCODE_TYPE),
								barcode.get(CaptureImage.BARCODE_CONFIDENCE),
								position[0].toString(),
								position[1].toString(),
								position[2].toString(),
								position[3].toString()));
					}

					if (barcodes.length > 0) {
						CoreHelper.displayMessage(this, message.toString(), getString(otmobile.R.string.Barcode_Title), null);
					} else {
						CoreHelper.displayMessage(this, getString(otmobile.R.string.Barcode_NotFound), getString(otmobile.R.string.Barcode_Title), null);
					}
					break;
				}
				
				case otmobile.R.id.ABBlackWhite: {
				    // Apply the adaptive black and white filter.
					applyFilter(CaptureImage.FILTER_ADAPTIVE_BINARY, getAdaptiveThresholdParameters());					
					break;
				}
				
				case otmobile.R.id.ABGray: {
				    // Apply the gray scale filter.
					applyFilter(CaptureImage.FILTER_GRAYSCALE, null);
					break;
				}

                case otmobile.R.id.ABDarker:
                case otmobile.R.id.ABLighter: {
                    // Apply the brightness filter.
                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put(CaptureImage.FILTER_PARAM_BRIGHTNESS_SCALE, menuID == otmobile.R.id.ABDarker ? -16 : 16);
                    applyFilter(CaptureImage.FILTER_BRIGHTNESS, parameters);
                    break;
                }

                case otmobile.R.id.ABIncreaseContrast:
                case otmobile.R.id.ABDecreaseContrast: {
                    // Apply the contrast filter.
                    HashMap<String, Object> parameters = new HashMap<>();
                    parameters.put(CaptureImage.FILTER_PARAM_CONTRAST_SCALE, menuID == otmobile.R.id.ABDecreaseContrast ? -64 : 64);
                    applyFilter(CaptureImage.FILTER_CONTRAST, parameters);
                    break;
                }

                case otmobile.R.id.ABRemoveNoise: {
                    // Apply the gray scale filter.
                    applyFilter(CaptureImage.FILTER_REMOVE_NOISE, getRemoveNoiseSize());
                    break;
                }

				case otmobile.R.id.ABDeskew: {
				    // Apply the deskew/perspective filter.
					applyFilter(CaptureImage.FILTER_PERSPECTIVE, null);
					break;
				}
				
				case otmobile.R.id.ABResize: {
				    // Resize the image to minus 200 pixels.
					HashMap<String, Object> parameters = new HashMap<>();
					Map<String, Object> properties = CaptureImage.getImageProperties();
					int imageWidth = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_WIDTH);
					int imageHeight = (Integer)properties.get(CaptureImage.IMAGE_PROPERTY_HEIGHT);
					
					// Resize width and height to be 80% of original width and height.
					parameters.put(CaptureImage.FILTER_PARAM_RESIZE_WIDTH, (int)(imageWidth * 0.80));
					parameters.put(CaptureImage.FILTER_PARAM_RESIZE_HEIGHT, (int) (imageHeight * 0.80));
					
					applyFilter(CaptureImage.FILTER_RESIZE, parameters);
					break;
				}
				
				case otmobile.R.id.ABRotate180:
				case otmobile.R.id.ABRotateLeft:
				case otmobile.R.id.ABRotateRight: {
				    // Rotate the image.
					HashMap<String, Object> parameters = new HashMap<>();
					parameters.put(CaptureImage.FILTER_PARAM_ROTATION_DEGREE, menuID == otmobile.R.id.ABRotateLeft ? 270 : (menuID == otmobile.R.id.ABRotateRight ? 90 : 180));
					applyFilter(CaptureImage.FILTER_ROTATION, parameters);
					break;
				}
				
				case otmobile.R.id.ABCrop: {
				    // Launch image cropping activity.
				    Intent intent = new Intent(this, EnhanceImageCropActivity.class);
				    startActivityForResult(intent, otmobile.R.id.ABCrop);
					break;
				}

                case otmobile.R.id.ABQuadCrop: {
					QCrop();
					break;
                }
				
				case otmobile.R.id.ABAutoCrop: {
				    // Apply the auto-cropping operation.
					applyFilter(CaptureImage.FILTER_CROP, getAutoCropPadding());
					break;
				}

				default: {
					return super.onOptionsItemSelected(item);
				}
			}
		}
        catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			CoreHelper.displayError(this, e);
		}

		return true;
    }

	/**
	 * The Undo All button handler. This will revert any changes made.
	 * @param view    The view for the control event.
	 */
	public void onUndoAll(View view) {
		// Cancel any edits, reload the image, and tell the view to refresh.
		undoAll();
	}
	public void onSendToSnap(View view) {

		PostToCaptiva Snap = new PostToCaptiva(this);
		Snap.OriginalImage = _filename;

		// If the image has not been edited, then leave without saving.
		if (!_imgEdited)
		{
			Snap.FileName = _filename;
			filenames.add(_filename);
		}

		// If the image has been edited, then save a copy of the image under a new filename to the gallery.
		try {
			String fullpath = saveCurrentImage();
			_newFileName = fullpath;

			// Let Android know so that it shows immediately in the image gallery. Note that TIFF
			// images cannot be viewed by the Android gallery viewer as of 4.2.2. However, if you
			// save a TIFF image to the gallery storage folder, it will still save and you can
			// verify that it is there by using the Android "My Files" application if available
			// on your device, or the Android Debug Bridge (adb). You can get the path to the file
			// by debugging this application's save function.
			if (fullpath != null) {
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(fullpath))));
				Snap.FileName = _newFileName;
				filenames.add(_newFileName);
			}
		}
		catch (CaptureException e) {
			// If an exception happens we finish this activity and send back the cancel result when the error dialog is dimissed.
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					completeAndReturn(RESULT_CANCELED);
				}
			};

			CoreHelper.displayError(this, e, listener);
		}

		Snap.execute();

	}

	/**
     * The Undo button handler. This will the last change made (one level undo only).
     * @param view    The view for the control event.
     */
    public void onUndo(View view) {

        // If the one-level undo stack is empty, notify user and do nothing.
        if (!CaptureImage.undoImageChanges()) {
            CoreHelper.displayError(this, CaptureImage.getLastError());
            return;
        }

        // Get a bitmap of the SDK image and set it to the ImageView.
        _imageView.setImageBitmap(getImage());
    }

    /* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed(){
	    
		 // If the image has not been edited, then leave without saving.
		 if (!_imgEdited)
		 {
			 Intent FScreen = new Intent(this,FirstScreen.class);
			 startActivity(FScreen);
			 finish();
			return;
		 }

		 // If the image has been edited, then save a copy of the image under a new filename to the gallery.
	     try {	        
            String fullpath = saveCurrentImage();
			_newFileName = fullpath;

            // Let Android know so that it shows immediately in the image gallery. Note that TIFF
            // images cannot be viewed by the Android gallery viewer as of 4.2.2. However, if you 
            // save a TIFF image to the gallery storage folder, it will still save and you can 
            // verify that it is there by using the Android "My Files" application if available 
            // on your device, or the Android Debug Bridge (adb). You can get the path to the file 
            // by debugging this application's save function.
            if (fullpath != null) {
            	sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(fullpath))));
            }
            
            // Finish this activity and return the result.
			 Intent FScreen = new Intent(this,FirstScreen.class);
			 startActivity(FScreen);
	        completeAndReturn(RESULT_OK);                
	    }
	    catch (CaptureException e) {
	        // If an exception happens we finish this activity and send back the cancel result when the error dialog is dimissed.
	        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					completeAndReturn(RESULT_CANCELED);
				}
			};
			
            CoreHelper.displayError(this, e, listener);                       
        }
	}
	
	/*
	 * (non-Javadoc) 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu.
		getMenuInflater().inflate(otmobile.R.menu.activity_enhance_image, menu);
		_menu = menu;
		return true;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		// If we don't have the focus, then no need to do anything.
		if (!hasFocus) {
		    return;
		}
		
		// Handle refreshing of the image here.
		if (!_displayed && _filename != null) {
		    // We haven't loaded the image yet so do that now.
			loadImage();
			_displayed = true;
		} 
		else {
		    // Refresh our image as we have a UI state change.
		    _imageView.setImageBitmap(getImage());
		}

		// Update our UI to reflect edit changes.
		if (_imgEdited) {
		    startEdit();
		} 
		else {
			cancelEdit();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);
		super.onCreate(null);
		setContentView(otmobile.R.layout.activity_enhance_image);
		
		// Get filename to load.
		Bundle b = getIntent().getExtras();
		picturecount = picturecount + 1;
		_filename = b.getString("Filename");
		b.clear();
		
		// Populate members.
		_imageView = (PZImageView) findViewById(otmobile.R.id.ImageView);
		_progressBar = (ProgressBar) findViewById(otmobile.R.id.ProgressStatusBar);
		_undoButton = (Button) findViewById(otmobile.R.id.UndoButton);
		_undoAllButton = (Button) findViewById(otmobile.R.id.UndoAllButton);
		_enhanceLayout = (RelativeLayout) findViewById(otmobile.R.id.EnhanceLayout);
		undoAll();
		
		// Determine whether we are being launched for the first time or have been rotated
		// and then set the appropriate edit mode from the start.
		if (!MainActivity._newLoad && _imgEdited) {
		    startEdit();
		} else {
		    _imgEdited = false;
		}

        // Set the enable undo of CaptureImage to that of the SdkSampleApp shared preferences.
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
        _enableUndo = gprefs.getBoolean(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_FILTER_ENABLE_UNDO), false);
        CaptureImage.enableUndoImage(_enableUndo);
		QCrop();
    }
	
	/**
	 * If crop applied then set edit mode = true;
	 * @param requestCode   The request code passed back from Crop.
	 * @param resultCode    The result code passed back from Crop.
	 * @param data 			Data passed back from Crop. 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == otmobile.R.id.ABCrop || requestCode == otmobile.R.id.ABQuadCrop) && resultCode == RESULT_OK ) {
        	startEdit();
        }
    }

	/*
	 * Undoes all edits to the image and restores the original.  Used by the undo buttons.
	 */
	private void undoAll() {
		cancelEdit();
		MainActivity._newLoad = true;
		loadImage();
		_imageView.invalidate();
	}

	/**
	 * Finish the activity based on the result code.
	 * @param resultCode    The result code to pass back.
	 */
	private void completeAndReturn(int resultCode) {
	    // Pass back our result and finish.
	    Intent retData = new Intent();
        setResult(resultCode, retData);
        finish();
	}

	/**
     * Will display an infinite progress bar if present.
     * @param enable        Pass <code>true</code> to display and <code>false</code> to hide.   
     */
	private void setProgressBar(boolean enable) {
        // If we have a valid progress bar then either enable or disable appropriately.
        if (_progressBar != null) {
        	_progressBar.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }    
    
	/**
	 * Set our edit mode and update the display to show that we have been edited.
	 */
	private void startEdit() {
		_imgEdited = true;
		_undoAllButton.setVisibility(View.VISIBLE);
		_undoButton.setVisibility(View.VISIBLE);
	}

	/**
	 * Cancel our edit mode and update the display to show that nothing has been edited.
	 */
	private void cancelEdit() {
		_imgEdited = false;
        _undoAllButton.setVisibility(View.INVISIBLE);
		_undoButton.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Load the image for the filename passed into this activity.
	 */
	private void loadImage() {
	    
	    // If the file exists then load the image into the SDK and display it.
		File file = new File(_filename);
		if (file.exists()) {
            Log.v (TAG, "Loading File = " + _filename);
            try {
                // We only load a new image if we are being re-launched from the main
                // activity or if there was an undo performed. This prevents a rotation
                // of the device from losing all the changes since a rotation will
                // destroy the activity and call onCreate again.
                if (MainActivity._newLoad) {
                    CaptureImage.load(file.getAbsolutePath());
                    MainActivity._newLoad = false;
                }
				
				_imageView.setImageBitmap(getImage());
            }
            catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
                CoreHelper.displayError(this, e);
            }
		}
	}
	
	/**
	 * When we retrieve the bitmap, we will attempt to save memory on the device by scaling the
	 * image to the size that will allow the image to fit into the image view. This does
	 * not affect the image actually loaded into the SDK. This only changes the image used
	 * to display. We could skip the scaling and just display the image as it, but large
	 * images could use up a lot of the memory on the device.
	 * @return    The bitmap to use for the display.
	 */
	private Bitmap getImage() {
	    
	    // Set the new rectangle to represent the new image's size.
        Rect rect = CoreHelper.calcImageSizePx(_imageView, 0);
	    
	    // Tell the SDK to scale the image to the new size.
		Bitmap bitmap = null;
		if (rect.width() > 0 && rect.height() > 0){
			bitmap = CaptureImage.getImageForDisplay(rect.width(), rect.height(), null);
		}
		
		// Return the newly scaled image.
		return bitmap;
	}	
	
	/**
	 * This function starts and asynchronous task to perform the operation.
	 * It will start a progress bar to display to the user while it is executing and
	 * then cancel the progress bar when finished. 
	 * @param operation    The filter operation to perform.
	 * @param parameters   The parameters for the filter.
	 */
	private void applyFilter(String operation, Map<String, Object> parameters) {	
		final String op = operation;
		final Map<String, Object> param = parameters;
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void> () {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					CaptureImage.applyFilters(new String[] { op }, param);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
				
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				
			    // Set the image, turn of progress bar, and enable controls.
			    _imageView.setImageBitmap(getImage());
				setProgressBar(false);
				if (_menu != null) {
				    _menu.setGroupVisible(otmobile.R.id.ABMainGroup, true);
				}
				
				_enhanceLayout.setEnabled(true);
				_undoAllButton.setEnabled(true);
                _undoButton.setEnabled(true);

		        _imageView._preventGesture = false;
				startEdit();
			}			
		};
	
		// Disable controls while filter is applied.
		_imageView._preventGesture = true;
		if (_menu != null) {
		    // This will cause slight flicker on some devices.
		    _menu.setGroupVisible(otmobile.R.id.ABMainGroup, false);
		}
		
		_enhanceLayout.setEnabled(false);
		_undoAllButton.setEnabled(false);
        _undoButton.setEnabled(false);

		setProgressBar(true);

		task.execute((Void) null);
	} 

	/**
	 * This function saves the currently loaded image to a new file in the gallery directory.
	 * @return                     The generated filename for this new file.
	 * @throws CaptureException    The type of exception that can be thrown from this save operation.
	 */
	private String saveCurrentImage() throws CaptureException {
	    String fileName;
		try {	
		    // Get the preferences for saving.            
            SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
            String imgFormat = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_IMAGEFORMAT), CaptureImage.SAVE_JPG);
            String temp = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_JPGQUALITY), "95");
            Integer jpgQuality = CoreHelper.getInteger(temp, 95);
            temp = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_DPIX), "0");
            Integer dpix = CoreHelper.getInteger(temp, 0);
            temp = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_DPIY), "0");
            Integer dpiy = CoreHelper.getInteger(temp, 0);
            
            // Prepare a new filename.
            File fullpath = new File(CoreHelper.getImageGalleryPath(), CoreHelper.getUniqueFilename("Img", "." + imgFormat));
		    fileName = fullpath.toString();
		    
		    // Normalize the extension to the formats we support.
		    if (imgFormat == null || (imgFormat.compareToIgnoreCase(CaptureImage.SAVE_JPG) != 0 &&
		    		imgFormat.compareToIgnoreCase(CaptureImage.SAVE_PNG) != 0 &&
		    		imgFormat.compareToIgnoreCase(CaptureImage.SAVE_TIF) != 0)) {
		        imgFormat = CaptureImage.SAVE_JPG;
		    }

            // Build the save parameters.
            HashMap<String, Object> parameters = new HashMap<>();
            if (dpix > 0) {
                parameters.put(CaptureImage.SAVE_DPIX, dpix);
            }
            
            if (dpiy > 0) {
                parameters.put(CaptureImage.SAVE_DPIY, dpiy);
            }
            
		    if (imgFormat.compareToIgnoreCase(CaptureImage.SAVE_JPG) == 0) {
		        parameters.put(CaptureImage.SAVE_JPG_QUALITY, jpgQuality);
		    }
		    
            // Save the file.
		    CaptureImage.saveToFile(fileName, imgFormat, parameters);
		}
	    catch (CaptureException e) {
	    	Log.e(TAG, e.getMessage(), e);
	    	throw e;
	    }
		
		return fileName;
	}
	
	/**
	 * Retrieve preferences for autocrop padding.
	 * @return The padding value in a Map.
	 */
	private Map<String, Object> getAutoCropPadding() {
		HashMap<String, Object> parameters = new HashMap<>();
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
        String paddingText = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_FILTER_CROP_PADDING), "0.0");
		float padding = CoreHelper.getFloat(paddingText, 0.0f);
		
		parameters.put(CaptureImage.FILTER_PARAM_CROP_PADDING, padding);
		
		return parameters;
	}

    private Map<String, Object> getRemoveNoiseSize() {
        HashMap<String, Object> parameters = new HashMap<>();
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
        String noiseSizeText = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_FILTER_REMOVE_NOISE), "7");
        int noiseSize = CoreHelper.getInteger(noiseSizeText, 7);

        parameters.put(CaptureImage.FILTER_PARAM_NOISE_SIZE, noiseSize);

        return parameters;
    }
	/**
	 * Retrieve preferences for adaptive threshold force parameter.
	 * @return The force value in a Map.
	 */
	private Map<String, Object> getAdaptiveThresholdParameters() {
		HashMap<String, Object> parameters = new HashMap<>();
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean force = gprefs.getBoolean(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_FILTER_ADAPTIVE_BINARY_FORCE), true);
		                
		parameters.put(CaptureImage.FILTER_PARAM_ADAPTIVE_BINARY_FORCE, force);

		String blacknessText = gprefs.getString(CoreHelper.getStringResource(this, otmobile.R.string.GPREF_FILTER_ADAPTIVE_BINARY_BLACKNESS), "6");
        int blackness = CoreHelper.getInteger(blacknessText, 6);
		parameters.put(CaptureImage.FILTER_PARAM_ADAPTIVE_BINARY_BLACKNESS, blackness);

		return parameters;
	}		
}

