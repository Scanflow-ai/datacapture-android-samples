package com.scanflow.datacapture.textcapturesample

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.scanflow.datacapture.config.TextCaptureConfig
import com.scanflow.datacapture.core.camera.ScanflowReader
import com.scanflow.datacapture.text.SFTextCaptureSession
import com.scanflow.datacapture.textcapturesample.databinding.ActivityTextCaptureBinding
import com.scanflow.datacapture.utils.PermissionUtils

class TyreCaptureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTextCaptureBinding
    private var mTextReader: ScanflowReader? = null

    private val isClickToScan = false //make it true, to check click to capture.

    companion object {
        private const val SCANFLOW_LICENSE_KEY = "-- ENTER YOUR SCANFLOW LICENSE KEY HERE --"
        private const val TAG = "TyreCaptureActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_text_capture)
        createSessionAndValidateLicense()
        setUi()
    }

    private fun toggleFlashLightState() {
        if (mTextReader == null) return
        val isTorch = mTextReader?.isTorchEnabled ?: false
        mTextReader?.enableTorch(!isTorch)
        binding.ivFlashLight.isSelected = !isTorch
    }

    private fun setUi() {
        binding.apply {
            ivFlashLight.setOnClickListener {
                toggleFlashLightState()
            }
            btnImageCapture.visibility = if (isClickToScan) View.VISIBLE else View.GONE
            btnImageCapture.setOnClickListener {
                if (!isClickToScan) return@setOnClickListener
                captureToScan()
            }
        }
    }

    private fun captureToScan() {
        mTextReader?.captureToScan()
    }

    private fun createSessionAndValidateLicense() {
        val textConfig = TextCaptureConfig.TYRE //pass scan type (TYRE, CONTAINER)
        mTextReader = SFTextCaptureSession.createScanSession(
            this,
            SCANFLOW_LICENSE_KEY,
            binding.scanflowView, textConfig
        )
        //We can config other properties as we required.
        mTextReader?.setOnTextScanResultCallback(
            object : ScanflowReader.OnTextScanResultCallback {
                override fun onScanResultFailure(error: String) {
                    Log.e(TAG, "onScanResultFailure: $error")
                    runOnUiThread {
                        Toast.makeText(
                            this@TyreCaptureActivity,
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onScanResultSuccess(result: ScanflowReader.TextScanResult) {
                    Log.d(TAG, "onScanResultSuccess: $result")
                    Log.d(TAG, "onScanResultSuccess: ${result.text}")
                    runOnUiThread {
                        Toast.makeText(this@TyreCaptureActivity, result.text, Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        )

        mTextReader?.apply {
            setPlayBeep(false) //enable or disable play beep sound
//            setVibrate(true) //enable or disable vibrate
//            setBeepSoundResource(R.raw.beep) //custom beep sound
//            setAutoFlashlight(autoFlashlight) //enable or disable auto flashlight mode
            bindFlashlightView(binding.ivFlashLight) //Pass flashlight view id

            bindSliderView(binding.sliderExposure)
            setIsContinuousScan(!isClickToScan) //by default true for continuous scan
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
        if (mTextReader == null) return
        if (PermissionUtils.checkPermission(this, Manifest.permission.CAMERA)) {
            mTextReader?.startCamera()
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
        val isTorch = mTextReader?.isTorchEnabled ?: false
        binding.ivFlashLight.isSelected = isTorch
    }

    override fun onStop() {
        mTextReader?.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        mTextReader?.release()
        super.onDestroy()
    }
}