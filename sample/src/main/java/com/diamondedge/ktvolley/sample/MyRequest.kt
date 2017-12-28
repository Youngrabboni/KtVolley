package com.diamondedge.ktvolley.sample

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.diamondedge.ktvolley.JacksonVolleyRequest
import com.diamondedge.ktvolley.KtVolleyError
import com.diamondedge.ktvolley.NetworkRequest

class MyRequest<T>(cls: Class<T>) : NetworkRequest<T>(cls) {

    private var errCode: String = ""

    /** overriden so MyRestRequest specific methods could be called after the path() is called
     */
    override fun path(path: String): MyRequest<T> {
        return super.path(path) as MyRequest<T>
    }

    override fun createVolleyRequest(httpVerb: Int, url: String, cls: Class<T>, priority: Request.Priority, contentType: String, headers: MutableMap<String, String>?, params: Map<String, String>?, body: Any?, listener: Response.Listener<T>, errorListener: Response.ErrorListener): Request<T> {
        val request = JacksonVolleyRequest(httpVerb, url, cls, headers, priority, listener, errorListener)
        request.bodyParams = params
        request.setBody(body)
        return request
    }

    override fun createError(ex: Exception, url: String): KtVolleyError {
        return MyError(errCode, ex, url)
    }

    override fun createError(error: VolleyError, url: String, requestBody: Any?, requestHeaders: Map<String, String>?): KtVolleyError {
        return MyError(error, errCode, url, requestBody, requestHeaders)
    }

    fun errorCode(errorCode: String): MyRequest<T> {
        errCode = errorCode
        return this
    }

    companion object {
        inline fun <reified T> create(): MyRequest<T> {
            return MyRequest(T::class.java)
        }
    }
}