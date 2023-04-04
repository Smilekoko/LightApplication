package com.shiki.koko.lightapplication

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.shiki.koko.lightapplication.RgbCmd.Companion.blueCmd
import com.shiki.koko.lightapplication.RgbCmd.Companion.brownCmd
import com.shiki.koko.lightapplication.RgbCmd.Companion.chingCmd
import com.shiki.koko.lightapplication.RgbCmd.Companion.greenCmd
import com.shiki.koko.lightapplication.RgbCmd.Companion.purpleCmd
import com.shiki.koko.lightapplication.RgbCmd.Companion.redCmd
import com.shiki.koko.lightapplication.RgbCmd.Companion.whiteCmd
import com.shiki.koko.lightapplication.RgbCmd.Companion.yellowCmd
import com.shiki.koko.lightapplication.ble.*
import com.shiki.koko.lightapplication.databinding.ActivityMainBinding
import com.shiki.koko.lightapplication.permission.IntentLauncher
import com.shiki.koko.lightapplication.permission.PermissionLauncher
import com.shiki.koko.lightapplication.ui.ConnectDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionLauncher: PermissionLauncher
    private lateinit var launcher: IntentLauncher
    private lateinit var rgbBleHelper: RgbBleHelper
    private var rgbDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionLauncher = PermissionLauncher(this)
        launcher = IntentLauncher(this)
        rgbBleHelper = RgbBleHelper(this)
        requestBluetoothPermission()

        initView()
        initListener()

    }

    private fun initListener() {
        binding.tv.setOnClickListener {

            RgbBleHelper.stopScan()
            RgbBleHelper.startScan(object : ScanCallback() {
                @SuppressLint("MissingPermission")
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    val device = result?.device
                    if (device != null) {
                        val name = device.name
                        if (name == "BT_AB0123456789") {
                            rgbDevice = device
                            binding.tv.text = rgbDevice?.name
                            RgbBleHelper.stopScan()
                        }
                    }
                }
            })
        }

        binding.btnConnect.setOnClickListener {
            rgbDevice?.let {
                val flag = rgbBleHelper.connect(it.address)
                Toast.makeText(this, "连接=$flag", Toast.LENGTH_SHORT).show()
            }
        }
        binding.bntSend.setOnClickListener {

            val list = listOf(whiteCmd, blueCmd, greenCmd, purpleCmd, brownCmd, redCmd, yellowCmd, chingCmd)

            lifecycleScope.launch {
                list.forEach {
                    delay(1500)
                    rgbBleHelper.writeBLECharacteristicValue(it.getCmdArr())
                }
            }

        }
        binding.btnRed.setOnClickListener {
            rgbBleHelper.writeBLECharacteristicValue(redCmd.getCmdArr())
        }
        binding.btnGreen.setOnClickListener {
            rgbBleHelper.writeBLECharacteristicValue(greenCmd.getCmdArr())
        }
        binding.btnBlue.setOnClickListener {
            rgbBleHelper.writeBLECharacteristicValue(blueCmd.getCmdArr())
        }
        binding.btnYellow.setOnClickListener {
            rgbBleHelper.writeBLECharacteristicValue(yellowCmd.getCmdArr())
        }
        binding.btnPurple.setOnClickListener {
            rgbBleHelper.writeBLECharacteristicValue(purpleCmd.getCmdArr())
        }
        binding.btnChing.setOnClickListener {
            rgbBleHelper.writeBLECharacteristicValue(chingCmd.getCmdArr())
        }
        binding.btnWhite.setOnClickListener {
            rgbBleHelper.writeBLECharacteristicValue(whiteCmd.getCmdArr())
        }
        binding.btnBrown.setOnClickListener {
            rgbBleHelper.writeBLECharacteristicValue(brownCmd.getCmdArr())
        }
    }

    private fun initView() {
    }


    @SuppressLint("MissingPermission")
    private fun register(device: BluetoothDevice) {
        if (device.name.isNullOrEmpty()) return

    }

    private fun requestBluetoothPermission() {
        permissionLauncher.launch(locationPermission) { callBack ->
            if (callBack.all { it.value }) {
                Toast.makeText(this, "权限通过", Toast.LENGTH_SHORT).show()
                //开启蓝牙
                val permissions =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) sPermissions else permissions
                permissionLauncher.launch(permissions) { callBack1 ->
                    if (callBack1.all { it.value }) {
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        if (!this.isBluetoothEnabled()) {
                            launcher.launch(intent) {}
                        }
                    }
                }
            } else {
                val packageURI: Uri = Uri.parse("package:$packageName")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                ContextCompat.startActivity(this, intent, null)
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        val locationPermission = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        @RequiresApi(Build.VERSION_CODES.S)
        val sPermissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}