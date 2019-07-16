package com.diamondedge.ktvolley

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend inline fun <T> sync(crossinline callback: (ResponseListener<T>) -> Unit) =
        try {
            suspendCoroutine<Result<T>> { cont ->
                callback { result ->
                    if (result.isSuccess())
                        cont.resume(result)
                    else {
                        val exception = if (result.error is Exception) result.error else result.error?.exception
                        if (exception != null)
                            cont.resumeWithException(exception)
                        else
                            cont.resume(result)
                    }
                }
            }
        } catch (e: Exception) {
            Result(null, e as? KtVolleyError)
        }

