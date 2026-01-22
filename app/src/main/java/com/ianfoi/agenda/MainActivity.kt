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
import com.ianfoi.agenda.data.AgendaDatabase
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.ui.VistaAnualScreen
import com.ianfoi.agenda.ui.VistaListaObjetivoScreen
import com.ianfoi.agenda.ui.VistaMensualScreen
import com.ianfoi.agenda.ui.VistaObjetivoScreen
import com.ianfoi.agenda.ui.VistaPrincipalScreen // Asumo que cambiaste el nombre a PantallaInicio o VistaPrincipalScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    // CORRECCIÓN 1: Quitamos @Composable de aquí. onCreate es código normal de Android.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AgendaDatabase.getDatabase(applicationContext)
        val dao = database.agendaDao()

        setContent {
            AppNavigation(dao = dao)
        }
    }
}

@Composable
fun AppNavigation(dao: AgendaDao) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "principal") {

        // --- PANTALLA PRINCIPAL ---
        composable("principal") {
            VistaPrincipalScreen(
                dao = dao,
                alHacerClickEnResumen = {

                    navController.navigate("anual/${LocalDate.now().year}")
                },
                alHacerClickEnPendientes = {},
                alHacerClickEnObjetivos = { navController.navigate("listaObjetivos") }
            )
        }

        // --- PANTALLA 1: VISTA ANUAL (Detalle) ---
        composable(
            route = "anual/{anioSeleccionado}", // Definimos que recibimos un año
            arguments = listOf(navArgument("anioSeleccionado") { type = NavType.IntType })
        ) { backStackEntry ->

            // CORRECCIÓN 3: Recuperamos el año de los argumentos
            val anio = backStackEntry.arguments?.getInt("anioSeleccionado") ?: LocalDate.now().year

            VistaAnualScreen(
                dao = dao,
                alHacerClickEnMes = { mesIndex -> navController.navigate("mes/$mesIndex") },
                onBack = { navController.navigate("principal") }
            )
        }

        // --- PANTALLA 2: VISTA MENSUAL ---
        composable(
            route = "mes/{mesIndex}",
            arguments = listOf(navArgument("mesIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val mesIndex = backStackEntry.arguments?.getInt("mesIndex") ?: 0

            VistaMensualScreen(
                dao = dao,
                mesIndex = mesIndex,
                onBack = { navController.popBackStack() }
            )
        }
        // PANTALLA DE LISTA DE OBJETIVOS.
        composable(
            route = "listaObjetivos"
        ) {
            VistaListaObjetivoScreen(
                dao = dao,
                onBack = { navController.popBackStack() },
                onItemClick = { objetivoId ->
                    navController.navigate("objetivo/$objetivoId")
                }
            )
        }

        composable(
            route = "objetivo/{objetivoId}",
            arguments = listOf(
                navArgument("objetivoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("objetivoId") ?: -1
            VistaObjetivoScreen(
                dao = dao,
                objetivoId = id,
                onBack = { navController.popBackStack() }
            )
        }

        }
    }

