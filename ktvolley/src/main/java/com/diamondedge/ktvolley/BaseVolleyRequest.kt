package com.diamondedge.ktvolley

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response.ErrorListener
import com.android.volley.Response.Listener
import com.android.volley.toolbox.HttpHeaderParser
import timber.log.Timber
import java.nio.charset.Charset

/**
 * Volley adapter matching NetworkRequest builder. Must be subclassed to handle various content types and ORM parsing libraries.
 */
abstract class BaseVolleyRequest<T>(
    method: Int,
    url: String,
    protected val cls: Class<T>,
    private val headers: Map<String, String>?,
    private val priority: Priority,
    private val contentType: String,
    private val listener: Listener<T>?,
    errorListener: ErrorListener?
) : Request<T>(method, url, errorListener) {

    private var logTag: String? = null
    private var logName: String? = null

    var bodyParams: Map<String, String>? = null
    //    private var params: Map<String, String>?
    var responseStatusCode = 0

    @Throws(AuthFailureError::class)
    override fun getHeaders(): Map<String, String> {
        return headers ?: super.getHeaders()
    }

    override fun deliverResponse(response: T) {
        listener?.onResponse(response)
    }

    override fun getBodyContentType(): String? {
        return contentType
    }

    fun getParam(key: String): String? {
        return bodyParams?.get(key)
    }

    @Throws(AuthFailureError::class)
    override fun getParams(): Map<String, String>? {
        return bodyParams
    }

    override fun getPriority(): Priority {
        return priority
    }

    fun setLogging(tag: String, name: String?) {
        this.logTag = tag
        this.logName = name
    }

    fun logResponse(networkResponse: NetworkResponse) {
        logTag?.let {
            logNetworkResponse(it, logName, url, networkResponse)
        }
    }

    override fun toString(): String {
        return "${javaClass.simpleName}{ ${getRequestMethodName(method)} $url" +
                ", cls=" + cls +
                ", headers=" + headers +
                ", bodyParams=" + params +
                ", body=" + body +
                ", contentType='" + contentType + '\'' +
                ", priority=" + priority +
                '}'
    }

    companion object {

        fun createStringResponse(response: NetworkResponse): String {
            return String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
        }

        private fun getRequestMethodName(method: Int): String {
            // see Request.Method in volley, these are the only ones we care about
            return when (method) {
                Method.GET -> "GET"
                Method.POST -> "POST"
                Method.PUT -> "PUT"
                Method.DELETE -> "DELETE"
                else -> "UNKNOWN: $method"
            }
        }

        fun logNetworkResponse(tag: String, name: String?, url: String, networkResponse: NetworkResponse) {
            try {
                val response = String(networkResponse.data, Charset.forName("UTF-8"))
                Timber.tag(tag)
                    .i("%sresponse(%d) %s", if (name == null) "" else "$name ", networkResponse.statusCode, url)
                val headers = networkResponse.headers
                for (key in headers.keys) {
                    Timber.tag(tag).v("  %s: %s", key, headers[key])
                }
                Timber.tag(tag).v(response)
            } catch (ex: Exception) {
            }
        }
    }
}