package otmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.R.style.Theme_DeviceDefault_Light_Dialog;

/**
 * Created by hutchg on 12/01/2017.
 */

public class CaptivaExport extends AsyncTask {

    private Context context;
    private ProgressDialog dialog;
    private RequestQueue queue;
    public String FileID;
    public String SnapFileName;
    public JSONObject uimObJect;
    public String UIMDocType;
    public String _ticket;
    private class ExportRequest {
        String env;
        String captureFlow;
        String batchName;
        int batchRootLevel;
        int batchPriority;
        String pdfGeneration;
        int iAServerConnection;
        String machineId;
        String dispatch;
        node[] nodes;
        value[] values;
    }
    private class node {
        int nodeId;
        int parentId;
    }
    private class value {
        int nodeId;
        String valueName;
        Object value;
        String valueType;
        int offset;
        String fileExtension;
    }
    public CaptivaExport(Context context){
        dialog = new ProgressDialog(context,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.context = context;
        //Start the Request Queue
        HttpsTrustManager.allowAllSSL();
        queue = Volley.newRequestQueue(context);
        queue.start();
    }
    protected void onPreExecute() {
        dialog.setMessage("Exporting Documents");
        dialog.show();
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Export();
        return null;
    }
    private void Export() {
        //Now we need to create the Batch Information
        ExportRequest ER = new ExportRequest();
        //specify the size of value array + 1 for the image to upload
        //First Create the Nodes
        node[] nodes = new node[2];
        nodes[0] = new node();
        //First set the root Document Node;
        //This is where all of the values will be saves
        nodes[0].nodeId = 1;
        nodes[0].parentId = 0;
        //Now set the image page node
        nodes[1] = new node();
        nodes[1].nodeId = 2;
        nodes[1].parentId = 1;
        //Set the Values for the image file
        value[] values = new value[3];
        values[0] = new value();
        values[0].valueType = "file";
        values[0].value = FileID;
        values[0].valueName = "OutputImage";
        values[0].fileExtension = SnapFileName.substring(SnapFileName.lastIndexOf(".")+1);
        values[0].nodeId = 2;

        //Now set the UIM data
        values[1] = new value();
        values[1].nodeId = 1;
        values[1].valueName = "UimData";
        values[1].valueType = "uimdata";
        values[1].value = uimObJect.toString();

        values[2] = new value();
        values[2].nodeId = 1;
        values[2].valueType = "string";
        values[2].valueName = "UimDocumentType";
        values[2].value = UIMDocType;

        //Now build the request
        ER.nodes = nodes;
        ER.values = values;
        ER.batchName = "MyBatch_{NextId}";
        ER.batchRootLevel =2;
        ER.dispatch = "S";
        ER.pdfGeneration = "OnePerDocument";
        ER.machineId = "";
        //Get the values from the preferences
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(context);
        ER.captureFlow = gprefs.getString("Captiva CaptureFlow","");
        ER.env = gprefs.getString("Snap Environment","");

        //Now create the post
        String url = gprefs.getString("Captiva URL", "");
        url = url + "/cp-rest/session/batches";
        Gson gson = new Gson();
        String json = gson.toJson(ER);
        Log.d("JSONString",json);
        JSONObject JImage = null;
        try {
            JImage = new JSONObject(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest EXPreq = new JsonObjectRequest(url, JImage,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // handle response
                        Log.d("Classify OK",response.toString());
                        //Get the response back
                        try {
                            String a = response.toString();
                            a = a + response.getString("title");
                            a = a;
                            dialog.dismiss();
                            ShowDialog("","Export Complete");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //If it fails look for the error message
                            try {
                                String ErrorMessage= response.getJSONArray("resultItems").getJSONObject(0).getString("errorMessage");
                                dialog.dismiss();
                                ShowDialog("Export Error",ErrorMessage);
                                //G0 back to the main menu
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;
                if(response != null && response.data != null){
                    String json = null;
                    json = new String(response.data);
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(json);
                        json = obj.getString("message");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(json != null) {
                        Log.d("Export Error",json);
                        ShowDialog("Export Error",json);
                    }
                } }
        })
        {
            @Override
            public String getBodyContentType()
            {
                return "application/vnd.emc.captiva+json; charset=utf-8";
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("CPTV-TICKET", _ticket);
                headers.put("Content-Type", "application/vnd.emc.captiva+json; charset=utf-8");
                return headers;
            }
        };

        int socketTimeout = 30000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        EXPreq.setRetryPolicy(policy);
        queue.add(EXPreq);
    }

    private void ShowDialog (String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(
                context,Theme_DeviceDefault_Light_Dialog).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);



        // Setting OK Button
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to execute after dialog closed
                Intent intent = new Intent(context, FirstScreen.class);
                context.startActivity(intent);

            }
        });

        // Showing Alert Message
        alertDialog.show();
        alertDialog.setIcon(R.drawable.otappicon);
        Button btn_OK = alertDialog.getButton(alertDialog.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) btn_OK.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER;

        btn_OK.setLayoutParams(positiveButtonLL);
    }
}
