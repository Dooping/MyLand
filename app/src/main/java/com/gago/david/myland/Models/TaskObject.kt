package com.gago.david.myland.models

import java.io.Serializable
import java.util.*

class TaskObject : Serializable, Cloneable {
    @JvmField
    var rowid: Long = 0
    @JvmField
    var land: String
    @JvmField
    var plantIndex: Int?
    @JvmField
    var taskType: String
    @JvmField
    var priority: Int
    @JvmField
    var creationDate: Date
    @JvmField
    var targetDate: Date?
    @JvmField
    var completed: Boolean
    @JvmField
    var observations: String

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

    constructor(rowid: Long, land: String, plantIndex: Int?, taskType: String, priority: Int, creationDate: Date, targetDate: Date?, completed: Boolean, observations: String) {
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
                observations == that.observations
    }

    override fun hashCode(): Int {
        return Objects.hash(rowid, land, plantIndex, taskType, priority, creationDate, targetDate, completed, observations)
    }

    public override fun clone(): TaskObject {
        return TaskObject(rowid, land, plantIndex, taskType, priority, creationDate, targetDate, completed, observations)
    }
}