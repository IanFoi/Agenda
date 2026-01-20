package com.ianfoi.agenda.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.model.Categoria
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaMensualScreen(
    dao: AgendaDao,
    mesIndex: Int,
    onBack: () -> Unit
) {
    val viewModel: AgendaViewModel = viewModel(
        factory = AgendaViewModelFactory(dao)
    )
    val categorias by dao.getCategorias().collectAsState(initial = emptyList())
    val nombreMes = meses.getOrElse(mesIndex) { "MES" }
    val anioActual = remember { LocalDate.now().year }
    val diasEnMes =  remember(mesIndex, anioActual) {
        YearMonth.of(anioActual, mesIndex + 1).lengthOfMonth()
    }
    val sharedScrollState = rememberScrollState()

    Scaffold(
        containerColor = Color(0xFFF5F5F5), // 1. Fondo gris suave
        topBar = {
            TopAppBar(
                title = { }, // Dejamos vacío aquí para personalizar el título abajo o usarlo simple
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
                .padding(horizontal = 16.dp)
        ) {

            Text(
                text = "$nombreMes $anioActual",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Espacio alineado con las categorías (100dp ancho + 12dp padding tarjeta)
                Spacer(modifier = Modifier.width(112.dp))

                // Scroll Horizontal de los números
                Row(modifier = Modifier.horizontalScroll(sharedScrollState)) {
                    (1..diasEnMes).forEach { dia ->
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 30.dp) // Un poco más ancho que alto
                                .padding(2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.DarkGray), // Estilo oscuro igual a la vista anual
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "$dia", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- CUERPO DE LA TABLA ---
            LazyColumn(
                modifier = Modifier.weight(1f), // Permite scroll vertical si hay muchas categorías
                verticalArrangement = Arrangement.spacedBy(8.dp), // Espacio entre tarjetas
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(categorias) { categoria ->
                    FilaMensual(
                        categoria = categoria,
                        mesIndex = mesIndex,
                        anio = anioActual,
                        diasDelMes = diasEnMes,
                        viewModel = viewModel,
                        scrollState = sharedScrollState,

                        )
                }
            }
        }
    }
}

// En VistaMensualScreen.kt

@Composable
fun FilaMensual(
    categoria: Categoria,
    mesIndex: Int,
    anio: Int,
    diasDelMes: Int,
    viewModel: AgendaViewModel,
    scrollState: ScrollState
) {

    val diasMarcados by viewModel.obtenerMarcasDelMes(categoria.id, mesIndex, anio)
        .collectAsState(initial = emptySet())

    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ... (Tu código de Surface/Nombre se queda igual) ...
            Surface(
                color = Color(categoria.color),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.width(100.dp).heightIn(min = 40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = categoria.nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // ... (Scroll de días) ...
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                (1..diasDelMes).forEach { dia ->
                    val fechaId = (anio * 10000 + mesIndex * 100 + dia).toLong()

                    // 2. BÚSQUEDA INSTANTÁNEA EN MEMORIA (Sin ir a la BD)
                    val estaMarcado = diasMarcados.contains(fechaId)

                    val colorFondo = if (estaMarcado) Color(categoria.color) else Color(0xFFEEEEEE)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorFondo)
                            .clickable {
                                viewModel.alternarMarca(categoria.id, fechaId, estaMarcado)
                            },
                        contentAlignment = Alignment.Center,
                    ){
                    }
                }
            }
        }
    }
}