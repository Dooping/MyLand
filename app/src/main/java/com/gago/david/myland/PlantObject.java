package com.gago.david.myland;

public class PlantObject {
    public String plantType;
    public String description;
    public float x;
    public float y;

    public PlantObject(String plantType, String description, float x, float y) {
        this.plantType = plantType;
        this.description = description;
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "PlantObject{" +
                "plantType='" + plantType + '\'' +
                ", description='" + description + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
