package com.example.doormonitringapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.example.doormonitringapp.ui.theme.DoorMonitringAppTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DoorMonitringAppTheme {

                var door1State by remember { mutableStateOf("UNKNOWN") }
                var door2State by remember { mutableStateOf("UNKNOWN") }
                var door3State by remember { mutableStateOf("UNKNOWN") }
                var door4State by remember { mutableStateOf("UNKNOWN") }
                var deviceOffline by remember { mutableStateOf(false) }
                var isRequestRunning by remember { mutableStateOf(false) }

                // Create queue only once
                val queue = remember {
                    Volley.newRequestQueue(this@MainActivity)
                }

                fun fetchStatus() {

                    if (isRequestRunning) return
                    isRequestRunning = true

                    val url = "http://10.216.16.251/status"

                    val request = StringRequest(
                        Request.Method.GET,
                        url,
                        { response ->
                            try {
                                val json = JSONObject(response)

                                door1State = json.optString("D1", "UNKNOWN")
                                door2State = json.optString("D2", "UNKNOWN")
                                door3State = json.optString("D3", "UNKNOWN")
                                door4State = json.optString("D4", "UNKNOWN")

                                deviceOffline = false

                            } catch (e: Exception) {
                                deviceOffline = true
                            }
                            isRequestRunning = false
                        },
                        {
                            deviceOffline = true
                            isRequestRunning = false
                        }
                    )

                    request.setShouldCache(false)
                    queue.add(request)
                }

                // Auto refresh every 3 seconds
                LaunchedEffect(Unit) {
                    while (true) {
                        fetchStatus()
                        delay(3000)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = if (deviceOffline) "ESP DEVICE OFFLINE"
                        else "Door Monitoring System",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (deviceOffline) Color.Red else Color.Unspecified
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DoorCard("Door 1", door1State, deviceOffline)
                        DoorCard("Door 2", door2State, deviceOffline)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DoorCard("Door 3", door3State, deviceOffline)
                        DoorCard("Door 4", door4State, deviceOffline)
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(onClick = { fetchStatus() }) {
                        Text("Refresh")
                    }
                }
            }
        }
    }
}

@Composable
fun DoorCard(title: String, state: String, offline: Boolean) {

    val targetColor =
        if (offline) Color.Gray
        else when (state.uppercase()) {
            "OPEN" -> Color(0xFFD32F2F)
            "CLOSED" -> Color(0xFF388E3C)
            else -> Color.LightGray
        }

    // Smooth color animation
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = ""
    )

    val displayText =
        if (offline) "⚠ OFFLINE"
        else when (state.uppercase()) {
            "OPEN" -> "🚪 OPEN"
            "CLOSED" -> "🔒 CLOSED"
            else -> "UNKNOWN"
        }

    Card(
        modifier = Modifier
            .width(150.dp)
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = animatedColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = displayText, color = Color.White)
        }
    }
}