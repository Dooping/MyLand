package com.gago.david.myland

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import com.gago.david.myland.LandOpenHelper.Companion.addImage
import com.gago.david.myland.LandOpenHelper.Companion.addItemType
import com.gago.david.myland.LandOpenHelper.Companion.addLand
import com.gago.david.myland.LandOpenHelper.Companion.addPlant
import com.gago.david.myland.LandOpenHelper.Companion.addTask
import com.gago.david.myland.LandOpenHelper.Companion.addTaskType
import com.gago.david.myland.LandOpenHelper.Companion.deleteImage
import com.gago.david.myland.models.TaskObject
import com.gago.david.myland.models.*
import com.gago.david.myland.models.LandContract.ItemEntry
import com.gago.david.myland.models.LandContract.LandEntry
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*

/**
 * Created by david on 27/01/2017.
 */
class LandImporterHelper(private val context: Context) : SQLiteOpenHelper(context, LAND_TABLE_NAME, null, DATABASE_VERSION) {
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

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(false)
    }

    @Throws(IOException::class)
    fun importDatabase(dbPath: Uri): Boolean {
        Log.v("FILE", "path: $dbPath")
        val oldDb = File("//data/data/com.gago.david.myland/databases/", LAND_TABLE_NAME)
        context.contentResolver.openInputStream(dbPath).use { `is` ->
            FileOutputStream(oldDb).use { os ->
                val buffer = ByteArray(1024)
                var length: Int
                if (`is` != null) {
                    while (`is`.read(buffer).also { length = it } > 0) {
                        os.write(buffer, 0, length)
                    }
                }
            }
        }
        val itemTypes: List<PlantTypeObject> = readPlantTypes()
        for (i in itemTypes) addItemType(context, i)
        val taskTypes: List<TaskTypeObject> = readTaskTypes()
        for (t in taskTypes) addTaskType(context, t)
        val lands = readLands()
        Log.v("Lands", lands.toString())
        for (l in lands) {
            l.plants = readPlants(l.name)
            val image = getImage(l.imageUri)
            val tasks: MutableList<TaskObject> = readTasks(l.name)
            addImage(context, image, l.imageUri)
            if (addLand(context, l)) {
                Log.v("Importer", "adicionou terreno")
                for (p in l.plants) {
                    val oldId = p.id
                    if (addPlant(context, p, l.name)) {
                        Log.v("Importer", "adicionou item")
                        val taskIterator = tasks.iterator()
                        while (taskIterator.hasNext()) {
                            val t = taskIterator.next()
                            if (t.plantIndex != null && t.plantIndex == oldId) {
                                Log.v("Importer", "adicionou tarefa")
                                t.plantIndex = p.id
                                addTask(context, t)
                                taskIterator.remove()
                            }
                        }
                    }
                }
                for (t in tasks) if (t.plantIndex == null) addTask(context, t)
            } else deleteImage(l.imageUri, context)
        }
        return true
    }

    @SuppressLint("Range")
    private fun getImage(name: String): Bitmap? {
        val db = readableDatabase
        val projection = arrayOf(
                "Name",
                "Image"
        )

        // How you want the results sorted in the resulting Cursor
        val sortOrder: String? = null
        val whereClause = "Name = ?"
        val whereArgs = arrayOf(name)
        val cur = db.query(
                "Images",  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                whereClause,  // The columns for the WHERE clause
                whereArgs,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder // The sort order
        )
        if (cur.moveToFirst()) {
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

    private fun readTaskTypes(): ArrayList<TaskTypeObject> {
        val db = readableDatabase

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

    private fun readPlantTypes(): ArrayList<PlantTypeObject> {
        val db = readableDatabase

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
                "Name",
                "Icon",
                "Color"
        )
        val cursor = db.query(
                "PlantTypes",  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                null,  // The columns for the WHERE clause
                null,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                null // The sort order
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
    private fun readLands(): List<LandObject> {
        val db = readableDatabase

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(
            LandEntry.COLUMN_NAME,
            LandEntry.COLUMN_IMAGE,
            LandEntry.COLUMN_DESCRIPTION,
            LandEntry.COLUMN_AREA,
            LandEntry.COLUMN_CENTER_LAT,
            LandEntry.COLUMN_CENTER_LON,
            LandEntry.COLUMN_ZOOM,
            LandEntry.COLUMN_BEARING,
            LandEntry.COLUMN_POLYGON
        )
        val cursor = db.query(
                LandEntry.TABLE_NAME,  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                null,  // The columns for the WHERE clause
                null,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                null // The sort order
        )
        /*Cursor cursor = db.rawQuery("select Name, ImageUri, Description, count(Tasks.Land) as 'Notification' \n" +
                "from Lands left outer join Tasks on Lands.Name = Tasks.Land\n" +
                "where Priority is null or Priority = 1\n" +
                "group by Tasks.Land", null
        );*/
        val lands: MutableList<LandObject> = ArrayList()
        while (cursor.moveToNext()) {
            val o = LandObject(
                cursor.getString(cursor.getColumnIndex(LandEntry.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndex(LandEntry.COLUMN_IMAGE)),
                cursor.getString(cursor.getColumnIndex(LandEntry.COLUMN_DESCRIPTION)),
                cursor.getDouble(cursor.getColumnIndex(LandEntry.COLUMN_AREA)),
                cursor.getDouble(cursor.getColumnIndex(LandEntry.COLUMN_CENTER_LAT)),
                cursor.getDouble(cursor.getColumnIndex(LandEntry.COLUMN_CENTER_LON)),
                cursor.getDouble(cursor.getColumnIndex(LandEntry.COLUMN_ZOOM)),
                cursor.getDouble(cursor.getColumnIndex(LandEntry.COLUMN_BEARING)),
                cursor.getString(cursor.getColumnIndex(LandEntry.COLUMN_POLYGON))
            )
            lands.add(o)
        }

        //Log.v("Lands", lands.toString());
        cursor.close()
        db.close()
        return lands
    }

    @SuppressLint("Range")
    private fun readPlants(name: String): ArrayList<PlantObject> {
        val db = readableDatabase
        val projection2 = arrayOf(
                ItemEntry.COLUMN_ID,
                ItemEntry.COLUMN_LAND,
                ItemEntry.COLUMN_DESCRIPTION,
                ItemEntry.COLUMN_PLANT_TYPE,
                ItemEntry.COLUMN_X,
                ItemEntry.COLUMN_Y
        )

        // Filter results WHERE "title" = 'My Title'
        val selection2 = "Land" + " = ?"

        // How you want the results sorted in the resulting Cursor
        val sortOrder2 = "Id ASC"
        val selectionArgs = arrayOf(name)
        val cur = db.query(
                ItemEntry.TABLE_NAME,  // The table to query
                projection2,  // The array of columns to return (pass null to get all)
                selection2,  // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                sortOrder2 // The sort order
        )
        val plants = ArrayList<PlantObject>()
        while (cur.moveToNext()) plants.add(PlantObject(cur.getInt(cur.getColumnIndex(ItemEntry.COLUMN_ID)), cur.getString(cur.getColumnIndex(ItemEntry.COLUMN_PLANT_TYPE)), cur.getString(cur.getColumnIndex(ItemEntry.COLUMN_DESCRIPTION)), cur.getFloat(cur.getColumnIndex(ItemEntry.COLUMN_X)), cur.getFloat(cur.getColumnIndex(ItemEntry.COLUMN_Y))))
        cur.close()
        db.close()
        Log.v("Read plants", plants.toString())
        return plants
    }

    @SuppressLint("Range")
    private fun readTasks(land: String): ArrayList<TaskObject> {
        val db = readableDatabase

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
        val selection = "Land = ?"
        val selectionArgs = arrayOf(land)
        val cursor = db.query(
                "Tasks",  // The table to query
                projection,  // The array of columns to return (pass null to get all)
                selection,  // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,  // don't group the rows
                null,  // don't filter by row groups
                null // The sort order
        )
        val tasks = ArrayList<TaskObject>()
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

    companion object {
        private const val DATABASE_VERSION = 4
        const val LAND_TABLE_NAME = "import.db"
    }
}