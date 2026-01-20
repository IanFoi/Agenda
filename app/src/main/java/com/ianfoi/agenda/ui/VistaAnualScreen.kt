package com.ianfoi.agenda.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.model.Categoria
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.compose.ui.unit.Dp

// Array auxiliar para los nombres de los meses
val meses = listOf("ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaAnualScreen(
    dao: AgendaDao,
    alHacerClickEnMes: (Int) -> Unit,
) {
    val categorias by dao.getCategorias().collectAsState(initial = emptyList())
    var mostrarDialogo by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val anioActual = remember { LocalDate.now().year }
    val scrollHorizontal = rememberScrollState()

    val configuration = LocalConfiguration.current
    val esHorizontal = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        floatingActionButton = {
            //Boton para agregar una categoria.
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Categoria")
            }
        }
    ) { paddingValues ->

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            //Proporciones que dependen de la configuracion de la pantalla.
            val (anchoNombre, anchoMes) = if (esHorizontal) {
                val anchoPantalla = maxWidth
                Pair(anchoPantalla * 0.2f, anchoPantalla * 0.065f)
            } else {
                val anchoPantalla = maxWidth
                Pair(anchoPantalla * 0.25f, 45.dp)

            }

            Column(modifier = Modifier.fillMaxSize()) {
            //Encabezado.
                Text(
                    text = "Agenda $anioActual",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                // --- CABECERA DE MESES ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(anchoNombre + 12.dp))

                    Row(modifier = Modifier.horizontalScroll(scrollHorizontal)) {
                        meses.forEachIndexed { index, mes ->
                            Box(
                                modifier = Modifier
                                    .width(anchoMes)
                                    .height(30.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.DarkGray)
                                    .clickable { alHacerClickEnMes(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mes.take(3),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // --- LISTA DE FILAS ---
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categorias) { categoria ->
                        FilaAnual(
                            categoria = categoria,
                            anioActual = anioActual,
                            dao = dao,
                            scrollState = scrollHorizontal,
                            anchoNombre = anchoNombre,
                            anchoMes = anchoMes
                        )
                    }
                }
            }
        }

        if (mostrarDialogo) {
            DialogoNuevaCategoria(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombreNuevo ->
                    scope.launch {
                        val colorRandom = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)).toArgb().toLong()
                        dao.insertarCategoria(Categoria(nombre = nombreNuevo, color = colorRandom, orden = categorias.size))
                        mostrarDialogo = false
                    }
                }
            )
        }
    }
}
@Composable
fun FilaAnual(
    categoria: Categoria,
    anioActual: Int,
    dao: AgendaDao,
    scrollState: ScrollState,
    anchoNombre: Dp,
    anchoMes: Dp
) {
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
            // COLUMNA NOMBRE DE CATEGORIA
            Surface(
                color = Color(categoria.color),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(anchoNombre)
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
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // COLUMNA CELDAS
            Row(modifier = Modifier.horizontalScroll(scrollState)) {
                meses.forEachIndexed { index, _ ->
                    val inicioMes = (anioActual * 10000 + index * 100 + 0).toLong()
                    val finMes = (anioActual * 10000 + index * 100 + 32).toLong()

                    val conteo by dao.contarPorMes(categoria.id, inicioMes, finMes).collectAsState(initial = 0)

                    CasillaMes(
                        valor = conteo,
                        colorCategoria = Color(categoria.color),
                        ancho = anchoMes
                    )
                }
            }
        }
    }
}@Composable
fun CasillaMes(
    valor: Int,
    colorCategoria: Color,
    ancho: Dp
) {
    val fondo = if (valor > 0) colorCategoria else Color(0xFFEEEEEE)
    val textoColor = if (valor > 0) Color.White else Color.Transparent

    Box(
        modifier = Modifier
            .width(ancho)
            .height(40.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        if (valor > 0) {
            Text(
                text = valor.toString(),
                color = textoColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
@Composable
fun DialogoNuevaCategoria(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nuevo Rubro") },
        text = { TextField(value = texto, onValueChange = { texto = it }, label = { Text("Nombre") }, singleLine = true) },
        confirmButton = { Button(onClick = { if (texto.isNotBlank()) onConfirm(texto) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}