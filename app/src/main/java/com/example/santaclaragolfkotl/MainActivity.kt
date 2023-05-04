package com.example.santaclaragolfkotl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var tvRegistrarse: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar el TextView y asignarle una referencia
        tvRegistrarse = findViewById(R.id.tvRegistrar)

        // Agregar un listener para el clic del TextView
        tvRegistrarse?.setOnClickListener {

            val intentRegistrar = Intent(this,Registrar::class.java)
            startActivity(intentRegistrar)
        }
    }


}