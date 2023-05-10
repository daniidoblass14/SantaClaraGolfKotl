package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.makeramen.roundedimageview.RoundedImageView

class MenuReservas: AppCompatActivity() {

    private lateinit var imagenCampo : RoundedImageView
    private lateinit var imagenRestaurante : RoundedImageView
    private lateinit var imagenMaterial : RoundedImageView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_layout)

        imagenCampo = findViewById(R.id.imageCampo)
        imagenRestaurante = findViewById(R.id.imageRestaurante)
        imagenMaterial = findViewById(R.id.imageMaterial)

        imagenCampo.setOnClickListener {

            val intentReservaCampo = Intent (this,ReservasCampo::class.java)
            startActivity(intentReservaCampo)
        }

        imagenRestaurante.setOnClickListener {

            val intentReservaRestaurante = Intent (this,ReservasRestaurante::class.java)
            startActivity(intentReservaRestaurante)
        }
    }
}