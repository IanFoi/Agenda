package com.ejemplo.miagenda.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.model.Categoria
import com.ianfoi.agenda.model.Registro
@Database(
    entities = [Categoria::class, Registro::class],
    version = 1,
    exportSchema = false
)
/**
 * Clase abstracta correspondiente a una base de datos Room.
 */
abstract class AgendaDatabase : RoomDatabase() {

    abstract fun agendaDao(): AgendaDao

    // Implementacion del patron Singleton para abrir el archivo una sola vez.
    // Tenemos una unica instancia de la base de datos.
    companion object {
        @Volatile
        private var INSTANCE: AgendaDatabase? = null

        /**
         * Funcion para devolver la  unica instancia de la base de datos de la agenda
         * siguiendo el patrón Singleton.
         */
        fun getDatabase(context: Context): AgendaDatabase {
            return INSTANCE ?: synchronized(this) {
                // Si la instancia no existe, la creamos
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AgendaDatabase::class.java,
                    "agenda_database" //Nombre del archivo de la base de datos.
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}