package com.barjek.barcode.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.barjek.barcode.R
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityCameraBinding
import com.barjek.barcode.databinding.LayoutChooseMoodBinding
import com.barjek.barcode.databinding.LayoutReasonMoodBinding
import com.barjek.barcode.geofence.GeofenceHelper
import com.barjek.barcode.model.Presence
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var db: DatabaseHelper
    private lateinit var cameraExecuter: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var absensi: AlertDialog.Builder

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var geofenceReceiver: BroadcastReceiver
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var isScanned = false
    private var hasStartedActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCameraBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        db = DatabaseHelper(this)

        binding.btnBack.setOnClickListener { finish() }

        cameraExecuter = Executors.newSingleThreadExecutor()
        barcodeScanner = BarcodeScanning.getClient()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    GeofenceHelper.setupGeofence(this@CameraActivity)
                }
            }
        }

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val backgroundLocationGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false

            if (cameraGranted) startCamera()
            if (locationGranted || coarseLocationGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    requestPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    )
                } else {
                    Log.d("Permission", "Izin lokasi foreground diberikan")
                }
                GeofenceHelper.checkLocation(this, fusedLocationClient, locationCallback)
            }
        }
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        geofenceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "com.barjek.LOCAL_GEOFENCE_EVENT") {
                    val inside = intent.getBooleanExtra("transition", false)
                    if (!inside && !hasStartedActivity) {
                        hasStartedActivity = true

                        val intent = Intent(this@CameraActivity, NoInsideActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }


        val intentFilter = IntentFilter("com.barjek.LOCAL_GEOFENCE_EVENT")
        LocalBroadcastManager.getInstance(this).registerReceiver(geofenceReceiver, intentFilter)

//        val dialog = AlertDialog.Builder(this)
//            .setTitle("Location Permissions Needed")
//            .setMessage("This app requires access to your location to set up geofences. Please grant the necessary permissions.")
//            .setPositiveButton("Grant Permissions") { _, _ ->
//                // Meluncurkan permintaan izin setelah penjelasan
//                requestPermissionLauncher.launch(
//                    arrayOf(
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
//                    )
//                )
//            }
//            .setNegativeButton("Cancel") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .create()
//
//        dialog.show()

        absensi = AlertDialog.Builder(this)
        absensi.setOnDismissListener {
            isScanned = false
        }
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
        if (isScanned) {
            imageProxy.close()
            return
        }
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
            absensi()

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
        }
    }

    private fun absensi() {
        val costumLayout = layoutInflater.inflate(R.layout.layout_choose_mood, null)
        val bindingView = LayoutChooseMoodBinding.bind(costumLayout)

        absensi.setView(costumLayout)
        absensi.setCancelable(false)
        absensi.show()
//            Log.d("Code", "Terdetekso")
        isScanned = true

        bindingView.smile.setOnClickListener {
            val costumLayout1 = layoutInflater.inflate(R.layout.layout_reason_mood, null)
            val bindingView1 = LayoutReasonMoodBinding.bind(costumLayout1)
            absensiLayout(bindingView1, costumLayout1, R.drawable.smile)
        }
        bindingView.neutral.setOnClickListener {
            val costumLayout2 = layoutInflater.inflate(R.layout.layout_reason_mood, null)
            val bindingView2 = LayoutReasonMoodBinding.bind(costumLayout2)
            absensiLayout(bindingView2, costumLayout2, R.drawable.neutral)
        }
        bindingView.frown.setOnClickListener {
            val costumLayout3 = layoutInflater.inflate(R.layout.layout_reason_mood, null)
            val bindingView3 = LayoutReasonMoodBinding.bind(costumLayout3)
            absensiLayout(bindingView3, costumLayout3, R.drawable.frown)
        }
    }

    private fun absensiLayout(bindingView: LayoutReasonMoodBinding, customLayout: View, drawableMood: Int) {
        absensi.setView(customLayout)
        val mood = when(drawableMood) {
            R.drawable.smile -> "Happy"
            R.drawable.neutral -> "Neutral"
            R.drawable.frown -> "Frown"
            else -> "Neutral"
        }
        bindingView.mood.setImageResource(drawableMood)
        bindingView.textView1.setText(R.string.baik)
        absensi.show()
        bindingView.btnKirim.setOnClickListener {
//            val presence = Presence(
//                userId = "1",
//                date = "Senin-08=01-2025",
//                status = "tes",
//                timestamp = "06:29",
//                location = "SMKN24",
//                mood = "Happy"
//            )
//            db.insertPresence(presence)
            val intent = Intent(this, ConfirmPresentActivity::class.java).apply {
                putExtra("mood", mood)
                putExtra("reason", bindingView.inputReason.text)
            }
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecuter.shutdown()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(geofenceReceiver)
    }

    override fun onStop() {
        super.onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onBackPressed() {
//        super.onBackPressed()

    }
}