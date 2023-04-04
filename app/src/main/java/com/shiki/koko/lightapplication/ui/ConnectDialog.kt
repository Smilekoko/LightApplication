package com.shiki.koko.lightapplication.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import com.shiki.koko.lightapplication.R
import com.shiki.koko.lightapplication.databinding.ConntionDialogBinding
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ConnectDialog(context: Context) : Dialog(context), CoroutineScope {
    private val job = SupervisorJob()
    private lateinit var binding: ConntionDialogBinding
    var retryAction: (() -> Unit)? = null
    var cancelAction: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ConntionDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListener()
    }


    override fun show() {
        super.show()
        inConnection()
    }


    //初始化监听器
    private fun initListener() {
        binding.btRetry.setOnClickListener {
            inConnection()
            retryAction?.invoke()
        }
        setOnDismissListener {
            job.cancel()
            cancelAction?.invoke()
        }
    }


    private fun inConnection() {
        bluetoothInConnectionUI()
    }

    //蓝牙连接中UI
    private fun bluetoothInConnectionUI() {
        binding.tvBluetooth.text = "正在连接蓝牙中，请等待"
//        GlideApp.with(context).asGif().load(R.drawable.loading).into(binding.ivBluetoothConn)
        binding.ivBluetoothConn.visibility = View.VISIBLE
        binding.btRetry.visibility = View.GONE
    }


    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main
}