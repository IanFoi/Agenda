package com.ianfoi.agenda.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.model.Categoria
import com.ianfoi.agenda.model.Registro
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaMensualScreen(
    dao: AgendaDao,
    mesIndex: Int,
    onBack: () -> Unit
) {
    val categorias by dao.getCategorias().collectAsState(initial = emptyList())
    val nombreMes = meses.getOrElse(mesIndex) { "MES" }

    // El cerebro compartido del scroll horizontal
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
                text = "Detalle: $nombreMes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // --- CABECERA DE DÍAS (1..31) ---
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
                    (1..31).forEach { dia ->
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
                        dao = dao,
                        scrollState = sharedScrollState
                    )
                }
            }
        }
    }
}

@Composable
fun FilaMensual(
    categoria: Categoria,
    mesIndex: Int,
    dao: AgendaDao,
    scrollState: ScrollState
) {
    val scope = rememberCoroutineScope()

    // 3. TARJETA (CARD) PARA CADA FILA
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
            // 1. COLUMNA FIJA: NOMBRE (Estilo Píldora)
            Surface(
                color = Color(categoria.color),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(100.dp)
                    .heightIn(min = 40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = categoria.nombre,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 2. COLUMNA MÓVIL: CASILLAS
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                (1..31).forEach { dia ->
                    val fechaId = (2026 * 10000 + mesIndex * 100 + dia).toLong()
                    val estaMarcado by dao.estaMarcado(categoria.id, fechaId).collectAsState(initial = false)

                    // Estilos de la casilla
                    val colorFondo = if (estaMarcado) Color(categoria.color) else Color(0xFFEEEEEE)

                    Box(
                        modifier = Modifier
                            .size(40.dp) // Cuadradas
                            .padding(2.dp) // Separación ("aire")
                            .clip(RoundedCornerShape(6.dp)) // Bordes redondeados
                            .background(colorFondo)
                            .clickable {
                                scope.launch {
                                    val registro = Registro(categoria.id, fechaId)
                                    if (estaMarcado) dao.desmarcar(registro) else dao.marcar(registro)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {

                        // if (estaMarcado) Icon(Icons.Default.Check, tint = Color.White, ...)
                    }
                }
            }
        }
    }
}