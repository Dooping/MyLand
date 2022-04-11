package com.gago.david.myland.models

import java.util.*

class LandObject {
    var name: String
    var imageUri: String
    var description: String
    var area: Double
    var notifications = 0
    var priority = 0
    var plants: ArrayList<PlantObject>
    var totalTasks: Int = 0
    var user: String
    var lat: Double = 0.0
    var lon: Double = 0.0
    var zoom: Double = 0.0
    var bearing: Double = 0.0
    var polygon: String?

    constructor(name: String, imageUri: String, description: String, area: Double, lat: Double, lon: Double, zoom: Double, bearing: Double, polygon: String) {
        this.name = name
        this.imageUri = imageUri
        this.description = description
        this.area = area
        plants = ArrayList()
        totalTasks = 0
        this.user = ""
        this.lat = lat
        this.lon = lon
        this.zoom = zoom
        this.bearing = bearing
        this.polygon = polygon
    }

    constructor(name: String, imageUri: String, description: String, user: String) {
        this.name = name
        this.imageUri = imageUri
        this.description = description
        this.area = 0.0
        this.user = user
        plants = ArrayList()
        totalTasks = 0
        polygon = null
    }

    constructor(name: String, imageUri: String, description: String, notifications: Int, priority: Int, area: Double, lat: Double, lon: Double, zoom: Double, bearing: Double, polygon: String) {
        this.name = name
        this.imageUri = imageUri
        this.description = description
        plants = ArrayList()
        this.notifications = notifications
        this.priority = priority
        this.area = area
        this.user = ""
        this.lat = lat
        this.lon = lon
        this.zoom = zoom
        this.bearing = bearing
        this.polygon = polygon
    }

    fun addPlant(plant: PlantObject) {
        plants.add(plant)
    }

    fun removePlant(plant: PlantObject?) {
        plants.remove(plant)
    }

    override fun toString(): String {
        return "LandObject(" +
                "name='$name', " +
                "imageUri='$imageUri', " +
                "description='$description', " +
                "area=$area, " +
                "notifications=$notifications, " +
                "priority=$priority, " +
                "plants=$plants, " +
                "totalTasks=$totalTasks, " +
                "user='$user', " +
                "lat=$lat, " +
                "lon=$lon, " +
                "zoom=$zoom, " +
                "bearing=$bearing" +
                ")"
    }

}