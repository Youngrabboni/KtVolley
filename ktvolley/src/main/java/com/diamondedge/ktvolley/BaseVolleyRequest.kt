package com.diamondedge.ktvolley

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response.ErrorListener
import com.android.volley.Response.Listener
import com.android.volley.toolbox.HttpHeaderParser
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

/**
 * Volley adapter matching NetworkRequest builder. Must be subclassed to handle various content types and ORM parsing libraries.
 */
abstract class BaseVolleyRequest<T>(method: Int, url: String, protected val cls: Class<T>, private val headers: Map<String, String>?,
                                    private val priority: Priority, private val contentType: String, private val listener: Listener<T>?, errorListener: ErrorListener?)
    : Request<T>(method, url, errorListener) {

    var bodyParams: Map<String, String>? = null
    //    private var params: Map<String, String>?

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

        @Throws(UnsupportedEncodingException::class)
        fun createStringResponse(response: NetworkResponse): String {
            return String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
        }

        // see Request.Method in volley, these are the only ones we care about
        fun getRequestMethodName(method: Int): String {
            when (method) {
                Request.Method.GET -> return "GET"
                Request.Method.POST -> return "POST"
                Request.Method.PUT -> return "PUT"
                Request.Method.DELETE -> return "DELETE"
                else -> return "UNKNOWN: " + method
            }
        }
    }
}