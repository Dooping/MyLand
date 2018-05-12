package com.gago.david.myland.Models;

public class PlantObject {
    public int id;
    public String plantType;
    public String description;
    public float x;
    public float y;

    public PlantObject(String plantType, String description, float x, float y) {
        this.plantType = plantType;
        this.description = description;
        this.x = x;
        this.y = y;
        id = -1;
    }

    public PlantObject(int id, String plantType, String description, float x, float y) {
        this.id = id;
        this.plantType = plantType;
        this.description = description;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "PlantObject{" +
                "id=" + id +
                ", plantType='" + plantType + '\'' +
                ", description='" + description + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
