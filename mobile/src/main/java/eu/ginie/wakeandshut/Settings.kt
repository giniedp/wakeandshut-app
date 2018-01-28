package eu.ginie.wakeandshut

import android.content.Context
import android.content.Context.MODE_PRIVATE
import org.json.JSONObject

class Settings(context: Context) {

    private val appSettings = context.applicationContext.getSharedPreferences("AppSettings", MODE_PRIVATE)
    private val hostSettings = context.applicationContext.getSharedPreferences("Hosts", MODE_PRIVATE)

    fun allHosts(): Collection<JSONObject> {
        return hostSettings.all.keys.map { hostData(it) }.mapNotNull { it }
    }

    fun hostData(host: String, data: JSONObject?) {
        if (data == null) {
            hostSettings.edit().remove(host).apply()
        } else {
            hostSettings.edit().putString(host, data.toString(0)).apply()
        }
    }

    fun hostData(host: String): JSONObject? {
        if (hostSettings.contains(host)) {
            return JSONObject(hostSettings.getString(host, "{}"))
        }
        return null
    }
}