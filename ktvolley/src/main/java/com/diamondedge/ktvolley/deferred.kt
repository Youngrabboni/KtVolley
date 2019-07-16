/*
 * Copyright 2016-2017 JetBrains s.r.o.
 * Most of this file is based on code from kotlinx.coroutines.experimental.Deferred.kt
*/
package com.diamondedge.ktvolley

/*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.selects.SelectClause1
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.suspendCoroutine

suspend inline fun <T> bg(crossinline callback: (ResponseListener<T>) -> Unit): KvDeferredCoroutine<T> {
    var job: KvDeferredCoroutine<T>? = null
    job = kvAsync {
        job?.result = try {
            suspendCoroutine { cont ->
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
    }
    return job
}

fun <T> kvAsync(context: CoroutineContext = DefaultDispatcher,
                start: CoroutineStart = CoroutineStart.DEFAULT,
                parent: Job? = null,
                block: suspend CoroutineScope.() -> Unit): KvDeferredCoroutine<T> {
    val newContext = newCoroutineContext(context, parent)
    val coroutine = KvDeferredCoroutine<T>(newContext, active = true)
    coroutine.initParentJob(newContext[Job])
    start(block, coroutine, coroutine)
    return coroutine
}

class KvDeferredCoroutine<T>(parentContext: CoroutineContext, active: Boolean)
    : AbstractCoroutine<Unit>(parentContext, active), Deferred<Result<T>> {

    var result: Result<T>? = null

    override fun getCompleted(): Result<T> {
        return try {
            val state = getCompletedInternal()
            println("getCompletedInternal() state: $state")
            result ?: Result<T>(null, null)
        } catch (e: Exception) {
            println("ERROR: getCompletedInternal(): $e")
            Result<T>(null, e as? KtVolleyError)
        }
    }

    override suspend fun await(): Result<T> {
        awaitInternal()
        return result ?: Result<T>(null, null)
    }

    override val onAwait: SelectClause1<Result<T>>
        get() = this as SelectClause1<Result<T>>
}
*/