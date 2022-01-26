package com.example.water_level_management

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothSocket: BluetoothSocket

    private lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()!!
        val switchBt = findViewById<SwitchCompat>(R.id.bluetooth_switch)
        val onOffText = findViewById<TextView>(R.id.on_off_text)
        val progress = findViewById<ProgressBar>(R.id.progress_bar)
        val animation = AnimationUtils.loadAnimation(this, R.anim.progressbar_anime)
        progress.progress = 70
        progress.animation = animation
        if(bluetoothAdapter.enable()){
            switchBt.isEnabled
            connectBT()
            onOffText.text = "bluetooth is turned on"
        }else{
            onOffText.text = "bluetooth is turned off"
        }
    }

    private fun connectBT(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(!bluetoothAdapter.enable()){
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, 0)
        }

        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS)
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
                val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                bluetoothAdapter.cancelDiscovery()
                try {
                    bluetoothSocket.connect()
                    Log.e("status", "connected successfully")
                }catch (socketError:IOException){
                    socketError.printStackTrace()
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

    companion object{
        lateinit var inputStream: InputStream
        lateinit var outputStream: OutputStream
        const val DEVICE_ADDRESS:String="70:26:05:B2:A7:B9"// the doorlock bluetooth address
    }
}