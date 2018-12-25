package com.diamondedge.ktvolley.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.diamondedge.ktvolley.ResponseListener
import com.diamondedge.ktvolley.bg
import com.diamondedge.ktvolley.sync
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<BouncingBall>(R.id.anim_view).setOnClickListener {
            runTest()
        }
    }

    override fun onStart() {
        super.onStart()
        runTest()
    }

    private fun runTest() {
        testSync {
            testParallel()
        }
    }

    fun log(msg: String) {
        Log.d(TAG, msg)
    }

    fun testParallel() {
        val tag = "testParallel"
        log("------------------------------------- $tag: start ----------------------------------------")
        launch {
            val start = System.nanoTime()
            val job1 = bg<String> { listener ->
                reddit(tag, 100, listener)
            }
            val job2 = bg<String> { listener ->
                wikipedia(tag, listener)
            }
            val job3 = bg<String> { listener ->
                redditError(tag) { result ->
                    if (result.isError()) {
                        println("$tag Error: " + result.error?.toLogString())
                    }
                    listener.invoke(result)
                }
            }
            val job4 = bg<String> { listener ->
                reddit(tag, 90, listener)
            }
            val job5 = bg<String> { listener ->
                MyVolley.requestQueue.add(MyRequest.create<String>().path("https://www.reddit.com/top.json?limit=50").get(listener))
            }
//            job1.cancel()
            log("$tag job1.await() " + job1.await())
            log("$tag job2.await() " + job2.await())
            log("$tag job3.await() " + job3.await())
            log("$tag job4.await() " + job4.await())
            log("$tag job5.await() " + job5.await())
            val end = System.nanoTime()
            val time = (end - start) / 1000_000f
            log(String.format("$tag timeElapsed %.3f ms thread: %s", time, Thread.currentThread().name))
            log("------------------------------------- $tag ----------------------------------------")
        }
    }

    fun testSync(runAfter: (() -> Unit)?) {
        val tag = "testSync"
        log("------------------------------------- $tag: start ----------------------------------------")
        launch {
            val response1 = sync<String> { listener ->
                wikipedia(tag, listener)
            }
            log("$tag: after wiki")
            val response2 = sync<String> { listener ->
                reddit(tag, listener)
            }
            log("$tag: after reddit")
            log("$tag 1: $response1")
            log("$tag 2: $response2")
            log("------------------------------------- $tag ----------------------------------------")
            runAfter?.invoke()
        }
    }

    fun testAsync() {
        val tag = "testAsync"
        log("------------------------------------- $tag: start ----------------------------------------")
        runBlocking {
            val start = System.nanoTime()
            val one = async {
                delay(1000)
                wikipedia(tag) { result ->
                    if (result.isSuccess())
                        println(result.response)
                    else
                        println("$tag error: " + result.error)
                }
            }
            println("The answer is ${one.await()}")

            val end = System.nanoTime()
            val time = (end - start) / 1000_000f
            log(String.format("$tag timeElapsed %.3f ms thread: %s", time, Thread.currentThread().name))
            log("------------------------------------- $tag ----------------------------------------")
        }
    }

    private fun redditError(tag: String, listener: ResponseListener<String>) {
        val url = "https://www.reddit.com/bogus.json?limit=10"
        log("$tag reddit: url: $url")
        val request = MyRequest.create<String>()
                .path(url)
                .errorCode("12")
                .get(listener)
        MyVolley.requestQueue.add(request)
    }

    private fun reddit(tag: String, listener: ResponseListener<String>) {
        reddit(tag, 10, listener)
    }

    private fun reddit(tag: String, limit: Int, listener: ResponseListener<String>) {
        val start = System.nanoTime()
        val url = "https://www.reddit.com/top.json?limit=$limit"
        log("$tag reddit: url: $url")
        val request = MyRequest.create<String>()
                .path(url)
                .errorCode("12")
                .useCache(false)
                .get { response ->
                    val end = System.nanoTime()
                    val time = (end - start) / 1000_000f
                    log(String.format("%s reddit timeElapsed %.3f ms thread: %s", tag, time, Thread.currentThread().name))
                    listener.invoke(response)
                }
        MyVolley.requestQueue.add(request)
    }

    private fun wikipedia(tag: String, listener: ResponseListener<String>) {
        val url = "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=top"
        log("$tag wikipedia: url: $url")
        val request = MyRequest.create<String>()
                .path(url)
                .errorCode("12")
                .useCache(false)
                .get(listener)
        MyVolley.requestQueue.add(request)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
