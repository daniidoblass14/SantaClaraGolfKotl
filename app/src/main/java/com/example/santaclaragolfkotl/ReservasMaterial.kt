package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isEmpty
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

class ReservasMaterial : AppCompatActivity() {

    private var palosDropdown: AutoCompleteTextView? = null
    private var guantesDropdown: AutoCompleteTextView? = null
    private var pelotasDropdown: AutoCompleteTextView? = null
    private var calzadoDropdown: AutoCompleteTextView? = null
    private var arreglaPiquesDropdown: AutoCompleteTextView? = null
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

        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val formattedDate = dateFormat.format(currentDate)

        textInputLayoutPalos = findViewById<TextInputLayout>(R.id.textInputLayoutPalos)
        textInputLayoutGuantes = findViewById<TextInputLayout>(R.id.textInputLayoutGuantes)
        textInputLayoutPelotas = findViewById<TextInputLayout>(R.id.textInputLayoutPelotas)
        textInputLayoutCalzados = findViewById<TextInputLayout>(R.id.textInputLayoutCalzados)
        textInputLayoutArreglapiques =
            findViewById<TextInputLayout>(R.id.textInputLayoutArreglapiques)

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
        arreglaPiquesDropdown =
            findViewById<AutoCompleteTextView>(R.id.arreglaPiquesFilledExposedDropdown)

        cardView = findViewById<CardView>(R.id.cardView)

        btnContinue = findViewById<Button>(R.id.btnContinue)
        btnConfirm = findViewById<Button>(R.id.btnConfirm)
        btnCancel = findViewById<Button>(R.id.btnCancel)

        rellenar()

        btnContinue?.setOnClickListener {

            btnContinue?.visibility = View.GONE
            cardView?.visibility = View.VISIBLE
            btnConfirm?.visibility = View.VISIBLE
            btnCancel?.visibility = View.VISIBLE

            palosDropdown?.isEnabled = false
            guantesDropdown?.isEnabled = false
            pelotasDropdown?.isEnabled = false
            calzadoDropdown?.isEnabled = false
            arreglaPiquesDropdown?.isEnabled = false

            val layoutParams = cardView?.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.topToBottom = R.id.textInputLayoutArreglapiques
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

        btnConfirm?.setOnClickListener {

            val reserva = hashMapOf(
                "nombre" to username,
                "telefono" to userPhone,
                "palos" to palosDropdown?.text.toString(),
                "guantes" to guantesDropdown?.text.toString(),
                "calzado" to calzadoDropdown?.text.toString(),
                "pelotas" to pelotasDropdown?.text.toString(),
                "Arreglapiques" to arreglaPiquesDropdown?.text.toString()
            )

            val reservaGeneral = hashMapOf(
                "nombre" to username,
                "telefono" to userPhone,
                "tipo reserva" to "material",
                "fecha" to formattedDate
            )

            val guantesText = textInputLayoutGuantes?.editText?.text.toString()
            val palosText = textInputLayoutPalos?.editText?.text.toString()
            val arreglapiquesText = textInputLayoutArreglapiques?.editText?.text.toString()
            val pelotasText = textInputLayoutPelotas?.editText?.text.toString()
            val calzadosText = textInputLayoutCalzados?.editText?.text.toString()

            if (guantesText.isEmpty() && palosText.isEmpty() && arreglapiquesText.isEmpty() && pelotasText.isEmpty() && calzadosText.isEmpty()) {
                Toast.makeText(this, getString(R.string.all_data), Toast.LENGTH_SHORT).show()
            } else {
                val reserva = hashMapOf(
                    "nombre" to username,
                    "telefono" to userPhone,
                    "palos" to palosText,
                    "guantes" to guantesText,
                    "calzado" to calzadosText,
                    "pelotas" to pelotasText,
                    "Arreglapiques" to arreglapiquesText
                )

                db.collection("reservasMaterial").add(reserva)
                    .addOnSuccessListener { documentReference ->
                        // La reserva se ha insertado con éxito
                        val reservaId = documentReference.id
                        showReservationSuccessDialog(reservaGeneral)
                    }
                    .addOnFailureListener { e ->
                        // Ocurrió un error al insertar la reserva
                        Toast.makeText(this, "Error al crear la reserva: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }

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
                val adapterPalos =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, palosArray)
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
                val adapterPelotas =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, pelotasArray)
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
                val adapterGuantes =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, guantesArray)
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
                val adapterCalzados =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, calzadosArray)
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
                val adapterArregla =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arreglaArray)
                arreglaPiquesDropdown?.setAdapter(adapterArregla)
            } else {
                // Maneja el error en caso de que la consulta falle
                val mensajeError =
                    "Error al recuperar los datos: ${task.exception?.message}"
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun showReservationSuccessDialog(reservaGeneral: HashMap<String, String?>) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.reservation_complete))
            .setMessage(getString(R.string.reservation_complete_message))
            .setPositiveButton(getString(R.string.accept)) { _, _ ->
                db.collection("reservasGeneral").add(reservaGeneral)
                    .addOnSuccessListener { documentReference ->

                        // La reserva se ha insertado con éxito
                        val reservaId = documentReference.id

                        // Redirigir al menú de las reservas
                        val intent = Intent(this, MenuReservas::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        // Ocurrió un error al insertar la reserva
                        Toast.makeText(
                            this,
                            "Error al crear la reserva General: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .create()

        dialog.show()
    }

    override fun onBackPressed() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(getString(R.string.back))
        dialogBuilder.setMessage(getString(R.string.back_reservation))
        dialogBuilder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            val intent = Intent(this, MenuReservas::class.java)
            startActivity(intent)
            finish()
        }
        dialogBuilder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }
}