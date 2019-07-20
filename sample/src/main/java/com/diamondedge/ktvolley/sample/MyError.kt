package com.diamondedge.ktvolley.sample

import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.diamondedge.ktvolley.BasicError

class MyError : BasicError {

    var errorCode: String = ""
    private val requestBody: Any?
    private val requestHeaders: Map<String, String>?

    constructor(
        volleyError: VolleyError, errorCode: String, url: String, requestBody: Any? = null,
        requestHeaders: Map<String, String>? = null
    ) : super(volleyError, url, requestBody, requestHeaders) {
        this.errorCode = errorCode
        this.requestBody = requestBody
        this.requestHeaders = requestHeaders
    }

    constructor(errorCode: String, throwable: Throwable? = null, url: String = "") : super(throwable, url) {
        this.errorCode = errorCode
        this.requestBody = null
        this.requestHeaders = null
    }

    override fun toLogString(): String {
        return errorCode + " " + super.toLogString()
    }

    override val userMessage: String
        get() = String.format("%s (Error %s)", message, errorCode)


    override fun createNoConnectionError(cause: NoConnectionError): BasicError {
        return MyError("1")
    }
}
