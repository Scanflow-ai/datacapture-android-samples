package com.scanflow.datacapture.barcodecapturesample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.scanflow.datacapture.barcodecapturesample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.apply {

            barCodeScan.setOnClickListener {
                startActivity(Intent(this@MainActivity, BarcodeScanActivity::class.java))
            }

            qrCodeScan.setOnClickListener {
                startActivity(Intent(this@MainActivity, QrScanActivity::class.java))
            }

            anyScan.setOnClickListener {
                startActivity(Intent(this@MainActivity, AnyScanActivity::class.java))
            }
        }

//        requestPermissionsLauncher.launch(
//            arrayOf(
//                android.Manifest.permission.CAMERA,
//            )
//        )
    }

//    private val requestPermissionsLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
//            if (permissions.all { it.value }) {
//                Log.d("TAG", ": permissions Granted")
//            } else {
//                Log.d("TAG", ": permissions NOT Granted")
//                finish()
//            }
//        }
}