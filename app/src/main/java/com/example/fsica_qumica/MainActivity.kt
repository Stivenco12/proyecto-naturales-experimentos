package com.example.fsica_qumica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Resorte", "Presión")

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("Física/Química ") })
        }) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { i, title ->
                        Tab(selected = tabIndex == i, onClick = { tabIndex = i }, text = { Text(title) })
                    }
                }
                when (tabIndex) {
                    0 -> SpringSimulator(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp))
                    1 -> PressureCalculator(modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun LabeledValue(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SpringSimulator(modifier: Modifier = Modifier) {
    var massKg by remember { mutableStateOf(2f) }
    var kNpm by remember { mutableStateOf(100f) }
    val g = 9.81f

    val topMarginPx = 40f
    val coilWidthPx = 40f
    val naturalLengthPx = 120f

    val extensionMeters = (massKg * g) / max(kNpm, 1f)
    val pxPerMeter = 200f
    val totalLengthPxTarget = naturalLengthPx + extensionMeters * pxPerMeter
    val animatedLengthPx by animateFloatAsState(targetValue = totalLengthPxTarget)

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Simulador de resorte (Ley de Hooke)",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Guarda los colores antes del Canvas
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            val outlineColor = MaterialTheme.colorScheme.outline

            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val topY = topMarginPx
                val bottomY = topMarginPx + animatedLengthPx

                drawLine(
                    color = primaryColor,
                    start = Offset(x = centerX - 100f, y = topY - 10f),
                    end = Offset(x = centerX + 100f, y = topY - 10f),
                    strokeWidth = 8f
                )

                val turns = 10
                val segment = (bottomY - topY) / turns
                val path = Path().apply {
                    moveTo(centerX, topY)
                    for (i in 0 until turns) {
                        val y1 = topY + i * segment + segment / 2f
                        val x1 = centerX - coilWidthPx / 2f
                        val y2 = topY + (i + 1) * segment
                        val x2 = centerX + coilWidthPx / 2f
                        lineTo(x1, y1)
                        lineTo(x2, y2)
                    }
                    lineTo(centerX, bottomY)
                }
                drawPath(
                    path = path,
                    color = secondaryColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                )

                val blockWidth = 80f
                val blockHeight = 50f
                drawRect(
                    color = tertiaryColor,
                    topLeft = Offset(centerX - blockWidth / 2f, bottomY),
                    size = androidx.compose.ui.geometry.Size(blockWidth, blockHeight)
                )

                drawLine(
                    color = outlineColor,
                    start = Offset(centerX - 120f, bottomY + blockHeight),
                    end = Offset(centerX + 120f, bottomY + blockHeight),
                    strokeWidth = 4f
                )
            }
        }


        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth()) {
            Text("Masa (kg): %.2f".format(massKg))
            Slider(value = massKg, onValueChange = { massKg = it }, valueRange = 0.1f..10f)
            Text("Constante elástica k (N/m): %.0f".format(kNpm))
            Slider(value = kNpm, onValueChange = { kNpm = it }, valueRange = 10f..500f)
        }

        Spacer(Modifier.height(8.dp))
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                LabeledValue("Extensión (m)", "%.3f m".format(extensionMeters))
                LabeledValue("Fuerza elástica (N)", "%.2f N".format(kNpm * extensionMeters))
                LabeledValue("Peso (N)", "%.2f N".format(massKg * g))
                Text(
                    "Nota: modelo cuasiestático (sin oscilaciones/damping).",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PressureCalculator(modifier: Modifier = Modifier) {
    var mode by remember { mutableStateOf(0) }

    Column(modifier = modifier) {
        SegmentedControl(options = listOf("Mecánica (F/A)", "Gas ideal (nRT/V)"), selectedIndex = mode) {
            mode = it
        }
        Spacer(Modifier.height(12.dp))
        when (mode) {
            0 -> MechanicalPressure()
            1 -> IdealGasPressure()
        }
    }
}

@Composable
fun SegmentedControl(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        options.forEachIndexed { idx, label ->
            FilterChip(
                selected = selectedIndex == idx,
                onClick = { onSelect(idx) },
                label = { Text(label, fontSize = 12.sp, textAlign = TextAlign.Center) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun MechanicalPressure() {
    var forceN by remember { mutableStateOf(100f) }
    var areaM2 by remember { mutableStateOf(1f) }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
        Text("Presión mecánica: P = F / A")
        Spacer(Modifier.height(8.dp))
        Text("Fuerza F (N): %.1f".format(forceN))
        Slider(value = forceN, onValueChange = { forceN = it }, valueRange = 0f..2000f)

        Text("Área A (m²): %.2f".format(areaM2))
        Slider(value = areaM2, onValueChange = { areaM2 = max(it, 0.01f) }, valueRange = 0.01f..10f)

        val pressurePa = forceN / max(areaM2, 1e-6f)
        val pressureBar = pressurePa / 100000f

        Spacer(Modifier.height(8.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                LabeledValue("Presión (Pa)", "%.2f Pa".format(pressurePa))
                LabeledValue("Presión (bar)", "%.4f bar".format(pressureBar))
            }
        }
    }
}

@Composable
fun IdealGasPressure() {
    var nMoles by remember { mutableStateOf(1f) }
    var temperatureK by remember { mutableStateOf(293f) }
    var volumeM3 by remember { mutableStateOf(1f) }
    val R = 8.314462618f

    Column(Modifier.fillMaxWidth()) {
        Text("Gas ideal: P = n R T / V")
        Spacer(Modifier.height(8.dp))

        Text("Cantidad de sustancia n (mol): %.2f".format(nMoles))
        Slider(value = nMoles, onValueChange = { nMoles = it }, valueRange = 0.1f..50f)

        Text("Temperatura T (K): %.1f".format(temperatureK))
        Slider(value = temperatureK, onValueChange = { temperatureK = it }, valueRange = 200f..1200f)

        Text("Volumen V (m³): %.2f".format(volumeM3))
        Slider(value = volumeM3, onValueChange = { volumeM3 = max(it, 0.01f) }, valueRange = 0.01f..10f)

        val pressurePa = (nMoles * R * temperatureK) / max(volumeM3, 1e-6f)
        val pressureAtm = pressurePa / 101325f

        Spacer(Modifier.height(8.dp))
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                LabeledValue("Presión (Pa)", "%.2f Pa".format(pressurePa))
                LabeledValue("Presión (atm)", "%.3f atm".format(pressureAtm))
                Text(
                    "Asume gas ideal (sin interacciones) y contenedor rígido.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
