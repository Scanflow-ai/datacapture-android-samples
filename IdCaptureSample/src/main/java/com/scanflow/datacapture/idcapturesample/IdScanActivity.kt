package com.scanflow.datacapture.idcapturesample

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.scanflow.datacapture.core.camera.ScanflowReader
import com.scanflow.datacapture.idcapturesample.databinding.ActivityIdscanBinding
import com.scanflow.datacapture.idcard.SFIDCardCaptureSession
import com.scanflow.datacapture.utils.PermissionUtils

class IdScanActivity : AppCompatActivity() {

    lateinit var binding: ActivityIdscanBinding

    private var mIdCardSession: ScanflowReader? = null

    companion object {
        private const val SCANFLOW_LICENSE_KEY = "-- ENTER YOUR SCANFLOW LICENSE KEY HERE --"
        private const val TAG = "IdScanActivity"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_idscan)

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
        if (mIdCardSession == null) return
        val isTorch = mIdCardSession?.isTorchEnabled ?: false
        mIdCardSession?.enableTorch(!isTorch)
        binding.ivFlashLight.isSelected = !isTorch
    }

    private fun createSessionAndValidateLicense() {
        mIdCardSession = SFIDCardCaptureSession.createScanSession(
            this, SCANFLOW_LICENSE_KEY, binding.scanflowView
        )
        mIdCardSession?.setOnIDCardScanResultCallback(resultCallback)
        mIdCardSession?.apply {
            setPlayBeep(false) //enable or disable play beep sound
            setVibrate(true) //enable or disable vibrate
//            setBeepSoundResource(R.raw.beep) //custom beep sound
//            setAutoFlashlight(autoFlashlight) //enable or disable auto flashlight mode
            bindFlashlightView(binding.ivFlashLight) //Pass flashlight view id

            bindSliderView(binding.sliderExposure)
//           setIsContinuousScan(isContinuousScan) //by default true for continuous scan
        }
    }

    private val resultCallback = object : ScanflowReader.OnIDCardScanResultCallback {
        override fun onScanResultSuccess(result: ScanflowReader.IDCardScanResult) {
            Log.d(TAG, "onScanResultSuccess: ${Gson().toJson(result)}")
            Log.d(TAG, "onScanResultSuccess: result.name ${result.name}")
            Log.d(TAG, "onScanResultSuccess: result.father name ${result.fathername}")
            Log.d(TAG, "onScanResultSuccess: result.dob ${result.dob}")
            Log.d(TAG, "onScanResultSuccess: result.uniqueId ${result.uniqueId}")
            Log.d(TAG, "onScanResultSuccess: result.frontImage BITMAP ${result.frontImage}")
            Log.d(TAG, "onScanResultSuccess: result.backImage BITMAP ${result.backImage}")

            runOnUiThread {
                Toast.makeText(
                    this@IdScanActivity,
                    "result.name :: ${result.name} , check log for all available data.",
                    Toast.LENGTH_LONG
                ).show()
            }
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
        if (mIdCardSession == null) return
        if (PermissionUtils.checkPermission(this, Manifest.permission.CAMERA)) {
            mIdCardSession?.startCamera()
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
        val isTorch = mIdCardSession?.isTorchEnabled ?: false
        binding.ivFlashLight.isSelected = isTorch
    }

    override fun onStop() {
        mIdCardSession?.stopCamera()
        super.onStop()
    }

    override fun onDestroy() {
        mIdCardSession?.release()
        super.onDestroy()
    }

}