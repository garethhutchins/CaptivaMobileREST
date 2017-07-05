package otmobile;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.util.Log;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hutchg on 18/04/2016.
 */
public class DownloadPDF extends AsyncTask {
    private Context context;
    private ProgressDialog dialog;
    private RequestQueue queue;
    public String _ticket = "";
    public String fileID;
    public String SnapFileName;
    public String contentType;
    public String _PDFFileID;



    private class serviceProps {
        public String name;
        public String value;
    }

    private class requestItems {
        public Integer nodeID;
        public values values[] = new values[2];
        public files files[] = new files[1];
    }

    private class values {
        public String name;
        public String value;
    }

    private class files {
        public String name;
        public String value;
        public String contentType;
    }

    private class FullPageOCRRequest {
        public serviceProps serviceProps[] = new serviceProps[2];
        public requestItems requestItems[] = new requestItems[1];
    }

    public DownloadPDF(Context context) {
        dialog = new ProgressDialog(context,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.context = context;
        //Start the Request Queue
        HttpsTrustManager.allowAllSSL();
        queue = Volley.newRequestQueue(context);
        queue.start();
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Converting to PDF");
        dialog.show();
        super.onPreExecute();
    }
    public void showPdf(File DownloadFile)
    {
        Uri DownloadURI = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", DownloadFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(DownloadURI, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "Open PDF"));
        /**
        File file = new File(DownloadFile);
        PackageManager packageManager = context.getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/pdf");
        context.startActivity(intent);
         **/
    }
    private void Download() {
        dialog.setMessage("Downloading PDF");
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(context);
        String url = gprefs.getString("Snap URL", "");
        url = url + "/cp-rest/session/files/" + _PDFFileID;
        Log.d("Requesting File",url);

        //
        try {
            URL urlOBJ = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlOBJ.openConnection();

            connection.addRequestProperty("CPTV-TICKET",_ticket);
            connection.setRequestProperty("CPTV-TICKET",_ticket);
            connection.connect();
            File file;
            int lenghtOfFile = connection.getContentLength();

            // download the file

            InputStream input = new BufferedInputStream(connection.getInputStream());
            String DownloadFile = "/sdcard/downloadedfile.pdf";
            file = new File(Environment.getExternalStorageDirectory(),"DownloadedFile.pdf");

            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];

            long total = 0;

            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress(""+(int)((total*100)/lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            //Stop the dialog
            dialog.dismiss();
            //Now Open the file
            showPdf(file);


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private void Convert() {
        //First get the URL from the preferences
        SharedPreferences gprefs = PreferenceManager.getDefaultSharedPreferences(context);
        String url = gprefs.getString("Snap URL", "");
        url = url + "/cp-rest/session/services/fullpageocr";

        // Build the request
        files POSTFile = new files();
        POSTFile.contentType = contentType;
        POSTFile.name = SnapFileName;
        POSTFile.value = fileID;

        values FileType = new values();
        FileType.name = "OutputType";
        FileType.value = "Pdf";

        values Format = new values();
        Format.name = "Format";
        Format.value = "ImageOnText";

        requestItems RequestItem = new requestItems();

        RequestItem.nodeID = 1;
        RequestItem.values[0] = FileType;
        RequestItem.values[1] = Format;
        RequestItem.files[0] = POSTFile;

        serviceProps engine = new serviceProps();
        engine.name = "AutoRotate";
        engine.value = "False";

        serviceProps env = new serviceProps();
        env.name = "Env";
        env.value = gprefs.getString("Snap Environment","");

        FullPageOCRRequest FullPageOCRRequest = new FullPageOCRRequest();
        FullPageOCRRequest.serviceProps[0] = engine;
        FullPageOCRRequest.serviceProps[1] = env;
        FullPageOCRRequest.requestItems[0] = RequestItem;

        //Now post the request

        Gson gson = new Gson();
        String json = gson.toJson(FullPageOCRRequest);
        Log.d("JSONString",json);
        JSONObject JImage = null;
        try {
            JImage = new JSONObject(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest OCRreq = new JsonObjectRequest(url, JImage,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // handle response
                        Log.d("Classify OK",response.toString());
                        //Get the response back
                        try {
                            //Get the New file ID
                            String PDFFileID= response.getJSONArray("resultItems").getJSONObject(0).getJSONArray("files").getJSONObject(0).getString("value");
                            Log.d("PDF file ID",PDFFileID);
                            //Set the PDF File ID
                            _PDFFileID = PDFFileID;
                            Download();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //If it fails look for the error message
                            try {
                                String ErrorMessage= response.getJSONArray("resultItems").getJSONObject(0).getString("errorMessage");
                                ShowDialog("Error Reading File",ErrorMessage);
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
                        Log.d("PDF Creation Error",json);
                        ShowDialog("PDF Creation Error",json);
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
        OCRreq.setRetryPolicy(policy);
        queue.add(OCRreq);

    }



    @Override
    protected Object doInBackground(Object[] objects) {
        Convert();

        return null;
    }

    private void ShowDialog (String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(
                context).create();

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
    }
}
