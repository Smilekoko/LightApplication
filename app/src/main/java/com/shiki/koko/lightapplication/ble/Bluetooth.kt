package com.shiki.koko.lightapplication.ble
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService

//检查蓝牙是否打开
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
fun Context.isBluetoothEnabled(): Boolean {
    return getSystemService<BluetoothManager>()?.adapter?.isEnabled == true
}

fun Context.getBluetoothAdapter(): BluetoothAdapter? {
    val bluetoothManager: BluetoothManager? =
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    return bluetoothManager?.adapter
}

//监听蓝牙变动