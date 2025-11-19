package com.example.vdrive

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvTamanhoValor: TextView
    private lateinit var clTamanhoContainer: ConstraintLayout
    private lateinit var sbOpacidade: SeekBar
    private lateinit var tvOpacidadeValor: TextView

    companion object {
        const val PREFS_NAME = "OverlaySettings"
        const val KEY_ICON_SIZE = "iconSize"
        const val KEY_OPACITY = "opacity"
        private const val TAG = "SettingsActivity"
        const val ACTION_UPDATE_SETTINGS = "vDrive.app.main.ACTION_UPDATE_SETTINGS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val toolbar: Toolbar = findViewById(R.id.toolbar_settings)
        setSupportActionBar(toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val actionBarSize = getActionBarHeight()
            v.layoutParams.height = actionBarSize + systemBars.top
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Configurações"

        tvTamanhoValor = findViewById(R.id.tv_tamanho_valor)
        clTamanhoContainer = findViewById(R.id.cl_tamanho_container)

        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSize = sharedPrefs.getString(KEY_ICON_SIZE, "Médio")
        tvTamanhoValor.text = savedSize

        clTamanhoContainer.setOnClickListener { showSizeSelectionDialog() }

        sbOpacidade = findViewById(R.id.sb_opacidade)
        tvOpacidadeValor = findViewById(R.id.tv_opacidade_valor)

        val savedOpacity = sharedPrefs.getInt(KEY_OPACITY, 100)
        sbOpacidade.progress = savedOpacity
        tvOpacidadeValor.text = "$savedOpacity%"

        sbOpacidade.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tvOpacidadeValor.text = "$progress%"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val opacity = seekBar?.progress ?: 100
                sharedPrefs.edit().putInt(KEY_OPACITY, opacity).apply()
                sendSettingsUpdateBroadcast()
            }
        })
    }

    private fun showSizeSelectionDialog() {
        val sizes = arrayOf("Pequeno", "Médio", "Grande")
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSize = sharedPrefs.getString(KEY_ICON_SIZE, "Médio")
        val checkedItem = sizes.indexOf(currentSize)

        AlertDialog.Builder(this)
            .setTitle("Tamanho do Ícone")
            .setSingleChoiceItems(sizes, checkedItem) { dialog, which ->
                val selectedSize = sizes[which]
                tvTamanhoValor.text = selectedSize
                sharedPrefs.edit().putString(KEY_ICON_SIZE, selectedSize).apply()
                sendSettingsUpdateBroadcast()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getActionBarHeight(): Int {
        val typedValue = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            return TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        }
        return 0
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun sendSettingsUpdateBroadcast() {
        val intent = Intent(ACTION_UPDATE_SETTINGS).apply {
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }
}