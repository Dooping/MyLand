package com.gago.david.myland

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import com.gago.david.myland.models.TaskObject
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

        if (oldVersion < 8)
            upgradeVersion8(db)
        if (oldVersion < 9)
            upgradeVersion9(db)
        if (oldVersion < 10)
            upgradeVersion10(db)
        if (oldVersion < 11)
            upgradeVersion11(db)
        if (oldVersion < 12)
            upgradeVersion12(db)
        if (oldVersion < 13)
            upgradeVersion13(db)
        if (oldVersion < 14)
            upgradeVersion14(db)
        if (oldVersion < 15)
            upgradeVersion15(db)
        if (oldVersion < 16)
            upgradeVersion16(db)
        else if (oldVersion < newVersion) {
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

    private fun upgradeVersion8(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE Tasks ADD COLUMN CompletedDate Long;")
        Log.v("DATABASE", "updated to version 8")
    }

    private fun upgradeVersion9(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE Tasks ADD COLUMN Archived BOOLEAN NOT NULL CHECK (Completed IN (0,1)) DEFAULT 0;")
        db.execSQL("ALTER TABLE Tasks ADD COLUMN ArchivedDate Long;")
        Log.v("DATABASE", "updated to version 9")
    }

    private fun upgradeVersion10(db: SQLiteDatabase) {
        db.execSQL("UPDATE PlantTypes SET Icon = Icon + 10 WHERE Icon >= 2131230870;")
        db.execSQL("UPDATE PlantTypes SET Icon = Icon + 6 WHERE Icon < 2131230870;")
        Log.v("DATABASE", "updated to version 10")
    }



    private fun upgradeVersion11(db: SQLiteDatabase) {
        db.execSQL("UPDATE PlantTypes SET Icon = Icon + 4;")
        Log.v("DATABASE", "updated to version 11")
    }

    private fun upgradeVersion12(db: SQLiteDatabase) {
        db.endTransaction()
        db.setForeignKeyConstraintsEnabled(false)
        db.beginTransactionNonExclusive()
        db.execSQL("ALTER TABLE PlantTypes ADD COLUMN Icon2 TEXT;")
        db.execSQL("UPDATE PlantTypes SET Icon2 = 'ic_tree' WHERE Icon = 2131230892;")
        db.execSQL("UPDATE PlantTypes SET Icon2 = 'ic_rock' WHERE Icon = 2131230888;")
        db.execSQL("UPDATE PlantTypes SET Icon2 = 'ic_grass' WHERE Icon = 2131230870;")
        db.execSQL("UPDATE PlantTypes SET Icon2 = 'ic_water' WHERE Icon = 2131230893;")
        db.execSQL("UPDATE PlantTypes SET Icon2 = 'ic_cut' WHERE Icon = 2131230866;")
        db.execSQL("UPDATE PlantTypes SET Icon2 = 'ic_paper_bag' WHERE Icon = 2131230886;")
        db.execSQL("UPDATE PlantTypes SET Icon2 = 'ic_watering_can' WHERE Icon = 2131230894;")
        db.execSQL("UPDATE PlantTypes SET Icon2 = 'ic_tree' WHERE Icon2 is null;")

        db.execSQL("DROP TABLE IF EXISTS plant_types_copy;")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS plant_types_copy(
                Name VARCHAR PRIMARY KEY,
                Icon VARCHAR,
                Color VARCHAR
            );""".trimIndent())
        db.execSQL("""
            INSERT INTO plant_types_copy (Name,Icon,Color) 
            SELECT Name,Icon2 as Icon,Color
            FROM PlantTypes;
        """.trimIndent())
        db.execSQL("DROP TABLE PlantTypes;")
        db.execSQL("ALTER TABLE plant_types_copy RENAME TO PlantTypes;")
        db.setTransactionSuccessful()
        db.endTransaction()
        db.setForeignKeyConstraintsEnabled(true)
        Log.v("DATABASE", "updated to version 12")
        db.beginTransaction()
    }

    private fun upgradeVersion13(db: SQLiteDatabase) {
        readAllLands(db).forEach {
            val image = oldGetImage(it.imageUri)!!
            val filename=it.imageUri.substring(it.imageUri.lastIndexOf("/")+1)
            it.imageUri = filename
            addImage(context, image, filename)
            updateLand(db, it)
            Log.v("DATABASE", "updated to version 13")
        }
    }

    private fun updateLand(db: SQLiteDatabase, land: LandObject) {
            db.execSQL("""
                |UPDATE ${LandContract.LandEntry.TABLE_NAME}
                |SET ${LandContract.LandEntry.COLUMN_IMAGE} = ${land.imageUri}
                |WHERE User = ${land.user} AND Name = ${land.name};
                |""".trimMargin())
    }

    private fun readAllLands(db: SQLiteDatabase): List<LandObject> {
        val projection = arrayOf(
            LandContract.LandEntry.COLUMN_NAME,
            LandContract.LandEntry.COLUMN_IMAGE,
            LandContract.LandEntry.COLUMN_DESCRIPTION,
            LandContract.LandEntry.COLUMN_USER
        )
        val c = db.query(
            LandContract.LandEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            null
        )
        val lands = generateSequence { if (c.moveToNext()) c else null }
            .map { cursorToLand(it) }
            .toList()

        c.close()
        return lands
    }

    @SuppressLint("Range")
    fun cursorToLand(cursor: Cursor): LandObject {
        return LandObject(
            cursor.getString(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_NAME)),
            cursor.getString(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_IMAGE)),
            cursor.getString(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_DESCRIPTION)),
            cursor.getString(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_USER)),
        )
    }

    private fun upgradeVersion14(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE ${LandContract.LandEntry.TABLE_NAME} ADD COLUMN ${LandContract.LandEntry.COLUMN_BEARING} Double default 0;")
        db.execSQL("ALTER TABLE ${LandContract.LandEntry.TABLE_NAME} ADD COLUMN ${LandContract.LandEntry.COLUMN_CENTER_LAT} Float default 0;")
        db.execSQL("ALTER TABLE ${LandContract.LandEntry.TABLE_NAME} ADD COLUMN ${LandContract.LandEntry.COLUMN_CENTER_LON} Float default 0;")
        db.execSQL("ALTER TABLE ${LandContract.LandEntry.TABLE_NAME} ADD COLUMN ${LandContract.LandEntry.COLUMN_ZOOM} Float default 0;")
        Log.v("DATABASE", "updated to version 14")
    }

    private fun upgradeVersion15(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE ${LandContract.LandEntry.TABLE_NAME} ADD COLUMN ${LandContract.LandEntry.COLUMN_POLYGON} VARCHAR;")
        Log.v("DATABASE", "updated to version 15")
    }

    private fun upgradeVersion16(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE ${LandContract.ItemEntry.TABLE_NAME} ADD COLUMN ${LandContract.ItemEntry.COLUMN_LAT} Float default 0;")
        db.execSQL("ALTER TABLE ${LandContract.ItemEntry.TABLE_NAME} ADD COLUMN ${LandContract.ItemEntry.COLUMN_LON} Float default 0;")
        Log.v("DATABASE", "updated to version 16")
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    companion object {
        private const val DATABASE_VERSION = 16
        private const val LAND_TABLE_NAME = "myland.db"

        fun getImage(context: Context, uri: String?): Bitmap? {
            Log.v("GETIMAGE", uri!!)
            return context.openFileInput(uri).use { BitmapFactory.decodeStream(it) }
        }

        fun oldGetImage(uri: String): Bitmap? {
            try {
                val f = File(uri)
                return BitmapFactory.decodeStream(FileInputStream(f))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return null
        }

        private fun saveToInternalStorage(context: Context, bitmapImage: Bitmap?, name: String): Boolean {
            return context.openFileOutput(name, Context.MODE_PRIVATE).use {
                bitmapImage!!.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }

        fun addImage(context: Context, image: Bitmap?): String {
            val name = UUID.randomUUID().toString() + ".png"
            return addImage(context, image, name)
        }

        fun addImage(context: Context, image: Bitmap?, name: String): String {
            Log.v("ADDIMAGE", name)
            saveToInternalStorage(context, image, name)
            return name
        }

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
            values.put("CompletedDate", t.completedDate?.time)
            values.put("Archived", t.archived)
            values.put("ArchivedDate", t.archivedDate?.time)
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

        fun deleteImage(name: String, context: Context): Boolean {
            Log.v("DELETE_IMAGE", name)
            return context.deleteFile(name)
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

        @SuppressLint("Range")
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

        @SuppressLint("Range")
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
                LandContract.LandEntry.COLUMN_AREA,
                LandContract.LandEntry.COLUMN_CENTER_LAT,
                LandContract.LandEntry.COLUMN_CENTER_LON,
                LandContract.LandEntry.COLUMN_ZOOM,
                LandContract.LandEntry.COLUMN_BEARING,
                LandContract.LandEntry.COLUMN_POLYGON
            )

            // How you want the results sorted in the resulting Cursor
            val prefs =
                context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")
            val cursor = db.query(
                """
                    |Lands left outer join (select * from tasks where completed = 0) as 'Tasks'
                    |on (Lands.Name = 'Tasks'.Land AND Lands.User = 'Tasks'.User)
                    |""".trimMargin(),  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                "Lands.User = ?", arrayOf(user),  // The values for the WHERE clause
                "Name",  // don't group the rows
                null,  // don't filter by row groups
                "Lands.rowid asc" // The sort order
            )
            val lands: MutableList<LandObject> = ArrayList()
            while (cursor.moveToNext()) {
                val priority = when {
                    cursor.isNull(cursor.getColumnIndex("Priority")) -> 0
                    else -> cursor.getInt(cursor.getColumnIndex("Priority"))
                }
                val o = LandObject(
                    cursor.getString(cursor.getColumnIndex("Name")),
                    cursor.getString(cursor.getColumnIndex("ImageUri")),
                    cursor.getString(cursor.getColumnIndex("Description")),
                    cursor.getInt(cursor.getColumnIndex("Notification")),
                    priority,
                    cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_AREA)),
                    cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_CENTER_LAT)),
                    cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_CENTER_LON)),
                    cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_ZOOM)),
                    cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_BEARING)),
                    cursor.getString(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_POLYGON))
                )
                o.totalTasks = getTotalTasksFromLand(context, o)
                lands.add(o)
            }
            Log.v("Lands", lands.toString())
            cursor.close()
            db.close()
            return lands
        }

        @SuppressLint("Range")
        fun readLandWithArea(landName: String?, context: Context): LandObject {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection = arrayOf(
                LandContract.LandEntry.COLUMN_NAME,
                LandContract.LandEntry.COLUMN_IMAGE,
                LandContract.LandEntry.COLUMN_DESCRIPTION,
                LandContract.LandEntry.COLUMN_AREA,
                LandContract.LandEntry.COLUMN_CENTER_LAT,
                LandContract.LandEntry.COLUMN_CENTER_LON,
                LandContract.LandEntry.COLUMN_ZOOM,
                LandContract.LandEntry.COLUMN_BEARING,
                LandContract.LandEntry.COLUMN_POLYGON
            )

            // Filter results WHERE "title" = 'My Title'
            val selection = "Name" + " = ?"
            val selectionArgs = arrayOf(landName)

            // How you want the results sorted in the resulting Cursor
            val sortOrder: String? = null
            val cursor = db.query(
                "Lands",  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                selection,  // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder // The sort order
            )
            cursor.moveToFirst()
            val l = LandObject(landName!!,
                cursor.getString(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_IMAGE)),
                cursor.getString(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_DESCRIPTION)),
                cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_AREA)),
                cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_CENTER_LAT)),
                cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_CENTER_LON)),
                cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_ZOOM)),
                cursor.getDouble(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_BEARING)),
                cursor.getString(cursor.getColumnIndex(LandContract.LandEntry.COLUMN_POLYGON)))
            cursor.close()
            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            val projection2 = arrayOf(
                "Id",
                "Land",
                "PlantType",
                "Description",
                "lat",
                "lon"
            )

            // Filter results WHERE "title" = 'My Title'
            val selection2 = "Land" + " = ?"

            // How you want the results sorted in the resulting Cursor
            val sortOrder2 = "Id ASC"
            val cursor2 = db.query(
                "Plants",  // The table to query
                projection2,  // The array of columns to return (pass null to get all)
                selection2,  // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder2 // The sort order
            )
            while (cursor2.moveToNext())
                l.addPlant(
                    PlantObject(
                        cursor2.getInt(cursor2.getColumnIndex("Id")),
                        cursor2.getString(2),
                        cursor2.getString(3),
                        cursor2.getFloat(4),
                        cursor2.getFloat(5)
                    )
                )
            cursor2.close()
            db.close()
            return l
        }

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
                val icon = context.resources.getIdentifier(cursor.getString(1), "drawable", context.packageName)
                val o = PlantTypeObject(cursor.getString(0), icon, cursor.getString(2))
                plants.add(o)
            }
            cursor.close()
            db.close()
            return plants
        }

        @SuppressLint("Range")
        fun readPlants(context: Context, name: String?): ArrayList<PlantObject> {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase
            val prefs =
                context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")
            val projection2 = arrayOf(
                "rowid",
                "Land",
                "PlantType",
                "Description",
                "lat",
                "lon"
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
            while (cur.moveToNext()) plants.add(
                PlantObject(
                    cur.getInt(
                        cur.getColumnIndex(
                            LandContract.ItemEntry.COLUMN_ID
                        )
                    ),
                    cur.getString(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_PLANT_TYPE)),
                    cur.getString(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_DESCRIPTION)),
                    cur.getFloat(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_LAT)),
                    cur.getFloat(cur.getColumnIndex(LandContract.ItemEntry.COLUMN_LON))
                )
            )
            cur.close()
            db.close()
            return plants
        }

        @SuppressLint("Range")
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

        @SuppressLint("Range")
        fun readTaskHistory(context: Context, land: String?): ArrayList<TaskObject> {
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
                "rowid",
                "CompletedDate"
            )

            // How you want the results sorted in the resulting Cursor
            val sortOrder = "CompletedDate DESC"
            val selection = "Land = ? AND User = ? AND Completed = 1 AND Archived = 0"
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

            tasks.clear()
            while (cursor.moveToNext()) {
                val cl = Calendar.getInstance()
                cl.timeInMillis = cursor.getLong(cursor.getColumnIndex("CreationDate"))
                val cl2 = if (cursor.isNull(cursor.getColumnIndex("ExpirationDate"))) null else Calendar.getInstance()
                if (cl2 != null) cl2.timeInMillis = cursor.getLong(cursor.getColumnIndex("ExpirationDate"))
                val targetDate = cl2?.time
                val cl3 = if (cursor.isNull(cursor.getColumnIndex("CompletedDate"))) null else Calendar.getInstance()
                if (cl3 != null) cl3.timeInMillis = cursor.getLong(cursor.getColumnIndex("CompletedDate"))
                val completedDate = cl3?.time
                val o = TaskObject(
                    cursor.getLong(cursor.getColumnIndex("rowid")),
                    cursor.getString(cursor.getColumnIndex("Land")),
                    if (cursor.isNull(cursor.getColumnIndex("PlantIndex"))) null else cursor.getInt(
                        cursor.getColumnIndex("PlantIndex")
                    ),
                    cursor.getString(cursor.getColumnIndex("TaskType")),
                    cursor.getInt(cursor.getColumnIndex("Priority")),
                    cl.time,
                    targetDate,
                    cursor.getInt(cursor.getColumnIndex("Completed")) > 0,
                    cursor.getString(cursor.getColumnIndex("Observations")),
                    completedDate
                )
                tasks.add(o)
                Log.v("read tasks", "task: $o")
            }
            cursor.close()
            db.close()
            return tasks
        }

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
            values.put("Description", l.description)
            values.put("Area", l.area)
            values.put("center_lat", l.lat)
            values.put("center_lon", l.lon)
            values.put("zoom", l.zoom)
            values.put("bearing", l.bearing)
            values.put("polygon", l.polygon)
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
            values.put("Description", l.description)
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
            values.put("lat", p.lat)
            values.put("lon", p.lon)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("Plants", null, values)
            p.id = newRowId.toInt()
            Log.v("ADDDETAIL", "row inserted: $newRowId")
            return newRowId > -1
        }

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
            values.put("Archived", t.archived)
            values.put("Completed", t.completed)
            values.put("Observations", t.observations)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("Tasks", null, values)
            db.close()
            return newRowId > -1
        }

        fun addItemType(context: Context, item: PlantTypeObject): Boolean {
            val mDbHelper = LandOpenHelper(context)

            val iconName = context.resources.getResourceEntryName(item.icon)

            // Gets the data repository in write mode
            val db = mDbHelper.writableDatabase
            val values = ContentValues()
            values.put("Name", item.name)
            values.put("Icon", iconName)
            values.put("Color", item.color)

// Insert the new row, returning the primary key value of the new row
            val newRowId = db.insert("PlantTypes", null, values)
            return newRowId > -1
        }

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

        fun archiveOldTasks(context: Context) {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.writableDatabase
            val prefs =
                context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")
            val contentValues = ContentValues()
            contentValues.put("archived", true)
            contentValues.put("archivedDate", Date().time)

            db.update(
                "Tasks",
                contentValues,
                "user = ? and not archived and completed",
                arrayOf(user)
            )
        }

        fun getTotalTasksFromLand(context: Context, land: LandObject): Int {
            val mDbHelper = LandOpenHelper(context)
            val db = mDbHelper.readableDatabase
            val prefs =
                context.getSharedPreferences(SettingsFragment.MY_PREFS_NAME, Context.MODE_PRIVATE)
            val user = prefs.getString("user", "")

            val cursor = db.rawQuery("select count(Land) as 'total' \n" +
                    "from Tasks\n" +
                    "where Land = ? and User = ? and archived = 0\n", arrayOf(land.name, user))

            var result = 0
            while (cursor.moveToNext())
                result = cursor.getInt(0)
            cursor.close()
            db.close()
            return result;
        }
    }
}