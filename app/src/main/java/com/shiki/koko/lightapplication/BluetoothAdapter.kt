package com.shiki.koko.lightapplication

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class BluetoothAdapter : BaseQuickAdapter<BluetoothDevice, BaseViewHolder>(R.layout.item_bluetooth) {
    @SuppressLint("MissingPermission")
    override fun convert(holder: BaseViewHolder, item: BluetoothDevice) {
        holder.setText(R.id.tv_name, item.name ?: item.address)
    }
}