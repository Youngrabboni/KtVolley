package com.diamondedge.ktvolley

class Result<out T>(val response: T?, val error: KtVolleyError?) {

    fun isSuccess() = response != null

    fun isError() = error != null

    override fun toString(): String {
        if (isSuccess()) return "response: " + response
        if (isError()) return "error: " + error
        return "KvResult(null)"
    }
}