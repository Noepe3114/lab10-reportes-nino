package com.example.lab10.view

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10.data.PostApiService
import com.example.lab10.data.PostModel
import kotlinx.coroutines.launch

@Composable
fun ContenidoPostsListado(navController: NavHostController, servicio: PostApiService) {
    val lista: SnapshotStateList<PostModel> = remember { mutableStateListOf() }

    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectPosts()
            lista.clear()
            lista.addAll(listado)
        } catch (e: Exception) {
            Log.e("POST-LISTA", "Falla de conexión: ${e.message}")
        }
    }

    LazyColumn {
        item {
            Row(
                modifier = Modifier.fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "ID", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Text(text = "POST", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.7f))
                Text(text = "Accion", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.2f))
            }
        }

        items(lista) { item ->
            Row(
                modifier = Modifier.padding(start = 8.dp).fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id}", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.1f))
                Column(modifier = Modifier.weight(0.6f)) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.body,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {
                        navController.navigate("postVer/${item.id}")
                        Log.e("POST-VER", "ID = ${item.id}")
                    },
                    modifier = Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver")
                }
                IconButton(
                    onClick = {
                        navController.navigate("postDel/${item.id}")
                        Log.e("POST-DEL", "ID = ${item.id}")
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
fun ContenidoPostEditar(
    navController: NavHostController,
    servicio: PostApiService,
    pid: Int = 0
) {
    val context = LocalContext.current
    val esNuevo = pid == 0
    var userId by remember { mutableStateOf(1) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (!esNuevo) {
            try {
                val r = servicio.selectPost(pid.toString())
                r.body()?.let {
                    userId = it.userId
                    title = it.title
                    body = it.body
                }
            } catch (e: Exception) {
                Log.e("POST-CARGAR", "Falla: ${e.message}")
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
        TextField(
            value = userId.toString(),
            onValueChange = { },
            label = { Text("User ID (solo lectura)") },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título:") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Contenido:") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                scope.launch {
                    try {
                        val obj = PostModel(pid, userId, title, body)
                        val respuesta = if (esNuevo)
                            servicio.insertPost(obj)
                        else
                            servicio.updatePost(pid.toString(), obj)

                        val verbo = if (esNuevo) "POST" else "PUT"
                        Log.e("POST-GRABAR", "$verbo código: ${respuesta.code()}")
                        Toast.makeText(context, "$verbo → código ${respuesta.code()}", Toast.LENGTH_LONG).show()
                        if (respuesta.isSuccessful) navController.navigate("posts")
                    } catch (e: Exception) {
                        Log.e("POST-GRABAR", "Falla: ${e.message}")
                    }
                }
            }
        ) {
            Text("Grabar", fontSize = 16.sp)
        }
    }
}

@Composable
fun ContenidoPostEliminar(
    navController: NavHostController,
    servicio: PostApiService,
    id: Int
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                navController.navigate("posts")
            },
            title = { Text(text = "Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar el Post?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    scope.launch {
                        try {
                            val r = servicio.deletePost(id.toString())
                            Log.e("POST-ELIMINAR", "DELETE código: ${r.code()}")
                            Toast.makeText(context, "DELETE → código ${r.code()}", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Log.e("POST-ELIMINAR", "Falla: ${e.message}")
                        }
                        navController.navigate("posts")
                    }
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                    navController.navigate("posts")
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}