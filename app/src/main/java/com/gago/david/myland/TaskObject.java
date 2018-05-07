package com.gago.david.myland;

import java.util.Date;

public class TaskObject {
    public String land;
    public Integer plantIndex;
    public String taskType;
    public Integer priority;
    public Date creationDate;
    public Date targetDate;
    public boolean completed;
    public String observations;

    public TaskObject(String land, Integer plantIndex, String taskType, Integer priority, Date creationDate, Date targetDate, boolean completed, String observations) {
        this.land = land;
        this.plantIndex = plantIndex;
        this.taskType = taskType;
        this.priority = priority;
        this.creationDate = creationDate;
        this.targetDate = targetDate;
        this.completed = completed;
        this.observations = observations;
    }



    @Override
    public String toString() {
        return "TaskObject{" +
                "land='" + land + '\'' +
                ", plantIndex=" + plantIndex +
                ", taskType='" + taskType + '\'' +
                ", priority='" + priority + '\'' +
                ", creationDate=" + creationDate +
                ", targetDate=" + targetDate +
                ", completed=" + completed +
                ", observations='" + observations + '\'' +
                '}';
    }
}
