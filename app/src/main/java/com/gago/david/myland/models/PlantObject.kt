package com.gago.david.myland.models

class PlantObject{
    var id: Int
    var plantType: String
    var description: String
    var x: Float
    var y: Float

    constructor(plantType: String, description: String, x: Float, y: Float) {
        this.plantType = plantType
        this.description = description
        this.x = x
        this.y = y
        id = -1
    }

    constructor(id: Int, plantType: String, description: String, x: Float, y: Float) {
        this.id = id
        this.plantType = plantType
        this.description = description
        this.x = x
        this.y = y
    }



    override fun toString(): String {
        return "PlantObject{" +
                "id=" + id +
                ", plantType='" + plantType + '\'' +
                ", description='" + description + '\'' +
                ", x=" + x +
                ", y=" + y +
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
        result = 31 * result + x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}