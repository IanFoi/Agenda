package com.ianfoi.agenda.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class correspondiente a la creacion de la tabla de categorias usando Room.
 * Una categoria consta de una Id única, inicialmente 0: una cadena correspondiente al nombre,
 * un entero hexadecimal correspondiente a su color y un peso para ordenarlas.
 */
@Entity(tableName ="tabla_objetivos")
data class Objetivo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String ,
    val descripcion: String = "",
    val color: Long = 0,
    val isCompletado: Boolean = false
)
