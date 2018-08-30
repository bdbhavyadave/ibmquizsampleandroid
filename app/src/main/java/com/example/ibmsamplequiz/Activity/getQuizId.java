package com.example.ibmsamplequiz.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blikoon.qrcodescanner.QrCodeActivity;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.SearchResult;
import com.cloudant.client.org.lightcouch.CouchDbException;
import com.example.ibmsamplequiz.Helper.ObjectSerializer;
import com.example.ibmsamplequiz.R;
import com.example.ibmsamplequiz.modelClass.Doc;


import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.VIBRATE;

public class getQuizId extends AppCompatActivity {

    EditText quizIdEdit;
    AppCompatButton quizIdSubmit, scanQR;
    ProgressDialog pDialog;
    SharedPreferences shared_quizDetails;
    SharedPreferences.Editor edit_quizDetails;

    private static final int REQUEST_CODE_QR_SCAN = 101;
    public static final int RequestPermissionCode = 7;

    String quizId;
    String ref;
    int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_quiz_id);

        shared_quizDetails = getSharedPreferences("QuizDetails", Context.MODE_PRIVATE);
        edit_quizDetails = shared_quizDetails.edit();

        quizIdEdit = findViewById(R.id.quizIdEdit);
        quizIdSubmit = findViewById(R.id.quizIdSubmit);
        scanQR = findViewById(R.id.scanQR);

        if (CheckingPermissionIsEnabledOrNot()) {
        } else {

            //Calling method to enable permission.
            RequestMultiplePermission();

        }


        quizIdSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quizId = quizIdEdit.getText().toString().trim();
                edit_quizDetails.clear().apply();

                if (quizId.equals("")) {
                    Toast.makeText(getQuizId.this, "Enter a quiz Id!", Toast.LENGTH_SHORT).show();
                } else {
                    buildProgressBar();
                    new AsyncCaller().execute();

                }
            }
        });

        quizIdEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 0) {
                    for (int i = 0; i < s.length(); i++) {
                        char currentCharacter = s.toString().charAt(i);

                        Log.d("character", "" + currentCharacter);

                        if (Character.isLowerCase(currentCharacter)) {
                            quizIdEdit.setText(s.toString().toUpperCase());
                            quizIdEdit.setSelection(s.length());
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    edit_quizDetails.clear().apply();
                    Intent i = new Intent(getQuizId.this, QrCodeActivity.class);
                    startActivityForResult(i, REQUEST_CODE_QR_SCAN);
                } else {
                    Toast.makeText(getQuizId.this, "Allow the application to use Camera!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private class AsyncCaller extends AsyncTask <Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {


            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            try {
                CloudantClient client = ClientBuilder.account("0394f385-5643-40db-b1aa-94749be22786-bluemix")
                        .username("0394f385-5643-40db-b1aa-94749be22786-bluemix")
                        .password("359ceb21f168b585b78d9386e2220d7bb49cfe6cc55399704532996ee267be2a")
                        .build();

                System.out.println("Server Version: " + client.serverVersion());

                Database db = client.database("quizzes", false);


                SearchResult <Doc> result = db.search("searchquiz/quizidindex")
                        .includeDocs(true)
                        .querySearchResult("quizid:" + quizId, Doc.class);


                Doc doc = result.getRows().get(0).getDoc();

                System.out.println(doc.getQuestions().get(0).getStatement());


                ArrayList <String> statements = new ArrayList <String>();
                ArrayList <String> optionA = new ArrayList <String>();
                ArrayList <String> optionB = new ArrayList <String>();
                ArrayList <String> optionC = new ArrayList <String>();
                ArrayList <String> optionD = new ArrayList <String>();
                ArrayList <String> correct = new ArrayList <String>();

                int size = doc.getQuestions().size();
                for (int i = 0; i < size; i++) {
                    statements.add(doc.getQuestions().get(i).getStatement());
                    optionA.add(doc.getQuestions().get(i).getOptions().get(0));
                    optionB.add(doc.getQuestions().get(i).getOptions().get(1));
                    optionC.add(doc.getQuestions().get(i).getOptions().get(2));
                    optionD.add(doc.getQuestions().get(i).getOptions().get(3));
                    correct.add(doc.getQuestions().get(i).getCorrectOption());


                }

                edit_quizDetails.putString("quizid", quizId);
                edit_quizDetails.putString("Statements", ObjectSerializer.serialize(statements));
                edit_quizDetails.putString("optionA", ObjectSerializer.serialize(optionA));
                edit_quizDetails.putString("optionB", ObjectSerializer.serialize(optionB));
                edit_quizDetails.putString("optionC", ObjectSerializer.serialize(optionC));
                edit_quizDetails.putString("optionD", ObjectSerializer.serialize(optionD));
                edit_quizDetails.putString("correct", ObjectSerializer.serialize(correct));
                edit_quizDetails.putString("Title", doc.getTitle());
                edit_quizDetails.putInt("passing", doc.getPassing());
                edit_quizDetails.putInt("totalTime", doc.getTotalTime());
                edit_quizDetails.putInt("marksPerQues", doc.getMarksPerQuestion());
                edit_quizDetails.putInt("noOfQues", doc.getNumberOfQuestions());
                edit_quizDetails.apply();

                flag = 0;
//                    List <String> databases = client.getAllDbs();
//                    System.out.println("All my databases : ");
//                    for (String data : databases) {
//                        System.out.println(data);
//                    }
//                }
            } catch (CouchDbException e) {
                flag = 1;
                e.printStackTrace();
            } catch (Exception e) {
                flag = 2;
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // this method will be running on UI thread
            pDialog.dismiss();
            if (flag == 0) {
                Intent i = new Intent(getQuizId.this, NameEmail.class);
                startActivity(i);
            } else if (flag == 1) {
                Toast.makeText(getQuizId.this, "Unstable network connection!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getQuizId.this, "Quiz not found!", Toast.LENGTH_SHORT).show();

            }
        }

    }

    private void buildProgressBar() {
        pDialog = new ProgressDialog(getQuizId.this);
        pDialog.setMessage("Finding Your Quiz");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();

    }

    @Override
    public void onBackPressed() {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            Log.d("log", "COULD NOT GET A GOOD RESULT.");
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if (result != null) {
                AlertDialog alertDialog = new AlertDialog.Builder(getQuizId.this).create();
                alertDialog.setTitle("Scan Error");
                alertDialog.setMessage("QR Code could not be scanned");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            return;

        }
        if (requestCode == REQUEST_CODE_QR_SCAN) {
            if (data == null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            Log.d("Tag", "Have scan result in your app activity :" + result);

            if (isNotURL(result)) {
                quizId = result;

                    buildProgressBar();
                    new AsyncCaller().execute();

            } else {
                Toast.makeText(this, "Scan correct QR code!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public boolean CheckingPermissionIsEnabledOrNot() {

        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), INTERNET);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_NETWORK_STATE);
        int ForthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int FifthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), VIBRATE);
        int SixthpermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        int SeventhpermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_WIFI_STATE);


        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ForthPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FifthPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SixthpermissionResult == PackageManager.PERMISSION_GRANTED &&
                SeventhpermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:

                if (grantResults.length > 0) {

                    boolean InternetPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadExternalStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean AccessNetworkStatePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean CameraPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean VibratePermission = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean wifiStatePermission = grantResults[6] == PackageManager.PERMISSION_GRANTED;


                    if (InternetPermission && ReadExternalStoragePermission && AccessNetworkStatePermission && CameraPermission && ReadExternalStoragePermission && VibratePermission && RecordPermission && wifiStatePermission) {

                        Toast.makeText(getQuizId.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                }

                break;
        }

    }

    private void RequestMultiplePermission() {

        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(getQuizId.this, new String[]
                {
                        INTERNET,
                        READ_EXTERNAL_STORAGE,
                        ACCESS_NETWORK_STATE,
                        CAMERA,
                        VIBRATE,
                        RECORD_AUDIO,
                        ACCESS_WIFI_STATE
                }, RequestPermissionCode);

    }

    private boolean isNotURL(String result) {
        if (Patterns.WEB_URL.matcher(result).matches()) {
            return false;
        } else if (result.contains("https://") || result.contains("http://")) {
            return false;
        } else return true;
    }

}
