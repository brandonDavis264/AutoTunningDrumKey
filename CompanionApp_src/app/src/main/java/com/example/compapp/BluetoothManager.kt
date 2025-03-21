package com.example.compapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket

object AppBluetoothManager {
    var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothSocket: BluetoothSocket? = null
}

object SelectedBluetoothDevice {
    var name: String? = null
    var address: String? = null
}
