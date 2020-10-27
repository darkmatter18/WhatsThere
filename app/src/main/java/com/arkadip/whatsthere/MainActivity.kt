package com.arkadip.whatsthere

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity:AppCompatActivity() {
    private val permissions = arrayOf(android.Manifest.permission.CAMERA)
    private val permissionsRequestCode = 103
    private val delayInMilliSeconds:Long = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_main)
        Handler(Looper.getMainLooper()).postDelayed({
            if (hasPermissions()){
                openActivity()
            }
            else {
                ActivityCompat.requestPermissions(this,permissions,permissionsRequestCode)
            }


        },delayInMilliSeconds)
    }

    private fun openActivity() {
        startActivity(Intent(this,LogicActivity::class.java))
        finish()

    }


        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==permissionsRequestCode){
            var isPermitted = true
            for (result in grantResults){
                if (result!=PackageManager.PERMISSION_GRANTED){
                    isPermitted = false
                    break
                }
            }
            if (isPermitted) {
                openActivity()
            }
            else {
                Toast.makeText(this,this.getString(R.string.error_msg_permssion_required),Toast.LENGTH_LONG).show()
            }

        }

    }

    private fun hasPermissions():Boolean{
        return if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            var permissionsGranted = true
            for(permission in permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) !=PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = false
                    break
                }
            }
            permissionsGranted
        }
        else {
            true
        }


    }
}