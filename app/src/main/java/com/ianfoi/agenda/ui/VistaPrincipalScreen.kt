package com.ianfoi.agenda.ui

import android.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ianfoi.agenda.data.AgendaDao
import java.time.LocalDate
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ianfoi.agenda.model.CategoriaTop
import com.ianfoi.agenda.viewmodel.AgendaViewModel
import com.ianfoi.agenda.viewmodel.AgendaViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaPrincipalScreen(
    dao: AgendaDao,
    alHacerClickEnResumen: (Int) -> Unit,
    alHacerClickEnPendientes: () -> Unit,
    alHacerClickEnObjetivos: () -> Unit
) {
    val viewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(dao))
    var anioSeleccionado by remember { mutableStateOf(LocalDate.now().year) }
    val top3 by viewModel.obtenerTop3(anioSeleccionado).collectAsState(initial = emptyList())

    val configuration = LocalConfiguration.current
    val esHorizontal = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color(0xFFF5F5F5),

        ) { padding ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),

        ) {
            val anchoPantalla = maxWidth

            //Proporciones que dependen de la configuracion de la pantalla.
            val (anchoNombre, anchoPuntuacion) = if (esHorizontal) {
                Pair(anchoPantalla * 0.2f, anchoPantalla * 0.065f)
            } else {
                Pair(anchoPantalla * 0.20f, 30.dp)

            }
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TarjetaResumen(
                        top3 = top3,
                        anchoNombre = anchoNombre,
                        anchoPuntuacion = anchoPuntuacion,
                        onClick = { alHacerClickEnResumen(anioSeleccionado) }

                    )
                    TarjetaTablaPendientes(
                        viewModel = viewModel,
                        anchoNombre = anchoNombre,
                        anchoPuntuacion = anchoPuntuacion,
                        onClick = { alHacerClickEnPendientes() }
                    )
                }
                TarjetaTablaObjetivos(
                    anchoNombre = anchoNombre,
                    anchoPuntuacion = anchoPuntuacion,
                    onClick = { alHacerClickEnObjetivos()}
                )
            }
        }
    }
}
@Composable
fun TarjetaResumen(
    top3: List<CategoriaTop>,
    anchoNombre: Dp,
    anchoPuntuacion: Dp,
    onClick: () -> Unit
) {
    val anchoTotal = anchoNombre + anchoPuntuacion + 8.dp + 40.dp + 4.dp

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(anchoTotal)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Encabezado de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Resumen", fontWeight = FontWeight.Bold, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(8.dp))

            if (top3.isEmpty()) {
                // Alineamos el texto de "Sin datos" al centro de este nuevo ancho
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Sin datos...", color = Color.LightGray)
                }
            } else {
                top3.forEachIndexed { index, cat ->
                    FilaTop3(cat, anchoNombre, anchoPuntuacion)
                    if (index < top3.lastIndex) Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
@Composable
fun FilaTop3(categoria: CategoriaTop, anchoNombre:Dp, anchoPuntuacion: Dp ) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        // 1. COLUMNA NOMBRE (Bloque de Color)
        Surface(
            color = Color(categoria.color),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .width(anchoNombre)
                .heightIn(min = 30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = categoria.nombre,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(anchoPuntuacion)
                .heightIn(min = 30.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.LightGray),

        contentAlignment = Alignment.Center,

        ) {
            Text(
                text = "${categoria.puntaje}",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Color(categoria.color)
            )
        }
    }
}
@Composable
fun TarjetaTablaPendientes(
    viewModel: AgendaViewModel,
    anchoNombre: Dp,
    anchoPuntuacion: Dp,
    onClick: () -> Unit
){
    val objetivos by viewModel.listaDeObjetivos.collectAsState(initial =emptyList())
    val anchoTotal = anchoNombre + anchoPuntuacion + 8.dp + 40.dp + 4.dp
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(anchoTotal)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Encabezado de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pendientes", fontWeight = FontWeight.ExtraBold, color = Color.Black,
                    textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(8.dp))


            }
        }
    }


@Composable
fun TarjetaTablaObjetivos(
    anchoNombre: Dp,
    anchoPuntuacion: Dp,
    onClick: () -> Unit
){
    val anchoTotal = anchoNombre + anchoPuntuacion + 8.dp + 40.dp + 4.dp
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .width(anchoTotal)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Encabezado de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Objetivos", fontWeight = FontWeight.ExtraBold, color = Color.Black,
                    textAlign = TextAlign.Center)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(8.dp))


        }
    }
}