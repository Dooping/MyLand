package com.gago.david.myland.models

import android.provider.BaseColumns

object LandContract {
    // Table contents are grouped together in an anonymous object.
    object LandEntry : BaseColumns {
        const val TABLE_NAME = "Lands"
        const val COLUMN_NAME = "Name"
        const val COLUMN_IMAGE = "ImageUri"
        const val COLUMN_DESCRIPTION = "Description"
        const val COLUMN_AREA = "Area"
        const val COLUMN_USER = "User"
        const val COLUMN_BEARING = "bearing"
        const val COLUMN_POLYGON = "polygon"
        const val COLUMN_CENTER_LAT = "center_lat"
        const val COLUMN_CENTER_LON = "center_lon"
        const val COLUMN_ZOOM = "zoom"
    }

    object ItemEntry : BaseColumns {
        const val TABLE_NAME = "Plants"
        const val COLUMN_ID = "Id"
        const val COLUMN_LAND = "Land"
        const val COLUMN_USER = "User"
        const val COLUMN_PLANT_TYPE = "PlantType"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_X = "x"
        const val COLUMN_Y = "y"
    }

    object TaskTypeEntry : BaseColumns {
        const val TABLE_NAME = "TaskTypes"
        const val COLUMN_NAME = "Name"
        const val COLUMN_DESCRIPTION = "Description"
    }

    object ItemTypeEntry : BaseColumns {
        const val TABLE_NAME = "PlantTypes"
        const val COLUMN_NAME = "Name"
        const val COLUMN_ICON = "Icon"
        const val COLUMN_COLOR = "Color"
    }

    object TaskEntry : BaseColumns {
        const val TABLE_NAME = "Tasks"
        const val COLUMN_LAND = "Land"
        const val COLUMN_USER = "User"
        const val COLUMN_PLANT_INDEX = "PlantIndex"
        const val COLUMN_TASK_TYPE = "TaskType"
        const val COLUMN_PRIORITY = "Priority"
        const val COLUMN_CREATION_DATE = "CreationDate"
        const val COLUMN_EXPIRATION_DATE = "ExpirationDate"
        const val COLUMN_COMPLETED = "Completed"
        const val COLUMN_OBSERVATIONS = "Observations"
    }
}