package com.scanflow.datacapture.textcapturesample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.scanflow.datacapture.textcapturesample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.apply {
            tyreScan.setOnClickListener {
                startActivity(Intent(this@MainActivity, TyreCaptureActivity::class.java))
            }

            containerScan.setOnClickListener {
                startActivity(Intent(this@MainActivity, ContainerCaptureActivity::class.java))
            }
        }
    }
}