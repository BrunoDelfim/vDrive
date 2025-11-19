package com.example.vdrive.overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.vdrive.R
import com.example.vdrive.SettingsActivity
import kotlin.math.roundToInt

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var positionPreferences: SharedPreferences
    private lateinit var settingsPreferences: SharedPreferences

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    companion object {
        const val POSITION_PREFS_NAME = "OverlayPositionPrefs"
        const val LAST_X_KEY = "lastX"
        const val LAST_Y_KEY = "lastY"
    }

    private val settingsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SettingsActivity.ACTION_UPDATE_SETTINGS) {
                updateOverlayFromSettings()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        positionPreferences = getSharedPreferences(POSITION_PREFS_NAME, Context.MODE_PRIVATE)
        settingsPreferences = getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE)

        val filter = IntentFilter(SettingsActivity.ACTION_UPDATE_SETTINGS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(settingsUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(settingsUpdateReceiver, filter)
        }

        setupOverlayView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOverlayView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_bubble, null)
        val initialSizePx = getSizeInPx()
        val initialOpacity = getOpacity()

        params = WindowManager.LayoutParams(
            initialSizePx, initialSizePx,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        overlayView.alpha = initialOpacity

        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = positionPreferences.getInt(LAST_X_KEY, 30)
        params.y = positionPreferences.getInt(LAST_Y_KEY, 100)

        windowManager.addView(overlayView, params)

        // Este listener de toque agora não tem mais o "TESTE" de atualização
        overlayView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).roundToInt()
                    params.y = initialY + (event.rawY - initialTouchY).roundToInt()
                    windowManager.updateViewLayout(overlayView, params)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    salvarPosicao()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun updateOverlayFromSettings() {
        val newSizePx = getSizeInPx()
        val newOpacity = getOpacity()
        params.width = newSizePx
        params.height = newSizePx
        overlayView.alpha = newOpacity
        windowManager.updateViewLayout(overlayView, params)
    }

    private fun getSizeInPx(): Int {
        val sizeString = settingsPreferences.getString(SettingsActivity.KEY_ICON_SIZE, "Médio")
        val sizeDp = when (sizeString) {
            "Pequeno" -> 48
            "Médio" -> 56
            "Grande" -> 72
            else -> 56
        }
        return (sizeDp * resources.displayMetrics.density).toInt()
    }

    private fun getOpacity(): Float {
        val opacityInt = settingsPreferences.getInt(SettingsActivity.KEY_OPACITY, 100)
        return opacityInt / 100f
    }

    private fun salvarPosicao() {
        positionPreferences.edit().putInt(LAST_X_KEY, params.x).putInt(LAST_Y_KEY, params.y).apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        salvarPosicao()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        unregisterReceiver(settingsUpdateReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retornamos START_STICKY para que o serviço tente reiniciar se for morto pelo sistema
        // Este serviço NÃO é um foreground service aqui, então não precisa de notificação.
        return START_STICKY
    }
}