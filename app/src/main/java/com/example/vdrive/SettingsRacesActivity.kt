package com.example.vdrive

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class SettingsRacesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Habilita o modo de tela cheia (igual sua outra tela)
        enableEdgeToEdge()

        // 2. Define o layout (Supondo que o arquivo se chame activity_races_settings.xml)
        setContentView(R.layout.activity_settings_races)

        // 3. Ajusta o padding para o conteúdo não ficar atrás da barra de status
        // Pegamos o ID que estava no seu XML: android:id="@+id/races_settings_main"
        val mainContainer = findViewById<View>(R.id.races_settings_main)

        ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Aplica o padding nas bordas para respeitar o "notch" e a barra de navegação
            view.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }
}