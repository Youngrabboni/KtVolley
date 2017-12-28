package com.diamondedge.ktvolley

interface ErrorListener {
    /**
     * Callback method that an error has been occurred with the
     * provided error code and optional user-readable message.
     */
    fun onErrorResponse(error: KtVolleyError)
}
