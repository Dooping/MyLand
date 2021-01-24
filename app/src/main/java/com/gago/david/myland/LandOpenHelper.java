package com.gago.david.myland;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import com.gago.david.myland.models.LandContract;
import com.gago.david.myland.models.LandObject;
import com.gago.david.myland.models.PlantObject;
import com.gago.david.myland.models.PlantTypeObject;
import com.gago.david.myland.models.PriorityObject;
import com.gago.david.myland.models.TaskObject;
import com.gago.david.myland.models.TaskTypeObject;

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
import java.util.List;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by david on 27/01/2017.
 */

public class LandOpenHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 7;
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
        /*if (oldVersion <  2)
            upgradeVersion2(db);
        if (oldVersion < 3)
            upgradeVersion3(db);
        if (oldVersion < 4)
            upgradeVersion4(db);
        if (oldVersion < 6)
            upgradeVersion6(db);
        if (oldVersion < 7)
            upgradeVersion7(db);*/
        if (oldVersion < newVersion){
            dropDatabase(db);
            onCreate(db);
        }

    }

    private void dropDatabase(SQLiteDatabase db){
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
    }

    private void upgradeVersion2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE Lands ADD COLUMN Area Double DEFAULT 0;");
        Log.v("DATABASE", "updated to version 2");
    }

    private void upgradeVersion3(SQLiteDatabase db){
        db.execSQL("UPDATE PlantTypes SET Icon = Icon + 1;");
    }

    private void upgradeVersion4(SQLiteDatabase db){
        db.execSQL("create table if not exists Users (Name VARCHAR PRIMARY KEY);");
        db.execSQL("alter table Lands ADD COLUMN User varchar default null REFERENCES Users(Name) on delete cascade;");
        db.execSQL("UPDATE PlantTypes SET Icon = Icon + 1;");
    }

    private void upgradeVersion6(SQLiteDatabase db){
        db.endTransaction();
        db.setForeignKeyConstraintsEnabled(false);
        db.beginTransaction();
        db = getWritableDatabase();
        db.execSQL("create table my_table_copy (" +
                "Name VARCHAR,\n" +
                "ImageUri VARCHAR,\n" +
                "Description TEXT,\n" +
                "Area Double DEFAULT 0,\n" +
                "User VARCHAR DEFAULT null,\n" +
                "PRIMARY KEY (Name, User),\n" +
                "FOREIGN KEY(ImageUri) REFERENCES Images(Name),\n" +
                "FOREIGN KEY(User) REFERENCES Users(Name) ON DELETE CASCADE);");
        db.execSQL("INSERT INTO my_table_copy (Name, ImageUri, Description, Area, User)\n" +
                "   SELECT Name, ImageUri, Description, Area, User FROM Lands;");
        db.execSQL("DROP TABLE Lands;");
        db.execSQL("ALTER TABLE my_table_copy RENAME TO Lands;");

        db.execSQL("CREATE TABLE my_table_copy(\n" +
                "Id INTEGER primary key autoincrement,\n" +
                "Land VARCHAR,\n" +
                "User VARCHAR,\n" +
                "PlantType VARCHAR,\n" +
                "description TEXT,\n" +
                "x FLOAT,\n" +
                "y FLOAT,\n" +
                "FOREIGN KEY(Land, User) REFERENCES Lands(Name, User) ON DELETE CASCADE,\n" +
                "FOREIGN KEY(PlantType) REFERENCES PlantTypes(Name));");
        db.execSQL("INSERT INTO my_table_copy (Id,Land,User,PlantType, description,x,y) \n" +
                "SELECT Id,Land,User,PlantType, Plants.description,x,y \n" +
                "FROM Plants inner join Lands on (Land=Name);");
        db.execSQL("DROP TABLE Plants;");
        db.execSQL("ALTER TABLE my_table_copy RENAME TO Plants;");

        db.execSQL("CREATE TABLE my_table_copy(\n" +
                "Land VARCHAR NOT NULL,\n" +
                "    User VARCHAR,\n" +
                "\tPlantIndex INTEGER,\n" +
                "    TaskType VARCHAR NOT NULL,\n" +
                "    Priority INTEGER NOT NULL,\n" +
                "\tCreationDate LONG NOT NULL,\n" +
                "\tExpirationDate LONG,\n" +
                "\tCompleted BOOLEAN NOT NULL CHECK (Completed IN (0,1)),\n" +
                "\tObservations TEXT,\n" +
                "\tFOREIGN KEY(PlantIndex) REFERENCES Plants(Id) ON DELETE CASCADE,\n" +
                "    FOREIGN KEY(Land, User) REFERENCES Lands(Name, User) ON DELETE CASCADE,\n" +
                "    FOREIGN KEY(TaskType) REFERENCES TaskTypes(Name) ON DELETE CASCADE,\n" +
                "    FOREIGN KEY(Priority) REFERENCES Priorities(P_order));");
        db.execSQL("INSERT INTO my_table_copy (Land,User,PlantIndex, TaskType, Priority, CreationDate, ExpirationDate, Completed, Observations) \n" +
                "SELECT Land,User,PlantIndex, TaskType, Priority, CreationDate, ExpirationDate, Completed, Observations \n" +
                "FROM Tasks inner join Lands on (Land=Name);");
        db.execSQL("DROP TABLE Tasks;");
        db.execSQL("ALTER TABLE my_table_copy RENAME TO Tasks;");
        db.endTransaction();
        db.setForeignKeyConstraintsEnabled(true);
        db.beginTransaction();
    }

    private void upgradeVersion7(SQLiteDatabase db){
        List<LandObject> list = readLands(context);
        for(LandObject l : list){
            Bitmap b = oldGetImage(context, l.imageUri);
            l.imageUri = saveToInternalStorage(context, b, l.imageUri);
            updateLand(context, l);
        }
        db.endTransaction();
        db.setForeignKeyConstraintsEnabled(false);
        db.beginTransaction();
        db = getWritableDatabase();
        db.execSQL("CREATE TABLE IF NOT EXISTS Lands2(\n" +
                "\tName VARCHAR,\n" +
                "\tImageUri VARCHAR,\n" +
                "\tDescription TEXT,\n" +
                "\tArea Double DEFAULT 0,\n" +
                "\tUser VARCHAR DEFAULT null,\n" +
                "\tprimary key (Name, User),\n" +
                "    FOREIGN KEY(User) REFERENCES Users(Name) ON DELETE CASCADE\n" +
                "\t);");
        db.execSQL("INSERT INTO Lands2 (Name, ImageUri, Description, Area, User)\n" +
                "   SELECT Name, ImageUri, Description, Area, User FROM Lands;");
        db.execSQL("DROP TABLE Lands;");
        db.execSQL("ALTER TABLE Lands2 RENAME TO Lands;");
        db.endTransaction();
        db.setForeignKeyConstraintsEnabled(true);
        db.beginTransaction();
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    public static Bitmap getImage(String uri){
        Log.v("GETIMAGE", uri);
        try {
            File f = new File(uri);
            return BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap oldGetImage(Context context, String name){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                "Name",
                "Image"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        String whereClause = "Name = ?";
        String[] whereArgs = new String[]{name};

        Log.v("GETIMAGE", name);

        Cursor cur = db.query(
                "Images",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                whereClause,              // The columns for the WHERE clause
                whereArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        if (cur.moveToFirst()){

            Log.v("GETIMAGE", cur.getString(cur.getColumnIndex("Name")) );
            Log.v("GETIMAGE", "column index: "+cur.getColumnIndex("Image") );
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

    private static String saveToInternalStorage(Context context, Bitmap bitmapImage, String name){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }

    public static String addImage(Context context, Bitmap image) {
        String name = UUID.randomUUID().toString()+".png";
        return addImage(context,image,name);
    }

    public static String addImage(Context context, Bitmap image, String name) {
        Log.v("ADDIMAGE", name);
        return saveToInternalStorage(context, image, name);
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
        db.close();
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
        db.close();

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

        SharedPreferences prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

        String whereClause = "Name = ? AND User = ?";
        String[] whereArgs = new String[]{l.name, user};
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
        if (isImageUsed(context, image))
            return false;
        File f=new File(image);
        return f.delete();
    }

    private static boolean isImageUsed(Context context, String image){
        SQLiteDatabase db = new LandOpenHelper(context).getReadableDatabase();
        // Filter results WHERE "title" = 'My Title'
        String selection = "ImageUri = ?";

        String[] selectionArgs = new String[]{image};
        Cursor cursor = db.query(
                "Lands",   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );
        return cursor.getCount()>0;
    }

    public static ArrayList<String> readUsers(Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name"
        };

        // How you want the results sorted in the resulting Cursor

        Cursor cursor = db.query(
                "Users",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        ArrayList<String> users = new ArrayList<>();

        while(cursor.moveToNext())
            users.add(cursor.getString(cursor.getColumnIndex("Name")));

        cursor.close();
        db.close();

        return users;
    }

    public static void createUser(Context context, String user){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Name", user);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("Users", null, values);
        Log.v("ADDDETAIL", "row inserted: "+newRowId);
        if (newRowId == -1)
            Toast.makeText(context,"Land already exists, choose a different name", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public static boolean deleteUser(Context context, String user){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);
        boolean success = true;

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String whereClause = "name = ?";
        String[] whereArgs = new String[]{user};
        long newRowId = db.delete("Users", whereClause, whereArgs);
        if (newRowId == 0)
            success = false;
        else
            Log.v("DeleteUser", "rows deleted: " + newRowId);

        db.close();
        return success;
    }

    public static void updateLands(Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL("update Lands set User = (select Name from Users) where User is null;");
    }

    public static List<LandObject> readLands(Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Lands.Name as 'Name'",
                "Lands.ImageUri as 'ImageUri'",
                "Lands.Description as 'Description'",
                "count('Tasks'.Land) as 'Notification'",
                "min('Tasks'.Priority) as 'Priority'",
                LandContract.LandEntry.COLUMN_AREA
        };

        // How you want the results sorted in the resulting Cursor

        SharedPreferences prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

        Cursor cursor = db.query(
                "Lands left outer join (select * from tasks where completed = 0) as 'Tasks' on (Lands.Name = 'Tasks'.Land AND Lands.User = 'Tasks'.User)" ,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                "Lands.User = ?",              // The columns for the WHERE clause
                new String[]{user},          // The values for the WHERE clause
                "Name",                   // don't group the rows
                null,                   // don't filter by row groups
                "Lands.rowid asc"               // The sort order
        );
        /*Cursor cursor = db.rawQuery("select Name, ImageUri, Description, count(Tasks.Land) as 'Notification' \n" +
                "from Lands left outer join Tasks on Lands.Name = Tasks.Land\n" +
                "where Priority is null or Priority = 1\n" +
                "group by Tasks.Land", null
        );*/

        List<LandObject> lands = new ArrayList<>();

        while(cursor.moveToNext()) {
            int priority = cursor.isNull(cursor.getColumnIndex("Priority")) ? 0 : cursor.getInt(cursor.getColumnIndex("Priority"));
            LandObject o = new LandObject(cursor.getString(cursor.getColumnIndex("Name"))
                    , cursor.getString(cursor.getColumnIndex("ImageUri"))
                    , cursor.getString(cursor.getColumnIndex("Description"))
                    , cursor.getInt(cursor.getColumnIndex("Notification"))
                    , priority
                    , cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_AREA)));
            lands.add(o);
        }

        Log.v("Lands", lands.toString());

        cursor.close();
        db.close();

        return lands;
    }

    public static ArrayList<PlantTypeObject> readPlantTypes(Context context){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                "Name",
                "Icon",
                "Color"
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = null;

        Cursor cursor = db.query(
                "PlantTypes",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
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

    public static ArrayList<PlantObject> readPlants(Context context, String name){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        SharedPreferences prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

        String[] projection2 = {
                "rowid",
                "Land",
                "PlantType",
                "Description",
                "x",
                "y"
        };

        // Filter results WHERE "title" = 'My Title'
        String selection2 = "Land = ? and User = ?";

        // How you want the results sorted in the resulting Cursor
        String sortOrder2 = "Id ASC";

        String[] selectionArgs = new String[]{name, user};

        Cursor cur = db.query(
                "Plants",   // The table to query
                projection2,             // The array of columns to return (pass null to get all)
                selection2,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder2               // The sort order
        );
        ArrayList<PlantObject> plants = new ArrayList<>();
        while (cur.moveToNext())
            plants.add(new PlantObject(cur.getInt(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_ID))
                    , cur.getString(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_PLANT_TYPE))
                    , cur.getString(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_DESCRIPTION))
                    , cur.getFloat(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_X))
                    , cur.getFloat(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_Y))));
        cur.close();
        db.close();
        return plants;
    }

    public static ArrayList<TaskObject> readTasks(Context context, String land){
        ArrayList<TaskObject> tasks = new ArrayList<>();
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        SharedPreferences prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

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

        // How you want the results sorted in the resulting Cursor
        String sortOrder = "Priority ASC";

        String selection = "Land = ? AND User = ? AND Completed = 0";
        String[] selectionArgs = new String[]{land, user};

        Cursor cursor = db.query(
                "Tasks",   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        //ArrayList<TaskObject> tasks = new ArrayList<>();
        tasks.clear();

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

    public static boolean addLand(Context context, LandObject l){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        SharedPreferences prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Name", l.name);
        values.put("ImageUri", l.imageUri);
        values.put("Description", l.Description);
        values.put("Area", l.area);
        values.put("User", user);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("Lands", null, values);
        db.close();
        Log.v("Importer", "land inserted: "+newRowId);
        return newRowId > -1;
    }

    public static boolean updateLand(Context context, LandObject l){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);
        boolean success = true;
        Log.v("Land to update", l.toString());

        SharedPreferences prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ImageUri", l.imageUri);
        values.put("Description", l.Description);
        values.put("Area", l.area);


        String whereClause = "Name = ? AND User = ?";
        String[] whereArgs = new String[]{l.name, user};
        long newRowId = db.update("Tasks", values, whereClause, whereArgs);
        if (newRowId == -1)
            success = false;
        else {
            Log.v("UpdateLand", "row updated: " + newRowId);
        }

        db.close();
        return success;
    }

    public static boolean addPlant(Context context, PlantObject p, String land) {
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        SharedPreferences prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

// Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Land", land);
        values.put("User", user);
        values.put("PlantType", p.plantType);
        values.put("Description", p.description);
        values.put("x", p.x);
        values.put("y", p.y);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("Plants", null, values);
        p.id = (int)newRowId;
        Log.v("ADDDETAIL", "row inserted: "+newRowId);
        return newRowId > -1;
    }

    public static boolean addTask(Context context, TaskObject t){
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        SharedPreferences prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, MODE_PRIVATE);
        String user = prefs.getString("user", "");

        ContentValues values = new ContentValues();
        values.put("Land", t.land);
        values.put("User", user);
        values.put("PlantIndex", t.plantIndex);
        values.put("TaskType", t.taskType);
        values.put("Priority", t.priority);
        values.put("CreationDate", t.creationDate.getTime());
        if (t.targetDate != null)
            values.put("ExpirationDate", t.targetDate.getTime());
        values.put("Completed", t.completed);
        values.put("Observations", t.observations);

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("Tasks", null, values);

        db.close();
        return newRowId > -1;
    }

    public static boolean addItemType(Context context, PlantTypeObject item) {
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Name", item.getName());
        values.put("Icon", item.getIcon());
        values.put("Color", item.getColor());

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("PlantTypes", null, values);
        return newRowId > -1;
    }

    public static boolean addTaskType(Context context, TaskTypeObject item) {
        LandOpenHelper mDbHelper = new LandOpenHelper(context);

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("Name", item.getName());
        values.put("Description", item.getDescription());

// Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("TaskTypes", null, values);
        db.close();
        return newRowId > -1;
    }
}
