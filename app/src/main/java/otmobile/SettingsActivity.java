/** -------------------------------------------------------------------------
 * Copyright 2013-2016 EMC Corporation.  All rights reserved.
 ---------------------------------------------------------------------------- */

package otmobile;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import java.util.Arrays;
import java.util.HashSet;

import emc.captiva.mobile.sdk.CaptureImage;

/**
 * This class handles managing all of the global preferences.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    /* (non-Javadoc)
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences, java.lang.String)
     */
    @SuppressWarnings("deprecation")
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        // Because this handler has preference sets that cause the handler to loop, let's unwire the handler
        // and then wire it back up at the end.
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        // Make sure our preferences conform to our allowed values.
        Preference p = findPreference(key);

        if (p instanceof EditTextPreference) {
            EditTextPreference pref = (EditTextPreference)p;            
            String temp;
            Integer i;
            Float f;
            //Save the Snap Settings
            if (key.compareToIgnoreCase("Captiva_USER") == 0) {
                temp = pref.getText();
                pref.setText(temp);
            }
            if (key.compareToIgnoreCase("Captiva_PASSWORD") == 0) {
                temp = pref.getText();
                pref.setText(temp);
            }
            if (key.compareToIgnoreCase("Captiva_URL") == 0) {
                temp = pref.getText();
                pref.setText(temp);
            }
            if (key.compareToIgnoreCase("IP_PROFILE") == 0) {
                temp = pref.getText();
                pref.setText(temp);
            }
            if (key.compareToIgnoreCase("Captiva_RECOGNITION_PROJECT") == 0) {
                temp = pref.getText();
                pref.setText(temp);
            }
            if (key.compareToIgnoreCase("Captiva_catpureflow") == 0) {
                temp = pref.getText();
                pref.setText(temp);
            }
            if (key.compareToIgnoreCase("GPREF_SENSOR_LIGHT_VALUE") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 10);
                i = Math.max(0, i);
                i = Math.min(5000, i);
                pref.setText(i.toString());
            }
            
            if (key.compareToIgnoreCase("GPREF_SENSOR_MOTION_VALUE") == 0) {
                temp = pref.getText();
                f = CoreHelper.getFloat(temp, .30f);
                f = Math.max(0.0f, f);
                f = Math.min(10.0f, f);
                pref.setText(f.toString());
            }
            
            if (key.compareToIgnoreCase("GPREF_CAPTUREDELAY") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 500);
                i = Math.max(0, i);
                pref.setText(i.toString());
            }

            if (key.compareToIgnoreCase("GPREF_CONTINUOUSCAPTUREFRAMEDELAY") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 500);
                i = Math.max(0, i);
                pref.setText(i.toString());
            }

            if (key.compareToIgnoreCase("GPREF_CAPTURETIMEOUT") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 2500);
                i = Math.max(0, i);
                pref.setText(i.toString());
            }

            if (key.compareToIgnoreCase("GPREF_CAPTURESIMILARITY") == 0) {
                temp = pref.getText();
                f = CoreHelper.getFloat(temp, 50f);
                f = Math.max(0, f);
                f = Math.min(f, 100);
                pref.setText(f.toString());
            }

            if (key.compareToIgnoreCase("GPREF_CAPTURECOUNT") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 2);
                i = Math.max(1, i);
                pref.setText(i.toString());
            }

            if (key.compareToIgnoreCase("GPREF_IMAGEFORMAT") == 0) {
                temp = pref.getText();
                if (temp == null || temp.isEmpty()) {
                    pref.setText(CaptureImage.SAVE_JPG);
                } 
                else if (temp.compareToIgnoreCase(CaptureImage.SAVE_JPG) != 0 && 
                		temp.compareToIgnoreCase(CaptureImage.SAVE_PNG) != 0 && 
                		temp.compareToIgnoreCase(CaptureImage.SAVE_TIF) != 0) {
                    pref.setText(CaptureImage.SAVE_JPG);
                }
            }
            
            if (key.compareToIgnoreCase("GPREF_JPGQUALITY") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 95);
                if (i < 0 || i > 100) {
                    pref.setText("95");
                } else {
                    pref.setText(i.toString());
                }
            }
            
            if (key.compareToIgnoreCase("GPREF_DPIX") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 0);
                if (i <= 0) {
                    pref.setText("");
                }                
            }

            if (key.compareToIgnoreCase("GPREF_DPIY") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 0);
                if (i <= 0) {
                    pref.setText("");
                }
            }
            
            if (key.compareToIgnoreCase("GPREF_FILTER_CROP_PADDING") == 0) {
            	temp = pref.getText();
                f = CoreHelper.getFloat(temp, 0.0f);
                f = Math.max(0.0f, f);
                f = Math.min(1.0f, f);
                pref.setText(f.toString());
            }

            if (key.compareToIgnoreCase("GPREF_FILTER_REMOVE_NOISE") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 7);
                if (i <= 0)
                {
                    pref.setText("0");
                }
                else
                {
                    pref.setText(i.toString());
                }
            }

            if (key.compareToIgnoreCase("GPREF_QUAD_CROP_COLOR") == 0) {
                temp = pref.getText();
                try {
                    Color.parseColor(temp);
                    pref.setText(temp);
                } catch (Exception exception) {
                    pref.setText("blue");
                }
            }

            if (key.compareToIgnoreCase("GPREF_QUAD_CROP_LINE_WIDTH") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 4);
                i = Math.max(1, i);
                pref.setText(i.toString());
            }

            if (key.compareToIgnoreCase("GPREF_QUAD_CROP_CIRCLE_RADIUS") == 0) {
                temp = pref.getText();
                i = CoreHelper.getInteger(temp, 24);
                i = Math.max(0, i);
                pref.setText(i.toString());
            }

            if (key.compareToIgnoreCase("GPREF_APPLICATIONID") == 0 ||
            	key.compareToIgnoreCase("GPREF_LICENSE") == 0) {
            	CoreHelper.license(this);
            }

            if (key.compareToIgnoreCase("GPREF_PICTURE_CROP_COLOR") == 0) {
                temp = pref.getText();
                try {
                    Color.parseColor(temp);
                    pref.setText(temp);
                } catch (Exception exception) {
                    pref.setText("green");
                }
            }

            if (key.compareToIgnoreCase("GPREF_PICTURE_CROP_WARNING_COLOR") == 0) {
                temp = pref.getText();
                try {
                    Color.parseColor(temp);
                    pref.setText(temp);
                } catch (Exception exception) {
                    pref.setText("red");
                }
            }

            if (key.compareToIgnoreCase("GPREF_PICTURE_CROP_ASPECT_WIDTH") == 0) {
                temp = pref.getText();
                float aspectWidthDefault = 8.5f;
                f = CoreHelper.getFloat(temp, aspectWidthDefault);
                // The width must be a positive value, set to default if not.
                if (f <= 0) {
                    f = aspectWidthDefault;
                }
                pref.setText(f.toString());
            }

            if (key.compareToIgnoreCase("GPREF_PICTURE_CROP_ASPECT_HEIGHT") == 0) {
                temp = pref.getText();
                float aspectHeightDefault = 11f;
                f = CoreHelper.getFloat(temp, aspectHeightDefault);
                // The height must be a positive value, set to default if not.
                if (f <= 0) {
                    f = aspectHeightDefault;
                }
                pref.setText(f.toString());
            }
        }

        // Wiring the preference changed handler back up.
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
    
    /* (non-Javadoc)
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        addPreferencesFromResource(otmobile.R.xml.preferences);
        addBarcodePreferences();
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        
        // Listen for changes.
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        // Cancel listening for changes.
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        // Now that our work is done, call the superclass onPause();
        super.onPause();
    }

    private String[] getBarcodeTypes() {
        return new String[] {
                CaptureImage.BARCODE_TYPE_ALL,
                CaptureImage.BARCODE_TYPE_ADD2,
                CaptureImage.BARCODE_TYPE_ADD5,
                CaptureImage.BARCODE_TYPE_AUSTRALIANPOST4STATE,
                CaptureImage.BARCODE_TYPE_AZTEC,
                CaptureImage.BARCODE_TYPE_BCDMATRIX,
                CaptureImage.BARCODE_TYPE_CODABAR,
                CaptureImage.BARCODE_TYPE_CODE128,
                CaptureImage.BARCODE_TYPE_CODE32,
                CaptureImage.BARCODE_TYPE_CODE39,
                CaptureImage.BARCODE_TYPE_CODE39EXTENDED,
                CaptureImage.BARCODE_TYPE_CODE93,
                CaptureImage.BARCODE_TYPE_CODE93EXTENDED,
                CaptureImage.BARCODE_TYPE_DATALOGIC2OF5,
                CaptureImage.BARCODE_TYPE_DATAMATRIX,
                CaptureImage.BARCODE_TYPE_EAN128,
                CaptureImage.BARCODE_TYPE_EAN13,
                CaptureImage.BARCODE_TYPE_EAN8,
                CaptureImage.BARCODE_TYPE_GS1DATABAR,
                CaptureImage.BARCODE_TYPE_IATA2OF5,
                CaptureImage.BARCODE_TYPE_INDUSTRY2OF5,
                CaptureImage.BARCODE_TYPE_INTELLIGENTMAIL,
                CaptureImage.BARCODE_TYPE_INTERLEAVED2OF5,
                CaptureImage.BARCODE_TYPE_INVERTED2OF5,
                CaptureImage.BARCODE_TYPE_MATRIX2OF5,
                CaptureImage.BARCODE_TYPE_PATCHCODE,
                CaptureImage.BARCODE_TYPE_PDF417,
                CaptureImage.BARCODE_TYPE_POSTNET,
                CaptureImage.BARCODE_TYPE_QRCODE,
                CaptureImage.BARCODE_TYPE_ROYALPOST4STATE,
                CaptureImage.BARCODE_TYPE_UPCA,
                CaptureImage.BARCODE_TYPE_UPCE
        };
    }

    private String[] remove(String[] values, String prefix) {
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].replace(prefix, "");
        }
        return values;
    }

    @SuppressWarnings("deprecation")
    private void addBarcodePreferences() {
        // Build up barcode preference UI from the CaptureImage barcode types
        PreferenceScreen screen = getPreferenceScreen();
        PreferenceCategory category = new PreferenceCategory(this);
        MultiSelectListPreference barcodeTypes = new MultiSelectListPreference(this);
        EditTextPreference barcodeCount = new EditTextPreference(this);

        category.setTitle(otmobile.R.string.GPREF_BARCODE_GROUP_TITLE);

        // Count preference
        barcodeCount.setTitle(otmobile.R.string.GPREF_BARCODE_COUNT_TITLE);
        barcodeCount.setDialogTitle(otmobile.R.string.GPREF_BARCODE_COUNT_TITLE);
        barcodeCount.setSummary(otmobile.R.string.GPREF_BARCODE_COUNT_SUMMARY);
        barcodeCount.setDefaultValue(getString(otmobile.R.string.GPREF_BARCODE_COUNT_DEFAULT));
        barcodeCount.setKey(getString(otmobile.R.string.GPREF_BARCODE_COUNT));

        // Type preference
        barcodeTypes.setTitle(otmobile.R.string.GPREF_BARCODE_TYPE_TITLE);
        barcodeTypes.setDialogTitle(otmobile.R.string.GPREF_BARCODE_TYPE_TITLE);
        barcodeTypes.setSummary(otmobile.R.string.GPREF_BARCODE_TYPE_SUMMARY);
        barcodeTypes.setEntries(remove(getBarcodeTypes(), "BarcodeType"));
        barcodeTypes.setEntryValues(getBarcodeTypes());
        barcodeTypes.setDefaultValue(new HashSet<String>(Arrays.asList(CaptureImage.BARCODE_TYPE_ALL)));
        barcodeTypes.setKey(getString(otmobile.R.string.GPREF_BARCODE_TYPE));

        screen.addPreference(category);
        category.addPreference(barcodeTypes);
        category.addPreference(barcodeCount);
    }
}