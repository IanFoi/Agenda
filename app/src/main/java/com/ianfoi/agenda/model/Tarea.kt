package com.ianfoi.agenda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabla_tareas",
    foreignKeys = [
        ForeignKey(
            entity = Objetivo::class,
            parentColumns = ["id"],
            childColumns = ["objetivoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Tarea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val objetivoId: Int,
    val nombre: String,
    val esCompletada: Boolean = false
)