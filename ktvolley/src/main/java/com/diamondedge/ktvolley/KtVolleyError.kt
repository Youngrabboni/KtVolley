package com.diamondedge.ktvolley

import com.android.volley.VolleyError

interface KtVolleyError {
    val url: String
    val responseBody: String
    val httpResponseCode: Int
    val exception: Exception
    val volleyError: VolleyError?
    fun toUserMessage(): String?
    fun toLogString(): String
}