package com.diamondedge.ktvolley

import com.android.volley.Response
import kotlin.coroutines.experimental.suspendCoroutine


inline suspend fun <T> sync(crossinline callback: (Response.Listener<T>, ErrorListener) -> Unit) =
        try {
            suspendCoroutine<Result<T>> { cont ->
                callback(Response.Listener<T> { result: T ->
                    cont.resume(Result(result, null))
                }, object : ErrorListener {
                    override fun onErrorResponse(error: KtVolleyError) {
                        cont.resumeWithException(if (error is Exception) error else error.exception)
                    }
                })
            }
        } catch (e: Exception) {
            Result(null, e as? KtVolleyError)
        }
