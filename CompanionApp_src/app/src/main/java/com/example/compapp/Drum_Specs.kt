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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class Drum_Specs : AppCompatActivity() {

    private lateinit var requestBluetoothPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestBluetoothSocketPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var selectedDeviceAdapter: BluetoothDeviceAdapter
    private var userDevice = ""

//    private val bluetoothStateReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (intent.action == android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED) {
//                val state = intent.getIntExtra(android.bluetooth.BluetoothAdapter.EXTRA_STATE, android.bluetooth.BluetoothAdapter.ERROR)
//                when (state) {
//                    android.bluetooth.BluetoothAdapter.STATE_OFF ->
//                        Toast.makeText(context, "Bluetooth turned off", Toast.LENGTH_LONG).show()
//                    android.bluetooth.BluetoothAdapter.STATE_ON ->
//                        Toast.makeText(context, "Bluetooth turned on", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        supportActionBar?.title="Drum Specifications"

        bluetoothManager = getSystemService(BluetoothManager::class.java)
        AppBluetoothManager.bluetoothAdapter = bluetoothManager.adapter

        displayPairedDevices()
        userDevice = selectedDeviceAdapter.getSelectedDevice()?.address ?: ""

        requestBluetoothSocketPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                connectToDevice(userDevice) { }
            } else {
                Toast.makeText(this, "Bluetooth Connect permission denied.", Toast.LENGTH_LONG).show()
            }
        }

//        registerReceiver(bluetoothStateReceiver, IntentFilter(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED))

        val lugCount: Spinner = findViewById(R.id.lugCountInt)
        val validCounts = listOf("Select", "2", "4", "6", "8", "10")
        val adapterLug = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, validCounts)
        lugCount.adapter = adapterLug

        val record: Button = findViewById(R.id.record)

        record.setOnClickListener {
            if (userDevice.isEmpty()) {
                Toast.makeText(this, "No device selected. Please select a device.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (AppBluetoothManager.bluetoothAdapter?.isEnabled == true) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        requestBluetoothSocketPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    } else {
                        Toast.makeText(this, "Permission required to connect", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    connectToDevice(userDevice) { isConnected ->
                        if (isConnected) {
                            val intent = Intent(this, Tuning::class.java)
                            val extraLug: Int = lugCount.getSelectedItem().toString().toInt()
                            intent.putExtra("lugCount", extraLug)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Connection failed. Please try again.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Bluetooth is not enabled. Please enable it and try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun connectToDevice(deviceAddress: String, onConnected: (Boolean) -> Unit) {
        val progressBar: ProgressBar = findViewById(R.id.progressBar)
        val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        runOnUiThread { progressBar.visibility = View.VISIBLE }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val device = AppBluetoothManager.bluetoothAdapter?.getRemoteDevice(deviceAddress)

                if (ActivityCompat.checkSelfPermission(this@Drum_Specs, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    requestBluetoothSocketPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        onConnected(false)
                    }
                    return@launch
                }

                AppBluetoothManager.bluetoothSocket = device?.createInsecureRfcommSocketToServiceRecord(uuid)
                AppBluetoothManager.bluetoothSocket?.connect()

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@Drum_Specs, "Connected to ESP", Toast.LENGTH_SHORT).show()
                    onConnected(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@Drum_Specs, "Connection failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    onConnected(false)
                }
            }
        }
    }

    data class BluetoothDeviceItem(val name: String, val address: String)

    class BluetoothDeviceAdapter(
        private val devices: List<BluetoothDeviceItem>,
        private val onDeviceSelected: (BluetoothDeviceItem) -> Unit
    ) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

        private var selectedPosition = RecyclerView.NO_POSITION
        private var selectedDevice: BluetoothDeviceItem? = null

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameTextView: TextView = itemView.findViewById(R.id.deviceName)
            val addressTextView: TextView = itemView.findViewById(R.id.deviceAddress)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item_layout, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val device = devices[position]
            holder.nameTextView.text = device.name
            holder.addressTextView.text = device.address

            val isSelected = position == selectedPosition
            val context = holder.itemView.context

            // Change text color for both name and address
            val textColor = if (isSelected) context.getColor(R.color.white) else context.getColor(R.color.black)
            holder.nameTextView.setTextColor(textColor)
            holder.addressTextView.setTextColor(textColor)

            // Change background color
            holder.itemView.setBackgroundColor(
                if (isSelected) context.getColor(R.color.black)
                else context.getColor(R.color.white)
            )

            holder.itemView.setOnClickListener {
                notifyItemChanged(selectedPosition) // Reset previous selection
                selectedPosition = holder.adapterPosition
                selectedDevice = devices[selectedPosition]
                notifyItemChanged(selectedPosition) // Apply new selection

                selectedDevice?.let { onDeviceSelected(it) }
            }
        }


        override fun getItemCount() = devices.size

        fun getSelectedDevice(): BluetoothDeviceItem? = selectedDevice
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun displayPairedDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            return
        }

        val pairedDevicesList = AppBluetoothManager.bluetoothAdapter?.bondedDevices?.map {
            BluetoothDeviceItem(it.name ?: "Unknown Device", it.address ?: "Unknown Address")
        } ?: emptyList()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
//        selectedDeviceAdapter = BluetoothDeviceAdapter(pairedDevicesList) { selectedDevice -> userDevice = selectedDevice.address }
        selectedDeviceAdapter = BluetoothDeviceAdapter(pairedDevicesList) { selectedDevice ->
            userDevice = selectedDevice.address
            SelectedBluetoothDevice.name = selectedDevice.name
            SelectedBluetoothDevice.address = selectedDevice.address
        }

        recyclerView.adapter = selectedDeviceAdapter
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        unregisterReceiver(bluetoothStateReceiver)
//    }
}
