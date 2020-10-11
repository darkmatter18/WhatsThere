/*
 * Copyright 2020 Arkadip Bhattacharya
 *
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

import android.media.Image
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils

class Classifier constructor(modelPath:String?) {
    private val model:Module = Module.load(modelPath)

    private fun preprocessor(image: Image, rotation:Int):Tensor {
        return TensorImageUtils.imageYUV420CenterCropToFloat32Tensor(image, rotation, 224,
                224, TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB)
    }

    private fun  argMax(inputs:FloatArray):Int {
        var maxIndex:Int = -1
        var maxValue = 0.0f
        inputs.forEachIndexed { i, input ->
            if (input > maxValue) {
                    maxIndex = i
                    maxValue = input
                }

        }
        return maxIndex
    }

    fun predict(image: Image, rotation: Int):Int {

        val tensor = preprocessor(image,rotation)
        val output =  model.forward(IValue.from(tensor)).toTensor()
        val scores: FloatArray = output.dataAsFloatArray
        return argMax(scores)
    }

}