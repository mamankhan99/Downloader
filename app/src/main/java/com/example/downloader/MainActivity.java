package com.example.downloader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    DownloadManager downloadManager = null;
    ArrayList<Long> downloadIDs = null;
    int currentDownload = 0;
    EditText editText = null;
    Button downloadButton = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int PERMISSION_REQUEST_CODE = 1;
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (!(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }
            }
        }


        registerReceiver(onDownloadComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        downloadButton = findViewById(R.id.download_button);
        editText = findViewById(R.id.edit_view_main);
        //String URLS = "https://file-examples.com/wp-content/uploads/2017/10/file-example_PDF_1MB.pdf,https://file-examples.com/wp-content/uploads/2017/10/file_example_JPG_100kB.jpg,https://file-examples.com/wp-content/uploads/2017/04/file_example_MP4_480_1_5MG.mp4";
        Log.e("link",editText.getText().toString());
        downloadIDs = new ArrayList<>();
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(editText.getText().toString().split(","));
            }
        });
    }

    private class DownloadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            for (String string : strings) downloadFile(string);
            return null;
        }

        private void downloadFile(String URL) {
            downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
            String fileName = URL.substring(URL.lastIndexOf('/') + 1);
            try {
                Uri Download_Uri = Uri.parse(URL);
                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setAllowedOverRoaming(false);
                request.setTitle(fileName);
                request.setDescription("Downloading...");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                long downloadReference = downloadManager.enqueue(request);
                downloadIDs.add(downloadReference);
            }
            catch (IllegalArgumentException e) {
                Toast.makeText(getApplicationContext(),"Can't Register Download Link Broken for " + fileName,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(downloadIDs.get(currentDownload) == intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                Toast.makeText(getApplicationContext(),"Download Complete",Toast.LENGTH_SHORT).show();
                currentDownload += 1;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onDownloadComplete);
    }
}