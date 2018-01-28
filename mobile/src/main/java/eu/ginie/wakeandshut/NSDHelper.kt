package eu.ginie.wakeandshut

import android.app.Activity
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class NSDHelper(val context: Activity, private val onServiceFound: (it: Item) -> Unit) {

    companion object {
        private val TAG = "NSD"
        private val SERVICE_TYPE = "_http._tcp."
        private val SERVICE_NAME = "SASR Daemon"
    }

    private var discoveryListener: DiscoveryListener? = null
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager

    fun startDiscovery() {
        if (discoveryListener == null) {
            discoveryListener = DiscoveryListener()
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            android.os.Handler().postDelayed({ stopDiscovery() }, 10000)
        }
    }

    fun stopDiscovery() {
        if (discoveryListener != null) {
            nsdManager.stopServiceDiscovery(discoveryListener)
            discoveryListener = null
        }
    }

    inner class ResolveListener : NsdManager.ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Log.e(TAG, "Resolve failed: " + errorCode)
        }
        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded: " + serviceInfo)
            val obj = JSONObject()
            serviceInfo.attributes.forEach({
                if (it.value == null) {
                    obj.put(it.key, "")
                } else {
                    obj.put(it.key, String(it.value, StandardCharsets.UTF_8))
                }
            })
            obj.put("host", serviceInfo.host.hostAddress)
            obj.put("port", serviceInfo.port)
            context.runOnUiThread {
                onServiceFound(Item(obj))
            }
        }
    }

    inner class DiscoveryListener : NsdManager.DiscoveryListener {

        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }
        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "service found: " + service)
            if (service.serviceName.contains(SERVICE_NAME)) {
                nsdManager.resolveService(service, ResolveListener())
            }
        }
        override fun onServiceLost(service: NsdServiceInfo) {
            Log.e(TAG, "service lost: " + service)
        }
        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: " + serviceType)
        }
        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: " + errorCode)
            nsdManager.stopServiceDiscovery(this)
        }
        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: " + errorCode)
            nsdManager.stopServiceDiscovery(this)
        }
    }
}