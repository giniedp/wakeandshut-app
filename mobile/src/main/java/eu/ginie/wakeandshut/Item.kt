package eu.ginie.wakeandshut

import eu.ginie.wakeandshut.tasks.HttpRequestTask
import eu.ginie.wakeandshut.tasks.WakeOnLanTask
import org.json.JSONObject

class Item(val json: JSONObject) {
    val host: String by lazy { json.optString("host", "") }
    val port: Int by lazy { json.optInt("port", 0) }
    val name: String by lazy { json.optString("name", "") }
    val os: String by lazy { json.optString("os", "") }
    val arch: String by lazy { json.optString("arch", "") }
    val ssl: Boolean by lazy { json.optString("ssl", "false") == "true" }
    val mac: String by lazy { json.optString("mac", "") }
    val protected: Boolean by lazy { json.optString("protected", "false") == "true" }

    var loading: Boolean = false
    var alive: Boolean = false

    var url: String = ""
        get() {
            val protocol = if (ssl) "https" else "http"
            return "$protocol://$host:$port"
        }

    fun sendPing(pass: String, cb: (Int) -> Unit) {
        sendRequest(pass, "GET", "$url/status", cb)
    }

    fun sendHibernate(pass: String, cb: (Int) -> Unit) {
        sendRequest(pass, "POST", "$url/hibernate", cb)
    }

    fun sendReboot(pass: String, cb: (Int) -> Unit) {
        sendRequest(pass, "POST", "$url/reboot", cb)
    }

    fun sendShutdown(pass: String, cb: (Int) -> Unit) {
        sendRequest(pass, "POST", "$url/shutdown", cb)
    }

    private fun sendRequest(pass: String, method: String, url: String, cb: (Int) -> Unit) {
        loading = true
        HttpRequestTask(method, url, pass, { res ->
            loading = false
            alive = res.err == null
            cb(res.code)
        }).execute()
    }

    fun sendWakeup(fn: (ok: Boolean) -> Unit) {
        loading = true
        WakeOnLanTask(host, mac, { ok ->
            loading = false
            fn(ok)
        }).execute()
    }
}
