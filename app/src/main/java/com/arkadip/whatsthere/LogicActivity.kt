/*
 * Copyright 2020 Arkadip Bhattacharya
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.arkadip.whatsthere

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_logic.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class LogicActivity:AppCompatActivity() {
    lateinit var cameraProvider:ProcessCameraProvider
    lateinit var cameraExecutor:ExecutorService
    lateinit var classifier: Classifier
    lateinit var cameraProviderListenableFuture:ListenableFuture<ProcessCameraProvider>
    lateinit var camera:Camera
    private var hasFlashUnit:Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logic)
        init()
    }

    private fun init() {
        flashToggle.setOnCheckedChangeListener { buttonView, isChecked -> toggleFlash(isChecked) }
        cameraExecutor = Executors.newSingleThreadExecutor()
        classifier = Classifier(Utils.assetFilePath(this,"mobilenet-v2.pt"))
        preview_view.post(this::setUpCamera)

    }

    private fun toggleFlash(enableFlash: Boolean) {
        if (!hasFlashUnit) { return}
        camera.cameraControl.enableTorch(enableFlash)
    }

    private fun setUpCamera() {
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderListenableFuture.addListener(Runnable {
            try {
                cameraProvider = cameraProviderListenableFuture.get()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))


    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindCameraUseCases() {
        val displayMetrics = DisplayMetrics()
        preview_view.display.getRealMetrics(displayMetrics)
        Log.d("DISPLAY", "Screen metrics: " + displayMetrics.widthPixels
                + " x " + displayMetrics.heightPixels)
        val aspectRatio = aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels)
        Log.d("DISPLAY", "Preview aspect ratio: $aspectRatio")
        val rotation = preview_view.display.rotation

        //camera selector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        //preview
        val preview = Preview.Builder().apply {
            setTargetAspectRatio(aspectRatio)
            setTargetRotation(rotation)
        }.build()

        //Image Analysis
        val imageAnalysis = ImageAnalysis.Builder().apply {
            setTargetResolution(Size(224,224))
            setTargetRotation(rotation)
        }.build()




        imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image: ImageProxy ->
            val r = image.imageInfo.rotationDegrees
            val value = classifier.predict(image.image!!, r)
            Log.d("OUTPUT", value.toString())
            val out = Utils.IMAGENET_CLASSES[value]
            runOnUiThread { textView.text = out }
            image.close()
        })

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(this, cameraSelector,preview,imageAnalysis)
                .also { hasFlashUnit = it.cameraInfo.hasFlashUnit() }
        preview.setSurfaceProvider(preview_view.surfaceProvider)



    }

    /**
     * Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     * of preview ratio to one of the provided values.
     *
     * @param width  - preview width
     * @param height - preview height
     * @return suitable aspect ratio
     */
    private fun aspectRatio(width:Int, height:Int):Int {
        val RATIO_4_3_VALUE = 4.0 / 3.0
        val RATIO_16_9_VALUE = 16.0 / 9.0
        val previewRatio: Double = Math.max(width, height).toDouble()  / Math.min(width, height)
        if (abs(previewRatio-RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return  AspectRatio.RATIO_4_3
        }
        else {
            return  AspectRatio.RATIO_16_9
        }

    }

    override fun onDestroy() {
        cameraExecutor.shutdown()
        super.onDestroy()
    }



}