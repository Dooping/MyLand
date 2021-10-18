package com.gago.david.myland

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import com.gago.david.myland.models.*
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*

/**
 * Created by david on 27/01/2017.
 */
class LandOpenHelper(private val context: Context) : SQLiteOpenHelper(context, LAND_TABLE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val inputStream = context.resources.openRawResource(R.raw.db)
        var queries = ""
        try {
            queries = IOUtils.toString(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        for (query in queries.split(";").toTypedArray()) {
            db.execSQL(query)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
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
        if (oldVersion < newVersion) {
            dropDatabase(db)
            onCreate(db)
        }
    }

    private fun dropDatabase(db: SQLiteDatabase) {
        val inputStream = context.resources.openRawResource(R.raw.deletedb)
        var queries = ""
        try {
            queries = IOUtils.toString(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        for (query in queries.split(";").toTypedArray()) {
            db.execSQL(query)
        }
    }

    private fun upgradeVersion2(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE Lands ADD COLUMN Area Double DEFAULT 0;")
        Log.v("DATABASE", "updated to version 2")
    }

    private fun upgradeVersion3(db: SQLiteDatabase) {
        db.execSQL("UPDATE PlantTypes SET Icon = Icon + 1;")
    }

    private fun upgradeVersion4(db: SQLiteDatabase) {
        db.execSQL("create table if not exists Users (Name VARCHAR PRIMARY KEY);")
        db.execSQL("alter table Lands ADD COLUMN User varchar default null REFERENCES Users(Name) on delete cascade;")
        db.execSQL("UPDATE PlantTypes SET Icon = Icon + 1;")
    }

    private fun upgradeVersion6(db: SQLiteDatabase) {
        var db = db
        db.endTransaction()
        db.setForeignKeyConstraintsEnabled(false)
        db.beginTransaction()
        db = writableDatabase
        db.execSQL("""
    create table my_table_copy (Name VARCHAR,
    ImageUri VARCHAR,
    Description TEXT,
    Area Double DEFAULT 0,
    User VARCHAR DEFAULT null,
    PRIMARY KEY (Name, User),
    FOREIGN KEY(ImageUri) REFERENCES Images(Name),
    FOREIGN KEY(User) REFERENCES Users(Name) ON DELETE CASCADE);
    """.trimIndent())
        db.execSQL("""INSERT INTO my_table_copy (Name, ImageUri, Description, Area, User)
   SELECT Name, ImageUri, Description, Area, User FROM Lands;""")
        db.execSQL("DROP TABLE Lands;")
        db.execSQL("ALTER TABLE my_table_copy RENAME TO Lands;")
        db.execSQL("""
    CREATE TABLE my_table_copy(
    Id INTEGER primary key autoincrement,
    Land VARCHAR,
    User VARCHAR,
    PlantType VARCHAR,
    description TEXT,
    x FLOAT,
    y FLOAT,
    FOREIGN KEY(Land, User) REFERENCES Lands(Name, User) ON DELETE CASCADE,
    FOREIGN KEY(PlantType) REFERENCES PlantTypes(Name));
    """.trimIndent())
        db.execSQL("""
    INSERT INTO my_table_copy (Id,Land,User,PlantType, description,x,y) 
    SELECT Id,Land,User,PlantType, Plants.description,x,y 
    FROM Plants inner join Lands on (Land=Name);
    """.trimIndent())
        db.execSQL("DROP TABLE Plants;")
        db.execSQL("ALTER TABLE my_table_copy RENAME TO Plants;")
        db.execSQL("""CREATE TABLE my_table_copy(
Land VARCHAR NOT NULL,
    User VARCHAR,
	PlantIndex INTEGER,
    TaskType VARCHAR NOT NULL,
    Priority INTEGER NOT NULL,
	CreationDate LONG NOT NULL,
	ExpirationDate LONG,
	Completed BOOLEAN NOT NULL CHECK (Completed IN (0,1)),
	Observations TEXT,
	FOREIGN KEY(PlantIndex) REFERENCES Plants(Id) ON DELETE CASCADE,
    FOREIGN KEY(Land, User) REFERENCES Lands(Name, User) ON DELETE CASCADE,
    FOREIGN KEY(TaskType) REFERENCES TaskTypes(Name) ON DELETE CASCADE,
    FOREIGN KEY(Priority) REFERENCES Priorities(P_order));""")
        db.execSQL("""
    INSERT INTO my_table_copy (Land,User,PlantIndex, TaskType, Priority, CreationDate, ExpirationDate, Completed, Observations) 
    SELECT Land,User,PlantIndex, TaskType, Priority, CreationDate, ExpirationDate, Completed, Observations 
    FROM Tasks inner join Lands on (Land=Name);
    """.trimIndent())
        db.execSQL("DROP TABLE Tasks;")
        db.execSQL("ALTER TABLE my_table_copy RENAME TO Tasks;")
        db.endTransaction()
        db.setForeignKeyConstraintsEnabled(true)
        db.beginTransaction()
    }

    private fun upgradeVersion7(db: SQLiteDatabase) {
        var db = db
        val list = readLands(context)
        for (l in list) {
            val b = oldGetImage(context, l.imageUri)
            l.imageUri = saveToInternalStorage(context, b, l.imageUri)
            updateLand(context, l)
        }
        db.endTransaction()
        db.setForeignKeyConstraintsEnabled(false)
        db.beginTransaction()
        db = writableDatabase
        db.execSQL("""CREATE TABLE IF NOT EXISTS Lands2(
	Name VARCHAR,
	ImageUri VARCHAR,
	Description TEXT,
	Area Double DEFAULT 0,
	User VARCHAR DEFAULT null,
	primary key (Name, User),
    FOREIGN KEY(User) REFERENCES Users(Name) ON DELETE CASCADE
	);""")
        db.execSQL("""INSERT INTO Lands2 (Name, ImageUri, Description, Area, User)
   SELECT Name, ImageUri, Description, Area, User FROM Lands;""")
        db.execSQL("DROP TABLE Lands;")
        db.execSQL("ALTER TABLE Lands2 RENAME TO Lands;")
        db.endTransaction()
        db.setForeignKeyConstraintsEnabled(true)
        db.beginTransaction()
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    companion object {
        private const val DATABASE_VERSION = 7
        private const val LAND_TABLE_NAME = "myland.db"
        @JvmStatic
        fun getImage(uri: String?): Bitmap? {
            Log.v("GETIMAGE", uri!!)
            try {
                val f = File(uri)
                return BitmapFactory.decodeStream(FileInputStream(f))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        fun oldGetImage(context: Context, name: String): Bitmap? {
            val mDbHelper = LandOpenHelper(context)

            // Gets the data repository in write mode
            val db = mDbHelper.readableDatabase
            val projection = arrayOf(
                    "Name",
                    "Image"
            )

            // How you want the results sorted in the resulting Cursor
            val sortOrder: String? = null
            val whereClause = "Name = ?"
            val whereArgs = arrayOf(name)
            Log.v("GETIMAGE", name)
            val cur = db.query(
                    "Images",  // The table to query
                    projection,  // The array of columns to return (pass null to get all)
                    whereClause,  // The columns for the WHERE clause
                    whereArgs,  // The values for the WHERE clause
                    null,  // don't group the rows
                    null,  // don't filter by row groups
                    null // The sort order
            )
            if (cur.moveToFirst()) {
                Log.v("GETIMAGE", cur.getString(cur.getColumnIndex("Name")))
                Log.v("GETIMAGE", "column index: " + cur.getColumnIndex("Image"))
                val imgByte = cur.getBlob(cur.getColumnIndex("Image"))
                val bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size)
                cur.close()
                db.close()
                return bitmap
            }
            if (!cur.isClosed) {
                cur.close()
                db.close()
            }
            Log.v("image", "algo falhou")
            return null
        }

        private fun saveToInternalStorage(context: Context, bitmapImage: Bitmap?, name: String): String {
            val newImage = File(name)
            var fos: FileOutputStream? = null
            try {
                newImage.createNewFile()
                fos = FileOutputStream(newImage)
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmapImage!!.compress(Bitmap.CompressFormat.PNG, 100, fos)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    fos!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return newImage.absolutePath
        }

        fun addImage(context: Context, image: Bitmap?): String {
            val name = UUID.randomUUID().toString() + ".png"
            return addImage(context, image, name)
        }

        @JvmStatic
        fun addImage(context: Context, image: Bitmap?, name: String): String {
            Log.v("ADDIMAGE", name)
            return saveToInternalStorage(context, image, name)
        }

        @JvmStatic
        fun readTaskTypes(context: Context): ArrayList<TaskTypeObject> {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection = arrayOf(
                    "Name",
                    "Description"
            )

            // How you want the results sorted in the resulting Cursor
            val sortOrder: String? = null
            val cursor = db.query(
                    "TaskTypes",  // The table to query
                    projection,  // The array of columns to return (pass null to get all)
                    null,  // The columns for the WHERE clause
                    null,  // The values for the WHERE clause
                    null,  // don't group the rows
                    null,  // don't filter by row groups
                    sortOrder // The sort order
            )
            val taskTypes = ArrayList<TaskTypeObject>()
            while (cursor.moveToNext()) {
                val o = TaskTypeObject(cursor.getString(0), cursor.getString(1))
                taskTypes.add(o)
            }
            cursor.close()
            db.close()
            return taskTypes
        }

        fun readPriorities(context: Context): ArrayList<PriorityObject> {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection = arrayOf(
                    "Name",
                    "P_order",
                    "Color"
            )

            // How you want the results sorted in the resulting Cursor
            val sortOrder = "P_order ASC"
            val cursor = db.query(
                    "Priorities",  // The table to query
                    projection,  // The array of columns to return (pass null to get all)
                    null,  // The columns for the WHERE clause
                    null,  // The values for the WHERE clause
                    null,  // don't group the rows
                    null,  // don't filter by row groups
                    sortOrder // The sort order
            )
            val priorities = ArrayList<PriorityObject>()
            while (cursor.moveToNext()) {
                val o = PriorityObject(cursor.getString(0), cursor.getInt(1), cursor.getString(2))
                priorities.add(o)
            }
            cursor.close()
            db.close()
            return priorities
        }

        fun updateTask(t: TaskObject, context: Context): Boolean {
            val mDbHelper = LandOpenHelper(context)
            var success = true
            Log.v("Task to update", t.toString())

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val values = ContentValues()
            values.put("Land", t.land)
            values.put("PlantIndex", t.plantIndex)
            values.put("TaskType", t.taskType)
            values.put("Priority", t.priority)
            values.put("CreationDate", t.creationDate.time)
            if (t.targetDate != null) values.put("ExpirationDate", t.targetDate!!.time)
            values.put("Completed", t.completed)
            values.put("Observations", t.observations)
            val whereClause = "rowid = ?"
            val whereArgs = arrayOf("" + t.rowid)
            val newRowId = db.update("Tasks", values, whereClause, whereArgs).toLong()
            if (newRowId == -1L) success = false else {
                Log.v("UpdateTask", "row updated: $newRowId")
            }
            db.close()
            return success
        }

        fun deleteTask(t: TaskObject, context: Context): Boolean {
            val mDbHelper = LandOpenHelper(context)
            var success = true

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val whereClause = "rowid = ?"
            val whereArgs = arrayOf("" + t.rowid)
            val newRowId = db.delete("Tasks", whereClause, whereArgs).toLong()
            if (newRowId == 0L) success = false else {
                Log.v("DeleteTask", "rows deleted: $newRowId")
            }
            db.close()
            return success
        }

        fun deleteLand(l: LandObject, context: Context): Boolean {
            val mDbHelper = LandOpenHelper(context)
            var success = true

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")
            val whereClause = "Name = ? AND User = ?"
            val whereArgs = arrayOf(l.name, user)
            val newRowId = db.delete("Lands", whereClause, whereArgs).toLong()
            if (newRowId == 0L) success = false else {
                Log.v("DeleteLand", "rows deleted: $newRowId")
                deleteImage(l.imageUri, context)
            }
            db.close()
            return success
        }

        @JvmStatic
        fun deleteImage(image: String, context: Context): Boolean {
            if (isImageUsed(context, image)) return false
            val f = File(image)
            return f.delete()
        }

        private fun isImageUsed(context: Context, image: String): Boolean {
            val db = LandOpenHelper(context).readableDatabase
            // Filter results WHERE "title" = 'My Title'
            val selection = "ImageUri = ?"
            val selectionArgs = arrayOf(image)
            val cursor = db.query(
                    "Lands",  // The table to query
                    null,  // The array of columns to return (pass null to get all)
                    selection,  // The columns for the WHERE clause
                    selectionArgs,  // The values for the WHERE clause
                    null,  // don't group the rows
                    null,  // don't filter by row groups
                    null // The sort order
            )
            return cursor.count > 0
        }

        fun readUsers(context: Context): ArrayList<String> {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection = arrayOf(
                    "Name"
            )

            // How you want the results sorted in the resulting Cursor
            val cursor = db.query(
                    "Users",  // The table to query
                    projection,  // The array of columns to return (pass null to get all)
                    null,  // The columns for the WHERE clause
                    null,  // The values for the WHERE clause
                    null,  // don't group the rows
                    null,  // don't filter by row groups
                    null // The sort order
            )
            val users = ArrayList<String>()
            while (cursor.moveToNext()) users.add(cursor.getString(cursor.getColumnIndex("Name")))
            cursor.close()
            db.close()
            return users
        }

        fun createUser(context: Context, user: String?) {
            val mDbHelper = LandOpenHelper(context)

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase

// Create a new map of values, where column names are the keys
            val values = ContentValues()
            values.put("Name", user)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("Users", null, values)
            Log.v("ADDDETAIL", "row inserted: $newRowId")
            if (newRowId == -1L) Toast.makeText(context, "Land already exists, choose a different name", Toast.LENGTH_SHORT).show()
            db.close()
        }

        fun deleteUser(context: Context, user: String): Boolean {
            val mDbHelper = LandOpenHelper(context)
            var success = true

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val whereClause = "name = ?"
            val whereArgs = arrayOf(user)
            val newRowId = db.delete("Users", whereClause, whereArgs).toLong()
            if (newRowId == 0L) success = false else Log.v("DeleteUser", "rows deleted: $newRowId")
            db.close()
            return success
        }

        fun updateLands(context: Context) {
            val mDbHelper = LandOpenHelper(context)

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            db.execSQL("update Lands set User = (select Name from Users) where User is null;")
        }

        @JvmStatic
        fun readLands(context: Context): MutableList<LandObject> {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection = arrayOf(
                    "Lands.Name as 'Name'",
                    "Lands.ImageUri as 'ImageUri'",
                    "Lands.Description as 'Description'",
                    "count('Tasks'.Land) as 'Notification'",
                    "min('Tasks'.Priority) as 'Priority'",
                    LandContract.LandEntry.COLUMN_AREA
            )

            // How you want the results sorted in the resulting Cursor
            val prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")
            val cursor = db.query(
                    "Lands left outer join (select * from tasks where completed = 0) as 'Tasks' on (Lands.Name = 'Tasks'.Land AND Lands.User = 'Tasks'.User)",  // The table to query
                    projection,  // The array of columns to return (pass null to get all)
                    "Lands.User = ?", arrayOf(user),  // The values for the WHERE clause
                    "Name",  // don't group the rows
                    null,  // don't filter by row groups
                    "Lands.rowid asc" // The sort order
            )
            /*Cursor cursor = db.rawQuery("select Name, ImageUri, Description, count(Tasks.Land) as 'Notification' \n" +
                "from Lands left outer join Tasks on Lands.Name = Tasks.Land\n" +
                "where Priority is null or Priority = 1\n" +
                "group by Tasks.Land", null
        );*/
            val lands: MutableList<LandObject> = ArrayList()
            while (cursor.moveToNext()) {
                val priority = if (cursor.isNull(cursor.getColumnIndex("Priority"))) 0 else cursor.getInt(cursor.getColumnIndex("Priority"))
                val o = LandObject(cursor.getString(cursor.getColumnIndex("Name")), cursor.getString(cursor.getColumnIndex("ImageUri")), cursor.getString(cursor.getColumnIndex("Description")), cursor.getInt(cursor.getColumnIndex("Notification")), priority, cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_AREA)))
                lands.add(o)
            }
            Log.v("Lands", lands.toString())
            cursor.close()
            db.close()
            return lands
        }

        @JvmStatic
        fun readPlantTypes(context: Context): ArrayList<PlantTypeObject> {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection = arrayOf(
                    "Name",
                    "Icon",
                    "Color"
            )

            // How you want the results sorted in the resulting Cursor
            val sortOrder: String? = null
            val cursor = db.query(
                    "PlantTypes",  // The table to query
                    projection,  // The array of columns to return (pass null to get all)
                    null,  // The columns for the WHERE clause
                    null,  // The values for the WHERE clause
                    null,  // don't group the rows
                    null,  // don't filter by row groups
                    sortOrder // The sort order
            )
            val plants = ArrayList<PlantTypeObject>()
            while (cursor.moveToNext()) {
                val o = PlantTypeObject(cursor.getString(0), cursor.getInt(1), cursor.getString(2))
                plants.add(o)
            }
            cursor.close()
            db.close()
            return plants
        }

        @JvmStatic
        fun readPlants(context: Context, name: String?): ArrayList<PlantObject> {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase
            val prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")
            val projection2 = arrayOf(
                    "rowid",
                    "Land",
                    "PlantType",
                    "Description",
                    "x",
                    "y"
            )

            // Filter results WHERE "title" = 'My Title'
            val selection2 = "Land = ? and User = ?"

            // How you want the results sorted in the resulting Cursor
            val sortOrder2 = "Id ASC"
            val selectionArgs = arrayOf(name, user)
            val cur = db.query(
                    "Plants",  // The table to query
                    projection2,  // The array of columns to return (pass null to get all)
                    selection2,  // The columns for the WHERE clause
                    selectionArgs,  // The values for the WHERE clause
                    null,  // don't group the rows
                    null,  // don't filter by row groups
                    sortOrder2 // The sort order
            )
            val plants = ArrayList<PlantObject>()
            while (cur.moveToNext()) plants.add(PlantObject(cur.getInt(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_ID)), cur.getString(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_PLANT_TYPE)), cur.getString(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_DESCRIPTION)), cur.getFloat(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_X)), cur.getFloat(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_Y))))
            cur.close()
            db.close()
            return plants
        }

        @JvmStatic
        fun readTasks(context: Context, land: String?): ArrayList<TaskObject> {
            val tasks = ArrayList<TaskObject>()
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase
            val prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection = arrayOf(
                    "Land",
                    "PlantIndex",
                    "TaskType",
                    "Priority",
                    "CreationDate",
                    "ExpirationDate",
                    "Completed",
                    "Observations",
                    "rowid"
            )

            // How you want the results sorted in the resulting Cursor
            val sortOrder = "Priority ASC"
            val selection = "Land = ? AND User = ? AND Completed = 0"
            val selectionArgs = arrayOf(land, user)
            val cursor = db.query(
                    "Tasks",  // The table to query
                    projection,  // The array of columns to return (pass null to get all)
                    selection,  // The columns for the WHERE clause
                    selectionArgs,  // The values for the WHERE clause
                    null,  // don't group the rows
                    null,  // don't filter by row groups
                    sortOrder // The sort order
            )

            //ArrayList<TaskObject> tasks = new ArrayList<>();
            tasks.clear()
            while (cursor.moveToNext()) {
                val cl = Calendar.getInstance()
                cl.timeInMillis = cursor.getLong(cursor.getColumnIndex("CreationDate"))
                val cl2 = if (cursor.isNull(cursor.getColumnIndex("ExpirationDate"))) null else Calendar.getInstance()
                if (cl2 != null) cl2.timeInMillis = cursor.getLong(cursor.getColumnIndex("ExpirationDate"))
                val targetDate = cl2?.time
                val o = TaskObject(
                        cursor.getLong(cursor.getColumnIndex("rowid")), cursor.getString(cursor.getColumnIndex("Land")), if (cursor.isNull(cursor.getColumnIndex("PlantIndex"))) null else cursor.getInt(cursor.getColumnIndex("PlantIndex")), cursor.getString(cursor.getColumnIndex("TaskType")), cursor.getInt(cursor.getColumnIndex("Priority")), cl.time, targetDate, cursor.getInt(cursor.getColumnIndex("Completed")) > 0, cursor.getString(cursor.getColumnIndex("Observations")))
                tasks.add(o)
                Log.v("read tasks", "task: $o")
            }
            cursor.close()
            db.close()
            return tasks
        }

        @JvmStatic
        fun addLand(context: Context, l: LandObject): Boolean {
            val mDbHelper = LandOpenHelper(context)

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")

// Create a new map of values, where column names are the keys
            val values = ContentValues()
            values.put("Name", l.name)
            values.put("ImageUri", l.imageUri)
            values.put("Description", l.Description)
            values.put("Area", l.area)
            values.put("User", user)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("Lands", null, values)
            db.close()
            Log.v("Importer", "land inserted: $newRowId")
            return newRowId > -1
        }

        fun updateLand(context: Context, l: LandObject): Boolean {
            val mDbHelper = LandOpenHelper(context)
            var success = true
            Log.v("Land to update", l.toString())
            val prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val values = ContentValues()
            values.put("ImageUri", l.imageUri)
            values.put("Description", l.Description)
            values.put("Area", l.area)
            val whereClause = "Name = ? AND User = ?"
            val whereArgs = arrayOf(l.name, user)
            val newRowId = db.update("Tasks", values, whereClause, whereArgs).toLong()
            if (newRowId == -1L) success = false else {
                Log.v("UpdateLand", "row updated: $newRowId")
            }
            db.close()
            return success
        }

        @JvmStatic
        fun addPlant(context: Context, p: PlantObject, land: String?): Boolean {
            val mDbHelper = LandOpenHelper(context)

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")

// Create a new map of values, where column names are the keys
            val values = ContentValues()
            values.put("Land", land)
            values.put("User", user)
            values.put("PlantType", p.plantType)
            values.put("Description", p.description)
            values.put("x", p.x)
            values.put("y", p.y)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("Plants", null, values)
            p.id = newRowId.toInt()
            Log.v("ADDDETAIL", "row inserted: $newRowId")
            return newRowId > -1
        }

        @JvmStatic
        fun addTask(context: Context, t: TaskObject): Boolean {
            val mDbHelper = LandOpenHelper(context)

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val prefs = context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")
            val values = ContentValues()
            values.put("Land", t.land)
            values.put("User", user)
            values.put("PlantIndex", t.plantIndex)
            values.put("TaskType", t.taskType)
            values.put("Priority", t.priority)
            values.put("CreationDate", t.creationDate.time)
            if (t.targetDate != null) values.put("ExpirationDate", t.targetDate!!.time)
            values.put("Completed", t.completed)
            values.put("Observations", t.observations)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("Tasks", null, values)
            db.close()
            return newRowId > -1
        }

        @JvmStatic
        fun addItemType(context: Context, item: PlantTypeObject): Boolean {
            val mDbHelper = LandOpenHelper(context)

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val values = ContentValues()
            values.put("Name", item.name)
            values.put("Icon", item.icon)
            values.put("Color", item.color)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("PlantTypes", null, values)
            return newRowId > -1
        }

        @JvmStatic
        fun addTaskType(context: Context, item: TaskTypeObject): Boolean {
            val mDbHelper = LandOpenHelper(context)

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val values = ContentValues()
            values.put("Name", item.name)
            values.put("Description", item.description)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("TaskTypes", null, values)
            db.close()
            return newRowId > -1
        }

        fun deletePlantObject(context: Context, plantObject: PlantObject): Boolean {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.writableDatabase
            val id = plantObject.id
            val whereClause = "Id = ?"
            val whereArgs = arrayOf("" + id)

            val i = db.delete("Plants", whereClause, whereArgs)
            Log.v("Remove Plant", "$i rows removed")
            db.close()
            return i > 0
        }
    }
}