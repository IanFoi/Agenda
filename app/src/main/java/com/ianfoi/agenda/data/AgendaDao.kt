package com.ianfoi.agenda.data

import androidx.compose.runtime.snapshots.SnapshotId
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ianfoi.agenda.model.Categoria
import com.ianfoi.agenda.model.Registro
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz AgendaDao
 * Dao para interpretar los elementos de la base de datos como objetos.
 */
@Dao
interface AgendaDao {
    /**
     * Funcion insertarCategoria que recibe un objeto de tipo Categoria para insertarlo en una base de datos
     * Funcion suspend para manejar la concurrencia de la base de datos.
     * @param Categoria categoria La categoria que se guardará en la base de datos.
     */
    @Insert
suspend fun insertarCategoria(categoria: Categoria)

    /**
     * Funcion getCategorias para obtener las categorias registradas en la base de datos
     *
     * @return Lista de categorias como un flujo de datos continuo.
     * La lista de categorias se obtiene de una tabla de categorias ordenada conforme se fue creando.
     */
    @Query("SELECT * FROM tabla_categorias ORDER BY orden ASC")
fun getCategorias() : Flow<List<Categoria>>

    /**
     * Funcion contarPorMes que obtiene de una tabla de registros mediante la id
     * la cantidad de apariciones de una categoria a lo largo de un periodo mensual.
     * @param catId la id de la categoria a buscar.
     * @param inicioMes inicio del intervalo mensual.
     * @param finMes el fin del intervalo mensual.
     * @return Un Flow de tipo entero correspondiente a las apariciones de la categoria en el intervalo.
     */
    @Query("SELECT COUNT(*) FROM tabla_registros WHERE categoriaId = :catId AND fecha >= :inicioMes AND fecha <= :finMes")
fun contarPorMes(catId: Int, inicioMes: Long, finMes: Long): Flow<Int>

    /**
     * Funcion estaMarcado que devuelve si la casilla correspondiente a una fecha y una categoria ha sido marcada.
     * @param catId La id de la categoria consultada
     * @param dia el dia a consultar.
     * @return <True> en el caso que la categoria esté marcada en la tabla con el dia idicada
     *         false en otro caso
     */
    @Query("SELECT EXISTS(SELECT * FROM tabla_registros WHERE categoriaId = :catId AND fecha = :dia)")
fun estaMarcado(catId: Int, dia: Long): Flow<Boolean>

    /**
     * Funciones para manejar conflictos.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun  marcar(registro: Registro)

    /**
     * Funcion para eliminar de la base de datos si una celda está marcada.
     * @param registro el registro correspondiente a la celda eliminada.
     */
    @Delete
suspend fun desmarcar(registro: Registro)

    /**
     * Función para obtener todas las celdas marcadas del un mes dada una categoria.
     */
    @Query("SELECT fecha FROM tabla_registros WHERE :categoriaId = categoriaId AND fecha BETWEEN :inicio AND :fin")
    fun obtenerFechasMarcadas(categoriaId: Int, inicio:Long, fin: Long): Flow<List<Long>>

}