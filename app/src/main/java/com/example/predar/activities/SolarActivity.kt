package com.example.predar.activities

import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.example.predar.helpers.DemoUtils
import com.example.predar.Planet
import com.example.predar.helpers.RotatingNode
import com.example.predar.helpers.SolarSettings
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Config.LightEstimationMode
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.example.predar.R
import com.example.predar.fragments.LessonFragment
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.*
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

class SolarActivity : AppCompatActivity() {
    private var arCoreFragment: LessonFragment? = null
    private var cameraPermissionRequested = false
    private var gestureDetector: GestureDetector? = null
    private var loadingMessageSnackbar: Snackbar? = null
    private var arSceneView: ArSceneView? = null
    private var sunRenderable: ModelRenderable? = null
    private var mercuryRenderable: ModelRenderable? = null
    private var venusRenderable: ModelRenderable? = null
    private var earthRenderable: ModelRenderable? = null
    private var lunaRenderable: ModelRenderable? = null
    private var marsRenderable: ModelRenderable? = null
    private var jupiterRenderable: ModelRenderable? = null
    private var saturnRenderable: ModelRenderable? = null
    private var uranusRenderable: ModelRenderable? = null
    private var neptuneRenderable: ModelRenderable? = null
    private var solarControlsRenderable: ViewRenderable? = null
    private val solarSettings = SolarSettings()

    // True once scene is loaded
    private var hasFinishedLoading = false

    // True once the scene has been placed.
    private var hasPlacedSolarSystem = false

