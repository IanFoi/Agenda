package com.ianfoi.agenda.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.viewmodel.AgendaViewModel
import com.ianfoi.agenda.viewmodel.AgendaViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaObjetivoScreen(
    dao: AgendaDao,
    objetivoId: Int,
    onBack: () -> Unit
) {
    val viewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(dao))
    val objetivo by viewModel.obtenerObjetivo(objetivoId).collectAsState(initial = null)

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text("${objetivo?.nombre?.uppercase()}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            // Verificamos si la base de datos ya respondió (puede tardar milisegundos)
            objetivo?.let { obj ->

                // TARJETA PRINCIPAL
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Objetivo #${obj.id}",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Aquí se pinta el texto dinámico
                        Text(
                            text = obj.nombre,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ESTADO DEL OBJETIVO
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Estado actual:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Chip o Etiqueta de estado
                    Surface(
                        color = if (obj.isCompletado) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = if (obj.isCompletado) "¡COMPLETADO! " else "EN PROCESO ",
                            color = if (obj.isCompletado) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

            } ?: run {
                // Muestra esto mientras carga los datos
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}