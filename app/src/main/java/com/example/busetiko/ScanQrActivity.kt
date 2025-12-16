package com.example.busetiko

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.busetiko.controller.TicketController
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

private val CAMERA_PERMISSION_CODE = 1001

class ScanQrActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private var isScanned = false

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
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(this)
            ){ imageProxy->
               processImageProxy(imageProxy)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            }catch (e: Exception){
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image?:run{
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        barcodeScanner.process(image).addOnSuccessListener {
            barcodes-> if (isScanned)return@addOnSuccessListener
            for (barcode in barcodes){
                val rawValue = barcode.rawValue
                if (rawValue!=null){
                    Log.d("QR_SCAN","Scanned QR : $rawValue")
                    isScanned = true
                    vibratePhone()
                    handleQrResult(rawValue)
                    break
                }
            }
        }
            .addOnFailureListener{e->Log.e("QR_SCAN","Scanning failed",e) }
            .addOnCompleteListener{
                imageProxy.close()
            }
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
    private val barcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    private fun handleQrResult(rawValue:String){
        if (!rawValue.contains("BUS_NO")|| !rawValue.contains("ROUTE")|| !rawValue.contains("BUS_ID")){
            Toast.makeText(this,"Invalid Bus QR!",Toast.LENGTH_SHORT).show()
            isScanned=false //allows re-scan
            return
        }

        val dataMap = rawValue.split(";").map {
            val pair = it.split("=")
            pair[0] to pair[1]
        }.toMap()

        val busId = dataMap["BUS_ID"]
        val busNo = dataMap["BUS_NO"]
        val route = dataMap["ROUTE"]
        goToTicketCOntroller(busId,busNo,route)
    }

    private fun vibratePhone(){
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200, //milliseconds
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }else{
            vibrator.vibrate(200)
        }
    }

    private fun goToTicketCOntroller(busId: String?, busNo: String?, route: String?) {
        val intent = Intent(this,TicketController::class.java)
        intent.putExtra("BUS_ID",busId)
        intent.putExtra("BUS_NO",busNo)
        intent.putExtra("ROUTE",route)
        startActivity(intent)
        finish() //🔥stop camera activity
    }

}





