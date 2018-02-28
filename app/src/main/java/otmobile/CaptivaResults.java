package otmobile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class CaptivaResults extends Activity {
    public String _ticket = "";
    private Context context;
    private String fileID;
    private String SnapFileName;
    private String contentType;
    private String UIMString = "";
    private JSONObject uimObject;
    private JSONArray DocValues;
    private String UIMDocType;
    private int NumValues = 0;
    private String filename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //First get the Image View of the results
        Bundle bundle = getIntent().getExtras();
        String FileName = bundle.getString("FileName");
        UIMString = bundle.getString("UIM");
        filename = bundle.getString("fnURI");

        _ticket = bundle.getString("Ticket");
        fileID = bundle.getString("fileID");
        SnapFileName = bundle.getString("SnapFileName");
        contentType = bundle.getString("contentType");

        setContentView(otmobile.R.layout.activity_snap_results);
        setPicture(FileName);
        try {
            uimObject = new JSONObject(UIMString);
            PrepareUIM();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button _pdf = (Button) findViewById(otmobile.R.id.btnPDF);

        _pdf.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                onPDFClick(v);
            }
        });

        Button _finish = (Button) findViewById(otmobile.R.id.btnFinish);

        _finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                onFinishClick(v);
            }
        });

        Button _export = (Button) findViewById(otmobile.R.id.btn_export);

        _export.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                onExportClick(v);
            }
        });
    }
    private void onExportClick(View view) {
        //Create the export Object
        CaptivaExport SE = new CaptivaExport(this);
        SE.FileID = fileID;
        SE.SnapFileName = SnapFileName;
        SE.UIMDocType = UIMDocType;
        SE.uimObJect = uimObject;
        SE._ticket = _ticket;
        SE.execute();
    }



    private void setPicture (String FileName) {
        Log.d("File",Uri.parse(FileName).toString());
        LinearLayout scrollContainer = (LinearLayout)findViewById(otmobile.R.id.scrollContainer);
        ScrollView scrollView = (ScrollView)scrollContainer.findViewById(otmobile.R.id.scrollView);
        LinearLayout LinearResults = (LinearLayout)scrollView.findViewById(otmobile.R.id.LinearResults);
        ImageView imageView = (ImageView)LinearResults.findViewById(otmobile.R.id.imageView);
        //Set the picture
        File file = new File(FileName);
        Bitmap d = BitmapFactory.decodeFile(file.getAbsolutePath());
        int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
        imageView.setImageBitmap(scaled);
    }

    private void PrepareUIM() {
        try {
            Log.d("UIM",uimObject.toString());
            String documentType = uimObject.getString("docType");
            if (documentType.equals(""))
            {
                documentType = "Unclassified";
            }
            UIMDocType = documentType;
            LinearLayout Container = (LinearLayout)findViewById(otmobile.R.id.scrollContainer);
            ScrollView SView = (ScrollView)Container.findViewById(otmobile.R.id.scrollView);
            LinearLayout LView = (LinearLayout) SView.findViewById(otmobile.R.id.LinearResults);
            TextView TDocType = new TextView(this);
            TDocType.setText("Document Type");
            LView.addView(TDocType);
            EditText EDocType = new EditText(this);
            EDocType.setText(documentType);
            LView.addView(EDocType);
            DocValues = uimObject.getJSONArray("nodeList");
            Integer N = DocValues.length();
            final TextView[] myTextViews = new TextView[N]; // create an empty array;
            final EditText[] myEditText = new EditText[N];
            NumValues = DocValues.length();
            for(int i = 0; i < DocValues.length(); i++){
                final TextView rowTextView = new TextView(this);
                final EditText rowEditText = new EditText(this);

                String name = DocValues.getJSONObject(i).getString("labelText");
                String Fieldtype = DocValues.getJSONObject(i).getString("indexFieldType");
                rowTextView.setText(name);
                LView.addView(rowTextView);
                myTextViews[i] = rowTextView;
                String value = DocValues.getJSONObject(i).getJSONArray("data").getJSONObject(0).getString("value");
                //need to create a final to pass in the position to the listener
                final int x;
                x = i;
                rowEditText.setText(value);
                Log.d("Field Type",Fieldtype);
                if (Fieldtype.equals("DateTime")) {
                    Log.d("Field Type",Fieldtype);
                    //remove the time characters
                    value = value.replace("T00:00:00.0000000","");
                    Log.d("New Date Value",value);
                    rowEditText.setText(value);
                    //rowEditText.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
                    rowTextView.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                }
                LView.addView(rowEditText);
                myEditText[i] = rowEditText;
                //Now try to add the listener....
                myEditText[i].addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        try {
                            DocValues.getJSONObject(x).getJSONArray("data").getJSONObject(0).put("value",charSequence);
                            uimObject.put("nodeList",DocValues);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                Log.d(name,value);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onPDFClick(View view) {
        // Launch the preference settings activity.
        Log.d("Button","PDF Pressed");
        DownloadPDF DPDF = new DownloadPDF(this);
        DPDF._ticket = _ticket;
        DPDF.fileID = fileID;
        DPDF.contentType = contentType;
        DPDF.SnapFileName = SnapFileName;
        DPDF.execute();
    }

    public void onFinishClick(View view) {
        // Launch the preference settings activity.
        Log.d("Finish","Finish Clicked");
        Intent FScreen = new Intent(this,FirstScreen.class);
        startActivity(FScreen);
        finish();
    }
    @Override
    public void onBackPressed() {
        //Go back to the image enhancement screen

        Intent intent = new Intent(this, EnhanceImageActivity.class);
        intent.putExtra("Filename", filename);
        boolean _newLoad = true;
        _newLoad = true;
        startActivity(intent);
        finish();
    }
}
