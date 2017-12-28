# KtVolley
KtVolley makes using Volley easier in Kotlin and provides the ability to join multiple requests together and execute some code in a couroutine after all the requests have been completed. Usage is as follows:
```kotlin
launch {
	val job1 = bg { listener, errListener ->
	    volleyRequest1(listener, errListener)
	}
	val job2 = bg { listener, errListener ->
	    volleyRequest2(listener, errListener)
	}
	job1.await()
	job2.await()
	// both requests have finished
	// code here will execute in a background thread
}
```
