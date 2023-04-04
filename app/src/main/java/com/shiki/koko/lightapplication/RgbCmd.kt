package com.shiki.koko.lightapplication

import kotlin.experimental.xor

data class RgbCmd(
    val head: Byte,
    val type: Byte,
    val no: Byte,
    val data: ByteArray,
) {

    companion object {
        val redCmd = RgbCmd(0x53, 0x43, 0x01, byteArrayOf(0xFF.toByte(), 0x00, 0x00))
        val greenCmd = RgbCmd(0x53, 0x43, 0x01, byteArrayOf(0x00.toByte(), 0xFF.toByte(), 0x00))
        val blueCmd = RgbCmd(0x53, 0x43, 0x01, byteArrayOf(0x00.toByte(), 0x00, 0xFF.toByte()))
        val yellowCmd = RgbCmd(0x53, 0x43, 0x01, byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0x00))
        val purpleCmd = RgbCmd(0x53, 0x43, 0x01, byteArrayOf(0xFF.toByte(), 0x00, 0xFF.toByte()))
        val chingCmd = RgbCmd(0x53, 0x43, 0x01, byteArrayOf(0x00, 0xFF.toByte(), 0xFF.toByte()))
        val whiteCmd = RgbCmd(0x53, 0x43, 0x01, byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte()))
        val brownCmd = RgbCmd(0x53, 0x43, 0x01, byteArrayOf(0x33, 0x13, 0x03))
    }

    fun getCmdArr(): ByteArray {
        return byteArrayOf(head).plus(type).plus(no).plus(data).plus(getCrc())
    }

    fun getCrc(): Byte {
        var temp = head xor type xor no
        data.forEach {
            temp = temp xor it
        }
        return temp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RgbCmd

        if (head != other.head) return false
        if (type != other.type) return false
        if (no != other.no) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = head
        result = (31 * result + type).toByte()
        result = (31 * result + no).toByte()
        result = (31 * result + data.contentHashCode()).toByte()
        return result.toInt()
    }

}
