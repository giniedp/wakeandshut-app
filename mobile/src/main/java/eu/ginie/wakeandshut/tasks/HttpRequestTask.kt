package eu.ginie.wakeandshut.tasks

import android.os.AsyncTask
import java.net.HttpURLConnection
import java.net.URL

class HttpRequestTask(
        private val method: String,
        private val url: String,
        private val pass: String,
        private val onComplete: (res: HttpRequestTask.Response) -> Unit
) : AsyncTask<Unit, Int, HttpRequestTask.Response>() {

    override fun doInBackground(vararg params: Unit): HttpRequestTask.Response {
        val url = URL(url)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 2500
        connection.readTimeout = 2500
        return try {
            connection.connect()
            Response(connection.responseCode, null)
        } catch (e: Exception) {
            Response(0, e)
        }
    }

    override fun onPostExecute(result: HttpRequestTask.Response) {
        onComplete(result)
    }

    data class Response(val code: Int, val err: Exception?)
}