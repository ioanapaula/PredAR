package com.example.predar.activities

import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.predar.R
import com.example.predar.databinding.ActivityLessonBinding
import com.example.predar.fragments.LessonFragment
import com.example.predar.helpers.FirebaseDatabaseManager
import com.example.predar.helpers.ResolveDialog
import com.example.predar.viewModels.LessonViewModel
import com.example.predar.viewModels.LessonViewModel.AppAnchorState
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


open class LessonActivity: AppCompatActivity() {
    protected lateinit var binding: ActivityLessonBinding
    private lateinit var viewModel: LessonViewModel

    protected var arCoreFragment: LessonFragment? = null
    protected var arObjectUri: String? = null
    var arObjectNode: TransformableNode? = null
    private var cloudAnchor: Anchor? = null
    private var appAnchorState = AppAnchorState.NONE

    private var firebaseDatabaseManager: FirebaseDatabaseManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LessonViewModel::class.java)

        arCoreFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as LessonFragment?
        arCoreFragment?.arSceneView?.scene?.addOnUpdateListener { onUpdateFrame() }
        arCoreFragment?.planeDiscoveryController?.hide()

        firebaseDatabaseManager = FirebaseDatabaseManager(this)

        initListeners()
    }

//    This method is responsible for setting listeners on clear and resolve buttons.
//    It also sets the TapArPlaneListener on ArCoreFragment and hides plane discovery controller to disable the hand gesture (optional)
    private fun initListeners() {
        binding.clearButton.setOnClickListener {
            setCloudAnchor(null)
        }

        binding.resolveButton.setOnClickListener(View.OnClickListener {
            ResolveDialog(
                this,
                object : ResolveDialog.PositiveButtonListener {
                    override fun onPositiveButtonClicked(dialogValue: String) {
                        resolveAnchor(dialogValue)
                    }
                },
                "Resolve",
                View.VISIBLE,
                View.VISIBLE
            ).show()
        })

       arCoreFragment?.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING || appAnchorState != AppAnchorState.NONE) { }
            if (cloudAnchor == null){
                val newAnchor = arCoreFragment?.arSceneView?.session?.hostCloudAnchor(hitResult.createAnchor())
                setCloudAnchor(newAnchor)
                appAnchorState = AppAnchorState.HOSTING
                showMessage("Now hosting anchor...")
            }

            arCoreFragment?.let { placeObject(it, cloudAnchor, Uri.parse(arObjectUri)) }
            arObjectUri?.let { it -> firebaseDatabaseManager?.storeModelUri(it) }
        }
    }

    fun resolveAnchor(dialogValue: String) {
        val shortCode = Integer.parseInt(dialogValue)
        firebaseDatabaseManager?.getCloudAnchorID(shortCode, object :
            FirebaseDatabaseManager.CloudAnchorIdListener {
            override fun onCloudAnchorIdAvailable(cloudAnchorId: String?) {
                val resolvedAnchor = arCoreFragment?.arSceneView?.session?.resolveCloudAnchor(cloudAnchorId)
                setCloudAnchor(resolvedAnchor)
                showMessage("Now Resolving Anchor...")

                appAnchorState = AppAnchorState.RESOLVING
            }
        })
        firebaseDatabaseManager?.getModelUri( object: FirebaseDatabaseManager.ModelUriListener {
            override fun onModelUriAvailable(modelUri: String?) {
                arCoreFragment?.let { placeObject(it, cloudAnchor, Uri.parse(modelUri)) }
            }
        })
    }

    //    This method sets a new Cloud Anchor and ensures there is only one cloud anchor at any point of time
     fun setCloudAnchor(newAnchor: Anchor?) {
        if (cloudAnchor != null) {
            cloudAnchor?.detach()
        }

        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
    }

    fun placeObject(fragment: ArFragment, anchor: Anchor?, model: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, model)
            .build()
            .thenAccept { renderable -> addNodeToScene(fragment, anchor, renderable) }
            .exceptionally { throwable ->
                val builder = android.app.AlertDialog.Builder(this)
                builder.setMessage(throwable.message)
                    .setTitle("Error!")
                val dialog = builder.create()
                dialog.show()
                null
            }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor?, renderable: Renderable) {
       if (arObjectNode != null){
           arObjectNode?.renderable = null
       }
        val anchorNode = AnchorNode(anchor)
        arObjectNode = TransformableNode(arCoreFragment?.transformationSystem)
        arObjectNode?.renderable = renderable
        arObjectNode?.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        arObjectNode?.select()
    }

    private fun onUpdateFrame() {
        checkUpdatedAnchor()
    }

    @Synchronized
    private fun checkUpdatedAnchor() {
        if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING) {
            return
        }
        val cloudState = cloudAnchor?.cloudAnchorState
        cloudState?.let { it ->
            if (appAnchorState == AppAnchorState.HOSTING) {
                if (it.isError) {
                    Toast.makeText(this, "Error hosting anchor.. $it", Toast.LENGTH_LONG).show()

                    appAnchorState = AppAnchorState.NONE
                } else if (it == Anchor.CloudAnchorState.SUCCESS) {
                    firebaseDatabaseManager?.nextShortCode(object :
                        FirebaseDatabaseManager.ShortCodeListener {
                        override fun onShortCodeAvailable(shortCode: Int?) {
                            if (shortCode == null) {
                                showMessage("Could not get shortCode")
                                return
                            }
                            cloudAnchor?.let {
                                firebaseDatabaseManager?.storeUsingShortCode(
                                    shortCode,
                                    it.cloudAnchorId
                                )
                            }

                            showMessage("Anchor hosted! Cloud Short Code: $shortCode")
                        }

                    })
                    appAnchorState = AppAnchorState.HOSTED
                }
            } else if (appAnchorState == AppAnchorState.RESOLVING) {
                if (it.isError) {
                    Toast.makeText(this, "Error hosting anchor.. $it", Toast.LENGTH_LONG).show()

                    appAnchorState = AppAnchorState.NONE
                } else if (it == Anchor.CloudAnchorState.SUCCESS) {
                    Toast.makeText(this, "Anchor resolved successfully", Toast.LENGTH_LONG).show()

                    appAnchorState = AppAnchorState.RESOLVED
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}