package com.example.smartvest.util.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

private const val TAG = "BleService"
private const val DEVICE_ADDRESS = "RN4870"  //** TODO: Verify name/set through PIC
private const val SCAN_TIMEOUT_PERIOD: Long = 10000  // 10 seconds

private const val UUID_TRANS_UART = "49535343-FE7D-4AE5-8FA9-9FAFD205E455"
private const val UUID_TRANS_UART_RX = "49535343-8841-43F4-A8D4-ECBE34729BB3"
private const val UUID_TRANS_UART_TX = "49535343-1E4D-4BD9-BA61-23C647249616"

@SuppressLint("MissingPermission")
class BleService : Service() {
    private var scanning: Boolean = false
    private var serviceLooper: Looper? = null
    private var serviceHandler: Handler? = null

    private val bluetoothManager: BluetoothManager = getSystemService(BLUETOOTH_SERVICE)
            as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val bleScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
    private val scanFilter: ScanFilter = ScanFilter.Builder()
        .setDeviceAddress(DEVICE_ADDRESS).build()

    private var bleDevice: BluetoothDevice? = null
    private var bleGatt: BluetoothGatt? = null
    private var connectionState: Int = BluetoothProfile.STATE_DISCONNECTED
    private var bleServices: List<BluetoothGattService>? = null

    companion object {
        const val ACTION_GATT_CONNECTED =
            "com.example.smartvest.util.service.BleService.ACTION_GATT_CONNECTED"  //** TODO: Figure out Intent, Action
        const val ACTION_GATT_DISCONNECTED =
            "com.example.smartvest.util.service.BleService.ACTION_GATT_DISCONNECTED"
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            serviceLooper = looper
            serviceHandler = Handler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //** TODO: figure out how threads work, update for BLE
        Toast.makeText(this, "Starting BLE Service", Toast.LENGTH_SHORT).show()
        scan()

        // Restart service if interrupted
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Toast.makeText(this, "Closing BLE Service", Toast.LENGTH_SHORT).show()
        close()  // end GATT connection
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.d(TAG, "Found device: ${result.device.name}")
            bleDevice = result.device
            connect()
        }
    }

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server")
                connectionState = BluetoothProfile.STATE_CONNECTED
                broadcast(ACTION_GATT_CONNECTED)  //** TODO: Figure out Intent, Action

                bleGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server")
                connectionState = BluetoothProfile.STATE_DISCONNECTED
                broadcast(ACTION_GATT_DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")  //** TODO: implement
                bleServices = gatt?.services
            } else {
                Log.w(TAG, "Service discovery failed: $status")
            }
        }
    }

    private fun scan() {
        if (!scanning) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) return  //** TODO: add permission request, callback

            serviceHandler?.postDelayed({
                scanning = false
                bleScanner.stopScan(scanCallback)
                Log.d(TAG, "Scan stopped")
            }, SCAN_TIMEOUT_PERIOD)  // stop scanning after timeout period

            scanning = true
            bleScanner.startScan(listOf(scanFilter), null, scanCallback)
            Log.d(TAG, "Scan started")
        } else {
            scanning = false
            bleScanner.stopScan(scanCallback)
        }
    }

    private fun connect() {
        bleGatt = bleDevice?.connectGatt(
            this,
            false,
            gattCallback
        )
    }

    private fun close() {
        bleGatt?.let { gatt ->
            gatt.close()
            bleGatt = null
        }
    }

    private fun broadcast(action: String) {
        sendBroadcast(Intent(action))
    }
}