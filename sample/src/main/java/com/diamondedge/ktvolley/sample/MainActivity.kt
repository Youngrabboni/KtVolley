package com.diamondedge.ktvolley.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.Response
import com.diamondedge.ktvolley.ErrorListener
import com.diamondedge.ktvolley.KtVolleyError
import com.diamondedge.ktvolley.bg
import com.diamondedge.ktvolley.sync
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*


class MainActivity : AppCompatActivity() {

//    lateinit var textView: TextView

    private val random = Random()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        textView = findViewById(R.id.text)
    }

    override fun onStart() {
        super.onStart()
        testParallel()
//        testSync()
    }

    fun log(msg: String) {
        Log.d(TAG, msg)
//        textView.text = StringBuilder(textView.text).append("\n").append(msg).toString()
    }

    fun testParallel() {
        log("testParallel: start")
        launch {
            val start = System.nanoTime()
            val job1 = bg<String> { listener, errListener ->
                reddit(100, listener, errListener)
            }
            val job2 = bg<String> { listener, errListener ->
                wikipedia(listener, errListener)
            }
            val job3 = bg<String> { listener, errListener ->
                redditError(listener, object : ErrorListener {
                    override fun onErrorResponse(error: KtVolleyError) {
                        println("Error: " + error.toLogString())
                        errListener.onErrorResponse(error)
                    }})
            }
            val job4 = bg<String> { listener, errListener ->
                reddit(90, listener, errListener)
            }
            val job5 = bg<String> { listener, errListener ->
                MyVolley.requestQueue.add( MyRequest.create<String>().path("https://www.reddit.com/top.json?limit=50").get(listener, errListener))
            }
//            job1.cancel()
            log("job1.await() " + job1.await())
            log("job2.await() " + job2.await())
            log("job3.await() " + job3.await())
            log("job4.await() " + job4.await())
            log("job5.await() " + job5.await())
            val end = System.nanoTime()
            val time = (end - start) / 1000_000f
            log(String.format("testParallel timeElapsed %.3f ms thread: %s", time, Thread.currentThread().name))
            log("-----------------------------------------------------------------------------")
        }
    }

    fun testSync() {
        log("testSync: start")
        launch {
            val response1 = sync<String> { listener, errListener ->
                wikipedia(listener, errListener)
            }
            log("testSync: after wiki")
            val response2 = sync<String> { listener, errListener ->
                reddit(listener, errListener)
            }
            log("testSync: after reddit")
            log("1: " + response1)
            log("2: " + response2)
            log("-----------------------------------------------------------------------------")
        }
    }

    fun testAsync() {
        log("    fun testAsync() {\n: start")
        runBlocking {
            val start = System.nanoTime()
            val one = async {
                delay(1000)
                wikipedia(Response.Listener { response: String -> println(response) }, object : ErrorListener {
                    override fun onErrorResponse(error: KtVolleyError) {
                        println("error: " + error)
                    }
                })
            }
            println("The answer is ${one.await()}")

            val end = System.nanoTime()
            val time = (end - start) / 1000_000f
            log(String.format("    fun testAsync() {\n timeElapsed %.3f ms thread: %s", time, Thread.currentThread().name))
            log("-----------------------------------------------------------------------------")
        }
    }

    private fun redditError(listener: Response.Listener<String>, errListener: ErrorListener) {
        val url = "https://www.reddit.com/top3.json?limit=10"
        log("reddit: url: " + url)
        val request = MyRequest.create<String>().path(url).errorCode("12").get(listener, errListener)
        MyVolley.requestQueue.add(request)
    }

    private fun reddit(listener: Response.Listener<String>, errListener: ErrorListener) {
        reddit(10, listener, errListener)
    }

    private fun reddit(limit: Int, listener: Response.Listener<String>, errListener: ErrorListener) {
        val start = System.nanoTime()
        val url = "https://www.reddit.com/top.json?limit=$limit"
        log("reddit: url: " + url)
        val request = MyRequest.create<String>().path(url).errorCode("12").useCache(false).get(Response.Listener { response ->
            val end = System.nanoTime()
            val time = (end - start) / 1000_000f
            log(String.format("reddit timeElapsed %.3f ms thread: %s", time, Thread.currentThread().name))
            listener.onResponse(response)
        }, errListener)
        MyVolley.requestQueue.add(request)
    }

    private fun wikipedia(listener: Response.Listener<String>, errListener: ErrorListener) {
        val url = "https://en.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=top"
        log("wikipedia: url: " + url)
        val request = MyRequest.create<String>().path(url).errorCode("12").useCache(false).get(listener, errListener)
        MyVolley.requestQueue.add(request)
    }

    companion object {
        private val TAG = "MainActivity"
    }
}
