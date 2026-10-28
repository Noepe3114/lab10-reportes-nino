package com.example.lab10.view

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.SerieApiService
import com.example.lab10.data.SerieModel
import kotlinx.coroutines.delay

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    var listaSeries: SnapshotStateList<SerieModel> = remember { mutableStateListOf() }

    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectSeries()
            listaSeries.clear()
            listado.forEach { listaSeries.add(it) }
        } catch (e: Exception) {
            Log.e("SERIE-LISTA", "Falla de conexión: ${e.message}")
        }
    }

    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = "SERIE", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                Text(text = "Accion", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
            }
        }

        items(listaSeries) { item ->
            Row(
                modifier = Modifier.padding(start = 8.dp).fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = item.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f))
                IconButton(
                    onClick = {
                        navController.navigate("serieVer/${item.id}")
                        Log.e("SERIE-VER", "ID = ${item.id}")
                    },
                    Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver")
                }
                IconButton(
                    onClick = {
                        navController.navigate("serieDel/${item.id}")
                        Log.e("SERIE-DEL", "ID = ${item.id}")
                    },
                    Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(
    navController: NavHostController,
    servicio: SerieApiService,
    pid: Int = 0
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("series_prefs", Context.MODE_PRIVATE) }
    val esNuevo = pid == 0

    var id by remember { mutableStateOf(pid) }
    var name by remember { mutableStateOf<String?>("") }
    var release_date by remember { mutableStateOf<String?>("") }
    var rating by remember { mutableStateOf<String?>("") }
    var category by remember { mutableStateOf<String?>("") }
    var grabar by remember { mutableStateOf(false) }

    if (esNuevo) {
        LaunchedEffect(Unit) {
            try {
                val maxServidor = servicio.selectSeries().maxOfOrNull { it.id } ?: 0
                val ultimoUsado = prefs.getInt("ultimo_id", 0)
                id = maxOf(maxServidor, ultimoUsado) + 1
            } catch (e: Exception) {
                Log.e("SERIE-ID", "Falla al calcular id: ${e.message}")
            }
        }
    } else {
        // Serie existente: cargar sus datos
        LaunchedEffect(Unit) {
            try {
                val objSerie = servicio.selectSerie(id.toString())
                delay(100)
                name = objSerie.body()?.name
                release_date = objSerie.body()?.release_date
                rating = objSerie.body()?.rating.toString()
                category = objSerie.body()?.category
            } catch (e: Exception) {
                Log.e("SERIE-CARGAR", "Falla: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = id.toString(),
            onValueChange = { },
            label = { Text("ID (automático)") },
            readOnly = true,
            singleLine = true
        )
        TextField(
            value = name ?: "",
            onValueChange = { name = it },
            label = { Text("Name: ") },
            singleLine = true
        )
        TextField(
            value = release_date ?: "",
            onValueChange = { release_date = it },
            label = { Text("Release Date:") },
            singleLine = true
        )
        TextField(
            value = rating ?: "",
            onValueChange = { rating = it },
            label = { Text("Rating:") },
            singleLine = true
        )
        TextField(
            value = category ?: "",
            onValueChange = { category = it },
            label = { Text("Category:") },
            singleLine = true
        )
        Button(
            enabled = !esNuevo || id != 0,
            onClick = { grabar = true }
        ) {
            Text("Grabar", fontSize = 16.sp)
        }
    }

    if (grabar) {
        LaunchedEffect(Unit) {
            try {
                val objSerie = SerieModel(
                    id,
                    name ?: "",
                    release_date ?: "",
                    rating?.toIntOrNull() ?: 0,
                    category ?: ""
                )
                val respuesta = if (esNuevo)
                    servicio.insertSerie(objSerie)
                else
                    servicio.updateSerie(id.toString(), objSerie)

                Log.e(
                    "SERIE-GRABAR",
                    "Código: ${respuesta.code()} Error: ${respuesta.errorBody()?.string()}"
                )

                if (respuesta.isSuccessful) {
                    if (esNuevo) prefs.edit().putInt("ultimo_id", id).apply()
                    navController.navigate("series")
                }
            } catch (e: Exception) {
                Log.e("SERIE-GRABAR", "Falla: ${e.message}")
            }
            grabar = false
        }
    }
}
@Composable
fun ContenidoSerieEliminar(
    navController: NavHostController,
    servicio: SerieApiService,
    id: Int
) {
    var showDialog by remember { mutableStateOf(true) }
    var borrar by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                navController.navigate("series")
            },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        borrar = true
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog = false
                        navController.navigate("series")
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (borrar) {
        LaunchedEffect(Unit) {
            servicio.deleteSerie(id.toString())
            borrar = false
            navController.navigate("series")
        }
    }
}