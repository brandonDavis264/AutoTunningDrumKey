package com.example.bluetoothsetup

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID

class MainActivity : ComponentActivity() {

    private lateinit var requestBluetoothPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestBluetoothSocketPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var bluetoothManager: BluetoothManager
    private var bAdapter: BluetoothAdapter? = null
    private var bSocket: BluetoothSocket? = null
    private var listeningThread: Thread? = null
    private var isListening = false

    private lateinit var bluetoothStatusTv: TextView
    private lateinit var bluetoothIv: ImageView
    private lateinit var turnOnBtn: Button
    private lateinit var connectBtn: Button
    private lateinit var pairedBtn: Button
    private lateinit var pairedTv: TextView
    private lateinit var dataLogTv: TextView

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        bluetoothStatusTv.text = "Bluetooth is off"
                        bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
                        Toast.makeText(context, "Bluetooth turned off", Toast.LENGTH_LONG).show()
                    }
                    BluetoothAdapter.STATE_ON -> {
                        bluetoothStatusTv.text = "Bluetooth is on"
                        bluetoothIv.setImageResource(R.drawable.ic_bluetooth_on)
                        Toast.makeText(context, "Bluetooth turned on", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        bAdapter = bluetoothManager.adapter

        // Initialize UI elements
        bluetoothStatusTv = findViewById(R.id.bluetoothStatusTv)
        bluetoothIv = findViewById(R.id.bluetoothIv)
        turnOnBtn = findViewById(R.id.turnOnBtn)
        connectBtn = findViewById(R.id.connectBtn)
        pairedBtn = findViewById(R.id.pairedBtn)
        pairedTv = findViewById(R.id.pairedTv)
        dataLogTv = findViewById(R.id.dataLogTv)

        // Register permission launcher
        requestBluetoothPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                displayPairedDevices()
            } else {
                Toast.makeText(this, "Permission denied. Cannot access paired devices.", Toast.LENGTH_LONG).show()
            }
        }

        requestBluetoothSocketPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                connectToDevice("2C:BC:BB:4C:A9:8A")
            } else {
                Toast.makeText(this, "Bluetooth Connect permission denied.", Toast.LENGTH_LONG).show()
            }
        }

        // Register Bluetooth state receiver
        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

        // Setup initial Bluetooth status
        bluetoothStatusTv.text = if (bAdapter?.isEnabled == true) "Bluetooth is on" else "Bluetooth is off"
        bluetoothIv.setImageResource(if (bAdapter?.isEnabled == true) R.drawable.ic_bluetooth_on else R.drawable.ic_bluetooth_off)

        // Set up listeners
        turnOnBtn.setOnClickListener {
            if (bAdapter?.isEnabled == false) {
                startActivity(Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS))
            } else {
                Toast.makeText(this, "Bluetooth is already on", Toast.LENGTH_SHORT).show()
            }
        }

        pairedBtn.setOnClickListener {
            if (bAdapter?.isEnabled == true) {
                displayPairedDevices()
            } else {
                Toast.makeText(this, "Turn on Bluetooth first", Toast.LENGTH_SHORT).show()
            }
        }

        connectBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestBluetoothSocketPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                } else {
                    Toast.makeText(this, "Permission required to connect.", Toast.LENGTH_SHORT).show()
                }
            } else {
                connectToDevice("2C:BC:BB:4C:A9:8A")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun displayPairedDevices() {
        pairedTv.text = "Paired Devices:"
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            return
        }
        bAdapter?.bondedDevices?.forEach { device ->
            val deviceName = device.name ?: "Unknown Device"
            val deviceAddress = device.address ?: "Unknown Address"
            pairedTv.append("\nDevice: $deviceName, $deviceAddress")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun connectToDevice(deviceAddress: String) {
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        try {
            val device = bAdapter?.getRemoteDevice(deviceAddress)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothSocketPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                return
            }
            bSocket = device?.createInsecureRfcommSocketToServiceRecord(uuid)
            bSocket?.connect()

            Toast.makeText(this, "Connected to ESP", Toast.LENGTH_SHORT).show()

            val inputStream = bSocket?.inputStream
            val outputStream = bSocket?.outputStream

            outputStream?.write("Sent from APP (Client) to ESP (Server)".toByteArray())

            runOnUiThread {
                dataLogTv.append("Sent: Sent from APP (Client) to ESP (Server)\n")
            }

            val buffer = ByteArray(1024)
            val bytesRead = inputStream?.read(buffer) ?: 0

            if (bytesRead > 0) {
                val receivedData = String(buffer, 0, bytesRead)
                runOnUiThread {
                    dataLogTv.append("Received: $receivedData\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                dataLogTv.append("Error: ${e.message}\n")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothStateReceiver)
    }
}
