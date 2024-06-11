package com.example.monitoramento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.monitoramento.ui.DashboardActivityTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val refreshDataset = remember { mutableIntStateOf(0) }
            val modelProducer = remember { ChartEntryModelProducer() }
            val datasetForModel = remember { mutableStateListOf<List<FloatEntry>>() }
            val datasetLineSpec = remember { arrayListOf<LineChart.LineSpec>() }
            val scrollState = rememberChartScrollState()

            LaunchedEffect(refreshDataset.value) {
                try {
                    val response = RetrofitClient.apiService.getData()
                    println("Response from API: $response")
                    val dataFromDB = response.mapIndexed { index, data ->
                        FloatEntry(x = index.toFloat(), y = data.volume.toFloat())
                    }
                    println("Data from DB: $dataFromDB")

                    datasetForModel.clear()
                    datasetLineSpec.clear()

                    datasetForModel.add(dataFromDB)
                    println("Dataset for model: $datasetForModel")

                    datasetLineSpec.add(
                        LineChart.LineSpec(
                            lineColor = Green.toArgb(),
                            lineBackgroundShader = DynamicShaders.fromBrush(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Green.copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_START),
                                        Green.copy(com.patrykandpatrick.vico.core.DefaultAlpha.LINE_BACKGROUND_SHADER_END)
                                    )
                                )
                            )
                        )
                    )
                    println("Dataset line spec: $datasetLineSpec")
                    modelProducer.setEntries(datasetForModel)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }


            DashboardActivityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                        ) {
                            if (datasetForModel.isNotEmpty()) {
                                ProvideChartStyle {
                                    val marker = rememberMarker()
                                    Chart(
                                        chart = lineChart(
                                            lines = datasetLineSpec
                                        ),
                                        chartModelProducer = modelProducer,

                                        startAxis = rememberStartAxis(
                                            title = "Volume",
                                                tickLength = 0.dp,
                                                valueFormatter = { value, _ ->
                                                    value.toInt().toString()
                                                },
                                            itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 6)
                                        ),

                                        bottomAxis = rememberBottomAxis(
                                            title = "Horário",
                                            tickLength = 0.dp,
                                            valueFormatter = { value, _ ->
                                                value.toInt().toString()
                                            },
                                            guideline = null
                                        ),
                                        marker = marker,
                                        chartScrollState = scrollState,
                                        isZoomEnabled = true
                                    )
                                }
                            }
                        }
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { refreshDataset.intValue++ }
                        ) {
                            Text(text = "Atualizar")
                        }
                    }
                }
            }
        }
    }
}
