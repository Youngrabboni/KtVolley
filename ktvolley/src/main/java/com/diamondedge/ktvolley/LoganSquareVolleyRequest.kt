package com.diamondedge.ktvolley

import com.android.volley.*
import com.android.volley.Response.ErrorListener
import com.android.volley.Response.Listener
import com.android.volley.toolbox.HttpHeaderParser
import com.bluelinelabs.logansquare.LoganSquare
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

/**
 * Volley adapter for JSON requests that will be parsed into Java objects by LoganSquare.
 */
class LoganSquareVolleyRequest<T>(
    method: Int, url: String, cls: Class<T>, val isList: Boolean, headers: Map<String, String>?,
    priority: Priority, listener: Listener<T>?, errorListener: ErrorListener?
) : BaseVolleyRequest<T>(method, url, cls, headers, priority, "application/json", listener, errorListener) {

    private var body: Any? = null

    fun setBody(body: Any?) {
        this.body = body
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray? {
        if (body == null)
            return super.getBody()
        try {
            if (body is String) {
                return (body as String).toByteArray()
            } else if (body is JSONObject) {
                return (body as JSONObject).toString().toByteArray()
            } else if (body is JSONArray) {
                return (body as JSONArray).toString().toByteArray()
            }

            val outputStream = ByteArrayOutputStream()
            LoganSquare.serialize(body, outputStream)
            return outputStream.toByteArray()
        } catch (e: Exception) {
            VolleyLog.e(e, "error in JsonVolleyRequest.getBody")
            return null
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun parseNetworkResponse(response: NetworkResponse): Response<T> {
        try {
            logResponse(response)
            responseStatusCode = response.statusCode
            val result = if (response.data == null || response.data.isEmpty()) {
                //TODO: should this be null or newInstance()?
                cls.newInstance()   // http code 204 returns no body, so create empty instance
            } else {
                when (cls) {
                    String::class.java -> createStringResponse(response) as T
                    JSONObject::class.java -> JSONObject(createStringResponse(response)) as T
                    JSONArray::class.java -> JSONArray(createStringResponse(response)) as T
                    else -> {
                        val inputStream = ByteArrayInputStream(response.data)
                        if (isList)
                            LoganSquare.parseList(inputStream, cls) as T
                        else
                            LoganSquare.parse(inputStream, cls) as T
                    }
                }
            }

            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response))

        } catch (e: Throwable) {
            return Response.error(ParseError(e))
        }
    }
}