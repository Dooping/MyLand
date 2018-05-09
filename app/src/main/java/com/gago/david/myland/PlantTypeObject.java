package com.gago.david.myland;

public class PlantTypeObject {
    public String name;
    public int icon;
    public String color;

    public PlantTypeObject(String name, int icon, String color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    @Override
    public String toString() {
        return "PlantTypeObject{" +
                "name='" + name + '\'' +
                ", icon=" + icon +
                ", color='" + color + '\'' +
                '}';
    }
}
