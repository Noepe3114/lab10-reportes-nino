package com.example.lab10.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.ReporteApiService
import com.example.lab10.data.ReporteModel
import kotlinx.coroutines.launch

@Composable
fun ContenidoReportesListado(navController: NavHostController, servicio: ReporteApiService) {
    val lista: SnapshotStateList<ReporteModel> = remember { mutableStateListOf() }

    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectReportes()
            lista.clear()
            lista.addAll(listado)
        } catch (e: Exception) {
            Log.e("REPORTE-LISTA", "Falla de conexión: ${e.message}")
        }
    }

    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = "REPORTE", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                Text(text = "Accion", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
            }
        }

        items(lista) { item ->
            Row(
                modifier = Modifier.padding(start = 8.dp).fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Column(modifier = Modifier.weight(0.6f)) {
                    Text(text = item.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${item.location} • ${item.severity} • ${item.status}", fontSize = 13.sp)
                }
                IconButton(
                    onClick = {
                        navController.navigate("reporteVer/${item.id}")
                        Log.e("REPORTE-VER", "ID = ${item.id}")
                    },
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver")
                }
                IconButton(
                    onClick = {
                        navController.navigate("reporteDel/${item.id}")
                        Log.e("REPORTE-DEL", "ID = ${item.id}")
                    },
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

@Composable
fun ContenidoReporteEditar(
    navController: NavHostController,
    servicio: ReporteApiService,
    pid: Int = 0
) {
    val esNuevo = pid == 0
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var event_type by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("") }
    var report_date by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("pendiente") }
    var description by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!esNuevo) {
            try {
                val r = servicio.selectReporte(pid.toString())
                r.body()?.let {
                    title = it.title
                    location = it.location
                    event_type = it.event_type
                    severity = it.severity
                    report_date = it.report_date
                    status = it.status
                    description = it.description
                }
            } catch (e: Exception) {
                Log.e("REPORTE-CARGAR", "Falla: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TextField(
            value = if (esNuevo) "(automático)" else pid.toString(),
            onValueChange = { },
            label = { Text("ID (solo lectura)") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(value = title, onValueChange = { title = it }, label = { Text("Título:") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextField(value = location, onValueChange = { location = it }, label = { Text("Lugar:") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextField(value = event_type, onValueChange = { event_type = it }, label = { Text("Tipo (inundación, huaico...):") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextField(value = severity, onValueChange = { severity = it }, label = { Text("Riesgo (bajo, medio, alto, crítico):") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextField(value = report_date, onValueChange = { report_date = it }, label = { Text("Fecha (AAAA-MM-DD):") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextField(value = status, onValueChange = { status = it }, label = { Text("Estado:") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        TextField(value = description, onValueChange = { description = it }, label = { Text("Descripción:") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                scope.launch {
                    try {
                        val obj = ReporteModel(pid, title, description, location, event_type, severity, report_date, status)
                        val respuesta = if (esNuevo)
                            servicio.insertReporte(obj)
                        else
                            servicio.updateReporte(pid.toString(), obj)

                        Log.e(
                            "REPORTE-GRABAR",
                            "Código: ${respuesta.code()} Error: ${respuesta.errorBody()?.string()}"
                        )
                        if (respuesta.isSuccessful) navController.navigate("reportes")
                    } catch (e: Exception) {
                        Log.e("REPORTE-GRABAR", "Falla: ${e.message}")
                    }
                }
            }
        ) {
            Text("Grabar", fontSize = 16.sp)
        }
    }
}

@Composable
fun ContenidoReporteEliminar(
    navController: NavHostController,
    servicio: ReporteApiService,
    id: Int
) {
    var showDialog by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                navController.navigate("reportes")
            },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar el Reporte?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    scope.launch {
                        try {
                            val r = servicio.deleteReporte(id.toString())
                            Log.e("REPORTE-ELIMINAR", "Código: ${r.code()}")
                        } catch (e: Exception) {
                            Log.e("REPORTE-ELIMINAR", "Falla: ${e.message}")
                        }
                        navController.navigate("reportes")
                    }
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                    navController.navigate("reportes")
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}