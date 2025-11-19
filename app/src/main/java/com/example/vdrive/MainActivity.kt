package com.example.vdrive

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.vdrive.overlay.OverlayService
import com.google.android.material.switchmaterial.SwitchMaterial
import android.widget.ImageView // Import adicionado para ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge

class MainActivity : AppCompatActivity() {

    private lateinit var switchLigarApp: SwitchMaterial

    companion object {
        const val PREFS_NAME = "MainSettings"
        const val KEY_SWITCH_STATE = "switchState"
    }

    // Função que você tinha para os cliques dos ícones
    private fun setupIconClickListeners() {
        val ivSettings: ImageView = findViewById(R.id.iv_settings) // Assumindo R.id.iv_settings
        val ivSettingsRaces: ImageView = findViewById(R.id.iv_settings_races) // Assumindo R.id.iv_settings_races

        ivSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        ivSettingsRaces.setOnClickListener {
            val intent = Intent(this, SettingsRacesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getActionBarHeight(): Int {
        val typedValue = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            return TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        }
        return 0
    }

    private val requestOverlayPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Permissão de sobreposição é necessária.", Toast.LENGTH_SHORT).show()
                switchLigarApp.isChecked = false
            } else {
                startOverlayService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val actionBarSize = getActionBarHeight()
            v.layoutParams.height = actionBarSize + systemBars.top
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        switchLigarApp = findViewById(R.id.switchLigarApp) // Ajuste o ID se for diferente

        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isSwitchOn = sharedPrefs.getBoolean(KEY_SWITCH_STATE, false)
        switchLigarApp.isChecked = isSwitchOn

        switchLigarApp.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(KEY_SWITCH_STATE, isChecked).apply()
            if (isChecked) {
                checkAndStartOverlay()
            } else {
                stopOverlayService()
            }
        }

        // Chamada para configurar os listeners dos ícones
        setupIconClickListeners()
    }

    private fun checkAndStartOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog()
        } else {
            startOverlayService()
        }
    }

    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissão Necessária")
            .setMessage("Para exibir o ícone flutuante, o vDrive precisa da permissão para 'Aparecer sobre outros apps'.")
            .setPositiveButton("Ativar Permissão") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                requestOverlayPermission.launch(intent)
            }
            .setNegativeButton("Cancelar") { _, _ ->
                switchLigarApp.isChecked = false
            }
            .show()
    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        // Apenas startService, sem startForegroundService e sem dados de MediaProjection aqui.
        startService(serviceIntent)
    }

    private fun stopOverlayService() {
        stopService(Intent(this, OverlayService::class.java))
    }
}