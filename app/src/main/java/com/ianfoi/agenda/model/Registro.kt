package com.ianfoi.agenda.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Data class para la creacion de una tabla de registros que relaciona una categoria mediante su
 * id con la fecha marcada en el registro con la regla de que no se pueden tener dos registros iguales.
 * Cuando se elimina una categoria, se elimina todos los registros asociados a ella.
 * Un registro consiste de un entero correspondiente a la id de la categoria y la fecha con la
 * que se relaciona.
 */
@Entity(
    tableName = "tabla_registros",
    primaryKeys = ["categoriaId", "fecha"],
    foreignKeys = [
        ForeignKey(
            entity = Categoria :: class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )

    ],
    indices = [Index(value = ["categoriaId"])]
)
data class Registro(
    val categoriaId : Int,
    val fecha: Long
)
