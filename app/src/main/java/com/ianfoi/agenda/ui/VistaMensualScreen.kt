package com.ianfoi.agenda.ui

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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

    val configuration = LocalConfiguration.current
    val esHorizontal = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Proporciones que dependen de la configuracion de la pantalla.
            val (anchoNombre, anchoDia) = if (esHorizontal) {
                val anchoPantalla = maxWidth
                Pair(anchoPantalla * 0.2f, anchoPantalla * 0.065f)
            } else {
                val anchoPantalla = maxWidth
                Pair(anchoPantalla * 0.25f, 45.dp)

            }

            Column(modifier = Modifier.fillMaxSize()) {

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

                // --- CABECERA DE DÍAS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Spacer del Nombre
                    Spacer(modifier = Modifier.width(anchoNombre))

                    //  Spacer interno (hueco entre nombre y días)
                    Spacer(modifier = Modifier.width(4.dp))

                    //  Fila de días
                    Row(modifier = Modifier.horizontalScroll(sharedScrollState)) {
                        (1..diasEnMes).forEach { dia ->
                            Box(
                                modifier = Modifier
                                    .width(anchoDia)
                                    .height(30.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.DarkGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "$dia", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                            anchoNombre = anchoNombre,
                            anchoDia = anchoDia
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilaMensual(
    categoria: Categoria,
    mesIndex: Int,
    anio: Int,
    diasDelMes: Int,
    viewModel: AgendaViewModel,
    scrollState: ScrollState,
    anchoNombre: Dp,
    anchoDia: Dp
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
            Surface(
                color = Color(categoria.color),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(anchoNombre)
                    .heightIn(min = 40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = categoria.nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // COLUMNA DIAS
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                (1..diasDelMes).forEach { dia ->
                    val fechaId = (anio * 10000 + mesIndex * 100 + dia).toLong()
                    val estaMarcado = diasMarcados.contains(fechaId)
                    val colorFondo = if (estaMarcado) Color(categoria.color) else Color(0xFFEEEEEE)

                    Box(
                        modifier = Modifier
                            .width(anchoDia) // <--- Ancho calculado para la celda
                            .height(40.dp)   // Alto fijo (cuadrado si anchoDia es ~40)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorFondo)
                            .clickable {
                                viewModel.alternarMarca(categoria.id, fechaId, estaMarcado)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                    }
                }
            }
        }
    }
}