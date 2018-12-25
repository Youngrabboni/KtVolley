package com.diamondedge.ktvolley

class Result<out T>(val response: T?, val error: KtVolleyError?, val statusCode: Int = 0) {

    fun isSuccess() = error == null

    fun isError() = error != null

    override fun toString(): String {
        if (isSuccess()) return "response: $response"
        return "error: $error"
    }
}
