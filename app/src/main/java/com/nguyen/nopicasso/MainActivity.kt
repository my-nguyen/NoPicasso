package com.example.nopicasso

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import com.nguyen.nopicasso.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val URL = "https://rkpandey.com/images/rkpDavidson.jpg"
    }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // withPicasso()
        // withMainThread()
        // withNetworkThread()
        withCoroutines()
    }

    private fun withPicasso() {
        Picasso.get().load(URL).into(binding.image)
    }

    private fun withMainThread() {
        val policy = StrictMode.ThreadPolicy
                .Builder()
                .permitNetwork()
                .build()
        StrictMode.setThreadPolicy(policy)
        val bitmap = downloadBitmap(URL)
        binding.image.setImageBitmap(bitmap)
    }

    private fun withNetworkThread() {
        val handler = Handler(Looper.getMainLooper())
        thread(start=true) {
            Log.i(TAG, "Current thread ${Thread.currentThread().name}")
            val bitmap = downloadBitmap(URL)
            handler.post {
                Log.i(TAG, "Current thread in the UI handler: ${Thread.currentThread().name}")
                binding.image.setImageBitmap(bitmap)
            }
        }
    }

    private fun withCoroutines() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(TAG, "Current thread ${Thread.currentThread().name}")
            val bitmap = downloadBitmap(URL)
            withContext(Dispatchers.Main) {
                Log.i(TAG, "Current thread in the main dispatcher ${Thread.currentThread().name}")
                binding.image.setImageBitmap(bitmap)
            }
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connect()
            val stream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(stream)
            stream.close()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            null
        }
    }
}