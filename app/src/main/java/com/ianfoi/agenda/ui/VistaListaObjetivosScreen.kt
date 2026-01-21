package com.ianfoi.agenda.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ianfoi.agenda.data.AgendaDao
import com.ianfoi.agenda.model.Objetivo
import com.ianfoi.agenda.viewmodel.AgendaViewModel
import com.ianfoi.agenda.viewmodel.AgendaViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaListaObjetivoScreen(
    dao: AgendaDao,
    onBack: () -> Unit,
    onItemClick: (Int) -> Unit
) {
    val viewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(dao))
    val configuration = LocalConfiguration.current
    val esHorizontal = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val objetivos by viewModel.listaDeObjetivos.collectAsState(initial = emptyList())
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Mis Objetivos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.LightGray)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.LightGray
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Objetivo")
            }
        }
    ) { padding ->
        if (objetivos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no tienes objetivos. \n Agreguemos algunos!", textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)

            ) {
                items(objetivos, key = { it.id }) { objetivo ->
                    ItemObjetivo(
                        objetivo = objetivo,
                        onToggle = { viewModel.toggleObjetivo(objetivo) },
                        onDelete = { viewModel.borrarObjetivo(objetivo) },
                        onClick = { onItemClick(objetivo.id) })
                }
            }
        }
        if(mostrarDialogo){
            DialogoNuevoObjetivo(
                onDismiss = {mostrarDialogo = false},
                onConfirm = { texto ->
                    viewModel.agregarObjetivo(texto)
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun DialogoNuevoObjetivo(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var texto by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Nuevo Objetivo ") },
        text = { TextField(value = texto, onValueChange = { texto = it }, label = { Text("Nombre") }, singleLine = true) },
        confirmButton = { Button(onClick = { if (texto.isNotBlank()) onConfirm(texto) }) { Text("Agregar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
fun ItemObjetivo(objetivo: Objetivo,
                 onToggle: () -> Unit,
                 onDelete: () -> Unit,
                 onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = objetivo.nombre,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {onClick()}
                    .padding(6.dp)
                ,
                textDecoration = if(objetivo.isCompletado) TextDecoration.LineThrough else null,
                color = if(objetivo.isCompletado) Color.Gray else Color.Black
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = Color.LightGray)
            }
        }
    }
}