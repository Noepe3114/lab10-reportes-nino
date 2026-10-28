package com.example.lab10.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab10.data.PostApiService
import com.example.lab10.data.ReporteApiService
import com.example.lab10.data.SerieApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun SeriesApp() {
    // Django local. Con adb reverse usa 127.0.0.1; sin adb reverse usa "http://10.0.2.2:8000/"
    val urlBase = "http://127.0.0.1:8000/"
    // Servicio web online (HTTPS)
    val urlBasePosts = "https://jsonplaceholder.typicode.com/"

    val retrofit = remember {
        Retrofit.Builder().baseUrl(urlBase)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
    val retrofitPosts = remember {
        Retrofit.Builder().baseUrl(urlBasePosts)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
    val servicio = remember { retrofit.create(SerieApiService::class.java) }
    val servicioRep = remember { retrofit.create(ReporteApiService::class.java) }
    val servicioPost = remember { retrofitPosts.create(PostApiService::class.java) }
    val navController = rememberNavController()

    Scaffold(
        topBar = { BarraSuperior() },
        bottomBar = { BarraInferior(navController) },
        floatingActionButton = { BotonFAB(navController) },
        content = { paddingValues ->
            Contenido(paddingValues, navController, servicio, servicioRep, servicioPost)
        }
    )
}

@Composable
fun BotonFAB(navController: NavHostController) {
    val cbeState by navController.currentBackStackEntryAsState()
    val rutaActual = cbeState?.destination?.route

    if (rutaActual == "series" || rutaActual == "reportes" || rutaActual == "posts") {
        FloatingActionButton(
            containerColor = Color.Magenta,
            contentColor = Color.White,
            onClick = {
                when (rutaActual) {
                    "series" -> navController.navigate("serieNuevo")
                    "reportes" -> navController.navigate("reporteNuevo")
                    else -> navController.navigate("postNuevo")
                }
            }
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "SERIES APP",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun BarraInferior(navController: NavHostController) {
    val cbeState by navController.currentBackStackEntryAsState()
    val ruta = cbeState?.destination?.route

    NavigationBar(containerColor = Color.LightGray) {
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = ruta == "inicio",
            onClick = { navController.navigate("inicio") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Favorite, contentDescription = "Series") },
            label = { Text("Series") },
            selected = ruta == "series",
            onClick = { navController.navigate("series") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Warning, contentDescription = "Reportes") },
            label = { Text("Reportes") },
            selected = ruta == "reportes",
            onClick = { navController.navigate("reportes") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Email, contentDescription = "Posts") },
            label = { Text("Posts") },
            selected = ruta == "posts",
            onClick = { navController.navigate("posts") }
        )
    }
}

@Composable
fun Contenido(
    pv: PaddingValues,
    navController: NavHostController,
    servicio: SerieApiService,
    servicioRep: ReporteApiService,
    servicioPost: PostApiService
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(pv)
    ) {
        NavHost(
            navController = navController,
            startDestination = "inicio"
        ) {
            composable("inicio") { ScreenInicio() }

            // ---- Series (Django) ----
            composable("series") { ContenidoSeriesListado(navController, servicio) }
            composable("serieNuevo") {
                ContenidoSerieEditar(navController, servicio, 0)
            }
            composable(
                "serieVer/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ContenidoSerieEditar(navController, servicio, it.arguments!!.getInt("id"))
            }
            composable(
                "serieDel/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ContenidoSerieEliminar(navController, servicio, it.arguments!!.getInt("id"))
            }

            // ---- Reportes (Django, Fenómeno de El Niño) ----
            composable("reportes") { ContenidoReportesListado(navController, servicioRep) }
            composable("reporteNuevo") {
                ContenidoReporteEditar(navController, servicioRep, 0)
            }
            composable(
                "reporteVer/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ContenidoReporteEditar(navController, servicioRep, it.arguments!!.getInt("id"))
            }
            composable(
                "reporteDel/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ContenidoReporteEliminar(navController, servicioRep, it.arguments!!.getInt("id"))
            }

            // ---- Posts (servicio web online: JSONPlaceholder) ----
            composable("posts") { ContenidoPostsListado(navController, servicioPost) }
            composable("postNuevo") {
                ContenidoPostEditar(navController, servicioPost, 0)
            }
            composable(
                "postVer/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ContenidoPostEditar(navController, servicioPost, it.arguments!!.getInt("id"))
            }
            composable(
                "postDel/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) {
                ContenidoPostEliminar(navController, servicioPost, it.arguments!!.getInt("id"))
            }
        }
    }
}