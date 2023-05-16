package com.example.myapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Tag;

public class MainActivity extends AppCompatActivity {

    View fileContainer;
    Button uploadBtn, detectBtn;
    TextView filename;
    VideoView videoContainer;

    int requestCode = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileContainer = findViewById(R.id.fileContainer);

        uploadBtn = findViewById(R.id.uploadButton);
        detectBtn = findViewById(R.id.detectButton);

        uploadBtnFunction(uploadBtn);
        detectBtnFunction(detectBtn);

    }

    //upload button function
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

    // sending data in api
    private void btnSendPostRequestClicked() {
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);
        Call<User> call = apiInterface.getUserInformation("TestName2");
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
                Log.e(TAG, "onFailureL " + t.getMessage());
                Toast.makeText(MainActivity.this, "onFailure: Error", Toast.LENGTH_SHORT).show();
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

                    filename.setText(displayName);

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

}