package com.gago.david.myland.Models;

import java.util.ArrayList;

public class LandObject {
    public String name;
    public String imageUri;
    public String Description;
    public ArrayList<PlantObject> plants;

    public LandObject(String name, String imageUri, String description) {
        this.name = name;
        this.imageUri = imageUri;
        Description = description;
        plants = new ArrayList<>();
    }

    public void addPlant(PlantObject plant){
        plants.add(plant);
    }

    public void removePlant(PlantObject plant){
        plants.remove(plant);
    }

    @Override
    public String toString() {
        return "LandObject{" +
                "name='" + name + '\'' +
                ", imageUri='" + imageUri + '\'' +
                ", Description='" + Description + '\'' +
                ", plants=" + plants +
                '}';
    }
}
