package com.example.smartvest.util.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartvest.R
import com.example.smartvest.util.PermissionUtil
import java.util.UUID


private const val TAG = "BleService"
private const val DEVICE_ADDRESS = "FC:0F:E7:BF:DF:62"
private const val DEVICE_NAME = "RN4870-DF62"
private const val SCAN_TIMEOUT_PERIOD: Long = 10000  // 10 seconds
private const val UUID_UART_SERVICE = "49535343-FE7D-4AE5-8FA9-9FAFD205E455"
private const val UUID_UART_CHARACTERISTIC_RX = "49535343-8841-43F4-A8D4-ECBE34729BB3"
private const val UUID_UART_CHARACTERISTIC_TX = "49535343-1E4D-4BD9-BA61-23C647249616"
private const val UUID_UART_CHARACTERISTIC_CTL = "49535343-4C8A-39B3-2F49-511CFF073B7E"
private const val UUID_CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"
private const val EMERGENCY_RESPONSE_CODE = "SOS"

/* TODO: Switch to Foreground Service, Type: Connected Device */

@SuppressLint("MissingPermission")
class BleService : Service() {
    private val permissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE
    )

    private var scanning: Boolean = false
    private var serviceLooper: Looper? = null
    private var serviceHandler: Handler? = null

    private val scanFilter: ScanFilter = ScanFilter.Builder()
        .setDeviceAddress(DEVICE_ADDRESS)
        .setDeviceName(DEVICE_NAME)
        .build()
    private val scanSettings: ScanSettings = ScanSettings.Builder().build()

    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner

    private var bleDevice: BluetoothDevice? = null
    private var bleGatt: BluetoothGatt? = null
    private var bleServices: List<BluetoothGattService>? = null
    private var bleUartService: BluetoothGattService? = null
    private var connectionState: Int = BluetoothProfile.STATE_DISCONNECTED

    companion object {
        const val SERVICE_ID = 1
        const val NOTIFICATION_CHANNEL_ID = "services.BleService"
        const val NOTIFICATION_CHANNEL_NAME = "BleService"

        val permissions = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
    }

    enum class Action {
        READ,
        WRITE,
        REFRESH,
        SET_NOTIFICATION
    }

    enum class Status {
        UNKNOWN,
        DEVICE_NOT_FOUND,
        SCANNING,
        GATT_CONNECTED,
        GATT_DISCONNECTED,
        SERVICES_DISCOVERED,
        UART_SERVICE_DISCOVERED,
        CHARACTERISTIC_READ,
        CHARACTERISTIC_CHANGED,
        EMERGENCY_RESPONSE
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
        Log.d(TAG, "Stopping service")
        close()  // end GATT connection
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            if (result.isConnectable && result.device.name != null) {  /* TODO: figure out why so many disconnects are happening */
                Log.d(TAG, "Found device: ${result.device.name}")
                bleDevice = result.device
                connect()
            }
        }
    }

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to Gatt server")
                connectionState = BluetoothProfile.STATE_CONNECTED
                broadcast(Status.GATT_CONNECTED)
                /* TODO: Handle disconnects/reconnects */

                bleGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from Gatt server")
                connectionState = BluetoothProfile.STATE_DISCONNECTED
                broadcast(Status.GATT_DISCONNECTED)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                bleServices = gatt?.services
                broadcast(Status.SERVICES_DISCOVERED)

                bleUartService = bleServices?.find {
                    it.uuid == UUID.fromString(UUID_UART_SERVICE)
                }
                bleUartService?.let {
                    broadcast(Status.UART_SERVICE_DISCOVERED)
                    enableCharacteristicNotification(
                        it.getCharacteristic(UUID.fromString(UUID_UART_CHARACTERISTIC_TX))
                    )
                    Log.d(TAG, "UART TX characteristic set to notify")
                } ?: run { Log.w(TAG, "UART service not found") }
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
                Log.d(TAG, "Read char: ${characteristic.uuid}, value: $value")
                broadcast(Status.CHARACTERISTIC_READ, characteristic, value)
            } else {
                Log.w(TAG, "Char read failed: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            broadcast(Status.CHARACTERISTIC_CHANGED, characteristic, value)

            /* TODO: Handle characteristic change - figure out encoding */
            if (characteristic.uuid == UUID.fromString(UUID_UART_CHARACTERISTIC_TX)) {
                Log.d(
                    TAG,
                    "Char change: ${characteristic.uuid}, value: ${value.contentToString()}"
                )
                if (value.toString(Charsets.UTF_8) == EMERGENCY_RESPONSE_CODE) {
                    Log.d(TAG, "Received emergency response code: ${value.contentToString()}")
                    broadcast(Status.EMERGENCY_RESPONSE)
                }
            }
        }
    }

    private fun start() {
        Log.d(TAG, "Starting service")

        if (!PermissionUtil.checkPermissionsBackground(this, permissions)) {
            Log.w(TAG, "Missing required permissions, stopping service")
            stopSelf()
            return
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Tracking active")
            .setSmallIcon(R.drawable.ic_launcher_foreground).build()
        startForeground(SERVICE_ID, notification)

        if (bleDevice == null)
            scan()
        else
            connect()
    }

    private fun scan() {
        if (!scanning) {
            serviceHandler?.postDelayed({
                scanning = false
                bleScanner.stopScan(scanCallback)
                Log.d(TAG, "Scan stopped")

                if (bleDevice == null) {
                    Log.w(TAG, "BLE device not found")
                    broadcast(Status.DEVICE_NOT_FOUND)
                    stopSelf()
                }
            }, SCAN_TIMEOUT_PERIOD)  // stop scanning after timeout period

            scanning = true
            bleScanner.startScan(listOf(scanFilter), scanSettings, scanCallback)
            broadcast(Status.SCANNING)
            Log.d(TAG, "Scan started")
        } else {
            scanning = false
            bleScanner.stopScan(scanCallback)
        }
    }

    private fun connect() {
        bleDevice?.let { device ->
            bleGatt = device.connectGatt(
                this,
                true,
                gattCallback
            )
        } ?: run {
            Log.w(TAG, "BLE device not initialized. Rescanning")
            scan()  /* TODO: Check for redundant scans */
        }
    }

    private fun close() {
        bleGatt?.let { gatt ->
            gatt.close()
            bleGatt = null
            Log.d(TAG, "Gatt connection closed")
        }
    }

    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        bleGatt?.readCharacteristic(characteristic)
    }

    private fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
    ) {
        bleGatt?.writeCharacteristic(characteristic, value, writeType)
    }

    private fun enableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        val descriptor: BluetoothGattDescriptor = characteristic
            .getDescriptor(UUID.fromString(UUID_CLIENT_CHARACTERISTIC_CONFIG))

        bleGatt?.writeDescriptor(
            descriptor,
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        )
    }

    private fun disableCharacteristicNotification(characteristic: BluetoothGattCharacteristic) {
        val descriptor: BluetoothGattDescriptor = characteristic
            .getDescriptor(UUID.fromString(UUID_CLIENT_CHARACTERISTIC_CONFIG))

        bleGatt?.writeDescriptor(
            descriptor,
            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        )
    }

    private fun broadcast(
        status: Status,
        characteristic: BluetoothGattCharacteristic? = null,
        msg: ByteArray? = null
    ) {
        val intent = Intent(status.name)
        msg?.let { m ->
                intent.putExtra("uuid", characteristic?.uuid)
                intent.putExtra("msg", m)
        }

        sendBroadcast(intent)
    }
}