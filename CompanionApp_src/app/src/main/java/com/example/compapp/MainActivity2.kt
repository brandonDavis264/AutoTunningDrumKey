package com.example.compapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class MainActivity2 : AppCompatActivity() {

    private lateinit var requestBluetoothPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestBluetoothSocketPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Toast.makeText(context, "Bluetooth turned off", Toast.LENGTH_LONG).show()
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Toast.makeText(context, "Bluetooth turned on", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter

        requestBluetoothSocketPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                connectToDevice("2C:BC:BB:4C:A9:8A")
            } else {
                Toast.makeText(this, "Bluetooth Connect permission denied.", Toast.LENGTH_LONG).show()
            }
        }

        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        // Drumhead Type Spinner
        val spinnerDrumheadType: Spinner = findViewById(R.id.spinner_drumhead_type)
        val drumheadOptions = listOf("Select", "Single-ply, coated", "Single-ply, clear", "Double-ply, coated", "Double-ply, clear")
        val adapter0 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, drumheadOptions)
        spinnerDrumheadType.adapter = adapter0

        // Shell Type Spinner
        val spinnerShellType: Spinner = findViewById(R.id.spinner_shell_type)
        val shellOptions = listOf("Select", "Wood: Maple", "Wood: Birch", "Wood: Mahogany", "Metal", "Acrylic")
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, shellOptions)
        spinnerShellType.adapter = adapter1

        // Shell Size Spinner
        val spinnerShellSize: Spinner = findViewById(R.id.spinner_shell_size)
        val shellSizeOptions = listOf(
            "Select",
            "Snare: 14x5.5 inches (Standard)",
            "Snare: 13x3 inches (Piccolo)",
            "Rack Tom: 12x8 inches (Standard)",
            "Rack Tom: 10x7 inches",
            "Floor Tom: 16x16 inches (Standard)",
            "Floor Tom: 14x14 inches",
            "Bass Drum: 22x16 inches (Rock Standard)",
            "Bass Drum: 20x14 inches (Jazz Standard)",
            "Bass Drum: 24x18 inches (Large)"
        )
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, shellSizeOptions)
        spinnerShellSize.adapter = adapter2

        // Shell Edge Spinner
        val spinnerShellEdge: Spinner = findViewById(R.id.spinner_shell_edge)
        val shellEdgeOptions = listOf("Select", "Sharp", "Rounded")
        val adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, shellEdgeOptions)
        spinnerShellEdge.adapter = adapter3

        // Hoop Type Spinner
        val spinnerHoopType: Spinner = findViewById(R.id.spinner_hoop_type)
        val hoopTypeOptions = listOf("Select", "Flanged Hoops", "Die-Cast Hoops")
        val adapter4 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, hoopTypeOptions)
        spinnerHoopType.adapter = adapter4

        val record: Button = findViewById(R.id.record)
        record.setOnClickListener {
            if (bluetoothAdapter?.isEnabled == true) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestBluetoothSocketPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    } else {
                        Toast.makeText(this, "Permission required to connect", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    try {
                        connectToDevice("2C:BC:BB:4C:A9:8A")
                        // Navigate to MainActivity3 only after a successful connection
                        val intent = Intent(this, MainActivity3::class.java)
                        startActivity(intent)
                    } catch (e: IOException) {
                        Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Bluetooth is not enabled. Please enable it and try again.", Toast.LENGTH_SHORT).show()
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun connectToDevice(deviceAddress: String) {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        try {
            val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothSocketPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return
            }
            bluetoothSocket = device?.createInsecureRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()

            Toast.makeText(this, "Connected to ESP", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothStateReceiver)
    }
}
