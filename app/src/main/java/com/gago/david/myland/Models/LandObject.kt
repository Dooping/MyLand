package com.gago.david.myland.models

import java.util.*

class LandObject {
    @JvmField
    var name: String
    @JvmField
    var imageUri: String
    @JvmField
    var Description: String
    @JvmField
    var area: Double
    var notifications = 0
    var priority = 0
    @JvmField
    var plants: ArrayList<PlantObject>

    constructor(name: String, imageUri: String, description: String, area: Double) {
        this.name = name
        this.imageUri = imageUri
        Description = description
        this.area = area
        plants = ArrayList()
    }

    constructor(name: String, imageUri: String, description: String, notifications: Int, priority: Int, area: Double) {
        this.name = name
        this.imageUri = imageUri
        Description = description
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
                ", Description='" + Description + '\'' +
                ", notifications=" + notifications +
                ", priority=" + priority +
                ", plants=" + plants +
                '}'
    }
}