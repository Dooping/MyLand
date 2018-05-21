package com.gago.david.myland.Models;

import java.util.ArrayList;

public class LandObject {
    public String name;
    public String imageUri;
    public String Description;
    public int notifications;
    public int priority;
    public ArrayList<PlantObject> plants;

    public LandObject(String name, String imageUri, String description) {
        this.name = name;
        this.imageUri = imageUri;
        Description = description;
        plants = new ArrayList<>();
    }

    public LandObject(String name, String imageUri, String description, int notifications, int priority) {
        this.name = name;
        this.imageUri = imageUri;
        Description = description;
        plants = new ArrayList<>();
        this.notifications = notifications;
        this.priority = priority;
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
                ", notifications=" + notifications +
                ", priority=" + priority +
                ", plants=" + plants +
                '}';
    }
}
