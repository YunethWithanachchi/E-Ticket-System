package com.example.busetiko

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private val CAMERA_PERMISSION_CODE = 1001

class ScanQrActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_scan)

        previewView = findViewById(R.id.previewView)

        permissionToOpenCamera()
    }

    private fun permissionToOpenCamera() {
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED){
                startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview
                )
            }catch (exc: Exception){
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    //I tried going ahead without onRequestPermissionResult function, then what happens
    // is permission is asked from the user and when permission is given its a black screen
    // but when you click for the second time, ah the camera works.
    //So the thing is, this function is a callback, getting the result of the permission prompt
    // and runs the required code after getting permission. More like a listener
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE){
            if (grantResults.isNotEmpty()&& grantResults[0]
                ==PackageManager.PERMISSION_GRANTED){
                startCamera()
            }else{
                Toast.makeText(this,"Camera Persmission is required to scan the QR",Toast.LENGTH_SHORT).show()
            }
        }
    }
}