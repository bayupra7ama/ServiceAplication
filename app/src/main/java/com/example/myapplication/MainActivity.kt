package com.example.myapplication

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var boundStatus = false
    private lateinit var boundService: MyBoundService

//    myboundService
    private val connection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val myBinder = service as MyBoundService.MyBinder
            boundService = myBinder.getService
            boundStatus = true
            getNumberFromService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundStatus = false
        }

    }

    private fun getNumberFromService() {
        boundService.numberLiveData.observe(this){ number ->
            binding.tvBoundServiceNumber.text = number.toString()

        }
    }

    private lateinit var binding : ActivityMainBinding

//    mintak permisi untuk android 14 keatas
    private val requestPermissionLauncher = registerForActivityResult(
       RequestPermission()
    ){
        isGrnated: Boolean ->
        if (isGrnated!!){
            Toast.makeText(this, "Unable to display Foreground service notification due to permission decline", Toast.LENGTH_SHORT).show()
        }
    }
    //    mintak permisi untuk android 14 keatas
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val serviceIntent = Intent(this, MyBackgroundService::class.java)
        binding.btnStartBackgroundService.setOnClickListener {
            startService(serviceIntent)
        }
        binding.btnStopBackgroundService.setOnClickListener {
            stopService(serviceIntent)
        }

//        untuk android 12
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED)
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        //        untuk android 12

        val foregroundServiceIntent = Intent(this, MyForegroundService::class.java)
        binding.btnStartForegroundService.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(foregroundServiceIntent)
            } else {
                startService(foregroundServiceIntent)
            }
        }
        binding.btnStopForegroundService.setOnClickListener {
            stopService(foregroundServiceIntent)
        }

//        boundService
        val boundServiceIntent = Intent(this , MyBoundService::class.java)
        binding.btnStartBoundService.setOnClickListener{
            bindService(boundServiceIntent , connection , BIND_AUTO_CREATE)
        }
        binding.btnStopBoundService.setOnClickListener{
            unbindService(connection)
        }
    }

    override fun onStop() {
        super.onStop()
        if (boundStatus){
            unbindService(connection)
            boundStatus = false
        }
    }
}