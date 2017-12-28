package com.diamondedge.ktvolley

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

/**
 */
abstract class NetworkRequest<T>(private val cls: Class<T>) : Response.Listener<T>, Response.ErrorListener {

    private var path: String = ""
    protected var headers: MutableMap<String, String>? = null
    private var queryParams: MutableMap<String, String>? = null
    private var bodyParams: MutableMap<String, String>? = null
    private var jsonBodyAttributes: MutableMap<String, Any>? = null
    protected val acceptType = JSON_MEDIA_TYPE
    private var contentType = JSON_MEDIA_TYPE
    private var timeout = -1
    private var maxRetryCount = -1
    private var retryPolicy: RetryPolicy? = null
    private var useCache = true
    private var tag: String? = null
    private var listener: Response.Listener<T>? = null
    private var errorListener: ErrorListener? = null
    private var body: Any? = null

    private var priority = Request.Priority.HIGH

    protected val url: String
        get() {
            if (queryParams != null && queryParams!!.isNotEmpty()) {
                val s = StringBuilder(path)
                s.append("?")
                var isFirst = true
                for ((key, value) in queryParams!!) {
                    if (!isFirst)
                        s.append("&")
                    s.append(key).append("=").append(value)
                    isFirst = false
                }
                return s.toString()
            }
            return path
        }

/*
    internal val bodyParamsEncoded: String
        get() {
            val result = StringBuilder()
            for ((key, value) in bodyParams!!) {
                val encodedName = StringUtil.encode(key, "UTF-8")
                val encodedValue = if (value != null) StringUtil.encode(value, "UTF-8") else ""
                if (result.length > 0)
                    result.append(PARAMETER_SEPARATOR)
                result.append(encodedName)
                result.append(NAME_VALUE_SEPARATOR)
                result.append(encodedValue)
            }
            return result.toString()
        }
*/

    open fun path(path: String): NetworkRequest<T> {
        this.path = path
        return this
    }

    fun priority(priority: Request.Priority): NetworkRequest<T> {
        this.priority = priority
        return this
    }

    fun useCache(shouldCache: Boolean): NetworkRequest<T> {
        this.useCache = shouldCache
        return this
    }

    fun header(name: String, value: String): NetworkRequest<T> {
        if (headers == null) {
            headers = HashMap()
        }
        headers?.put(name, value)
        return this
    }

    fun queryParam(name: String, value: String): NetworkRequest<T> {
        if (queryParams == null) {
            queryParams = HashMap()
        }
        queryParams?.put(name, value)
        return this
    }

    fun bodyParam(name: String, value: String): NetworkRequest<T> {
        if (bodyParams == null)
            bodyParams = HashMap()
        bodyParams?.put(name, value)
        return this
    }

    fun jsonBody(name: String, value: Any): NetworkRequest<T> {
        if (jsonBodyAttributes == null)
            jsonBodyAttributes = HashMap()
        jsonBodyAttributes?.put(name, value)
        return this
    }

    fun session(sessionId: String): NetworkRequest<T> {
        return header("Authorization", "Bearer " + sessionId)
    }

    fun accept(vararg acceptTypes: String): NetworkRequest<T> {
        val acceptType = StringBuilder()
        for (type in acceptTypes) {
            if (acceptType.isNotEmpty())
                acceptType.append(";")
            acceptType.append(type)
        }
        return header("Accept", acceptType.toString())
    }

    fun type(contentType: String): NetworkRequest<T> {
        this.contentType = contentType
        return this
    }

    fun getContentType(): String {
        return if (jsonBodyAttributes != null) {
            JSON_MEDIA_TYPE
        } else {
            contentType
        }
    }

    fun getHeader(name: String): String? {
        return headers?.get(name)
    }

    fun hasHeader(name: String): Boolean {
        return headers?.containsKey(name) ?: false
    }

    fun tag(tag: String): NetworkRequest<T> {
        this.tag = tag
        return this
    }

