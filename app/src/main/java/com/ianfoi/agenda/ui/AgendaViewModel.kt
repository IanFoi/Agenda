package com.ianfoi.agenda.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.model.Registro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AgendaViewModel(private val dao: AgendaDao) : ViewModel() {

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