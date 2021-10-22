package com.gago.david.myland.models

import java.io.Serializable
import java.util.*

class TaskObject : Serializable, Cloneable {
    var rowid: Long = 0
    var land: String
    var plantIndex: Int?
    var taskType: String
    var priority: Int
    var creationDate: Date
    var targetDate: Date?
    var completed: Boolean
    var observations: String
    var completedDate: Date? = null

    constructor(land: String, plantIndex: Int?, taskType: String, priority: Int, creationDate: Date, targetDate: Date?, completed: Boolean, observations: String) {
        this.land = land
        this.plantIndex = plantIndex
        this.taskType = taskType
        this.priority = priority
        this.creationDate = creationDate
        this.targetDate = targetDate
        this.completed = completed
        this.observations = observations
    }

    constructor(
        rowid: Long,
        land: String,
        plantIndex: Int?,
        taskType: String,
        priority: Int,
        creationDate: Date,
        targetDate: Date?,
        completed: Boolean,
        observations: String
    ) {
        this.rowid = rowid
        this.land = land
        this.plantIndex = plantIndex
        this.taskType = taskType
        this.priority = priority
        this.creationDate = creationDate
        this.targetDate = targetDate
        this.completed = completed
        this.observations = observations
    }

    constructor(
        rowid: Long,
        land: String,
        plantIndex: Int?,
        taskType: String,
        priority: Int,
        creationDate: Date,
        targetDate: Date?,
        completed: Boolean,
        observations: String,
        completedDate: Date?
    ) {
        this.rowid = rowid
        this.land = land
        this.plantIndex = plantIndex
        this.taskType = taskType
        this.priority = priority
        this.creationDate = creationDate
        this.targetDate = targetDate
        this.completed = completed
        this.observations = observations
        this.completedDate = completedDate
    }

    override fun toString(): String {
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
                ", completedDate='" + completedDate + '\'' +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as TaskObject
        return rowid == that.rowid && completed == that.completed &&
                land == that.land &&
                plantIndex == that.plantIndex &&
                taskType == that.taskType &&
                priority == that.priority &&
                creationDate == that.creationDate &&
                targetDate == that.targetDate &&
                observations == that.observations &&
                completedDate == that.completedDate
    }

    override fun hashCode(): Int {
        return Objects.hash(rowid, land, plantIndex, taskType, priority, creationDate, targetDate, completed, observations, completedDate)
    }

    public override fun clone(): TaskObject {
        return TaskObject(rowid, land, plantIndex, taskType, priority, creationDate, targetDate, completed, observations, completedDate)
    }
}