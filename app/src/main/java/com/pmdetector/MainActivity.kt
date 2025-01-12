Maintenant, créons le fichier de la classe principale MainActivity.

Créez le fichier `app/src/main/java/com/pmdetector/MainActivity.kt` avec ce contenu :

```kotlin
package com.pmdetector

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pmdetector.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val SCREEN_CAPTURE_REQUEST_CODE = 100
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var isServiceRunning = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.startButton.setOnClickListener {
            if (!isServiceRunning) startScreenCapture() else stopService()
        }
    }
    
    private fun startScreenCapture() {
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, SCREEN_CAPTURE_REQUEST_CODE)
    }
    
    private fun stopService() {
        stopService(Intent(this, ScreenCaptureService::class.java))
        isServiceRunning = false
        binding.startButton.text = getString(R.string.start_monitoring)
        binding.statusText.text = getString(R.string.monitoring_stopped)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startService(ScreenCaptureService.newIntent(this, resultCode, data))
            isServiceRunning = true
            binding.startButton.text = getString(R.string.stop_monitoring)
            binding.statusText.text = getString(R.string.monitoring_active)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
```

Dites-moi quand c'est fait et je vous donnerai le fichier suivant.