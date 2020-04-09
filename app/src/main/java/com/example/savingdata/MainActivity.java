package com.example.savingdata;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private final String filename = "myfile";

    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
           @NonNull int[] grantResults) {
                if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE) {
                    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            Log.d("MainActivity", "WRITE_EXTERNAL_STORAGE permission granted");
                        } else {
                            Log.d("MainActivity", "WRITE_EXTERNAL_STORAGE permission request denied");
                        }
                }
            }

    private void requestWriteExternalStoragePermission() {
        Log.d("MainActivity", "WRITE_EXTERNAL_STORAGE permission has NOT been granted. Requesting permission.");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);

    }

    public void save(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
            requestWriteExternalStoragePermission();
        } else {
            FileOutputStream outputStream;
            try {
                File documentFile = getStorageDocumentsFile(filename);
                documentFile.delete();
                outputStream = new FileOutputStream(documentFile);
                String string = "Hello Android! at " + System.currentTimeMillis();
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void restore(View view) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(getStorageDocumentsFile(filename));
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            TextView textView = (TextView)findViewById(R.id.hello_text);
            textView.setText(sb.toString());
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getStorageDocumentsFile(String filename) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), filename);
        file.getParentFile().mkdirs();
        return file;
    }

    public void delete(View view) {
        File file = getStorageDocumentsFile(filename);
        Log.d("MainActivity", "getFreeSpace: " + file.getFreeSpace());
        file.delete();
    }

    private long lastId;

    public void insertDb(View view) {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        lastId = System.currentTimeMillis() / 1000;
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID, lastId);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, "タイトル");
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT, "内容");

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                "",
                values);

        Log.d("newRowId", Long.toString(newRowId));
    }

    public void selectDb(View view) {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor c = db.query(FeedReaderContract.FeedEntry.TABLE_NAME,
                    new String[]{
                        FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,
                        FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT
                    },
                    "",
                    new String[0],
                    "",
                    "",
                    "");


        List<String> records = new ArrayList<>();
        while(c.moveToNext()) {
            String record = c.getString(0) + "," +  c.getString(1)  + "," +  c.getString(2) + "\r\n";
            records.add(record);
        }

        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(new ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            records));
    }

    public void deleteDb(View view) {
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(FeedReaderContract.FeedEntry.TABLE_NAME,
                    "",
                    new String[0]);
    }

    public void updateDb(View view) {
            FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
            SQLiteDatabase db = mDbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, "更新済みタイトル");
            values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT, "更新済み内容");

            Log.d("lastId", Long.toString(lastId));

            db.update(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                values,
                FeedReaderContract.FeedEntry.COLUMN_NAME_ENTRY_ID + " = ?",
                new String[]{Long.toString(lastId)});
    }
}