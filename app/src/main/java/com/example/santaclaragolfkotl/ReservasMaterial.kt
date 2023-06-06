package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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

    private var btnContinue: Button? = null
    private var btnConfirm: Button? = null
    private var btnCancel: Button? = null

    private var cardView: CardView? = null
    private var username: String? = null
    private var userPhone: String? = null

    private var textViewName: TextView? = null
    private var textViewPhone: TextView? = null
    private var textViewSticks: TextView? = null
    private var textViewGloves: TextView? = null
    private var textViewShoes: TextView? = null
    private var textViewBalls: TextView? = null
    private var textViewPitchFork: TextView? = null

    private var textInputLayoutPalos: TextInputLayout? = null
    private var textInputLayoutGuantes: TextInputLayout? = null
    private var textInputLayoutPelotas: TextInputLayout? = null
    private var textInputLayoutCalzados: TextInputLayout? = null
    private var textInputLayoutArreglapiques: TextInputLayout? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.material_reservation_layout)

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        textInputLayoutPalos = findViewById<TextInputLayout>(R.id.textInputLayoutPalos)
        textInputLayoutGuantes = findViewById<TextInputLayout>(R.id.textInputLayoutGuantes)
        textInputLayoutPelotas = findViewById<TextInputLayout>(R.id.textInputLayoutPelotas)
        textInputLayoutCalzados = findViewById<TextInputLayout>(R.id.textInputLayoutCalzados)
        textInputLayoutArreglapiques = findViewById<TextInputLayout>(R.id.textInputLayoutArreglapiques)



        textViewName = findViewById<TextView>(R.id.textViewName)
        textViewPhone = findViewById<TextView>(R.id.textViewPhone)
        textViewSticks = findViewById<TextView>(R.id.textViewSticks)
        textViewGloves = findViewById<TextView>(R.id.textViewGloves)
        textViewShoes = findViewById<TextView>(R.id.textViewShoes)
        textViewBalls = findViewById<TextView>(R.id.textViewBalls)
        textViewPitchFork = findViewById<TextView>(R.id.textViewPitchFork)


        palosDropdown = findViewById<AutoCompleteTextView>(R.id.palosFilledExposedDropdown)
        guantesDropdown = findViewById<AutoCompleteTextView>(R.id.guantesFilledExposedDropdown)
        pelotasDropdown = findViewById<AutoCompleteTextView>(R.id.pelotasFilledExposedDropdown)
        calzadoDropdown = findViewById<AutoCompleteTextView>(R.id.calzadoFilledExposedDropdown)
        arreglaPiquesDropdown = findViewById<AutoCompleteTextView>(R.id.arreglaPiquesFilledExposedDropdown)

        cardView = findViewById<CardView>(R.id.cardView)

        btnContinue = findViewById<Button>(R.id.btnContinue)
        btnConfirm = findViewById<Button>(R.id.btnConfirm)
        btnCancel = findViewById<Button>(R.id.btnCancel)

        rellenar()

        btnContinue?.setOnClickListener {

            btnContinue?.visibility = View.GONE
            textInputLayoutPalos?.visibility = View.GONE
            textInputLayoutGuantes?.visibility = View.GONE
            textInputLayoutPelotas?.visibility = View.GONE
            textInputLayoutCalzados?.visibility = View.GONE
            textInputLayoutArreglapiques?.visibility = View.GONE

            cardView?.visibility = View.VISIBLE
            btnConfirm?.visibility = View.VISIBLE
            btnCancel?.visibility = View.VISIBLE

            palosDropdown?.isEnabled = false
            guantesDropdown?.isEnabled = false
            pelotasDropdown?.isEnabled = false
            calzadoDropdown?.isEnabled = false
            arreglaPiquesDropdown?.isEnabled = false

            val layoutParams = cardView?.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topToBottom = R.id.textInputLayoutGuests
            cardView?.layoutParams = layoutParams

            if (currentUser != null && currentUser.email != null) {
                val userEmail = currentUser.email

                db.collection("users").whereEqualTo("email", userEmail.toString()).get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val querySnapshot = task.result
                            if (querySnapshot != null && !querySnapshot.isEmpty) {
                                val document = querySnapshot.documents[0]
                                username = document.getString("nombre")
                                userPhone = document.getString("telefono")

                                // Utiliza los valores obtenidos del documento del usuario
                                textViewName?.text = username
                                textViewPhone?.text = userPhone
                            } else {
                                // No se encontró ningún documento con el email del usuario actual
                            }
                        } else {
                            // Error al realizar la consulta
                        }
                    }
            }
            textViewSticks?.text = palosDropdown?.text.toString()
            textViewGloves?.text = guantesDropdown?.text.toString()
            textViewShoes?.text = calzadoDropdown?.text.toString()
            textViewBalls?.text = pelotasDropdown?.text.toString()
            textViewPitchFork?.text = arreglaPiquesDropdown?.text.toString()
        }

        btnCancel?.setOnClickListener {
            btnContinue?.visibility = View.VISIBLE
            textInputLayoutPalos?.visibility = View.VISIBLE
            textInputLayoutGuantes?.visibility = View.VISIBLE
            textInputLayoutPelotas?.visibility = View.VISIBLE
            textInputLayoutCalzados?.visibility = View.VISIBLE
            textInputLayoutArreglapiques?.visibility = View.VISIBLE

            cardView?.visibility = View.GONE
            btnConfirm?.visibility = View.GONE
            btnCancel?.visibility = View.GONE

            palosDropdown?.isEnabled = true
            guantesDropdown?.isEnabled = true
            calzadoDropdown?.isEnabled = true
            pelotasDropdown?.isEnabled = true
            arreglaPiquesDropdown?.isEnabled = true

            palosDropdown?.setText("")
            guantesDropdown?.setText("")
            calzadoDropdown?.setText("")
            pelotasDropdown?.setText("")
            arreglaPiquesDropdown?.setText("")

        }

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