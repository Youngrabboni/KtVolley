package com.diamondedge.ktvolley

import android.text.TextUtils
import com.android.volley.NetworkResponse
import com.android.volley.VolleyError
import java.lang.StringBuilder

open class BasicError : Exception, KtVolleyError {

    private val requestBody: Any?
    private val requestHeaders: Map<String, String>?

    final override val url: String

    override val exception: Exception
        get() = this

    override val volleyError: VolleyError?
        get() = cause as? VolleyError

    override val httpResponseCode: Int
        get() {
            var rsp: NetworkResponse? = null
            if (cause is VolleyError)
                rsp = (cause as VolleyError).networkResponse
            return if (rsp == null) 0 else rsp.statusCode
        }

    override val message: String?
        get() = super.message

    override fun toUserMessage(): String? {
        return message
    }

    override fun toLogString(): String {
        val sb = StringBuffer()
        if (httpResponseCode > 0) {
            sb.append("status: ").append(httpResponseCode).append(' ')
        }
        if (!TextUtils.isEmpty(url))
            sb.append("url: ").append(url).append(' ')
        sb.append(cause.toString()).append(' ')
        return sb.toString()
    }

    val headers: String
        get() {
            val s = StringBuilder()
            if (requestHeaders != null)
                s.append(requestHeaders).append(" ")
            val volleyError = volleyError
            if (volleyError?.networkResponse?.headers != null) {
                s.append("Response: ")
                s.append(volleyError.networkResponse.headers)
            }
            return s.toString()
        }

    override val responseBody: String
        get()  {
            return NetworkRequest.createStringResponse(volleyError?.networkResponse) }

    val body: String
        get() {
            val s = StringBuilder()
            if (requestBody != null)
                s.append(requestBody).append(" ")
            s.append("Response: ").append(responseBody)
            return s.toString()
        }

    @JvmOverloads constructor(volleyError: VolleyError, url: String, requestBody: Any? = null,
                              requestHeaders: Map<String, String>? = null) : super(volleyError) {
        this.url = url
        this.requestBody = requestBody
        this.requestHeaders = requestHeaders
    }

    constructor(throwable: Throwable?, url: String = "") : super(throwable) {
        this.url = url
        this.requestBody = null
        this.requestHeaders = null
    }
}
