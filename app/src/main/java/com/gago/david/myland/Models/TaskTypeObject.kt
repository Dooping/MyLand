package com.gago.david.myland.models

import java.io.Serializable

class TaskTypeObject(var name: String, var description: String) : Serializable {
    override fun toString(): String {
        return "TaskTypeObject{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}'
    }
}