package com.diamondedge.ktvolley.sample

import android.content.Context
import com.android.volley.Cache
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BaseHttpStack
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import java.io.File

object MyVolley {
    private val DEFAULT_CACHE_DIR = "volley"

    val requestQueue: RequestQueue

    init {
        requestQueue = createRequestQueue(App.context, null, -1)
    }

    private fun createRequestQueue(context: Context, stack: BaseHttpStack?, maxDiskCacheBytes: Int): RequestQueue {
        var stack = stack
        val cacheDir = File(context.cacheDir, DEFAULT_CACHE_DIR)
        val cache: Cache
        if (maxDiskCacheBytes < 0)
            cache = DiskBasedCache(cacheDir)  // No maximum size specified
        else
            cache = DiskBasedCache(cacheDir, maxDiskCacheBytes) // Disk cache size specified

        if (stack == null) {
            stack = HurlStack(null)
        }
        val network = BasicNetwork(stack)

        val numThreads = 4

        val queue = RequestQueue(cache, network, numThreads)
        queue.start()
        return queue
    }
}