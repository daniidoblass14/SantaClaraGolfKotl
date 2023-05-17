package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReservasMaterial: AppCompatActivity() {

    private var palosDropdown : AutoCompleteTextView? = null
    private var guantesDropdown : AutoCompleteTextView? = null
    private var pelotasDropdown : AutoCompleteTextView? = null
    private var calzadoDropdown : AutoCompleteTextView? = null
    private var arreglaPiquesDropdown : AutoCompleteTextView? = null
    private val db = FirebaseFirestore.getInstance();
    private var palosDatos = mutableListOf<String>()
    private var pelotasDatos = mutableListOf<String>()
    private var calzadosDatos = mutableListOf<String>()
    private var guantesDatos = mutableListOf<String>()
    private var arreglaPiquesDatos = mutableListOf<String>()


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.material_reservation_layout)

        palosDropdown = findViewById<AutoCompleteTextView>(R.id.palosFilledExposedDropdown)
        guantesDropdown = findViewById<AutoCompleteTextView>(R.id.guantesFilledExposedDropdown)
        pelotasDropdown = findViewById<AutoCompleteTextView>(R.id.pelotasFilledExposedDropdown)
        calzadoDropdown = findViewById<AutoCompleteTextView>(R.id.calzadoFilledExposedDropdown)
        arreglaPiquesDropdown = findViewById<AutoCompleteTextView>(R.id.arreglaPiquesFilledExposedDropdown)

        rellenar()
    }

    private fun rellenar() {
        FirebaseApp.initializeApp(this)

        db.collection("Palos").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result?.documents

                for (document in documents!!) {
                    // Accede a los datos del documento
                    val nombre = document.getString("nombre")

                    // Muestra los resultados en un Toast
                    palosDatos.add(nombre!!)
                }
                val palosArray = palosDatos.toTypedArray()
                val adapterPalos = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, palosArray)
                palosDropdown?.setAdapter(adapterPalos)
            } else {
                // Maneja el error en caso de que la consulta falle
                val mensajeError =
                    "Error al recuperar los datos: ${task.exception?.message}"
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
            }

        }

        db.collection("Pelotas").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result?.documents

                for (document in documents!!) {
                    // Accede a los datos del documento
                    val nombre = document.getString("nombre")

                    // Muestra los resultados en un Toast
                    pelotasDatos.add(nombre!!)
                }
                val pelotasArray = pelotasDatos.toTypedArray()
                val adapterPelotas = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, pelotasArray)
                pelotasDropdown?.setAdapter(adapterPelotas)
            } else {
                // Maneja el error en caso de que la consulta falle
                val mensajeError =
                    "Error al recuperar los datos: ${task.exception?.message}"
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
            }

        }

        db.collection("Guantes").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result?.documents

                for (document in documents!!) {
                    // Accede a los datos del documento
                    val nombre = document.getString("Talla")

                    // Muestra los resultados en un Toast
                    guantesDatos.add(nombre!!)
                }
                val guantesArray = guantesDatos.toTypedArray()
                val adapterGuantes = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, guantesArray)
                guantesDropdown?.setAdapter(adapterGuantes)
            } else {
                // Maneja el error en caso de que la consulta falle
                val mensajeError =
                    "Error al recuperar los datos: ${task.exception?.message}"
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
            }

        }

        db.collection("Calzados").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result?.documents

                for (document in documents!!) {
                    // Accede a los datos del documento
                    val nombre = document.getString("Talla")

                    // Muestra los resultados en un Toast
                    calzadosDatos.add(nombre!!)
                }
                val calzadosArray = calzadosDatos.toTypedArray()
                val adapterCalzados = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, calzadosArray)
                calzadoDropdown?.setAdapter(adapterCalzados)
            } else {
                // Maneja el error en caso de que la consulta falle
                val mensajeError =
                    "Error al recuperar los datos: ${task.exception?.message}"
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
            }

        }

        db.collection("Arreglapiques").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result?.documents

                for (document in documents!!) {
                    // Accede a los datos del documento
                    val nombre = document.getString("nombre")

                    // Muestra los resultados en un Toast
                    arreglaPiquesDatos.add(nombre!!)
                }
                val arreglaArray = arreglaPiquesDatos.toTypedArray()
                val adapterArregla = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arreglaArray)
                arreglaPiquesDropdown?.setAdapter(adapterArregla)
            } else {
                // Maneja el error en caso de que la consulta falle
                val mensajeError =
                    "Error al recuperar los datos: ${task.exception?.message}"
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
            }

        }
    }

}