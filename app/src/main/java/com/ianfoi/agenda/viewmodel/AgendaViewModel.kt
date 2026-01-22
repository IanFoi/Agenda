package com.ianfoi.agenda.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.model.CategoriaTop
import com.ianfoi.agenda.model.Objetivo
import com.ianfoi.agenda.model.Registro
import com.ianfoi.agenda.model.Tarea
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AgendaViewModel(private val dao: AgendaDao) : ViewModel() {
    val listaDeObjetivos: Flow<List<Objetivo>> = dao.obtenerObjetivos()
    /**
     * Función para obtener las marcas correspondientes a una categoria en un mes.
     * @param categoriaId Categoria consultada.
     * @param mesIndex Indice del mes consultado
     * @param anio Entero correspondiente al año consultado
     * @return Un conjunto de identificaciones correspondientes a las marcas del mes.
     */
    fun obtenerMarcasDelMes(categoriaId: Int, mesIndex: Int, anio: Int): Flow<Set<Long>> {
        val inicio = (anio * 10000 + mesIndex * 100 + 0).toLong()
        val fin = (anio * 10000 + mesIndex * 100 + 32).toLong() // 32 para cubrir hasta el 31

        return dao.obtenerFechasMarcadas(categoriaId, inicio, fin)
            .map { it.toSet() }
    }

    /**
     * Funcion para alternar el estado de marcado de la casilla en la base de datos.
     * @param categoriaId La categoria marcada o desmarcada.
     * @param fechaId La identificacion de la fecha en la que está marcada o desmarcada la categoria.
     * @param estaMarcadoActualmente Booleano correspondiente a la marca en la aplicación.
     */
    fun alternarMarca(categoriaId: Int, fechaId: Long, estaMarcadoActualmente: Boolean) {
        viewModelScope.launch {
            val registro = Registro(categoriaId, fechaId)
            if (estaMarcadoActualmente) {
                dao.desmarcar(registro)
            } else {
                dao.marcar(registro)
            }
        }
    }
    /**
     * Funcion para obtener las 3 categorias mas marcadas en un año.
     * @param anio El año del que se quiere consultar.
     * @return Una FlowList de CategoriaTop con hasta 3 categorias mas seleccionadas.
     */
    fun obtenerTop3(anio: Int): Flow<List<CategoriaTop>> {
        val inicio = (anio * 10000).toLong()
        val fin = (anio * 10000 + 1300).toLong()
        return dao.obtenerTop3Categorias(inicio, fin)
    }

    /**
     * Función para agregar un objetivo a la base de datos.
     * @param nombre El nombre del objetivo.
     */
    fun agregarObjetivo(nombre: String){
        viewModelScope.launch {
            dao.insertarObjetivo(Objetivo(nombre = nombre))
        }
    }

    /**
     * Funcion para cambiar el estado del objetivo de completado a no completado y viceversa.
     * @param objetivo El objetivo al que se le cambia el estado.
     */
    fun toggleObjetivo(objetivo: Objetivo){
        viewModelScope.launch {
            dao.actualizarObjetivo(objetivo.copy(isCompletado = !objetivo.isCompletado))
        }
    }

    /**
     * Funcion para eliminar un objetivo de la base de datos.
     * @param objetivo el objetivo que se quiere eliminar.
     */
    fun borrarObjetivo(objetivo: Objetivo){
        viewModelScope.launch {
            dao.eliminarObjetivo(objetivo)
        }
    }

    /**
     * Funcion para obtener un objetivo de la base de datos a partir de su Id.
     * @param id La id del objetivo buscado.
     */
    fun obtenerObjetivo(id: Int): Flow<Objetivo> {
        return dao.obtenerObjetivoPorId(id)
    }
    /**
     * Funcion para obtener la lista de tareas relacionadas a un objetivo.
     * @param objetivoId la Id del objetivo padre.
     * @return FlowList con las tareas asociadas al objetivo.
     */
    fun obtenerTareas(objetivoId: Int): Flow<List<Tarea>> {
        return dao.obtenerTareasDeObjetivo(objetivoId)
    }
    /**
     * Funcion para agregar una tarea asociada a un objetivo.
     * @param objetivoId la id del objetivo padre de la tarea.
     * @param nombre El nombre de la tarea.
     */
    fun agregarTarea(objetivoId: Int, nombre: String){
        viewModelScope.launch {
            val nuevaTarea = Tarea(objetivoId = objetivoId, nombre = nombre)
            dao.insertarTarea(nuevaTarea)
        }
    }
    /**
     * Funcion para cambiar el estado de la tarea.
     * @param tarea la tarea a la que se cambia su estado
     */
    fun toggleTarea(tarea: Tarea){
        viewModelScope.launch {
            dao.actualizarTarea(tarea.copy(esCompletada = !tarea.esCompletada))
        }
    }
    /**
     * Funcion para eliminar una tarea del objetivo.
     * @param tarea La tarea a eliminar
     */
    fun eliminarTarea(tarea: Tarea){
        viewModelScope.launch {
            dao.eliminarTarea(tarea)
        }
    }
}

/**
 * Clase fabrica para crear instancias de AgendaViewModel.
 * La clase fabrica instancias de AgendaViewModel con la base de datos necesaria para el registro.
 */
class AgendaViewModelFactory(private val dao: AgendaDao) : ViewModelProvider.Factory {
    /**
     * Crea una nueva instancia de la clase de ViewModel solicitada.
     *
     * @param modelClass La clase del ViewModel que se quiere crear.
     * @return Una instancia de AgendaViewModelcon el DAO ya inyectado.
     * @throws IllegalArgumentException Si se intenta crear una clase de ViewModel desconocida.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendaViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }



}