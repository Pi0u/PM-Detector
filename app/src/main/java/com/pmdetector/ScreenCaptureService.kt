Maintenant créons le service de capture d'écran.

Créez le fichier `app/src/main/java/com/pmdetector/ScreenCaptureService.kt` avec ce contenu :

```kotlin
package com.pmdetector

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.*

class ScreenCaptureService : Service() {
    private lateinit var mediaProjection: MediaProjection
    private lateinit var virtualDisplay: VirtualDisplay
    private lateinit var imageReader: ImageReader
    private val textRecognizer = TextRecognition.getClient()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private val pmAnalyzer = PMAnalyzer()
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, 0) ?: return START_NOT_STICKY
        val data = intent.getParcelableExtra<Intent>(EXTRA_DATA) ?: return START_NOT_STICKY
        
        startForeground(NOTIFICATION_ID, createNotification())
        startProjection(resultCode, data)
        
        return START_STICKY
    }
    
    private fun startProjection(resultCode: Int, data: Intent) {
        val metrics = resources.displayMetrics
        imageReader = ImageReader.newInstance(
            metrics.widthPixels,
            metrics.heightPixels,
            PixelFormat.RGBA_8888,
            2
        )
        
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            metrics.widthPixels,
            metrics.heightPixels,
            metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )
        
        startPeriodicCapture()
    }
    
    private fun startPeriodicCapture() {
        serviceScope.launch {
            while (isActive) {
                captureAndAnalyzeScreen()
                delay(30000) // Capture every 30 seconds
            }
        }
    }
    
    private fun captureAndAnalyzeScreen() {
        val image = imageReader.acquireLatestImage() ?: return
        val bitmap = imageToBitmap(image)
        image.close()
        
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(inputImage)
            .addOnSuccessListener { text ->
                val pmNumbers = pmAnalyzer.extractPMNumbers(text.text)
                checkForDuplicates(pmNumbers)
            }
    }
    
    private fun checkForDuplicates(pmNumbers: List<String>) {
        if (pmNumbers.isNotEmpty()) {
            showDuplicateNotification(pmNumbers)
        }
    }
    
    private fun showDuplicateNotification(pmNumbers: List<String>) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PM Doubles détectés !")
            .setContentText("PMs trouvés : ${pmNumbers.joinToString()}")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    override fun onDestroy() {
        serviceScope.cancel()
        virtualDisplay.release()
        mediaProjection.stop()
        super.onDestroy()
    }
    
    companion object {
        private const val CHANNEL_ID = "PMDetectorChannel"
        private const val NOTIFICATION_ID = 1
        private const val EXTRA_RESULT_CODE = "RESULT_CODE"
        private const val EXTRA_DATA = "DATA"
        
        fun newIntent(context: Context, resultCode: Int, data: Intent?): Intent {
            return Intent(context, ScreenCaptureService::class.java).apply {
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_DATA, data)
            }
        }
    }
}
```

Dites-moi quand c'est fait et je vous donnerai le dernier fichier de code Kotlin.