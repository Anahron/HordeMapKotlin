package ru.newlevel.hordemap.presentation.map.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.app.IDLE_TIMEOUT
import ru.newlevel.hordemap.app.ROTATION_TIMEOUT

class MapInteractionHandler(private val onUpdateRotation: () -> Unit) {

    private var isStopped = true
    private var onPause = true
    private var isMove = false
    private var job: Job? = null

    fun getIsStopped(): Boolean = isStopped

    fun start() {
        job?.cancel()
        isStopped = false
        job = CoroutineScope(Dispatchers.Main).launch {
            while (isActive && !isStopped) {
                if (!isMove)
                    onUpdateRotation.invoke()
                delay(ROTATION_TIMEOUT)
            }
        }
    }

    fun stop() {
        isStopped = true
        job?.cancel()
    }

    private fun restart() {
        job?.cancel()
        start()
    }

    fun onCameraMove(isFromUser: Boolean) {
        isMove = true
        if (!isStopped && isFromUser) {
            job?.cancel()
            isStopped = true
            job = CoroutineScope(Dispatchers.Main).launch {
                delay(IDLE_TIMEOUT)
                isStopped = false
                restart()
            }
        }
    }

    fun onCameraIdle() {
        isMove = false
    }

    fun onDestroy() {
        stop()
    }

    fun onPause() {
        job?.cancel()
        onPause = isStopped
    }

    fun onResume() {
        isStopped = onPause
        if (!onPause) {
            restart()
        }
    }
}