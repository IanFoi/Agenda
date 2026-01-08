package com.ianfoi.agenda

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ejemplo.miagenda.data.AgendaDatabase
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.ui.VistaAnualScreen
import com.ianfoi.agenda.ui.VistaMensualScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. INICIALIZAR LA BASE DE DATOS
        // Usamos la función 'getDatabase' que creamos en el Singleton para no abrirla dos veces.
        val database = AgendaDatabase.getDatabase(applicationContext)
        val dao = database.agendaDao()

        setContent {
            // 2. INICIAR LA NAVEGACIÓN
            // Le pasamos el 'dao' a la navegación para que se lo reparta a las pantallas.
            AppNavigation(dao = dao)
        }
    }
}

@Composable
fun AppNavigation(dao: AgendaDao) {
    // El controlador que gestiona el "Historial" de pantallas (para poder volver atrás)
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "anual") {

        // --- PANTALLA 1: VISTA ANUAL (Dashboard) ---
        composable("anual") {
            VistaAnualScreen(
                dao = dao,
                alHacerClickEnMes = { mesIndex ->
                    // Navegamos a la ruta "mes/0", "mes/1", etc.
                    navController.navigate("mes/$mesIndex")
                }
            )
        }

        // --- PANTALLA 2: VISTA MENSUAL (Detalle) ---
        composable(
            route = "mes/{mesIndex}",
            arguments = listOf(navArgument("mesIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            // Recuperamos el número de mes que viene en la URL
            val mesIndex = backStackEntry.arguments?.getInt("mesIndex") ?: 0

            VistaMensualScreen(
                dao = dao,
                mesIndex = mesIndex,
                onBack = { navController.popBackStack() } // Acción para el botón atrás
            )
        }
    }
}