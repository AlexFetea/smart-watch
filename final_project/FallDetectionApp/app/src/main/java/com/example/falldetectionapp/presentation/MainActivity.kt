package com.example.falldetectionapp.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.falldetectionapp.R
import okhttp3.*
import weka.classifiers.Classifier
import weka.core.DenseInstance
import weka.core.Instances
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.IOException
import java.util.LinkedList
import kotlin.concurrent.fixedRateTimer
import kotlin.math.pow
import kotlin.math.sqrt
import java.util.Timer
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var accelerometerTextView: TextView
    private lateinit var classificationTextView: TextView

    private lateinit var classifier: Classifier
    private lateinit var datasetFormat: Instances

    private val windowSize = 1000
    private val accelerometerData = ArrayDeque<FloatArray>(windowSize)
    private val lock = Any()

    private val pushoverToken = "SECRET_TOKEN"
    private val pushoverUserKey = "USER_KEY"

    private var classifyTimer: Timer? = null
    private var lastNotificationTime = 0L

    private lateinit var wakeLock: PowerManager.WakeLock // Partial wake lock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep the screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FallDetectionApp::WakeLock")
        wakeLock.acquire()

        accelerometerTextView = findViewById(R.id.accelerometerTextView)
        classificationTextView = findViewById(R.id.classificationTextView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        } ?: run {
            accelerometerTextView.text = "Accelerometer not available"
        }

        try {
            loadModel()
            Toast.makeText(this, "Model loaded successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show()
        }

        classifyTimer = fixedRateTimer("classifyTimer", initialDelay = 1000, period = 1000) {
            synchronized(lock) {
                classifyWindow()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        classifyTimer?.cancel()

        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            synchronized(lock) {
                if (accelerometerData.size >= windowSize) {
                    accelerometerData.removeFirst()
                }
                accelerometerData.addLast(event.values.clone())
            }

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            runOnUiThread {
                accelerometerTextView.text = "Accelerometer\nX: $x\nY: $y\nZ: $z"
            }
        }
    }

    private fun loadModel() {
        val modelInputStream: InputStream = assets.open("binary_spike.model")
        val objectInputStream = ObjectInputStream(modelInputStream)
        classifier = objectInputStream.readObject() as Classifier
        objectInputStream.close()

        val arffStream: InputStream = assets.open("dataset_format.arff")
        datasetFormat = Instances(arffStream.reader())
        datasetFormat.setClassIndex(datasetFormat.numAttributes() - 1)
    }

    private fun List<Float>.standardDeviation(): Double {
        val mean = this.average()
        return sqrt(this.sumOf { (it - mean).pow(2) } / this.size)
    }

    private fun classifyWindow() {
        if (accelerometerData.size < windowSize) {
            Log.d("FallDetection", "Not enough data to classify. Accelerometer size: ${accelerometerData.size}")
            return
        }

        val stats = FloatArray(13)
        synchronized(lock) {
            for (i in 0..2) {
                val data = accelerometerData.map { it[i] }
                stats[i * 4] = data.average().toFloat() // Mean
                stats[i * 4 + 1] = data.standardDeviation().toFloat() // Std
                stats[i * 4 + 2] = (data.maxOrNull() ?: 0f) - (data.minOrNull() ?: 0f) // Range
                stats[i * 4 + 3] = data.zipWithNext { a, b -> kotlin.math.abs(a - b) }.maxOrNull() ?: 0f // Max Spike
            }

            val totalAccData = accelerometerData.map { sqrt(it[0].pow(2) + it[1].pow(2) + it[2].pow(2)) }
            stats[12] = totalAccData.average().toFloat()
        }

        if (datasetFormat.numAttributes() != stats.size + 1) {
            Log.e(
                "FallDetection",
                "Dataset format mismatch: Expected ${datasetFormat.numAttributes()} attributes, but got ${stats.size + 1}"
            )
            return
        }

        val instance = DenseInstance(datasetFormat.numAttributes())
        instance.setDataset(datasetFormat)
        stats.forEachIndexed { index, value ->
            instance.setValue(index, value.toDouble())
        }

        try {
            val result = classifier.classifyInstance(instance)
            val activityClass = datasetFormat.classAttribute().value(result.toInt())

            runOnUiThread {
                if (!isDestroyed && !isFinishing) {
                    classificationTextView.text = "Classified as: $activityClass"
                    val rootView = window.decorView.rootView
                    rootView.setBackgroundColor(
                        if (activityClass == "fall") resources.getColor(android.R.color.holo_red_light, theme)
                        else resources.getColor(android.R.color.background_light, theme)
                    )
                }
            }

            Log.d("FallDetection", "Classified activity: $activityClass")

            val currentTime = System.currentTimeMillis()
            if (activityClass == "fall" && currentTime - lastNotificationTime >= TimeUnit.MINUTES.toMillis(1)) {
                lastNotificationTime = currentTime
                sendPushoverNotification("Fall Detection", "Fall detected!")
            }
        } catch (e: Exception) {
            Log.e("FallDetection", "Error during classification: ${e.localizedMessage}", e)
        }
    }



    private fun sendPushoverNotification(title: String, message: String) {
        val client = OkHttpClient()

        val formBody = FormBody.Builder()
            .add("token", pushoverToken)
            .add("user", pushoverUserKey)
            .add("title", title)
            .add("message", message)
            .build()

        val request = Request.Builder()
            .url("https://api.pushover.net/1/messages.json")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Pushover", "Notification failed from onFailure: ${e.localizedMessage}", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("Pushover", "Notification sent successfully")
                } else {
                    Log.e("Pushover", "Notification Failed: ${response.code} ${response.message}")
                    response.body?.let { body ->
                        Log.e("Pushover", "Response body: ${body.string()}")
                    }
                }
            }
        })
    }
}