    // CompletableFuture requires api level 24
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!DemoUtils.checkIsSupportedDeviceOrFinish(this)) {
            // Not a supported device.
            return
        }
        setContentView(R.layout.activity_lesson)
        arCoreFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as LessonFragment?
        arSceneView = arCoreFragment?.arSceneView

        // Build all the planet models.
        val sunStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Sol.sfb")).build()
        val mercuryStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Mercury.sfb")).build()
        val venusStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Venus.sfb")).build()
        val earthStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Earth.sfb")).build()
        val lunaStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Luna.sfb")).build()
        val marsStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Mars.sfb")).build()
        val jupiterStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Jupiter.sfb")).build()
        val saturnStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Saturn.sfb")).build()
        val uranusStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Uranus.sfb")).build()
        val neptuneStage: CompletableFuture<ModelRenderable> =
            ModelRenderable.builder().setSource(this, Uri.parse("Neptune.sfb")).build()

        // Build a renderable from a 2D View.
        val solarControlsStage: CompletableFuture<ViewRenderable> = ViewRenderable.builder().setView(this, R.layout.solar_controls).build()
        CompletableFuture.allOf(
            sunStage,
            mercuryStage,
            venusStage,
            earthStage,
            lunaStage,
            marsStage,
            jupiterStage,
            saturnStage,
            uranusStage,
            neptuneStage,
            solarControlsStage
        )
            .handle<Any?> { _: Void?, throwable: Throwable? ->
                // When you build a Renderable, Sceneform loads its resources in the background while
                // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                // before calling get().
                if (throwable != null) {
                    DemoUtils.displayError(this, getString(R.string.unavailable_renderable), throwable)
                    return@handle null
                }
                try {
                    sunRenderable = sunStage.get()
                    mercuryRenderable = mercuryStage.get()
                    venusRenderable = venusStage.get()
                    earthRenderable = earthStage.get()
                    lunaRenderable = lunaStage.get()
                    marsRenderable = marsStage.get()
                    jupiterRenderable = jupiterStage.get()
                    saturnRenderable = saturnStage.get()
                    uranusRenderable = uranusStage.get()
                    neptuneRenderable = neptuneStage.get()
                    solarControlsRenderable = solarControlsStage.get()

                    // Everything finished loading successfully.
                    hasFinishedLoading = true
                } catch (ex: InterruptedException) {
                    DemoUtils.displayError(this, getString(R.string.unavailable_renderable), ex)
                } catch (ex: ExecutionException) {
                    DemoUtils.displayError(this, getString(R.string.unavailable_renderable), ex)
                }
                null
            }

        // Set up a tap gesture detector.
        gestureDetector = GestureDetector(
            this,
            object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    onSingleTap(e)
                    return true
                }

                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }
            })

        // Set a touch listener on the Scene to listen for taps.
        arSceneView
            ?.scene
            ?.setOnTouchListener { hitTestResult: HitTestResult?, event: MotionEvent? ->
                // If the solar system hasn't been placed yet, detect a tap and then check to see if
                // the tap occurred on an ARCore plane to place the solar system.
                if (!hasPlacedSolarSystem) {
                    return@setOnTouchListener gestureDetector!!.onTouchEvent(event)
                }
                false
            }

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
            ?.scene
            ?.addOnUpdateListener { frameTime: FrameTime? ->
                if (loadingMessageSnackbar == null) {
                    return@addOnUpdateListener
                }
                val frame = arSceneView!!.arFrame ?: return@addOnUpdateListener
                if (frame.camera.trackingState != TrackingState.TRACKING) {
                    return@addOnUpdateListener
                }
                for (plane in frame.getUpdatedTrackables(
                    Plane::class.java
                )) {
                    if (plane.trackingState == TrackingState.TRACKING) {
                        hideLoadingMessage()
                    }
                }
            }
    }

    override fun onResume() {
        super.onResume()
        if (arSceneView == null) {
            return
        }
        if (arSceneView!!.session == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                val lightEstimationMode =
                    LightEstimationMode.ENVIRONMENTAL_HDR
                val session =
                    if (cameraPermissionRequested) DemoUtils.createArSessionWithInstallRequest(
                        this,
                        lightEstimationMode
                    ) else DemoUtils.createArSessionNoInstallRequest(this, lightEstimationMode)
                if (session == null) {
                    cameraPermissionRequested = DemoUtils.hasCameraPermission(this)
                    return
                } else {
                    arSceneView!!.setupSession(session)
                }
            } catch (e: UnavailableException) {
                DemoUtils.handleSessionException(this, e)
            }
        }
        try {
            arSceneView!!.resume()
        } catch (ex: CameraNotAvailableException) {
            DemoUtils.displayError(this, getString(R.string.camera_denied), ex)
            finish()
            return
        }
        if (arSceneView!!.session != null) {
            showLoadingMessage()
        }
    }

    override fun onPause() {
        super.onPause()
        if (arSceneView != null) {
            arSceneView!!.pause()
        }
    }

   override fun onDestroy() {
        super.onDestroy()
        if (arSceneView != null) {
            arSceneView!!.destroy()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Standard Android full-screen functionality.
            window
                .decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun onSingleTap(tap: MotionEvent) {
        if (!hasFinishedLoading) {
            // We can't do anything yet.
            return
        }
        val frame = arSceneView!!.arFrame
        if (frame != null) {
            if (!hasPlacedSolarSystem && tryPlaceSolarSystem(tap, frame)) {
                hasPlacedSolarSystem = true
            }
        }
    }

    private fun tryPlaceSolarSystem(
        tap: MotionEvent?,
        frame: Frame
    ): Boolean {
        if (tap != null && frame.camera.trackingState == TrackingState.TRACKING) {
            for (hit in frame.hitTest(tap)) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon( hit.hitPose)) {
                    // Create the Anchor.
                    val anchor = hit.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arSceneView!!.scene)
                    val solarSystem = createSolarSystem()
                    anchorNode.addChild(solarSystem)
                    return true
                }
            }
        }
        return false
    }

    private fun createSolarSystem(): Node {
        val base = Node()
        val sun = Node()
        sun.setParent(base)
        sun.localPosition = Vector3(0.0f, 0.5f, 0.0f)
        val sunVisual = Node()
        sunVisual.setParent(sun)
        sunVisual.renderable = sunRenderable
        sunVisual.localScale = Vector3(0.5f, 0.5f, 0.5f)
        val solarControls = Node()
        solarControls.setParent(sun)
        solarControls.renderable = solarControlsRenderable
        solarControls.localPosition = Vector3(0.0f, 0.5f, 0.0f)
        val solarControlsView = solarControlsRenderable!!.view
        val orbitSpeedBar = solarControlsView.findViewById<SeekBar>(R.id.orbitSpeedBar)
        orbitSpeedBar.progress = (solarSettings.orbitSpeedMultiplier * 10.0f).toInt()
        orbitSpeedBar.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val ratio =
                        progress.toFloat() / orbitSpeedBar.max.toFloat()
                    solarSettings.orbitSpeedMultiplier = ratio * 10.0f
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        val rotationSpeedBar = solarControlsView.findViewById<SeekBar>(R.id.rotationSpeedBar)
        rotationSpeedBar.progress = (solarSettings.rotationSpeedMultiplier * 10.0f).toInt()
        rotationSpeedBar.setOnSeekBarChangeListener(
            object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val ratio =
                        progress.toFloat() / rotationSpeedBar.max.toFloat()
                    solarSettings.rotationSpeedMultiplier = ratio * 10.0f
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

        // Toggle the solar controls on and off by tapping the sun.
        sunVisual.setOnTapListener { hitTestResult: HitTestResult?, motionEvent: MotionEvent? ->
            solarControls.isEnabled = !solarControls.isEnabled
        }
        createPlanet(getString(R.string.mercury_label), sun, 0.4f, 47f, mercuryRenderable, 0.019f, 0.03f)
        createPlanet(getString(R.string.venus_label), sun, 0.7f, 35f, venusRenderable, 0.0475f, 2.64f)
        val earth = createPlanet(getString(R.string.earth_label), sun, 1.0f, 29f, earthRenderable, 0.05f, 23.4f)
        createPlanet(getString(R.string.moon_label), earth, 0.15f, 100f, lunaRenderable, 0.018f, 6.68f)
        createPlanet(getString(R.string.mars_label), sun, 1.5f, 24f, marsRenderable, 0.0265f, 25.19f)
        createPlanet(getString(R.string.jupiter_label), sun, 2.2f, 13f, jupiterRenderable, 0.16f, 3.13f)
        createPlanet(getString(R.string.saturn_label), sun, 3.5f, 9f, saturnRenderable, 0.1325f, 26.73f)
        createPlanet(getString(R.string.uranus_label), sun, 5.2f, 7f, uranusRenderable, 0.1f, 82.23f)
        createPlanet(getString(R.string.neptune_label), sun, 6.1f, 5f, neptuneRenderable, 0.074f, 28.32f)
        return base
    }

    private fun createPlanet(
        name: String,
        parent: Node,
        auFromParent: Float,
        orbitDegreesPerSecond: Float,
        renderable: ModelRenderable?,
        planetScale: Float,
        axisTilt: Float
    ): Node {
        // Orbit is a rotating node with no renderable positioned at the sun.
        // The planet is positioned relative to the orbit so that it appears to rotate around the sun.
        // This is done instead of making the sun rotate so each planet can orbit at its own speed.
        val orbit = RotatingNode(solarSettings, true, false, 0F)
        orbit.setDegreesPerSecond(orbitDegreesPerSecond)
        orbit.setParent(parent)

        // Create the planet and position it relative to the sun.
        val planet = Planet(
            this, name, planetScale, orbitDegreesPerSecond, axisTilt, renderable, solarSettings
        )
        planet.setParent(orbit)
        planet.localPosition = Vector3(
            auFromParent * AU_TO_METERS,
            0.0f,
            0.0f
        )
        return planet
    }

    private fun showLoadingMessage() {
        if (loadingMessageSnackbar != null && (loadingMessageSnackbar as Snackbar).isShownOrQueued) {
            return
        }
        loadingMessageSnackbar = Snackbar.make(
            this@SolarActivity.findViewById(android.R.id.content),
            R.string.plane_finding,
            Snackbar.LENGTH_INDEFINITE
        )
        loadingMessageSnackbar?.view?.setBackgroundColor(-0x40cdcdce)
        loadingMessageSnackbar?.show()
    }

    private fun hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return
        }
        (loadingMessageSnackbar as Snackbar).dismiss()
        loadingMessageSnackbar = null
    }

    companion object {
        const val RC_PERMISSIONS = 0x123

        // Astronomical units to meters ratio. Used for positioning the planets of the solar system.
        const val AU_TO_METERS = 0.5f
    }
}