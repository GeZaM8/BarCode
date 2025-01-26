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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
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
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.barjek.barcode.R
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityCameraBinding
import com.barjek.barcode.databinding.LayoutChooseMoodBinding
import com.barjek.barcode.databinding.LayoutReasonMoodBinding
import com.barjek.barcode.geofence.GeofenceHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var db: DatabaseHelper
    private lateinit var cameraExecuter: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var absensiDialog: AlertDialog
    private lateinit var absensi: AlertDialog.Builder
    private var dataQr: String? = null

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
                        if (!isScanned) handleBarcode(barcode)
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
        dataQr = barcode.url?.url ?: barcode.displayValue
        if (dataQr != null) {
            isScanned = true
            val dataQrJSON = JSONObject().apply {
                put("qrcode", dataQr)
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val req = APIRequest("validate/qrcode", "POST", dataQrJSON.toString()).execute()

                    withContext(Dispatchers.Main) {
                        if (req.code in 200 until 300) {
                            absensi()
                        } else {
                            val jsonResponse = JSONObject(req.data)
                            val messages = jsonResponse.getJSONObject("messages").getString("error")
                            Toast.makeText(this@CameraActivity, messages, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun absensi() {
        val customLayout = layoutInflater.inflate(R.layout.layout_choose_mood, null)
        val bindingView = LayoutChooseMoodBinding.bind(customLayout)

        absensi = AlertDialog.Builder(this)
        absensiDialog = absensi.setView(customLayout)
            .setCancelable(false)
            .create()

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
        absensiDialog.show()
    }

    private fun absensiLayout(bindingView: LayoutReasonMoodBinding, customLayout: View, drawableMood: Int) {
        absensiDialog.dismiss()
        absensiDialog = absensi.setView(customLayout).create()
        val (mood, text) = when (drawableMood) {
            R.drawable.smile -> "Happy" to R.string.baik
            R.drawable.neutral -> "Neutral" to R.string.biasa
            R.drawable.frown -> "Frown" to R.string.buruk
            else -> "Neutral" to R.string.biasa
        }

        bindingView.mood.setImageResource(drawableMood)
        bindingView.textView1.setText(text)
        bindingView.btnKirim.setOnClickListener {
            val intent = Intent(this, ConfirmPresentActivity::class.java).apply {
                putExtra("mood", mood)
                putExtra("reason", bindingView.inputReason.text.toString())
                putExtra("qrcode", dataQr)
            }
            absensiDialog.dismiss()
            startActivity(intent)
            finish()
        }
        absensiDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        absensiDialog.dismiss()
        cameraExecuter.shutdown()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(geofenceReceiver)
    }

    override fun onStop() {
        super.onStop()
        absensiDialog.dismiss()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }
}