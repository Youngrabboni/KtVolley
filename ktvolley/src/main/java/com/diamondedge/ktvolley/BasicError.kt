package com.diamondedge.ktvolley

import android.app.Activity
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.NetworkResponse
import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import timber.log.Timber

open class BasicError : Exception, KtVolleyError {

    private val requestBody: Any?
    private val requestHeaders: Map<String, String>?

    final override val url: String

    @JvmOverloads
    constructor(
        volleyError: VolleyError, url: String, requestBody: Any? = null,
        requestHeaders: Map<String, String>? = null
    ) : super(volleyError) {
        this.url = url
        this.requestBody = requestBody
        this.requestHeaders = requestHeaders
    }

    constructor(throwable: Throwable?, url: String = "") : super(throwable) {
        this.url = url
        this.requestBody = null
        this.requestHeaders = null
    }

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

    override val userMessage: String
        get() = message ?: ""

    override fun toString(): String {
        return userMessage
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
        get() = NetworkRequest.createStringResponse(volleyError?.networkResponse)

    val body: String
        get() {
            val s = StringBuilder()
            if (requestBody != null)
                s.append(requestBody).append(" ")
            s.append("Response: ").append(responseBody)
            return s.toString()
        }

    open fun createNoConnectionError(cause: NoConnectionError): BasicError {
        return BasicError(cause)
    }

    fun show(fragment: Fragment) {
        val activity = fragment.activity
        if (activity == null || fragment.isDetached || fragment.isRemoving) {
            return
        }
        show(activity)
    }

    open fun show(activity: Activity) {
        val cause = cause
        if (cause is NoConnectionError)
            show(activity, createNoConnectionError(cause))
        else
            show(activity, this)
    }

    open fun show(activity: Activity, error: BasicError) {
        Timber.i("show($this) $url")
        if (activity.isFinishing || activity.isDestroyed)
            return
        Toast.makeText(activity, error.userMessage, Toast.LENGTH_LONG).show()
    }
}
