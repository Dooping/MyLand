package com.gago.david.myland

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.gago.david.myland.LandOpenHelper.Companion.getImage
import com.gago.david.myland.LandOpenHelper.Companion.readLands
import com.gago.david.myland.LandOpenHelper.Companion.readPlantTypes
import com.gago.david.myland.LandOpenHelper.Companion.readPlants
import com.gago.david.myland.LandOpenHelper.Companion.readTaskTypes
import com.gago.david.myland.LandOpenHelper.Companion.readTasks
import com.gago.david.myland.models.*
import com.gago.david.myland.models.LandContract.ItemEntry
import com.gago.david.myland.models.LandContract.ItemTypeEntry
import com.gago.david.myland.models.LandContract.LandEntry
import com.gago.david.myland.models.LandContract.TaskEntry
import com.gago.david.myland.models.LandContract.TaskTypeEntry
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.*

/**
 * Created by david on 27/01/2017.
 */
class LandExporterHelper(private val context: Context) : SQLiteOpenHelper(context, LAND_TABLE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val inputStream = context.resources.openRawResource(R.raw.db_export)
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

    /**
     * Copies the database file at the specified location over the current
     * internal application database.
     */
    @Throws(IOException::class)
    fun exportDatabase(dbPath: Uri?): Boolean {
        val lands: List<LandObject> = readLands(context)
        val taskTypes: List<TaskTypeObject> = readTaskTypes(context)
        val itemTypes: List<PlantTypeObject> = readPlantTypes(context)
        val tasks: MutableList<TaskObject> = ArrayList()
        for (land in lands) {
            addImage(getImage(land.imageUri), land.imageUri)
            writePlants(readPlants(context, land.name), land.name)
            tasks.addAll(readTasks(context, land.name))
        }
        writeTaskTypes(taskTypes)
        writeItemTypes(itemTypes)
        writeLands(lands)
        writeTasks(tasks)
        close()
        val newDb = File("//data/data/com.gago.david.myland/databases/", LAND_TABLE_NAME)
        val pfd = context.contentResolver.openFileDescriptor(dbPath!!, "w")
        try {
            val `is`: InputStream = FileInputStream(newDb)
            val os = FileOutputStream(pfd!!.fileDescriptor)
            val buffer = ByteArray(1024)
            var length: Int
            while (`is`.read(buffer).also { length = it } > 0) {
                os.write(buffer, 0, length)
            }
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return false
    }

    private fun writeLands(lands: List<LandObject>) {
        val db = writableDatabase
        for (land in lands) {
            val values = ContentValues()
            values.put(LandEntry.COLUMN_NAME, land.name)
            values.put(LandEntry.COLUMN_IMAGE, land.imageUri)
            values.put(LandEntry.COLUMN_DESCRIPTION, land.Description)
            values.put(LandEntry.COLUMN_AREA, land.area)
            db.insert(LandEntry.TABLE_NAME, null, values)
        }
        close()
    }

    private fun writePlants(plants: List<PlantObject>, land: String) {
        val db = writableDatabase
        for (p in plants) {
            Log.v("write", "" + p.id)
            val values = ContentValues()
            values.put(ItemEntry.COLUMN_ID, p.id)
            values.put(ItemEntry.COLUMN_LAND, land)
            values.put(ItemEntry.COLUMN_PLANT_TYPE, p.plantType)
            values.put(ItemEntry.COLUMN_DESCRIPTION, p.description)
            values.put(ItemEntry.COLUMN_X, p.x)
            values.put(ItemEntry.COLUMN_Y, p.y)
            db.insert(ItemEntry.TABLE_NAME, null, values)
        }
        close()
    }

    private fun writeTaskTypes(taskTypes: List<TaskTypeObject>) {
        val db = writableDatabase
        for (t in taskTypes) {
            val values = ContentValues()
            values.put(TaskTypeEntry.COLUMN_NAME, t.name)
            values.put(TaskTypeEntry.COLUMN_DESCRIPTION, t.description)
            db.insert(TaskTypeEntry.TABLE_NAME, null, values)
        }
        close()
    }

    private fun writeItemTypes(itemTypes: List<PlantTypeObject>) {
        val db = writableDatabase
        for (p in itemTypes) {
            val values = ContentValues()
            values.put(ItemTypeEntry.COLUMN_NAME, p.name)
            values.put(ItemTypeEntry.COLUMN_ICON, p.icon)
            values.put(ItemTypeEntry.COLUMN_COLOR, p.color)
            db.insert(ItemTypeEntry.TABLE_NAME, null, values)
        }
        close()
    }

    private fun addImage(image: Bitmap?, name: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("Name", name)
        values.put("Image", getBitmapAsByteArray(image))
        db.insert("Images", null, values)
        close()
    }

    private fun writeTasks(tasks: List<TaskObject>) {
        val db = writableDatabase
        for (t in tasks) {
            val values = ContentValues()
            values.put(TaskEntry.COLUMN_LAND, t.land)
            values.put(TaskEntry.COLUMN_PLANT_INDEX, t.plantIndex)
            values.put(TaskEntry.COLUMN_TASK_TYPE, t.taskType)
            values.put(TaskEntry.COLUMN_PRIORITY, t.priority)
            values.put(TaskEntry.COLUMN_CREATION_DATE, t.creationDate.time)
            if (t.targetDate != null) values.put(TaskEntry.COLUMN_EXPIRATION_DATE, t.targetDate!!.time)
            values.put(TaskEntry.COLUMN_COMPLETED, t.completed)
            values.put(TaskEntry.COLUMN_OBSERVATIONS, t.observations)
            db.insert(TaskEntry.TABLE_NAME, null, values)
        }
        close()
    }

    companion object {
        private const val DATABASE_VERSION = 4
        const val LAND_TABLE_NAME = "export.db"
        private fun getBitmapAsByteArray(bitmap: Bitmap?): ByteArray {
            val outputStream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 0, outputStream)
            return outputStream.toByteArray()
        }
    }
}