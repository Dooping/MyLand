package com.gago.david.myland;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.gago.david.myland.Models.LandObject;
import com.gago.david.myland.Models.PriorityObject;
import com.gago.david.myland.Models.TaskObject;
import com.gago.david.myland.Models.TaskTypeObject;
import com.gago.david.myland.Utils.Utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by david on 27/01/2017.
 */

public class LandOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
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
        if (oldVersion <  2)
            upgradeVersion2(db);
        if (oldVersion < 3)
            upgradeVersion3(db);
    }

    private void upgradeVersion2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE Lands ADD COLUMN Area Double DEFAULT 0;");
        Log.v("DATABASE", "updated to version 2");
    }

    private void upgradeVersion3(SQLiteDatabase db){
        db.execSQL("UPDATE PlantTypes SET Icon = Icon + 1;");
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
            byte[] imgByte = cur.getBlob(cur.getColumnIndex("Image"));
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            cur.close();
            db.close();
            return bitmap;
        }
        if (cur != null && !cur.isClosed()) {
            cur.close();
            db.close();
        }

        Log.v("image", "algo falhou");

        return null ;
    }

    public static ArrayList<TaskTypeObject> readTaskTypes(Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "Description"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        Cursor cursor = db.query(
                "TaskTypes",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<TaskTypeObject> taskTypes = new ArrayList<>();

        while(cursor.moveToNext()) {
            TaskTypeObject o = new TaskTypeObject(cursor.getString(0), cursor.getString(1));
            taskTypes.add(o);
        }

        cursor.close();

        return taskTypes;
    }

    public static ArrayList<PriorityObject> readPriorities(Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "P_order",
                "Color"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = "P_order ASC";

        Cursor cursor = db.query(
                "Priorities",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        ArrayList<PriorityObject> priorities = new ArrayList<>();

        while(cursor.moveToNext()) {
            PriorityObject o = new PriorityObject(cursor.getString(0), cursor.getInt(1), cursor.getString(2));
            priorities.add(o);
        }

        cursor.close();

        return priorities;
    }

    public static boolean updateTask(TaskObject t, Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);
        boolean success = true;
        Log.v("Task to update", t.toString());

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("Land", t.land);
        values.put("PlantIndex", t.plantIndex);
        values.put("TaskType", t.taskType);
        values.put("Priority", t.priority);
        values.put("CreationDate", t.creationDate.getTime());
        if (t.targetDate != null)
            values.put("ExpirationDate", t.targetDate.getTime());
        values.put("Completed", t.completed);
        values.put("Observations", t.observations);

        String whereClause = "rowid = ?";
        String[] whereArgs = new String[]{""+t.rowid};
        long newRowId = db.update("Tasks", values, whereClause, whereArgs);
        if (newRowId == -1)
            success = false;
        else {
            Log.v("UpdateTask", "row updated: " + newRowId);
        }

        db.close();
        return success;
    }

    public static boolean deleteTask(TaskObject t, Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);
        boolean success = true;

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = "rowid = ?";
        String[] whereArgs = new String[]{""+t.rowid};
        long newRowId = db.delete("Tasks", whereClause, whereArgs);
        if (newRowId == 0)
            success = false;
        else {
            Log.v("DeleteTask", "rows deleted: " + newRowId);

        }

        db.close();
        return success;
    }

    public static boolean deleteLand(LandObject l, Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);
        boolean success = true;

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = "name = ?";
        String[] whereArgs = new String[]{""+l.name};
        long newRowId = db.delete("Lands", whereClause, whereArgs);
        if (newRowId == 0)
            success = false;
        else {
            Log.v("DeleteLand", "rows deleted: " + newRowId);
            deleteImage(l.imageUri, context);
        }

        db.close();
        return success;
    }

    public static boolean deleteImage(String image, Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);
        boolean success = true;

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = "name = ?";
        String[] whereArgs = new String[]{""+image};
        long newRowId = db.delete("Images", whereClause, whereArgs);
        if (newRowId == 0)
            success = false;
        else {
            Log.v("DeleteImage", "rows deleted: " + newRowId);

        }

        db.close();
        return success;
    }
}
