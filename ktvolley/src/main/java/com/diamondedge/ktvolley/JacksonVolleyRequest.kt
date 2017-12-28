package com.diamondedge.ktvolley

import android.util.Log
import com.android.volley.*
import com.android.volley.Response.ErrorListener
import com.android.volley.Response.Listener
import com.android.volley.toolbox.HttpHeaderParser
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
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

    override fun parseNetworkResponse(response: NetworkResponse): Response<T> {
        try {
            val strData = String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
            Log.i("JsonVolleyRequest", "parseNetworkResponse: " + strData.length / 1000 + "K " + getUrl())
            var result: T? = null
            if (response.data == null || response.data.isEmpty()) {   // http code 204 returns no body
                result = null
            } else if (cls == null || cls == String::class.java) {
                result = createStringResponse(response) as T
            } else if (cls == JSONObject::class.java) {
                result = JSONObject(createStringResponse(response)) as T
            } else if (cls == JSONArray::class.java) {
                result = JSONArray(createStringResponse(response)) as T
            } else {
                result = objectMapper.readValue<T>(strData, cls)
            }

            return Response.success(result, HttpHeaderParser.parseCacheHeaders(response))

        } catch (e: UnsupportedEncodingException) {
            return Response.error(ParseError(e))
        } catch (je: JSONException) {
            return Response.error(ParseError(je))
        } catch (e: JsonMappingException) {
            return Response.error(ParseError(e))
        } catch (e: JsonParseException) {
            return Response.error(ParseError(e))
        } catch (e: IOException) {
            return Response.error(ParseError(e))
        } catch (e: Exception) {
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