package com.shiki.koko.lightapplication.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.shiki.koko.lightapplication.HexData
import java.util.*
import kotlin.concurrent.timerTask

class RgbBleHelper(val context: Context) {

    private val TAG = this::class.java.name

    val ecServerId = "0000FFF0-0000-1000-8000-00805F9B34FB"
    val ecWriteCharacteristicId = "0000FFF2-0000-1000-8000-00805F9B34FB"
    val ecReadCharacteristicId = "0000FFF1-0000-1000-8000-00805F9B34FB"

    //ble服务可用
    var serviceReady = false

    //计时器
    private val mTimer = Timer()

    private var bluetoothAdapter: BluetoothAdapter? = null

    private var bluetoothGatt: BluetoothGatt? = null

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        //连接状态改变
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.e(TAG, "onConnectionStateChange status=$status newState=$newState")
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mTimer.schedule(timerTask {
                    bluetoothGatt?.discoverServices() //扫描服务
                }, 500)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            }
        }

        //BLE设备上报GATT Server 可用
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.e(TAG, "onServicesDiscovered status=$status")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                serviceReady = true
                mTimer.schedule(timerTask {
                    notifyBLECharacteristicValueChange()
                }, 500)
            }
        }

        //特征读取回调
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            Log.e(TAG, "onCharacteristicRead status=$status")
        }

        //写入特征回调
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val byte = characteristic?.value ?: byteArrayOf()
                val hex = HexData.hexToString(byte)
                println(hex)
            }
            Log.e(TAG, "onCharacteristicWrite  $status")
        }

        //特征变动
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            val bytes = HexData.hexToString(value)
            Log.e(TAG, "onCharacteristicChanged value=$bytes")
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int, value: ByteArray) {
            Log.e(TAG, "onDescriptorRead")
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            Log.e(TAG, "onCharacteristicWrite  $status")
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }
    }

    init {
        bluetoothAdapter = context.getBluetoothAdapter()
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
        }
    }


    /**
     * 通过地址连接
     */
    @SuppressLint("MissingPermission")
    fun connect(address: String): Boolean {
        var device: BluetoothDevice? = null
        bluetoothAdapter?.let { adapter ->
            try {
                device = bluetoothAdapter?.getRemoteDevice(address)
            } catch (e: Exception) {
                Log.w(TAG, "Device not found with provided address.")
                return false
            }
        }
        if (device != null) {
            bluetoothGatt = device?.connectGatt(context, false, bluetoothGattCallback)
            Log.e(TAG, "bluetoothGatt 创建成功")
        }
        return device != null
    }

    @SuppressLint("MissingPermission")
    private fun notifyBLECharacteristicValueChange() {
        val service = bluetoothGatt?.getService(UUID.fromString(ecServerId))
        val characteristicRead = service?.getCharacteristic(UUID.fromString(ecReadCharacteristicId))
        val res = bluetoothGatt?.setCharacteristicNotification(characteristicRead, true)
        if (characteristicRead != null) {
            for (descriptor in characteristicRead.descriptors) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bluetoothGatt?.writeDescriptor(
                        descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE,
                    )
                } else {
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    bluetoothGatt?.writeDescriptor(descriptor)
                }
            }
        }
        Thread {
            Thread.sleep(300)
            bluetoothGatt?.requestMtu(500)
        }.start()
    }

    /**
     * 写入数据
     */
    @SuppressLint("MissingPermission")
    fun writeBLECharacteristicValue(data: ByteArray) {
        if (serviceReady) {
            val service = bluetoothGatt?.getService(UUID.fromString(ecServerId))
            val characteristicWrite = service?.getCharacteristic(UUID.fromString(ecWriteCharacteristicId))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                characteristicWrite?.let { bluetoothGatt?.writeCharacteristic(it, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE) }
            } else {
                characteristicWrite?.value = data
                characteristicWrite?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                bluetoothGatt?.writeCharacteristic(characteristicWrite)
            }
        } else {
            Log.e(TAG, "GATT Server 不可用")
        }
    }

    companion object {
        //扫描标志
        private var isScanning = false

        //扫描结果回调
        private var scanCallback: ScanCallback? = null

        //开始扫描
        @SuppressLint("MissingPermission")
        fun startScan(scanCallback: ScanCallback) {
            isScanning = true
            RgbBleHelper.scanCallback = scanCallback
            try {
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                // Android5.0新增的扫描API，扫描返回的结果更友好，比如BLE广播数据以前是byte[] scanRecord，而新API帮我们解析成ScanRecord类
                bluetoothLeScanner.startScan(scanCallback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //结束扫描
        @SuppressLint("MissingPermission")
        fun stopScan() {
            try {
                if (scanCallback == null) return
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                bluetoothLeScanner.stopScan(scanCallback) //停止扫描
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isScanning = false
        }
    }
}