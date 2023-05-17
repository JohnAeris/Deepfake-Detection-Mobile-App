package com.example.myapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
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
import java.net.InetAddress;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.Tag;

public class MainActivity extends AppCompatActivity {

    View fileContainer;
    Button uploadBtn, detectBtn;
    TextView filename;
    VideoView videoContainer;

    String file_path = null;

    //int requestCode = 1;
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

        //uploadBtnFunction(uploadBtn);
        //detectBtnFunction(detectBtn);

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

    /*//upload button function
    private void uploadBtnFunction(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Upload Button is working", Toast.LENGTH_SHORT).show();
                showFileChooser();
            }
        });
    }

    // detect button function
    private void detectBtnFunction(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "Detect Button is working", Toast.LENGTH_SHORT).show();
                btnSendPostRequestClicked();
            }
        });
    }

    //file picker
    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a file"), requestCode);
        } catch (Exception exception) {
            Toast.makeText(this, "Please install a file manager", Toast.LENGTH_SHORT).show();
        }
    }

    //file selected
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        filename = findViewById(R.id.filenameContainer);
        videoContainer = findViewById(R.id.videoContainer);

        try {
            if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
                Uri uri = data.getData();
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    cursor.close();

                    String path = uri.getPath();
                    File file = new File(path);

                    //filename.setText(displayName);

                    MediaController mediaController = new MediaController(this);
                    mediaController.setAnchorView(videoContainer);
                    videoContainer.setMediaController(mediaController);

                    videoContainer.setVideoURI(uri);
                    videoContainer.start();
                }
            }

            super.onActivityResult(requestCode, resultCode, data);

        } catch (Exception exception) {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
        }
    }

    // sending data in api
    private void btnSendPostRequestClicked() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        Call<User> call = apiInterface.getUserInformation("TestName4");
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.e(TAG, "onResponse: " + response.code());
                Log.e(TAG, "onResponse: id : " + response.body().getId());
                Log.e(TAG, "onResponse: name : " + response.body().getName());
                Log.e(TAG, "onResponse: created : " + response.body().getCreated());
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "onFailure " + t.getMessage());
                Toast.makeText(MainActivity.this, "onFailure: Error", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void UploadFile() {
        UploadTask uploadTask = new UploadTask();
        uploadTask.execute(file_path);
        Toast.makeText(MainActivity.this, "File Uploaded Successfully", Toast.LENGTH_SHORT).show();
        Log.d("File Uploaded", "File: " + file_path);
    }

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
                        /*.addFormDataPart("some key", "some_value")
                        .addFormDataPart("submit", "submit")*/
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

    private void filePicker() {
        Toast.makeText(MainActivity.this, "File picker call", Toast.LENGTH_SHORT).show();
        Intent openGallery = new Intent(Intent.ACTION_PICK);
        openGallery.setType("video/*");
        startActivityForResult(openGallery, REQUEST_GALLERY);
    }

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

    public void requestPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(MainActivity.this, "Give permission to upload file", Toast.LENGTH_SHORT).show();
        }
        else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(result == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            return false;
        }
    }

    //@Override
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