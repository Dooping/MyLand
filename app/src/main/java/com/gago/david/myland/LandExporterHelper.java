package com.gago.david.myland;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.gago.david.myland.models.LandContract.*;
import com.gago.david.myland.models.LandObject;
import com.gago.david.myland.models.PlantObject;
import com.gago.david.myland.models.PlantTypeObject;
import com.gago.david.myland.models.TaskObject;
import com.gago.david.myland.models.TaskTypeObject;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by david on 27/01/2017.
 */

public class LandExporterHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    public static final String LAND_TABLE_NAME = "export.db";

    private Context context;

    public LandExporterHelper(Context context) {
        super(context, LAND_TABLE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.db_export);

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
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(false);
    }

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     * */
    public boolean exportDatabase(Uri dbPath) throws IOException {
        List<LandObject> lands = LandOpenHelper.readLands(context);
        List<TaskTypeObject> taskTypes = LandOpenHelper.readTaskTypes(context);
        List<PlantTypeObject> itemTypes = LandOpenHelper.readPlantTypes(context);
        List<TaskObject> tasks = new ArrayList<>();
        for (LandObject land : lands) {
            addImage(LandOpenHelper.getImage(land.imageUri), land.imageUri);
            writePlants(LandOpenHelper.readPlants(context, land.name), land.name);
            tasks.addAll(LandOpenHelper.readTasks(context, land.name));
        }
        writeTaskTypes(taskTypes);
        writeItemTypes(itemTypes);
        writeLands(lands);
        writeTasks(tasks);

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

    private void writeLands(List<LandObject> lands){
        SQLiteDatabase db = getWritableDatabase();
        for (LandObject land : lands){
            ContentValues values = new ContentValues();
            values.put(LandEntry.COLUMN_NAME, land.name);
            values.put(LandEntry.COLUMN_IMAGE, land.imageUri);
            values.put(LandEntry.COLUMN_DESCRIPTION, land.Description);
            values.put(LandEntry.COLUMN_AREA, land.area);
            db.insert(LandEntry.TABLE_NAME, null, values);
        }
        close();
    }

    private void writePlants(List<PlantObject> plants, String land){
        SQLiteDatabase db = getWritableDatabase();
        for (PlantObject p : plants){
            Log.v("write", ""+p.id);
            ContentValues values = new ContentValues();
            values.put(ItemEntry.COLUMN_ID, p.id);
            values.put(ItemEntry.COLUMN_LAND, land);
            values.put(ItemEntry.COLUMN_PLANT_TYPE, p.plantType);
            values.put(ItemEntry.COLUMN_DESCRIPTION, p.description);
            values.put(ItemEntry.COLUMN_X, p.x);
            values.put(ItemEntry.COLUMN_Y, p.y);
            db.insert(ItemEntry.TABLE_NAME, null, values);
        }
        close();
    }

    private void writeTaskTypes(List<TaskTypeObject> taskTypes){
        SQLiteDatabase db = getWritableDatabase();
        for (TaskTypeObject t : taskTypes){
            ContentValues values = new ContentValues();
            values.put(TaskTypeEntry.COLUMN_NAME, t.name);
            values.put(TaskTypeEntry.COLUMN_DESCRIPTION, t.description);
            db.insert(TaskTypeEntry.TABLE_NAME, null, values);
        }
        close();
    }

    private void writeItemTypes(List<PlantTypeObject> itemTypes){
        SQLiteDatabase db = getWritableDatabase();
        for (PlantTypeObject p : itemTypes){
            ContentValues values = new ContentValues();
            values.put(ItemTypeEntry.COLUMN_NAME, p.name);
            values.put(ItemTypeEntry.COLUMN_ICON, p.icon);
            values.put(ItemTypeEntry.COLUMN_COLOR, p.color);
            db.insert(ItemTypeEntry.TABLE_NAME, null, values);
        }
        close();
    }

    private void addImage(Bitmap image, String name) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Name", name);
        values.put("Image", getBitmapAsByteArray(image));

        db.insert("Images", null, values);
        close();
    }

    private static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    private void writeTasks(List<TaskObject> tasks){
        SQLiteDatabase db = getWritableDatabase();
        for (TaskObject t : tasks){
            ContentValues values = new ContentValues();
            values.put(TaskEntry.COLUMN_LAND, t.land);
            values.put(TaskEntry.COLUMN_PLANT_INDEX, t.plantIndex);
            values.put(TaskEntry.COLUMN_TASK_TYPE, t.taskType);
            values.put(TaskEntry.COLUMN_PRIORITY, t.priority);
            values.put(TaskEntry.COLUMN_CREATION_DATE, t.creationDate.getTime());
            if (t.targetDate != null)
                values.put(TaskEntry.COLUMN_EXPIRATION_DATE, t.targetDate.getTime());
            values.put(TaskEntry.COLUMN_COMPLETED, t.completed);
            values.put(TaskEntry.COLUMN_OBSERVATIONS, t.observations);
            db.insert(TaskEntry.TABLE_NAME, null, values);
        }
        close();
    }

}
