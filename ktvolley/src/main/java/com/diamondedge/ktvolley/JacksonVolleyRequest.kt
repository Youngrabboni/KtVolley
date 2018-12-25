package com.diamondedge.ktvolley

import com.android.volley.*
import com.android.volley.Response.ErrorListener
import com.android.volley.Response.Listener
import com.android.volley.toolbox.HttpHeaderParser
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.Charset

/**
 * Volley adapter for JSON requests that will be parsed into Java objects by Jackson.
 */
class JacksonVolleyRequest<T>(method: Int, url: String, cls: Class<T>, headers: Map<String, String>?,
                              priority: Priority, listener: Listener<T>?, errorListener: ErrorListener?)
    : BaseVolleyRequest<T>(method, url, cls, headers, priority, "application/json", listener, errorListener) {

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

            return objectMapper.writeValueAsString(body).toByteArray()
        } catch (e: Exception) {
            VolleyLog.e(e, "error in JsonVolleyRequest.getBody")
            return null
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun parseNetworkResponse(response: NetworkResponse): Response<T> {
        try {
            val strData = String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
            responseStatusCode = response.statusCode
//            Log.i("JsonVolleyRequest", "parseNetworkResponse: " + strData.length / 1000 + "K " + getUrl())
            val result = if (response.data == null || response.data.isEmpty()) {
                //TODO: should this be null or newInstance()?
                cls.newInstance()   // http code 204 returns no body, so create empty instance
            } else {
                when (cls) {
                    String::class.java -> createStringResponse(response) as T
                    JSONObject::class.java -> JSONObject(createStringResponse(response)) as T
                    JSONArray::class.java -> JSONArray(createStringResponse(response)) as T
                    else -> objectMapper.readValue<T>(strData, cls)
                }
            }

            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response))


        } catch (e: Throwable) {
            return Response.error(ParseError(e))
        }
    }

    companion object {

        private val objectMapper: ObjectMapper = ObjectMapper()

        init {
            //        objectMapper = GedcomJacksonModule.createObjectMapper(Gedcomx.class);
            //        objectMapper = new ObjectMapper();
            //        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        }
   }
}