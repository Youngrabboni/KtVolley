package com.diamondedge.ktvolley.sample

import android.app.Activity
import android.app.Fragment
import android.util.Log
import android.widget.Toast
import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.diamondedge.ktvolley.BasicError

class MyError : BasicError {

    var errorCode: String = ""
    private val requestBody: Any?
    private val requestHeaders: Map<String, String>?

    constructor(volleyError: VolleyError, errorCode: String, url: String, requestBody: Any? = null,
                              requestHeaders: Map<String, String>? = null) : super(volleyError, url, requestBody, requestHeaders) {
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

    override fun toString(): String {
        return String.format("%s (Error %s)", message, errorCode)
    }

    fun show(fragment: Fragment) {
        if (fragment.activity == null || fragment.isDetached || fragment.isRemoving) {
            return
        }
        show(fragment.activity)
    }

    fun show(activity: Activity) {
        Log.i(TAG, "show($errorCode) $url")
        if (cause is NoConnectionError) {
            show(activity, NoConnection)
        } else {
            show(activity, this)
        }
    }

    companion object {
        private val TAG = "MyError"
        var Generic = MyError("0")
        var NoConnection = MyError("1")

        private fun show(activity: Activity, error: MyError) {
            if (activity.isFinishing || activity.isDestroyed)
                return
            Toast.makeText(activity, error.toString(), Toast.LENGTH_LONG).show()
        }

    }
}
