package com.andreibelous.plankdetektor

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.view.SurfaceView
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.andreibelous.plankdetektor.camera.CameraSource
import com.andreibelous.plankdetektor.feature.PlankFeature
import com.andreibelous.plankdetektor.feature.data.AttemptsDataSource
import com.andreibelous.plankdetektor.feature.mapper.NewsToViewAction
import com.andreibelous.plankdetektor.feature.mapper.StateToViewModel
import com.andreibelous.plankdetektor.feature.mapper.UiEventToWish
import com.andreibelous.plankdetektor.ml.MoveNet
import com.badoo.binder.Binder
import com.badoo.binder.using
import com.badoo.mvicore.android.lifecycle.CreateDestroyBinderLifecycle
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        private const val FRAGMENT_DIALOG = "dialog"
    }

    private lateinit var surfaceView: SurfaceView
    private var disposables: CompositeDisposable? = null

    private var cameraSource: CameraSource? = null
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openCamera()
            } else {
                ErrorDialog
                    .newInstance(getString(R.string.tfe_pe_request_permission))
                    .show(supportFragmentManager, FRAGMENT_DIALOG)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // keep screen on while app is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        surfaceView = findViewById(R.id.surfaceView)
        if (!isCameraPermissionGranted()) {
            requestPermission()
        }

        disposables = CompositeDisposable()

        val view = MainView(this)
        val dataSource = AttemptsDataSource(this)
        val feature = PlankFeature(dataSource)
        disposables?.add(feature)

        with(Binder(CreateDestroyBinderLifecycle(lifecycle))) {
            bind(view to feature using UiEventToWish)
            bind(feature to view using StateToViewModel)
            bind(view to ::handleUiEvent.asConsumer())
            bind(feature.news to view::execute.asConsumer() using NewsToViewAction)
        }
    }

    private fun handleUiEvent(event: MainView.Event) {
        when (event) {
            is MainView.Event.CloseClicked -> {
                dispose()
                finish()
            }
            else -> Unit
        }
    }

    private fun dispose() {
        disposables?.dispose()
        disposables = null
    }

    override fun onStart() {
        super.onStart()
        openCamera()
    }

    override fun onResume() {
        cameraSource?.resume()
        super.onResume()
    }

    override fun onPause() {
        cameraSource?.close()
        cameraSource = null
        super.onPause()
    }

    private fun isCameraPermissionGranted(): Boolean =
        checkPermission(
            Manifest.permission.CAMERA,
            Process.myPid(),
            Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED

    private fun openCamera() {
        if (isCameraPermissionGranted()) {
            if (cameraSource == null) {
                cameraSource =
                    CameraSource(surfaceView, lifecycleScope).apply {
                        prepareCamera()
                    }
                lifecycleScope.launch(Dispatchers.Main) {
                    cameraSource?.initCamera()
                }
            }
            createPoseEstimator()
        }
    }

    private fun createPoseEstimator() =
        MoveNet.create(this).also { cameraSource?.setDetector(it) }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> openCamera()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    // do nothing
                }
                .create()

        companion object {

            private const val ARG_MESSAGE = "message"

            fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }
}
