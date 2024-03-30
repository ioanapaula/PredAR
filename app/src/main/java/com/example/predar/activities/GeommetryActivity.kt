package com.example.predar.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import com.example.predar.R
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch

class GeommetryActivity: LessonActivity() {
    private var modelUri = "cube.sfb"
    private var selectedGalleryItem: ImageView? = null
    private lateinit var arModel: ModelRenderable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(super.binding.root)

        super.binding.assetGalleryContainer.visibility = View.VISIBLE

        initializeGallery()
    }

    private fun initializeGallery() {
        var cubeIcon = ImageView(this)
        cubeIcon.setImageResource(R.drawable.cube_icon)
        cubeIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE
        cubeIcon.setOnClickListener { onGalleryItemSelected(cubeIcon, "cube.sfb")  }
        super.binding.assetGallery.addView(cubeIcon)

        var sphereIcon = ImageView(this)
        sphereIcon.setImageResource(R.drawable.sphere_icon)
        sphereIcon.setOnClickListener { onGalleryItemSelected(sphereIcon, "sphere.sfb")  }
        super.binding.assetGallery.addView(sphereIcon)

        var cuboidIcon = ImageView(this)
        cuboidIcon.setImageResource(R.drawable.cuboid_icon)
        cuboidIcon.setOnClickListener { onGalleryItemSelected(cuboidIcon, "paralelipiped.sfb")  }
        super.binding.assetGallery.addView(cuboidIcon)

        var cilinderIcon = ImageView(this)
        cilinderIcon.setImageResource(R.drawable.cilinder_icon)
        cilinderIcon.setOnClickListener { onGalleryItemSelected(cilinderIcon, "cilinder.sfb")  }
        super.binding.assetGallery.addView(cilinderIcon)

        var coneIcon = ImageView(this)
        coneIcon.setImageResource(R.drawable.cone_icon)
        coneIcon.setOnClickListener { onGalleryItemSelected(coneIcon, "cone.sfb")  }
        super.binding.assetGallery.addView(coneIcon)

        var snowmanIcon = ImageView(this)
        snowmanIcon.setImageResource(R.drawable.snowman_icon)
        snowmanIcon.setOnClickListener { onGalleryItemSelected(snowmanIcon, "Snowman.sfb")  }
        super.binding.assetGallery.addView(snowmanIcon)

        var trafficIcon = ImageView(this)
        trafficIcon.setImageResource(R.drawable.traffic_light_icon)
        trafficIcon.setOnClickListener { onGalleryItemSelected(trafficIcon, "TrafficLight.sfb")  }
        super.binding.assetGallery.addView(trafficIcon)

        var closetIcon = ImageView(this)
        closetIcon.setImageResource(R.drawable.closet_icon)
        closetIcon.setOnClickListener { onGalleryItemSelected(closetIcon, "Closet2.sfb")  }
        super.binding.assetGallery.addView(closetIcon)

        var bedIcon = ImageView(this)
        bedIcon.setImageResource(R.drawable.bedking_icon)
        bedIcon.setOnClickListener { onGalleryItemSelected(bedIcon, "BedKing.sfb")  }
        super.binding.assetGallery.addView(bedIcon)

        var bookcaseIcon = ImageView(this)
        bookcaseIcon.setImageResource(R.drawable.bookcase_icon)
        bookcaseIcon.setOnClickListener { onGalleryItemSelected(bookcaseIcon, "BookCaseBooks.sfb")  }
        super.binding.assetGallery.addView(bookcaseIcon)

        var cakeIcon = ImageView(this)
        cakeIcon.setImageResource(R.drawable.cake_icon)
        cakeIcon.setOnClickListener { onGalleryItemSelected(cakeIcon, "Cake.sfb")  }
        super.binding.assetGallery.addView(cakeIcon)
    }

    private fun onGalleryItemSelected(modelIcon: ImageView, modelUriPath: String) {
        modelIcon.background = getDrawable(R.drawable.gallery_item_background)
        if (selectedGalleryItem != null){
            selectedGalleryItem?.background = null
        }

        selectedGalleryItem = modelIcon
        arObjectUri = modelUriPath
        //loadModel()
    }

    private fun loadModel() {
        lifecycleScope.launch {
            arModel = ModelRenderable
                .builder()
                .setSource(
                    arCoreFragment?.context,
                    Uri.parse(modelUri)
                )
                .build()
                .await()

            initTapListener()
        }
    }

    private fun initTapListener() {
        super.arCoreFragment?.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchorNode = AnchorNode(
                hitResult.createAnchor()
            )
            anchorNode.setParent(arCoreFragment?.arSceneView?.scene)
            val arNode = TransformableNode(arCoreFragment?.transformationSystem)
            arNode.renderable = arModel
            arNode.setParent(anchorNode)
        }
    }
}