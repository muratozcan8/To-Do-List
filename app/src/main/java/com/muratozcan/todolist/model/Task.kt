package com.muratozcan.todolist.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Task(

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "notes")
    val notes: String,

    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean,

    @ColumnInfo(name = "dueDate")
    val dueDate: String,

    ) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id = 0

}