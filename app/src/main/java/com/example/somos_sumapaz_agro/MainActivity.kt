package com.example.somos_sumapaz_agro

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.somos_sumapaz_agro.db.VisitasDbHelper
import com.example.somos_sumapaz_agro.ui.screens.AgricolaForm
import com.example.somos_sumapaz_agro.ui.screens.HistorialScreen
import com.example.somos_sumapaz_agro.ui.screens.IndexScreen
import com.example.somos_sumapaz_agro.ui.screens.PecuariaForm
import com.example.somos_sumapaz_agro.ui.theme.Somos_sumapaz_agroTheme

enum class Screen {
    Index,
    Pecuaria,
    Agricola,
    Historial
}

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: VisitasDbHelper

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar base de datos local
        dbHelper = VisitasDbHelper(this)
        
        enableEdgeToEdge()
        setContent {
            Somos_sumapaz_agroTheme {
                // Solicitar permisos de GPS al inicio
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                    val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (!fine && !coarse) {
                        Toast.makeText(
                            this, 
                            "Permisos de GPS denegados. Deberá ingresar la ubicación a mano.", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }

                var currentScreen by remember { mutableStateOf(Screen.Index) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { 
                                Text(
                                    text = when (currentScreen) {
                                        Screen.Index -> "ULATA Sumapaz Agro"
                                        Screen.Pecuaria -> "Visita Pecuaria"
                                        Screen.Agricola -> "Visita Agrícola"
                                        Screen.Historial -> "Historial de Visitas"
                                    }, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                ) 
                            },
                            navigationIcon = {
                                if (currentScreen != Screen.Index) {
                                    IconButton(onClick = { currentScreen = Screen.Index }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Volver al Índice",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (currentScreen) {
                            Screen.Index -> IndexScreen(
                                onNavigate = { currentScreen = it }
                            )
                            Screen.Pecuaria -> PecuariaForm(
                                dbHelper = dbHelper,
                                onNavigateToHistorial = { currentScreen = Screen.Historial }
                            )
                            Screen.Agricola -> AgricolaForm(
                                dbHelper = dbHelper,
                                onNavigateToHistorial = { currentScreen = Screen.Historial }
                            )
                            Screen.Historial -> HistorialScreen(
                                dbHelper = dbHelper
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}