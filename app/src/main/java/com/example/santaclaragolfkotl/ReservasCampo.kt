package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReservasCampo: AppCompatActivity() {

    private var textFieldDay : TextInputEditText? = null
    private var textFieldHour : TextInputEditText? = null

    private var textInputLayoutPacks: TextInputLayout? = null
    private var textInputLayoutGuests: TextInputLayout? = null

    private var packsDropdown : AutoCompleteTextView? = null
    private var guestsDropdown : AutoCompleteTextView? = null

    private var packsDatos = mutableListOf<String>()
    private var guestsDatos = mutableListOf<String>()

    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var timePicker: MaterialTimePicker

    private val db = FirebaseFirestore.getInstance();

    private var btnContinue: Button? = null
    private var btnConfirm: Button? = null
    private var btnCancel: Button? = null

    private var cardView: CardView? = null
    private var username: String? = null
    private var userPhone: String? = null

    private var textViewName: TextView? = null
    private var textViewPhone: TextView? = null
    private var textViewDate: TextView? = null
    private var textViewTime: TextView? = null
    private var textViewPack: TextView? = null
    private var textViewGuests: TextView? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.field_reservation_layout)

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        textViewName = findViewById<TextView>(R.id.textViewName)
        textViewPhone = findViewById<TextView>(R.id.textViewPhone)
        textViewDate = findViewById<TextView>(R.id.textViewDate)
        textViewTime = findViewById<TextView>(R.id.textViewTime)
        textViewGuests = findViewById<TextView>(R.id.textViewGuests)
        textViewPack = findViewById<TextView>(R.id.textViewPack)

        btnConfirm = findViewById<Button>(R.id.btnConfirm)
        btnCancel = findViewById<Button>(R.id.btnCancel)
        btnContinue = findViewById<Button>(R.id.btnContinue)
        cardView = findViewById<CardView>(R.id.cardView)

        textFieldDay = findViewById<TextInputEditText>(R.id.textInputDay)
        textFieldDay?.inputType = InputType.TYPE_NULL

        textFieldHour = findViewById<TextInputEditText>(R.id.textInputHour)
        textFieldHour?.inputType = InputType.TYPE_NULL

        textInputLayoutPacks = findViewById<TextInputLayout>(R.id.textInputLayoutPacks)
        textInputLayoutGuests = findViewById<TextInputLayout>(R.id.textInputLayoutGuests)

        packsDropdown = findViewById<AutoCompleteTextView>(R.id.packsFilledExposedDropdown)
        guestsDropdown = findViewById<AutoCompleteTextView>(R.id.guestsFilledExposedDropdown)

        guestsDatos = mutableListOf("1", "2", "3", "4")
        val guestsAdapter = ArrayAdapter(this, androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item, guestsDatos)
        guestsDropdown?.setAdapter(guestsAdapter)

        setupDatePicker()
        setDatePickerListener()
        setupTimePicker()
        setTimePickerListener()
        searchPacks()

        btnContinue?.setOnClickListener {
            btnContinue?.visibility = View.GONE
            cardView?.visibility = View.VISIBLE
            btnConfirm?.visibility = View.VISIBLE
            btnCancel?.visibility = View.VISIBLE

            packsDropdown?.isEnabled = false
            guestsDropdown?.isEnabled = false

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
            textViewDate?.text = textFieldDay?.text.toString()
            textViewTime?.text = textFieldHour?.text.toString()
            textViewPack?.text = packsDropdown?.text.toString()
            textViewGuests?.text = guestsDropdown?.text.toString()
        }

        btnConfirm?.setOnClickListener {

            val reserva = hashMapOf("nombre" to username,"telefono" to userPhone ,"fecha" to textViewDate?.text.toString(),"hora" to textViewTime?.text.toString(),
                "pack de la reserva" to packsDropdown?.text.toString(),"acompañantes" to guestsDropdown?.text.toString())

            db.collection("reservasCampo").add(reserva).addOnSuccessListener {documentReference ->

                // La reserva se ha insertado con éxito
                val reservaId = documentReference.id

                showReservationSuccessDialog()
            }
                .addOnFailureListener { e ->
                    // Ocurrió un error al insertar la reserva
                    Toast.makeText(this, "Error al crear la reserva: ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }

        btnCancel?.setOnClickListener {

            btnContinue?.visibility = View.VISIBLE
            cardView?.visibility = View.GONE
            btnConfirm?.visibility = View.GONE
            btnCancel?.visibility = View.GONE

            packsDropdown?.isEnabled = true
            guestsDropdown?.isEnabled = true

            packsDropdown?.setText("")
            guestsDropdown?.setText("")
            textFieldDay?.setText("")
            textFieldHour?.setText("")
        }

    }

    private fun searchPacks() {

        db.collection("Pack de reservas").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result?.documents

                for (document in documents!!) {
                    // Accede a los datos del documento
                    val nombre = document.getString("nombre")

                    // Muestra los resultados en un Toast
                    packsDatos.add(nombre!!)
                }
                val palosArray = packsDatos.toTypedArray()
                val adapterPalos = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, palosArray)
                packsDropdown?.setAdapter(adapterPalos)
            } else {
                // Maneja el error en caso de que la consulta falle
                val mensajeError =
                    "Error al recuperar los datos: ${task.exception?.message}"
                Toast.makeText(this, mensajeError, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun setupTimePicker() {
        val isSystem24HourFormat = DateFormat.is24HourFormat(this)

        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (isSystem24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setHour(8)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()
    }

    private fun setTimePickerListener() {
        textFieldHour?.setOnClickListener {
            timePicker.show(supportFragmentManager, "TimePicker")
        }

        timePicker.addOnPositiveButtonClickListener {
            val selectedHour = timePicker.hour
            val selectedMinute = timePicker.minute

            if (isTimeInRange(selectedHour, selectedMinute)) {
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                textFieldHour?.setText(formattedTime)
            } else {
                showOutOfRangeDialog()
                textFieldHour?.text?.clear()
            }
        }
    }

    private fun isTimeInRange(hour: Int, minute: Int): Boolean {
        val selectedTime = hour * 100 + minute

        return selectedTime in 800..1800
    }

    private fun showOutOfRangeDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Invalid Time")
            .setMessage("Please select a time between 8:00 and 18:00.")
            .setPositiveButton("OK", null)
            .create()

        dialog.show()
    }

    private fun setupDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select Date")

        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = today

        val maxDate = calendar.apply { set(Calendar.YEAR, 2025) }.timeInMillis

        builder.setSelection(today)
        builder.setCalendarConstraints(
            CalendarConstraints.Builder()
                .setStart(today)
                .setEnd(maxDate)
                .setValidator(DateValidatorPointForward.from(today))
                .build()
        )

        datePicker = builder.build()
    }
    private fun setDatePickerListener() {
        textFieldDay?.setOnClickListener {
            datePicker.show(supportFragmentManager, "DatePicker")
        }

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate

            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            textFieldDay?.setText(formattedDate)
        }
    }


    private fun showReservationSuccessDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Reserva completada")
            .setMessage("Gracias por tu reserva. Serás redirigido al menú de la aplicación.")
            .setPositiveButton("Aceptar") { _, _ ->
                // Aquí puedes realizar alguna acción al hacer clic en el botón Aceptar, como redirigir al menú de la aplicación
            }
            .create()

        dialog.show()
    }
}