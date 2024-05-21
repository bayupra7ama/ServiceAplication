package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyBoundService : Service() {
    companion object{
        private val TAG = MyBoundService::class.java.simpleName
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    var numberLiveData : MutableLiveData<Int> = MutableLiveData()
    private val binder = MyBinder()
    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: ")
        serviceScope.launch {
            for (i in 1 .. 50 ){
                delay(1000)
                Log.d(TAG, "DO Something:$i ")
                numberLiveData.postValue(i)
            }
            Log.d(TAG, "onBind: service dihentikan")
        }
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: stop")
    }

    override fun onUnbind(intent: Intent?): Boolean {

        serviceJob.cancel()
        Log.d(TAG, "onUnbind: ")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d(TAG, "onRebind: ")
    }

    internal inner class MyBinder(): Binder(){
        val getService : MyBoundService = this@MyBoundService

    }
}