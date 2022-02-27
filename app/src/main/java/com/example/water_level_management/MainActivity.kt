package com.example.water_level_management

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothManager
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var constraintLayout: ConstraintLayout

    private lateinit var handler: Handler
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetoothAdapter = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val switchBt = findViewById<SwitchCompat>(R.id.bluetooth_switch)
        val onOffText = findViewById<TextView>(R.id.on_off_text)
        val progress = findViewById<ProgressBar>(R.id.progress_bar)
        val animation = AnimationUtils.loadAnimation(this, R.anim.progressbar_anime)
        constraintLayout = findViewById(R.id.constraint)
        progress.progress = 70
        progress.animation = animation
        checkPermission()
        if(bluetoothAdapter.adapter.enable()){
            switchBt.isEnabled
            connectBT()
            onOffText.text = "bluetooth is turned on"
        }else{
            onOffText.text = "bluetooth is turned off"
        }
    }
     // check permission
    private fun checkPermission(){
         if (ActivityCompat.checkSelfPermission(
                 this,
                 Manifest.permission.BLUETOOTH_CONNECT
             ) != PackageManager.PERMISSION_GRANTED
         ) {
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                 ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),50)
             }
             return
         }
    }

    private fun connectBT(){
        checkPermission()
        bluetoothAdapter = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if(!bluetoothAdapter.adapter.enable()){
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            resultActivity.launch(intent)
        }

        val bluetoothDevice = bluetoothAdapter.adapter.getRemoteDevice(DEVICE_ADDRESS)
        val uuid = bluetoothDevice.uuids[0].uuid
        var socket:BluetoothSocket? = null

        try{
            socket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid)
        }catch (e:IOException){
            e.printStackTrace()
        }
        bluetoothSocket=socket!!

        Timer().schedule(object: TimerTask(){
            override fun run() {
                checkPermission()
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter.cancelDiscovery()
                try {
                    bluetoothSocket.connect()
                    Snackbar("Connected Successfully")
                    Log.e("status", "connected successfully")
                }catch (socketError:IOException){
                    socketError.printStackTrace()
                    Snackbar(socketError.message!!)
                    Log.e("status", "error", socketError)
                    try {
                        bluetoothSocket.close()
                        Log.e("status", "closed")

                    }catch (e:IOException){
                        e.printStackTrace()

                        Log.e("status", "error", e)
                    }

                }
            }

        }, 200)

        try{
            inputStream = bluetoothSocket.inputStream
            outputStream = bluetoothSocket.outputStream
        }catch (e:IOException){
            e.printStackTrace()
        }


    }
    private  var resultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
        if (result.resultCode == Activity.RESULT_OK){
            val intent = result.data
            Snackbar(intent.toString())
        }

    }
  private  fun Snackbar(message:String){
      Snackbar.make(constraintLayout,message,Snackbar.LENGTH_SHORT)
          .show()

  }
    companion object{
        lateinit var inputStream: InputStream
        lateinit var outputStream: OutputStream
        const val DEVICE_ADDRESS:String="70:26:05:B2:A7:B9"// THE BLUETOOTH MAC-ADDRESS
    }
}