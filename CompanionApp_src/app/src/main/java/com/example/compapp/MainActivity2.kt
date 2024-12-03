package com.example.compapp

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class MainActivity2 : AppCompatActivity() {

    private lateinit var requestBluetoothPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestBluetoothSocketPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var bluetoothManager: BluetoothManager

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, android.bluetooth.BluetoothAdapter.ERROR)
                when (state) {
                    android.bluetooth.BluetoothAdapter.STATE_OFF -> {
                        Toast.makeText(context, "Bluetooth turned off", Toast.LENGTH_LONG).show()
                    }
                    android.bluetooth.BluetoothAdapter.STATE_ON -> {
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

        // Initialize Bluetooth Manager and Adapter
        bluetoothManager = getSystemService(BluetoothManager::class.java)
        AppBluetoothManager.bluetoothAdapter = bluetoothManager.adapter

        // Initialize permission request launchers
        requestBluetoothSocketPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                connectToDevice("2C:BC:BB:4C:A9:8A")
            } else {
                Toast.makeText(this, "Bluetooth Connect permission denied.", Toast.LENGTH_LONG).show()
            }
        }

        registerReceiver(bluetoothStateReceiver, IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED))

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

        // Lug Count Spinner
        val spinnerlugCount: Spinner = findViewById(R.id.spinner_lug_count)
        val lugCountOptions = listOf("Select", "6", "8", "10", "12")
        val adapter5 = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, lugCountOptions)
        spinnerlugCount.adapter = adapter5

        // Record button functionality
        val record: Button = findViewById(R.id.record)
        record.setOnClickListener {
            if (AppBluetoothManager.bluetoothAdapter?.isEnabled == true) {
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
            val device = AppBluetoothManager.bluetoothAdapter?.getRemoteDevice(deviceAddress)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothSocketPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return
            }
            AppBluetoothManager.bluetoothSocket = device?.createInsecureRfcommSocketToServiceRecord(uuid)
            AppBluetoothManager.bluetoothSocket?.connect()

            Toast.makeText(this, "Connected to ESP", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothStateReceiver)
    }
}
