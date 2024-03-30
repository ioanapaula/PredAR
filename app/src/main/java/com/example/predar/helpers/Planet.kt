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
package com.example.predar

import android.content.Context
import android.view.MotionEvent
import android.widget.TextView
import com.example.predar.helpers.RotatingNode
import com.example.predar.helpers.SolarSettings
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Node.OnTapListener
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable

/**
 * Node that represents a planet.
 *
 *
 * The planet creates two child nodes when it is activated:
 *
 *
 *  * The visual of the planet, rotates along it's own axis and renders the planet.
 *  * An info card, renders an Android View that displays the name of the planerendt. This can be
 * toggled on and off.
 *
 *
 * The planet is rendered by a child instead of this node so that the spinning of the planet doesn't
 * make the info card spin as well.
 */
class Planet(
    private val context: Context,
    private val planetName: String,
    private val planetScale: Float,
    private val orbitDegreesPerSecond: Float,
    private val axisTilt: Float,
    private val planetRenderable: ModelRenderable?,
    private val solarSettings: SolarSettings
) : Node(), OnTapListener {
    private var infoCard: Node? = null
    private var planetVisual: RotatingNode? = null
    override fun onActivate() {
        checkNotNull(scene) { "Scene is null!" }
        if (infoCard == null) {
            infoCard = Node()
            infoCard!!.setParent(this)
            infoCard!!.isEnabled = false
            infoCard!!.localPosition = Vector3(
                0.0f,
                planetScale * INFO_CARD_Y_POS_COEFF,
                0.0f
            )
            ViewRenderable.builder()
                .setView(context, R.layout.planet_card_view)
                .build()
                .thenAccept { renderable: ViewRenderable ->
                    infoCard!!.renderable = renderable
                    val textView = renderable.view as TextView
                    textView.text = planetName
                }
                .exceptionally { throwable: Throwable? ->
                    throw AssertionError(
                        "Could not load plane card view.",
                        throwable
                    )
                }
        }
        if (planetVisual == null) {
            // Put a rotator to counter the effects of orbit, and allow the planet orientation to remain
            // of planets like Uranus (which has high tilt) to keep tilted towards the same direction
            // wherever it is in its orbit.
            val counterOrbit =
                RotatingNode(solarSettings, true, true, 0f)
            counterOrbit.setDegreesPerSecond(orbitDegreesPerSecond)
            counterOrbit.setParent(this)
            planetVisual =
                RotatingNode(solarSettings, false, false, axisTilt)
            planetVisual!!.setParent(counterOrbit)
            planetVisual!!.renderable = planetRenderable
            planetVisual!!.localScale = Vector3(planetScale, planetScale, planetScale)
        }
    }

    override fun onTap(
        hitTestResult: HitTestResult,
        motionEvent: MotionEvent
    ) {
        if (infoCard == null) {
            return
        }
        infoCard!!.isEnabled = !infoCard!!.isEnabled
    }

    override fun onUpdate(frameTime: FrameTime) {
        if (infoCard == null) {
            return
        }

        // Typically, getScene() will never return null because onUpdate() is only called when the node
        // is in the scene.
        // However, if onUpdate is called explicitly or if the node is removed from the scene on a
        // different thread during onUpdate, then getScene may be null.
        if (scene == null) {
            return
        }
        val cameraPosition = scene!!.camera.worldPosition
        val cardPosition = infoCard!!.worldPosition
        val direction = Vector3.subtract(cameraPosition, cardPosition)
        val lookRotation =
            Quaternion.lookRotation(direction, Vector3.up())
        infoCard!!.worldRotation = lookRotation
    }

    companion object {
        private const val INFO_CARD_Y_POS_COEFF = 1.0f
    }

    init {
        setOnTapListener(this)
    }
}