    fun timeout(msTimeout: Int): NetworkRequest<T> {
        timeout = msTimeout
        return this
    }

    fun retries(maxRetryCount: Int): NetworkRequest<T> {
        this.maxRetryCount = maxRetryCount
        return this
    }

    fun retryPolicy(policy: RetryPolicy): NetworkRequest<T> {
        this.retryPolicy = policy
        return this
    }

    fun getBody(): Any? {
        if (body != null)
            return body
        if (jsonBodyAttributes != null) {
            try {
                val json = JSONObject()
                for ((key, value) in jsonBodyAttributes!!) {
                    json.put(key, value)
                }
                return json
            } catch (ex: Exception) {
                errorListener?.onErrorResponse(createError(ex, url))
            }

        }
        return null
    }

    abstract fun createError(ex: Exception, url: String): KtVolleyError
    abstract fun createError(error: VolleyError, url: String, requestBody: Any?, requestHeaders: Map<String, String>?): KtVolleyError

    @JvmOverloads
    fun body(body: Any, contentType: String? = null): NetworkRequest<T> {
        if (contentType != null) {
            this.contentType = contentType
        }
        this.body = body
        return this
    }

    override fun onResponse(response: T) {
        listener?.onResponse(response)
        listener = null
        errorListener = null
    }

    override fun onErrorResponse(error: VolleyError) {
        if (errorListener != null) {
            var body = getBody()
            if (bodyParams != null)
                body = if (body == null) bodyParams.toString() else body.toString() + " params: " + bodyParams.toString()
            errorListener?.onErrorResponse(createError(error, url, body, headers))
        }
        listener = null
        errorListener = null
    }

    protected fun createRequest(httpVerb: Int, listener: Response.Listener<T>, errorListener: ErrorListener): Request<T> {
        this.listener = listener
        this.errorListener = errorListener
        val request = createVolleyRequest(httpVerb, url, cls, priority, getContentType(), headers, bodyParams, getBody(), this, this)
        request.setShouldCache(useCache)
        if (tag != null)
            request.setTag(tag)
        if (retryPolicy == null && (timeout >= 0 || maxRetryCount >= 0)) {
            var timeoutVal = DefaultRetryPolicy.DEFAULT_TIMEOUT_MS;
            var retryCount = DefaultRetryPolicy.DEFAULT_MAX_RETRIES
            if (timeout >= 0)
                timeoutVal = timeout
            if (maxRetryCount >= 0)
                retryCount = maxRetryCount
            retryPolicy = DefaultRetryPolicy(timeoutVal, retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        }
        if (retryPolicy != null)
            request.setRetryPolicy(retryPolicy)
        return request
    }

    abstract fun createVolleyRequest(httpVerb: Int, url: String, cls: Class<T>, priority: Request.Priority, contentType: String, headers: MutableMap<String, String>?, params: Map<String, String>?, body: Any?, listener: Response.Listener<T>, errorListener: Response.ErrorListener): Request<T>

    operator fun get(listener: Response.Listener<T>, errorListener: ErrorListener): Request<T> {
        if (!hasHeader("Accept")) {
            header("Accept", acceptType)
        }
        return createRequest(Request.Method.GET, listener, errorListener)
    }

    fun post(listener: Response.Listener<T>, errorListener: ErrorListener): Request<T> {
        return createRequest(Request.Method.POST, listener, errorListener)
    }

    fun put(listener: Response.Listener<T>, errorListener: ErrorListener): Request<T> {
        return createRequest(Request.Method.PUT, listener, errorListener)
    }

    fun delete(listener: Response.Listener<T>, errorListener: ErrorListener): Request<T> {
        return createRequest(Request.Method.DELETE, listener, errorListener)
    }

    companion object {
        val TAG = "NetworkRequest"
        private val JSON_MEDIA_TYPE = "application/json"

        @Throws(UnsupportedEncodingException::class)
        fun createStringResponse(response: NetworkResponse?): String {
            if (response?.data == null)
                return ""
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
