/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.predar.helpers

import android.animation.ObjectAnimator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3

/** Node demonstrating rotation and transformations.  */
class RotatingNode(
    private val solarSettings: SolarSettings,
    private val isOrbit: Boolean,
    private val clockwise: Boolean,
    private val axisTiltDeg: Float
) :
    Node() {
    // We'll use Property Animation to make this node rotate.
    private var orbitAnimation: ObjectAnimator? = null
    private var degreesPerSecond = 90.0f
    private var lastSpeedMultiplier = 1.0f
    override fun onUpdate(frameTime: FrameTime) {
        super.onUpdate(frameTime)

        // Animation hasn't been set up.
        if (orbitAnimation == null) {
            return
        }

        // Check if we need to change the speed of rotation.
        val speedMultiplier = speedMultiplier

        // Nothing has changed. Continue rotating at the same speed.
        if (lastSpeedMultiplier == speedMultiplier) {
            return
        }
        if (speedMultiplier == 0.0f) {
            orbitAnimation!!.pause()
        } else {
            orbitAnimation!!.resume()
            val animatedFraction = orbitAnimation!!.animatedFraction
            orbitAnimation!!.duration = animationDuration
            orbitAnimation!!.setCurrentFraction(animatedFraction)
        }
        lastSpeedMultiplier = speedMultiplier
    }

    /** Sets rotation speed  */
    fun setDegreesPerSecond(degreesPerSecond: Float) {
        this.degreesPerSecond = degreesPerSecond
    }

    override fun onActivate() {
        startAnimation()
    }

    override fun onDeactivate() {
        stopAnimation()
    }

    private val animationDuration: Long
        private get() = (1000 * 360 / (degreesPerSecond * speedMultiplier)).toLong()

    private val speedMultiplier: Float
        private get() = if (isOrbit) {
            solarSettings.orbitSpeedMultiplier
        } else {
            solarSettings.rotationSpeedMultiplier
        }

    private fun startAnimation() {
        if (orbitAnimation != null) {
            return
        }
        orbitAnimation =
            createAnimator(clockwise, axisTiltDeg)
        orbitAnimation!!.target = this
        orbitAnimation!!.duration = animationDuration
        orbitAnimation!!.start()
    }

    private fun stopAnimation() {
        if (orbitAnimation == null) {
            return
        }
        orbitAnimation!!.cancel()
        orbitAnimation = null
    }

    companion object {
        /** Returns an ObjectAnimator that makes this node rotate.  */
        private fun createAnimator(
            clockwise: Boolean,
            axisTiltDeg: Float
        ): ObjectAnimator {
            // Node's setLocalRotation method accepts Quaternions as parameters.
            // First, set up orientations that will animate a circle.
            val orientations =
                arrayOfNulls<Quaternion>(4)
            // Rotation to apply first, to tilt its axis.
            val baseOrientation =
                Quaternion.axisAngle(
                    Vector3(1.0f, 0f, 0.0f),
                    axisTiltDeg
                )
            for (i in orientations.indices) {
                var angle = i * 360 / (orientations.size - 1).toFloat()
                if (clockwise) {
                    angle = 360 - angle
                }
                val orientation =
                    Quaternion.axisAngle(
                        Vector3(0.0f, 1.0f, 0.0f),
                        angle
                    )
                orientations[i] =
                    Quaternion.multiply(baseOrientation, orientation)
            }
            val orbitAnimation = ObjectAnimator()
            // Cast to Object[] to make sure the varargs overload is called.
            orbitAnimation.setObjectValues(*orientations as Array<Any?>)

            // Next, give it the localRotation property.
            orbitAnimation.setPropertyName("localRotation")

            // Use Sceneform's QuaternionEvaluator.
            orbitAnimation.setEvaluator(QuaternionEvaluator())

            //  Allow orbitAnimation to repeat forever
            orbitAnimation.repeatCount = ObjectAnimator.INFINITE
            orbitAnimation.repeatMode = ObjectAnimator.RESTART
            orbitAnimation.interpolator = LinearInterpolator()
            orbitAnimation.setAutoCancel(true)
            return orbitAnimation
        }
    }

}