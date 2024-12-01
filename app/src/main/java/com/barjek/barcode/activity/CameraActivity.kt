package com.barjek.barcode.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Size
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.barjek.barcode.R
import com.barjek.barcode.databinding.ActivityCameraBinding
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraExecuter: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCameraBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        cameraExecuter = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {

            }
        }
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val screenSize = Size(1280, 720)
        val resolutionSelector = ResolutionSelector.Builder().setResolutionStrategy(
            ResolutionStrategy(screenSize, ResolutionStrategy.FALLBACK_RULE_NONE)
        ).build()

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().setResolutionSelector(resolutionSelector)
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecuter, { imageProxy ->
                        processImageProxy(imageProxy)
                    })
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        handleBarcode(barcode)
                    }
                }
                .addOnFailureListener {
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    @SuppressLint("InflateParams")
    private fun handleBarcode(barcode: Barcode) {
        val url = barcode.url?.url ?: barcode.displayValue
        if (url != null) {
            val absensi = AlertDialog.Builder(this)

            val costumLayout = layoutInflater.inflate(R.layout.layout_choose_mood, null)
            absensi.setView(costumLayout)
            absensi.show()
//            binding.textResult.text = url
//            binding.textResult.setOnClickListener {
//                val Open = AlertDialog.Builder(this)
//                Open.setMessage("Want to open here?")
//                Open.setNegativeButton("No") {dialog, _ ->
//                    val open = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                    startActivity(open)
//                }
//                Open.setPositiveButton("Yes") { dialog, _ ->
//                    val intent = Intent(this, WebViewActivity::class.java)
//                    intent.putExtra("url", url)
//                    startActivity(intent)
//                }
//                Open.show()
//            }
        } else {
//            binding.textResult.text = "No QR Code detected"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecuter.shutdown()
    }
}