package com.example.smartvest.util.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.smartvest.R
import com.example.smartvest.data.SettingsStore

private const val TAG = "BleService"
private const val DEVICE_ADDRESS = "FC:0F:E7:BF:DF:62"
private const val SCAN_TIMEOUT_PERIOD: Long = 10000  // 10 seconds
private const val UUID_TRANS_UART = "49535343-FE7D-4AE5-8FA9-9FAFD205E455"
private const val UUID_TRANS_UART_RX = "49535343-8841-43F4-A8D4-ECBE34729BB3"
private const val UUID_TRANS_UART_TX = "49535343-1E4D-4BD9-BA61-23C647249616"

/* TODO: Switch to Foreground Service, Type: Connected Device */

@SuppressLint("MissingPermission")
class BleService : Service() {
    private var scanning: Boolean = false
    private var serviceLooper: Looper? = null
    private var serviceHandler: Handler? = null

    private val scanFilter: ScanFilter = ScanFilter.Builder()
        .setDeviceAddress(DEVICE_ADDRESS).build()
    private val scanSettings: ScanSettings = ScanSettings.Builder().build()

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner

    private var bleDevice: BluetoothDevice? = null
    private var bleGatt: BluetoothGatt? = null
    private var bleServices: List<BluetoothGattService>? = null
    private var connectionState: Int = BluetoothProfile.STATE_DISCONNECTED

    companion object {
        const val SERVICE_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "services.BleService"
        const val NOTIFICATION_CHANNEL_NAME = "BleService"
    }

    enum class Actions {
        READ,
        WRITE,
        REFRESH,
        SET_NOTIFICATION
    }

    enum class Broadcasts {
        GATT_CONNECTED,
        GATT_DISCONNECTED,
        SERVICES_DISCOVERED,
        CHARACTERISTIC_READ,
        CHARACTERISTIC_CHANGED
    }

    override fun onCreate() {
        bluetoothManager = this.getSystemService(BLUETOOTH_SERVICE)
                as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bleScanner = bluetoothAdapter.bluetoothLeScanner

        HandlerThread("BleHandlerThread", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            serviceLooper = looper
            serviceHandler = Handler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        start()

        // Restart service if interrupted
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "Stopping BLE Service")
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
                broadcast(Broadcasts.GATT_CONNECTED.name)

                bleGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server")
                connectionState = BluetoothProfile.STATE_DISCONNECTED
                broadcast(Broadcasts.GATT_DISCONNECTED.name)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                bleServices = gatt?.services
                broadcast(Broadcasts.SERVICES_DISCOVERED.name)
            } else {
                Log.w(TAG, "Service discovery failed: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                /* TODO: Handle characteristic read success */
                Log.d(TAG, "Read characteristic: ${characteristic.uuid}, value: $value")
                broadcast(Broadcasts.CHARACTERISTIC_READ.name, characteristic, value)
            } else {
                Log.w(TAG, "Characteristic read failed: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            /* TODO: Handle characteristic change */
            Log.d(TAG, "Characteristic changed: ${characteristic.uuid}, value: $value")
            broadcast(Broadcasts.CHARACTERISTIC_CHANGED.name, characteristic, value)
        }
    }

    private fun start() {
        Log.d(TAG, "Starting BLE Service")

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Tracking active")
            .setSmallIcon(R.drawable.ic_launcher_foreground).build()
        startForeground(SERVICE_ID, notification)

        scan()
    }

    private fun scan() {
        if (!scanning) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) return  /* TODO: add permission request, callback */

            serviceHandler?.postDelayed({
                scanning = false
                bleScanner.stopScan(scanCallback)
                Log.d(TAG, "Scan stopped")
            }, SCAN_TIMEOUT_PERIOD)  // stop scanning after timeout period

            scanning = true
            bleScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
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

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bleGatt?.readCharacteristic(characteristic) ?: run {
            Log.w(TAG, "Gatt Server not initialized")
        }
    }

    private fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) {
        bleGatt?.writeCharacteristic(characteristic, value, writeType) ?: run {
            Log.w(TAG, "Gatt Server not initialized")
        }
    }

    private fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ) {
        bleGatt?.setCharacteristicNotification(characteristic, enable) ?: run {
            Log.w(TAG, "Gatt Server not initialized")
        }
    }

    private fun broadcast(action: String) {
        sendBroadcast(Intent(action))
    }

    private fun broadcast(
        action: String,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val intent = Intent(action)
        intent.putExtra("uuid", characteristic.uuid.toString())
        intent.putExtra("value", value)
        sendBroadcast(intent)
    }
}