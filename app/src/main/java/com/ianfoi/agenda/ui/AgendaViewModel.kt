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

    // Función optimizada: Devuelve un Flow con una LISTA de días marcados (ej: [20260101, 20260105])
    // La UI solo tendrá que verificar si el día actual está en esta lista.
    fun obtenerMarcasDelMes(categoriaId: Int, mesIndex: Int, anio: Int): Flow<Set<Long>> {
        val inicio = (anio * 10000 + mesIndex * 100 + 0).toLong()
        val fin = (anio * 10000 + mesIndex * 100 + 32).toLong() // 32 para cubrir hasta el 31

        return dao.obtenerFechasMarcadas(categoriaId, inicio, fin)
            .map { it.toSet() } // Convertimos a Set para que la búsqueda sea instantánea
    }

    // Acciones de usuario
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

class AgendaViewModelFactory(private val dao: AgendaDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AgendaViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}