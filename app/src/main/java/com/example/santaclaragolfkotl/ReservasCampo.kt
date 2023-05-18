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
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReservasCampo: AppCompatActivity() {

    private var textFieldDay : TextInputEditText? = null
    private var textFieldHour : TextInputEditText? = null

    private var packsDropdown : AutoCompleteTextView? = null
    private var guestsDropdown : AutoCompleteTextView? = null

    private var packsDatos = mutableListOf<String>()
    private var guestsDatos = mutableListOf<String>()

    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var timePicker: MaterialTimePicker

    private val db = FirebaseFirestore.getInstance();

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.field_reservation_layout)

        textFieldDay = findViewById<TextInputEditText>(R.id.textInputDay)
        textFieldDay?.inputType = InputType.TYPE_NULL

        textFieldHour = findViewById<TextInputEditText>(R.id.textInputHour)
        textFieldHour?.inputType = InputType.TYPE_NULL

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



}