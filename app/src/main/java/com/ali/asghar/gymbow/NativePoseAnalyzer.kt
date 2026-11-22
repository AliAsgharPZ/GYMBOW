package com.ali.asghar.gymbow

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

data class PosePoint(val x: Float, val y: Float, val z: Float, val visibility: Float)

class NativePoseAnalyzer(
    context: Context,
    private val onPose: (List<PosePoint>) -> Unit,
    private val onError: (String) -> Unit
) : ImageAnalysis.Analyzer, Closeable {
    private val busy = AtomicBoolean(false)
    private val landmarker: PoseLandmarker
    @Volatile private var closed = false

    init {
        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("pose_landmarker_full.task").build())
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.55f)
            .setMinPosePresenceConfidence(0.55f)
            .setMinTrackingConfidence(0.55f)
            .setResultListener { result, _ ->
                busy.set(false)
                val pose = result.worldLandmarks().firstOrNull().orEmpty().map { p ->
                    PosePoint(p.x(), p.y(), p.z(), p.visibility().orElse(0f))
                }
                onPose(pose)
            }
            .setErrorListener { error ->
                busy.set(false)
                Log.e("GYMBOW-Pose", error.message.orEmpty())
                onError(error.message ?: "Pose detection failed")
            }
            .build()
        landmarker = PoseLandmarker.createFromOptions(context, options)
    }

    override fun analyze(image: ImageProxy) {
        if (closed || !busy.compareAndSet(false, true)) {
            image.close()
            return
        }
        try {
            val bitmap: Bitmap = image.toBitmap()
            val mpImage = BitmapImageBuilder(bitmap).build()
            val processing = ImageProcessingOptions.builder()
                .setRotationDegrees(image.imageInfo.rotationDegrees)
                .build()
            landmarker.detectAsync(mpImage, processing, SystemClock.uptimeMillis())
        } catch (t: Throwable) {
            busy.set(false)
            onError(t.message ?: "Could not process camera frame")
        } finally {
            image.close()
        }
    }

    override fun close() {
        closed = true
        landmarker.close()
    }
}

object PoseMath {
    fun angle(a: PosePoint, b: PosePoint, c: PosePoint): Int {
        val abx = a.x - b.x
        val aby = a.y - b.y
        val abz = a.z - b.z
        val cbx = c.x - b.x
        val cby = c.y - b.y
        val cbz = c.z - b.z
        val dot = abx * cbx + aby * cby + abz * cbz
        val mag = kotlin.math.sqrt((abx * abx + aby * aby + abz * abz) * (cbx * cbx + cby * cby + cbz * cbz))
        if (mag <= 1e-6f) return 0
        return Math.toDegrees(kotlin.math.acos((dot / mag).coerceIn(-1f, 1f)).toDouble()).toInt()
    }
}
