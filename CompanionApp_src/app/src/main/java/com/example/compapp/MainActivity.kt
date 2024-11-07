package com.example.compapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compapp.ui.theme.CompAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GreetingWithButton(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GreetingWithButton(modifier: Modifier = Modifier) {
    // State to track the recording status
    var isRecording by remember { mutableStateOf(false) }

    // Column layout to organize text and button
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display recording status text
        Text(
            text = if (isRecording) "Listening..." else "Idle",
            modifier = Modifier.padding(bottom = 32.dp) // Space between text and button
        )

        // Button with toggle functionality
        Button(
            onClick = {
                // Toggle the recording status
                isRecording = !isRecording
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // Make the button taller
        ) {
            // Toggle button text based on recording status
            Text(text = if (isRecording) "End Recording" else "Start Recording", modifier = Modifier.padding(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingWithButtonPreview() {
    CompAppTheme {
        GreetingWithButton()
    }
}
