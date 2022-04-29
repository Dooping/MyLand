package com.gago.david.myland.models

class PlantObject{
    var id: Int
    var plantType: String
    var description: String
    var lat: Double = 0.0
    var lon: Double = 0.0

    constructor(plantType: String, description: String, lat: Double, lon: Double) {
        this.plantType = plantType
        this.description = description
        id = -1
        this.lat = lat
        this.lon = lon
    }

    constructor(id: Int, plantType: String, description: String, lat: Double, lon: Double) {
        this.id = id
        this.plantType = plantType
        this.description = description
        this.lat = lat
        this.lon = lon
    }



    override fun toString(): String {
        return "PlantObject{" +
                "id=" + id +
                ", plantType='" + plantType + '\'' +
                ", description='" + description + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlantObject

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + plantType.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lon.hashCode()
        return result
    }
}