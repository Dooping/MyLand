package com.gago.david.myland.models

class PlantObject {
    @JvmField
    var id: Int
    @JvmField
    var plantType: String
    @JvmField
    var description: String
    @JvmField
    var x: Float
    @JvmField
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
}