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

    constructor(name: String, imageUri: String, description: String, area: Double) {
        this.name = name
        this.imageUri = imageUri
        this.description = description
        this.area = area
        plants = ArrayList()
        totalTasks = 0
    }

    constructor(name: String, imageUri: String, description: String, notifications: Int, priority: Int, area: Double) {
        this.name = name
        this.imageUri = imageUri
        this.description = description
        plants = ArrayList()
        this.notifications = notifications
        this.priority = priority
        this.area = area
    }

    fun addPlant(plant: PlantObject) {
        plants.add(plant)
    }

    fun removePlant(plant: PlantObject?) {
        plants.remove(plant)
    }

    override fun toString(): String {
        return "LandObject{" +
                "name='" + name + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", Description='" + description + '\'' +
                ", notifications=" + notifications + '\'' +
                ", totalTasks=" + totalTasks + '\'' +
                ", priority=" + priority + '\'' +
                ", plants=" + plants + '\'' +
                '}'
    }
}