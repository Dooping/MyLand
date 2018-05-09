package com.gago.david.myland;

public class TaskTypeObject {
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
