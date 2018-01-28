package eu.ginie.wakeandshut.tasks

import android.os.AsyncTask
import android.util.Log
import java.net.*

class WakeOnLanTask(
        private val host: String,
        private val mac: String,
        private val cb: (ok: Boolean) -> Unit
) : AsyncTask<Unit, Int, Boolean>() {
    override fun doInBackground(vararg params: Unit): Boolean {
        return try {
            // TODO: find proper broadcast address
            val broadcast = host
                    .split(".")
                    .mapIndexed { index, s -> if (index == 3) { "255" } else { s }}
                    .joinToString(".")

            val bytes = getMagicPacket(mac, "")
            val address = InetAddress.getByName(broadcast)
            val packet = DatagramPacket(bytes, bytes.size, address, 9)
            val socket = DatagramSocket()
            socket.send(packet)
            socket.disconnect()
            socket.close()
            true
        } catch (e: Exception) {
            Log.e("WOL", "failed", e)
            false
        }
    }

    override fun onPostExecute(result: Boolean) {
        cb(result)
    }

    private fun getMagicPacket(macStr: String, pass: String): ByteArray {
        val data = ArrayList<Byte>()

        // The Synchronization Stream is defined as 6 bytes of FFh.
        for (i in 1..6) {
            data.add(0xFF.toByte())
        }

        // The Target MAC block contains 16 duplications of the IEEE address of the target, with no breaks or interruptions.
        val macBytes = getMacBytes(macStr)
        for (i in 1..16) {
            data.addAll(macBytes)
        }

        // The Password field is optional, but if present, contains either 4 bytes or 6 bytes
        when {
            pass.isEmpty() -> {
                // no password given
            }
            pass.length == 4 || pass.length == 6 -> {
                pass.forEach { data.add(it.toByte()) }
            }
            else -> {
                throw Exception("Password must be 4 or 6 characters long but was ${pass.length}")
            }
        }

        return data.toByteArray()
    }

    private fun getMacBytes(macStr: String): Array<Byte> {
        val result = macStr
                .split("([:-])".toRegex())
                .filter { !it.isEmpty() }
                .map { Integer.parseInt(it, 16).toByte() }
                .toTypedArray()
        if (result.size != 6) {
            throw IllegalArgumentException("Invalid MAC address.")
        }
        return result
    }
}
