package com.gago.david.myland.models;

import java.io.Serializable;

public class TaskTypeObject implements Serializable {
    public String name;
    public String description;

    public TaskTypeObject(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "TaskTypeObject{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
