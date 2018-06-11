package com.gago.david.myland;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.gago.david.myland.Models.LandContract.*;
import com.gago.david.myland.Models.LandObject;
import com.gago.david.myland.Models.PlantObject;
import com.gago.david.myland.Models.PlantTypeObject;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by david on 27/01/2017.
 */

public class LandImporterHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 4;
    public static final String LAND_TABLE_NAME = "import.db";

    private Context context;

    public LandImporterHelper(Context context) {
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
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(false);
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

        List<PlantTypeObject> itemTypes = readPlantTypes();
        for (PlantTypeObject i : itemTypes)
            LandOpenHelper.addItemType(context, i);

        List<TaskTypeObject> taskTypes = readTaskTypes();
        for (TaskTypeObject t : taskTypes)
            LandOpenHelper.addTaskType(context, t);

        List<LandObject> lands = readLands();
        Log.v("Lands", lands.toString());
        for (LandObject l : lands){
            l.plants = readPlants(l.name);
            Bitmap image = getImage(l.imageUri);
            List<TaskObject> tasks = readTasks(l.name);
            LandOpenHelper.addImage(context, image, l.imageUri);
            if(LandOpenHelper.addLand(context, l)){
                Log.v("Importer", "adicionou terreno");
                for (PlantObject p : l.plants){
                    int oldId = p.id;
                    if (LandOpenHelper.addPlant(context, p, l.name)){
                        Log.v("Importer", "adicionou item");
                        Iterator<TaskObject> taskIterator = tasks.iterator();
                        while (taskIterator.hasNext()) {
                            TaskObject t = taskIterator.next();
                            if (t.plantIndex != null && t.plantIndex == oldId) {
                                Log.v("Importer", "adicionou tarefa");
                                t.plantIndex = p.id;
                                LandOpenHelper.addTask(context, t);
                                taskIterator.remove();
                            }
                        }
                    }
                }
                for (TaskObject t : tasks)
                    if(t.plantIndex == null)
                        LandOpenHelper.addTask(context, t);
            }
            else
                LandOpenHelper.deleteImage(l.imageUri, context);
        }

        return true;
    }


    private Bitmap getImage(String name){
        SQLiteDatabase db = getReadableDatabase();

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
        if (!cur.isClosed()) {
            cur.close();
            db.close();
        }

        Log.v("image", "algo falhou");

        return null ;
    }

    private ArrayList<TaskTypeObject> readTaskTypes(){
        SQLiteDatabase db = getReadableDatabase();

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
        db.close();

        return taskTypes;
    }

    private ArrayList<PlantTypeObject> readPlantTypes(){
        SQLiteDatabase db = getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "Icon",
                "Color"
        };

        Cursor cursor = db.query(
                "PlantTypes",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        ArrayList<PlantTypeObject> plants = new ArrayList<>();

        while(cursor.moveToNext()) {
            PlantTypeObject o = new PlantTypeObject(cursor.getString(0), cursor.getInt(1), cursor.getString(2));
            plants.add(o);
        }

        cursor.close();
        db.close();
        return plants;
    }

    private List<LandObject> readLands(){

        SQLiteDatabase db = getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                LandEntry.COLUMN_NAME,
                LandEntry.COLUMN_IMAGE,
                LandEntry.COLUMN_DESCRIPTION,
                LandEntry.COLUMN_AREA
        };

        Cursor cursor = db.query(
                LandEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null              // The sort order
        );
        /*Cursor cursor = db.rawQuery("select Name, ImageUri, Description, count(Tasks.Land) as 'Notification' \n" +
                "from Lands left outer join Tasks on Lands.Name = Tasks.Land\n" +
                "where Priority is null or Priority = 1\n" +
                "group by Tasks.Land", null
        );*/

        List<LandObject> lands = new ArrayList<>();

        while(cursor.moveToNext()) {
            LandObject o = new LandObject(cursor.getString(cursor.getColumnIndex(LandEntry.COLUMN_NAME))
                    , cursor.getString(cursor.getColumnIndex(LandEntry.COLUMN_IMAGE))
                    , cursor.getString(cursor.getColumnIndex(LandEntry.COLUMN_DESCRIPTION))
                    , cursor.getDouble(cursor.getColumnIndex(LandEntry.COLUMN_AREA)));
            lands.add(o);
        }

        //Log.v("Lands", lands.toString());

        cursor.close();
        db.close();

        return lands;
    }

    private ArrayList<PlantObject> readPlants(String name){
        SQLiteDatabase db = getReadableDatabase();
        String[] projection2 = {
                ItemEntry.COLUMN_ID,
                ItemEntry.COLUMN_LAND,
                ItemEntry.COLUMN_DESCRIPTION,
                ItemEntry.COLUMN_PLANT_TYPE,
                ItemEntry.COLUMN_X,
                ItemEntry.COLUMN_Y
        };

        // Filter results WHERE "title" = 'My Title'
        String selection2 = "Land" + " = ?";

        // How you want the results sorted in the resulting Cursor
        String sortOrder2 = "Id ASC";

        String[] selectionArgs = new String[]{name};

        Cursor cur = db.query(
                ItemEntry.TABLE_NAME,   // The table to query
                projection2,             // The array of columns to return (pass null to get all)
                selection2,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder2               // The sort order
        );
        ArrayList<PlantObject> plants = new ArrayList<>();
        while (cur.moveToNext())
            plants.add(new PlantObject(cur.getInt(cur.getColumnIndex(ItemEntry.COLUMN_ID))
                    , cur.getString(cur.getColumnIndex(ItemEntry.COLUMN_PLANT_TYPE))
                    , cur.getString(cur.getColumnIndex(ItemEntry.COLUMN_DESCRIPTION))
                    , cur.getFloat(cur.getColumnIndex(ItemEntry.COLUMN_X))
                    , cur.getFloat(cur.getColumnIndex(ItemEntry.COLUMN_Y))));
        cur.close();
        db.close();

        Log.v("Read plants", plants.toString());
        return plants;
    }

    private ArrayList<TaskObject> readTasks(String land){

        SQLiteDatabase db = getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Land",
                "PlantIndex",
                "TaskType",
                "Priority",
                "CreationDate",
                "ExpirationDate",
                "Completed",
                "Observations",
                "rowid"
        };

        String selection = "Land = ?";
        String[] selectionArgs = new String[]{land};

        Cursor cursor = db.query(
                "Tasks",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        ArrayList<TaskObject> tasks = new ArrayList<>();

        while(cursor.moveToNext()) {
            Calendar cl = Calendar.getInstance();
            cl.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("CreationDate")));

            Calendar cl2 = (cursor.isNull(cursor.getColumnIndex("ExpirationDate"))) ? null : Calendar.getInstance();
            if(cl2 != null)
                cl2.setTimeInMillis(cursor.getLong(cursor.getColumnIndex("ExpirationDate")));
            Date targetDate = cl2 == null ? null : cl2.getTime();
            TaskObject o = new TaskObject(
                    cursor.getLong(cursor.getColumnIndex("rowid"))
                    , cursor.getString(cursor.getColumnIndex("Land"))
                    , cursor.isNull(cursor.getColumnIndex("PlantIndex")) ? null : cursor.getInt(cursor.getColumnIndex("PlantIndex"))
                    , cursor.getString(cursor.getColumnIndex("TaskType"))
                    , cursor.getInt(cursor.getColumnIndex("Priority"))
                    , cl.getTime()
                    , targetDate
                    , cursor.getInt(cursor.getColumnIndex("Completed")) > 0
                    , cursor.getString(cursor.getColumnIndex("Observations")));
            tasks.add(o);
            Log.v("read tasks", "task: "+o.toString());
        }

        cursor.close();
        db.close();
        return tasks;
    }


}
