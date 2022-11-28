package com.scanflow.datacapture.batchcapturesample

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.scanflow.datacapture.barcode.SFBarcodeCaptureSession
import com.scanflow.datacapture.barcode.config.DecodeConfig
import com.scanflow.datacapture.batchcapturesample.databinding.ActivityBatchBinding
import com.scanflow.datacapture.core.camera.ScanflowReader
import com.scanflow.datacapture.model.OneOFManyCodesScanResults
import com.scanflow.datacapture.model.ScanResultSuccess
import com.scanflow.datacapture.utils.PermissionUtils

class BatchScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBatchBinding

    private var mBarcodeReader: ScanflowReader? = null

    private var scannedDataList = HashSet<String>()

    companion object {
        private const val SCANFLOW_LICENSE_KEY = "-- ENTER YOUR SCANFLOW LICENSE KEY HERE --"
        private const val TAG = "BatchScanActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_batch)
        createSessionAndValidateLicense()
        setUi()
    }

    private fun setUi() {
        binding.apply {
            ivFlashLight.setOnClickListener {
                toggleFlashLightState()
            }
        }
    }

    private fun toggleFlashLightState() {
        if (mBarcodeReader == null) return
        val isTorch = mBarcodeReader?.isTorchEnabled ?: false
        mBarcodeReader?.enableTorch(!isTorch)
        binding.ivFlashLight.isSelected = !isTorch
    }

    private fun createSessionAndValidateLicense() {
        mBarcodeReader = SFBarcodeCaptureSession.createScanSession(
            this, SCANFLOW_LICENSE_KEY, binding.scanflowView, DecodeConfig.BATCH_INVENTORY
        )
        mBarcodeReader?.apply {
            setOnBarcodeScanResultCallback(barCodeResultCallBack) //callback
            setPlayBeep(false) //enable or disable play beep sound
//            setVibrate(true) //enable or disable vibrate
//            setBeepSoundResource(R.raw.beep) //custom beep sound
//            setAutoFlashlight(autoFlashlight) //enable or disable auto flashlight mode
            bindFlashlightView(binding.ivFlashLight) //Pass flashlight view id
            bindSliderView(binding.sliderExposure)
//           setIsContinuousScan(isContinuousScan) //by default true for continuous scan
        }
    }

    //callback functions.
    private val barCodeResultCallBack: ScanflowReader.OnBarcodeScanResultCallback =
        object : ScanflowReader.OnBarcodeScanResultCallback {
            override fun onBatchScanResultSuccess(result: ArrayList<ScanResultSuccess>) {
                Log.d(TAG, "onBatchScanResultSuccess: >>>>> $result <<<<<")
                result.forEach {
                    scannedDataList.add(it.barcodeFormat.name + " - " + it.text)
                }
                runOnUiThread {
                    Toast.makeText(
                        this@BatchScanActivity,
                        "Total no of scans : ${scannedDataList.size}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onOneOfManyCodeResult(results: HashSet<OneOFManyCodesScanResults>) {
                Log.d(TAG, "onOneOfManyCodeResult: >>>>> $results <<<<<")
            }

            override fun onOneofManyCodeRemoved(result: OneOFManyCodesScanResults) {
                Log.d(TAG, "onOneofManyCodeRemoved: >>>>> $result <<<<<")
            }

            override fun onOneofManyCodeSelected(result: OneOFManyCodesScanResults) {
                Log.d(TAG, "onOneofManyCodeSelected: >>>>> $result <<<<<")
            }

            override fun onScanResultFailure(error: String) {
                Log.e(TAG, "onScanResultFailure: >>>>> $error <<<<<")
            }

            override fun onScanResultSuccess(result: ScanResultSuccess) {
                Log.d(TAG, "onScanResultSuccess: >>>>> $result <<<<<")
            }
        }


    /**
     * This method is used to start camera once user Give Permissions.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.requestPermissionsResult(
                    Manifest.permission.CAMERA,
                    permissions,
                    grantResults
                )
            ) {
                startCamera()
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Permission Not granted")
                finish()
            }
        }
    }

    /**
     * This method is used to start the camera preview and check whether the camera permission enabled or not.
     * we are requesting runtime permission to enable if not enabled.
     */
    private fun startCamera() {
        if (mBarcodeReader == null) return
        if (PermissionUtils.checkPermission(this, Manifest.permission.CAMERA)) {
            mBarcodeReader?.startCamera()
        } else {
            PermissionUtils.requestPermission(
                this,
                Manifest.permission.CAMERA,
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        startCamera()
        //setting flash by default.
        val isTorch = mBarcodeReader?.isTorchEnabled ?: false
        binding.ivFlashLight.isSelected = isTorch
    }

    override fun onStop() {
        mBarcodeReader?.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        mBarcodeReader?.release()
        super.onDestroy()
    }
}