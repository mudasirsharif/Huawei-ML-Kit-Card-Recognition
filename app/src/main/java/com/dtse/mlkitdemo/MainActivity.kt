/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dtse.mlkitdemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dtse.mlkitdemo.MLUtils.EmiratesIDCardProcess
import com.dtse.mlkitdemo.databinding.ActivityMainBinding
import com.huawei.hms.mlplugin.card.gcr.*
import kotlinx.android.synthetic.main.activity_main.*

const val PERMISSIONS_CODE = 11
const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private lateinit var mainActivityViewModel: MainActivityViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainActivityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        binding.viewModel = mainActivityViewModel
        binding.lifecycleOwner = this@MainActivity

        observeEvents()
    }

    private fun observeEvents() {

        mainActivityViewModel.buttonClick.observe(this@MainActivity, Observer {

            if (it == "scan_front") {

                startScanning()
            }
            if (it == "scan_back") {

                startScanning()
            }
        })
    }

    private fun startScanning() {

        // Checking for required permission
        if (isPermissionGranted()) {

            startCaptureActivity(null, this.callback)
        } else {
            //request permissions
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_CODE)
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode != PERMISSIONS_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Add Business code
            startCaptureActivity(null, this.callback)
            return
        }
    }

    /**
     * Set the recognition parameters, call the recognizer capture interface for recognition, and the recognition result will be returned through the callback function.
     *
     * @param callBack The callback of cards analyse.
     */
    private fun startCaptureActivity(
        `object`: Any?,
        callBack: MLGcrCapture.Callback
    ) {
        val cardConfig = MLGcrCaptureConfig.Factory().create()
        val uiConfig = MLGcrCaptureUIConfig.Factory()
            .setTipTextColor(Color.WHITE)
            .setScanBoxScreenRatio(0.9f)
            .setScanBoxCornerColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setTipText("Recognizing, Please align edges")
            .setOrientation(MLGcrCaptureUIConfig.ORIENTATION_PORTRAIT).create()
        val ocrManager =
            MLGcrCaptureFactory.getInstance().getGcrCapture(cardConfig, uiConfig)
        ocrManager.capturePreview(this, `object`, callBack)
    }

    private val callback: MLGcrCapture.Callback = object : MLGcrCapture.Callback {
        // This method requires the following status codes:
        // MLGcrCaptureResult.CAPTURE_CONTINUE: The returned result does not meet the requirements (for example, no result is returned or the returned result is incorrect). In camera stream or picture taking mode, the recognition continues.
        // MLGcrCaptureResult.CAPTURE_STOP: The returned result meets the requirements and the recognition stops.
        override fun onResult(result: MLGcrCaptureResult?, o: Any?): Int {
            // Process the recognition result. Implement post-processing logic based on your use case to extract valid information and return the status code.
            Log.d(TAG, "onResult")
            if (mainActivityViewModel.buttonClick.value == "scan_front") {
                if (result != null) {
                    // Check whether a result is returned.
                    // Recognition result processing logic.
                    if (!isInformationMatch(result, true)) {
                        // Check whether the processing result meets the requirements. Implement the isMatch method based on your use case.
                        return MLGcrCaptureResult.CAPTURE_CONTINUE // The processing result does not meet the requirements, and the recognition continues.
                    }
                    val finalResult = EmiratesIDCardProcess(
                        result.text,
                        true
                    ).result
                    mainActivityViewModel.idNumber.value = "ID Number: " + finalResult.idNumber
                    mainActivityViewModel.name.value = "Name: " + finalResult.name
                    mainActivityViewModel.nationality.value =
                        "Nationality: " + finalResult.nationality
                    card_iv.setImageBitmap(result.cardBitmap)
                }
                return MLGcrCaptureResult.CAPTURE_STOP
            } else {

                if (result != null) {
                    // Check whether a result is returned.
                    // Recognition result processing logic.
                    if (!isInformationMatch(result, false)) {
                        // Check whether the processing result meets the requirements. Implement the isMatch method based on your use case.
                        return MLGcrCaptureResult.CAPTURE_CONTINUE // The processing result does not meet the requirements, and the recognition continues.
                    }
                    val finalResult = EmiratesIDCardProcess(
                        result.text, false
                    ).result
                    mainActivityViewModel.dob.value = "Date of Birth: " + finalResult.dob
                    mainActivityViewModel.gender.value = "Gender: " + finalResult.gender
                    mainActivityViewModel.expiryDare.value =
                        "Expiry Date: " + finalResult.expiryDate
                    card_iv.setImageBitmap(result.cardBitmap)
                }
                return MLGcrCaptureResult.CAPTURE_STOP
            }
            // The processing ends, and the recognition exits.
        }

        override fun onCanceled() {
            // Processing for recognition request cancellation.
            Log.d(TAG, "onCanceled")
            showToast("Scanning has been cancelled")
        }

        // Callback method used when no text is recognized or a system exception occurs during recognition.
        // retCode: result code.
        // bitmap: bank card image that fails to be recognized.
        override fun onFailure(retCode: Int, bitmap: Bitmap) {
            // Exception handling.
            Log.d(TAG, "onFailure")
            showToast("Scanning has been Failed")
        }

        override fun onDenied() {
            // Processing for recognition request deny scenarios, for example, the camera is unavailable.
            Log.d(TAG, "onDenied")
            showToast("Scanning has been denied")
        }
    }

    private fun isInformationMatch(result: MLGcrCaptureResult, isFrontSide: Boolean): Boolean {

        val finalResult = EmiratesIDCardProcess(
            result.text,
            isFrontSide
        ).result

        return if (isFrontSide)
            finalResult != null
                    && finalResult.idNumber.isNotEmpty()
                    && finalResult.nationality.isNotEmpty()
                    && finalResult.name.isNotEmpty()
        else
            finalResult != null
                    && finalResult.dob.isNotEmpty()
                    && finalResult.gender.isNotEmpty()
                    && finalResult.expiryDate.isNotEmpty()
    }

    private fun showToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
    }

    private fun isPermissionGranted(): Boolean = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

}
