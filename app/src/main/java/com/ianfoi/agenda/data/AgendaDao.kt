package com.ianfoi.agenda.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ianfoi.agenda.model.Categoria
import com.ianfoi.agenda.model.CategoriaTop
import com.ianfoi.agenda.model.Objetivo
import com.ianfoi.agenda.model.Registro
import com.ianfoi.agenda.model.Tarea
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
     * Función para obtener las celdas marcadas en un intervalo de tiempo dada una categoria.
     * @param categoriaId la id de la categoria a buscar.
     * @param inicio inicio del intervalo mensual.
     * @param fin el fin del intervalo mensual.
     * @return Un Flow de tipo lista de long correspondiente a las marcas de la categoria en el intervalo.
     */

    @Query("SELECT fecha FROM tabla_registros WHERE :categoriaId = categoriaId AND fecha BETWEEN :inicio AND :fin")
    fun obtenerFechasMarcadas(categoriaId: Int, inicio:Long, fin: Long): Flow<List<Long>>

    /**
     * Funcion que obtiene de la base de datos las categorias mas marcadas en un intervalo de tiempo.
     * @param inicio El inicio del intervalo consultado.
     * @param fin El fin del intervalo consultado.
     */
    @Query("""
    SELECT c.Id, c.nombre, c.color, COUNT(r.fecha) as puntaje 
    FROM tabla_categorias c 
    LEFT JOIN tabla_registros r ON c.id = r.categoriaId AND r.fecha BETWEEN :inicio AND :fin
    GROUP BY c.id 
    ORDER BY puntaje DESC 
    LIMIT 3
""")
    fun obtenerTop3Categorias(inicio: Long, fin: Long): Flow<List<CategoriaTop>>



    // --- SECCIÓN OBJETIVOS ---
    /**
     * Funcion que obtiene de la base de datos la lista de objetivos del usuario.
     * @return Flow List con los objetivos del usuario.
     */
    @Query("SELECT * FROM tabla_objetivos ORDER BY id DESC")
    fun obtenerObjetivos(): Flow<List<Objetivo>>

    /**
     * Funcion que recupera de la base de datos un objetivo a partir de su Id.
     * @param id La id del objetivo a buscar.
     */
    @Query("SELECT * FROM tabla_objetivos WHERE id = :id")
    fun obtenerObjetivoPorId(id: Int): Flow<Objetivo>

    /**
     * Funcion que inserta en la base de datos un objetivo.
     * @param objetivo el objetivo a insertar.
     */
    @Insert
    suspend fun insertarObjetivo(objetivo: Objetivo)

    /**
     * Funcion para actualizar el estado de un objetivo.
     * @param objetivo el objetivo a actualizar.
     */
    @Update
    suspend fun actualizarObjetivo(objetivo: Objetivo)

    /**
     * Funciom para eliminar un objetivo de la base de datos.
     * @param objetivo el objetivo a eliminar.
     */
    @Delete
    suspend fun eliminarObjetivo(objetivo: Objetivo)
// SECCION DE TAREAS.
    /**
     * Funcion para obtener las tareas relacionadas a un objetivo a partir de su id.
     * @param objetivo El objetivo del que recuperamos sus tareas.
     * @return FlowList con las tareas asociadas a un objetivo
     */
    @Query("SELECT * FROM tabla_tareas WHERE objetivoId = :objetivoId ORDER BY id ASC")
    fun obtenerTareasDeObjetivo(objetivoId: Int): Flow<List<Tarea>>

    /**
     * Funcion para agregar una tarea a la base de datos.
     * @param tarea la tarea a agregar.
     */
    @Insert
    suspend fun insertarTarea(tarea: Tarea)

    /**
     * Funcion para actualizar la informacion de una tarea.
     * @param tarea la tarea a actualizar.
     */
    @Update
    suspend fun actualizarTarea(tarea: Tarea)

    /**
     * Funcion para eliminar una tarea de la tabla de tareas de un objetivo.
     * @param tarea La tarea a eliminar.
     */
    @Delete
    suspend fun eliminarTarea(tarea: Tarea)
}