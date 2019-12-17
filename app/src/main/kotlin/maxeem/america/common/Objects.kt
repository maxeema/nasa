package maxeem.america.common

/**
 * Global objects
 */

val pid        get() = android.os.Process.myPid()
val thread     get() = Thread.currentThread()
val timeMillis get() = System.currentTimeMillis()
