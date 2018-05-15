package com.gago.david.myland;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;

import com.gago.david.myland.Models.LandObject;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 27/01/2017.
 */

public class LandOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String LAND_TABLE_NAME = "myland.db";

    private Context context;

    public LandOpenHelper(Context context) {
        super(context, LAND_TABLE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.db);

        String queries = "";
        try {
            queries = IOUtils.toString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String query : queries.split(";")) {
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.deletedb);

        String queries = "";
        try {
            queries = IOUtils.toString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String query : queries.split(";")) {
            db.execSQL(query);
        }

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     * */
    public boolean exportDatabase(Uri dbPath) throws IOException {
        close();
        File newDb = new File("//data/data/com.gago.david.myland/databases/",LAND_TABLE_NAME);
        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(dbPath, "w");
        try {
            InputStream is = new FileInputStream(newDb);
            FileOutputStream os = new FileOutputStream(pfd.getFileDescriptor());
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean importDatabase(Uri dbPath) throws IOException {
        File f = Utils.getFileForUri(dbPath);
        Log.v("file", f.getPath() + " " + f.exists());
        File oldDb = new File("//data/data/com.gago.david.myland/databases/",LAND_TABLE_NAME);
        if(f.exists())
            try (InputStream is = new FileInputStream(f); FileOutputStream os = new FileOutputStream(oldDb)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            }
            else
                return false;
        return true;
    }

    public Bitmap getImage(String name){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String[] projection = {
                "Name",
                "Image"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        String whereClause = "Name = ?";
        String[] whereArgs = new String[]{name};

        Cursor cur = db.query(
                "Images",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                whereClause,              // The columns for the WHERE clause
                whereArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        if (cur.moveToFirst()){
            byte[] imgByte = cur.getBlob(1);
            cur.close();
            return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        }
        if (cur != null && !cur.isClosed()) {
            cur.close();
        }

        Log.v("image", "algo falhou");

        return null ;
    }
}
