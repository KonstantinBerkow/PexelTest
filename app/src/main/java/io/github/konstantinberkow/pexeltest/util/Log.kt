package io.github.konstantinberkow.pexeltest.util

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach

fun <T> Flow<T>.logEach(tag: String, msg: (T) -> String): Flow<T> {
    return this.onEach { Log.d(tag, msg(it)) }
}

fun <T> Flow<T>.logError(tag: String, msg: (Throwable) -> String): Flow<T> {
    return this.catch { Log.d(tag, msg(it)) }
}
