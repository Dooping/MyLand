package com.gago.david.myland.models

import java.io.Serializable

class PlantTypeObject(var name: String, var icon: Int, var color: String) : Serializable {
    override fun toString(): String {
        return "PlantTypeObject{" +
                "name='" + name + '\'' +
                ", icon=" + icon +
                ", color='" + color + '\'' +
                '}'
    }
}