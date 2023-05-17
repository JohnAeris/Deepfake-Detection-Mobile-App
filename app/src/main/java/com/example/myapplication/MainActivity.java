package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // variables declarations
    View fileContainer;
    Button uploadBtn, detectBtn;
    TextView filename;
    VideoView videoContainer;

    // initial file path
    String file_path = null;

    // request codes
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_GALLERY = 2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileContainer = findViewById(R.id.fileContainer);
        uploadBtn = findViewById(R.id.uploadButton);
        detectBtn = findViewById(R.id.detectButton);
        filename = findViewById(R.id.filenameContainer);
        videoContainer = findViewById(R.id.videoContainer);

        // upload button
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=23){
                    if(checkPermission()){
                        filePicker();
                    }
                    else{
                        requestPermission();
                    }
                }
                else{
                    filePicker();
                }
            }
        });

        // detect button
        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(file_path != null){
                    UploadFile();
                }
                else{
                    Toast.makeText(MainActivity.this, "Please select a file first", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    // POST request: sending file to API
    private void UploadFile() {
        UploadTask uploadTask = new UploadTask();
        uploadTask.execute(file_path);
        Toast.makeText(MainActivity.this, "File Uploaded Successfully", Toast.LENGTH_SHORT).show();
        Log.d("File Uploaded", "File: " + file_path);
    }

    // upload task class
    @SuppressLint("StaticFieldLeak")
    public class UploadTask extends AsyncTask<String, String, String>{

        @Override
        protected void onPostExecute(String s){
            super.onPostExecute(s);
            if(s.equalsIgnoreCase("true")){
                Toast.makeText(MainActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(MainActivity.this, "Failed Upload", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            if(uploadFile(strings[0])){
                return "true";
            }
            else {
                return "failed";
            }
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        private boolean uploadFile(String path){
            File file = new File(path);
            try {
                RequestBody requestBody =  new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("video", file.getName(), RequestBody.create(MediaType.parse("video/*"), file))
                        .build();

                Request request = new Request.Builder()
                        .url("http://192.168.18.6:8000/addVideo/")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    }
                });
                return true;
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

    }

    // selecting file method
    private void filePicker() {
        Toast.makeText(MainActivity.this, "File picker call", Toast.LENGTH_SHORT).show();
        Intent openGallery = new Intent(Intent.ACTION_PICK);
        openGallery.setType("video/*");
        startActivityForResult(openGallery, REQUEST_GALLERY);
    }

    // displaying the selected file
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_GALLERY && resultCode == Activity.RESULT_OK){
            String filePath = getRealPathFromUri(data.getData(), MainActivity.this);
            Log.d("File Path", " " + filePath);

            MediaController mediaController = new MediaController(MainActivity.this);
            mediaController.setAnchorView(videoContainer);
            videoContainer.setMediaController(mediaController);

            videoContainer.setVideoURI(data.getData());
            videoContainer.start();

            this.file_path = filePath;

            File file = new File(filePath);
            filename.setText(file.getName());
        }
    }

    // get file path from uri method
    public String getRealPathFromUri(Uri uri, Activity activity){
        Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
        if(cursor==null){
            return uri.getPath();
        }
        else{
            cursor.moveToFirst();
            int id = cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA);
            return cursor.getString(id);
        }
    }

    // request permission method
    public void requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(MainActivity.this, "Give permission to upload file", Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    // check permission method
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission Successful", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Permission Failed", Toast.LENGTH_SHORT).show();
                }
        }
    }

}