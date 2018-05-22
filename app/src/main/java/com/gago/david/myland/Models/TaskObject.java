package com.gago.david.myland.Models;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class TaskObject implements Serializable {
    public long rowid;
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

    public TaskObject(long rowid, String land, Integer plantIndex, String taskType, Integer priority, Date creationDate, Date targetDate, boolean completed, String observations) {
        this.rowid = rowid;
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
                "rowid=" + rowid +
                ", land='" + land + '\'' +
                ", plantIndex=" + plantIndex +
                ", taskType='" + taskType + '\'' +
                ", priority=" + priority +
                ", creationDate=" + creationDate +
                ", targetDate=" + targetDate +
                ", completed=" + completed +
                ", observations='" + observations + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskObject that = (TaskObject) o;
        return rowid == that.rowid &&
                completed == that.completed &&
                Objects.equals(land, that.land) &&
                Objects.equals(plantIndex, that.plantIndex) &&
                Objects.equals(taskType, that.taskType) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(creationDate, that.creationDate) &&
                Objects.equals(targetDate, that.targetDate) &&
                Objects.equals(observations, that.observations);
    }

    @Override
    public int hashCode() {

        return Objects.hash(rowid, land, plantIndex, taskType, priority, creationDate, targetDate, completed, observations);
    }

    public TaskObject clone() {
        return new TaskObject(rowid, land, plantIndex, taskType, priority, creationDate, targetDate, completed, observations);
    }
}
