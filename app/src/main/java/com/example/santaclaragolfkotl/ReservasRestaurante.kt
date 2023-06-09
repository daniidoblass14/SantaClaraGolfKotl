package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.collections.HashMap

class ReservasRestaurante : AppCompatActivity() {

    private var textFieldDay: TextInputEditText? = null
    private var textFieldHour: TextInputEditText? = null

    private var guestsDropdown: AutoCompleteTextView? = null
    private var guestsDatos = mutableListOf<String>()

    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var timePicker: MaterialTimePicker

    private var cardView: CardView? = null

    private var btnContinue: Button? = null
    private var btnConfirm: Button? = null
    private var btnCancel: Button? = null

    private var textViewName: TextView? = null
    private var textViewPhone: TextView? = null
    private var textViewDate: TextView? = null
    private var textViewTime: TextView? = null
    private var textViewGuests: TextView? = null
    private var textInputGuests: TextInputLayout? = null

    private val db = FirebaseFirestore.getInstance();
    private var username: String? = null
    private var userPhone: String? = null

    private val currentDate = Date()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy")
    private val formattedDate = dateFormat.format(currentDate)


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_reservation_layout)

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        textViewName = findViewById<TextView>(R.id.textViewName)
        textViewPhone = findViewById<TextView>(R.id.textViewPhone)
        textViewDate = findViewById<TextView>(R.id.textViewDate)
        textViewTime = findViewById<TextView>(R.id.textViewTime)
        textViewGuests = findViewById<TextView>(R.id.textViewGuests)
        textInputGuests = findViewById<TextInputLayout>(R.id.textInputLayoutGuests)

        cardView = findViewById<CardView>(R.id.cardView)

        btnConfirm = findViewById<Button>(R.id.btnConfirm)
        btnCancel = findViewById<Button>(R.id.btnCancel)
        btnContinue = findViewById<Button>(R.id.btnContinue)
        btnContinue?.isEnabled = false


        textFieldDay = findViewById<TextInputEditText>(R.id.textInputDay)
        textFieldDay?.inputType = InputType.TYPE_NULL

        textFieldHour = findViewById<TextInputEditText>(R.id.textInputHour)
        textFieldHour?.inputType = InputType.TYPE_NULL

        guestsDropdown = findViewById<AutoCompleteTextView>(R.id.guestsFilledExposedDropdown)
        guestsDatos = mutableListOf("1", "2", "3", "4")
        val guestsAdapter = ArrayAdapter(
            this,
            androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
            guestsDatos
        )
        guestsDropdown?.setAdapter(guestsAdapter)

        // Agregar TextWatcher a cada campo de texto
        textFieldDay?.addTextChangedListener(textWatcher)
        textFieldHour?.addTextChangedListener(textWatcher)
        guestsDropdown?.addTextChangedListener(textWatcher)

        setupDatePicker()
        setDatePickerListener()
        setupTimePicker()
        setTimePickerListener()


        btnContinue?.setOnClickListener {
            btnContinue?.visibility = View.GONE
            cardView?.visibility = View.VISIBLE
            btnConfirm?.visibility = View.VISIBLE
            btnCancel?.visibility = View.VISIBLE

            textFieldDay?.isEnabled = false
            textFieldHour?.isEnabled = false
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
            textViewGuests?.text = guestsDropdown?.text.toString()

        }

        btnCancel?.setOnClickListener {
            btnContinue?.visibility = View.VISIBLE
            cardView?.visibility = View.GONE
            btnConfirm?.visibility = View.GONE
            btnCancel?.visibility = View.GONE

            //Volver a habilitar los inputs
            textFieldDay?.isEnabled = true
            textFieldHour?.isEnabled = true
            guestsDropdown?.isEnabled = true

            // Borrar los valores de los campos de texto
            textFieldDay?.setText("")
            textFieldHour?.setText("")
            guestsDropdown?.setText("")
        }

        btnConfirm?.setOnClickListener {

            val reserva = hashMapOf("nombre" to username,"telefono" to userPhone ,"fecha" to textViewDate?.text.toString(),"hora" to textViewTime?.text.toString(),
            "acompañantes" to textViewGuests?.text.toString())

            val reservaGeneral = hashMapOf("nombre" to username, "telefono" to userPhone, "tipo reserva" to "restaurante","fecha" to formattedDate)
            val textInputGuests = textInputGuests?.editText?.text.toString()

            if(textInputGuests.isEmpty()){
                Toast.makeText(this, getString(R.string.all_data), Toast.LENGTH_SHORT).show()
            } else {
                db.collection("reservasRestaurante").add(reserva).addOnSuccessListener {documentReference ->

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

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // No es necesario implementar este método
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // No es necesario implementar este método
        }

        override fun afterTextChanged(s: Editable?) {
            // Verificar si todos los campos contienen texto
            val dayFilled = textFieldDay?.text?.isNotEmpty() ?: false
            val hourFilled = textFieldHour?.text?.isNotEmpty() ?: false
            val guestsFilled = guestsDropdown?.text?.isNotEmpty() ?: false

            // Habilitar o deshabilitar el botón btnContinue
            btnContinue?.isEnabled = dayFilled && hourFilled && guestsFilled
        }
    }

    private fun setupTimePicker() {
        val isSystem24HourFormat = DateFormat.is24HourFormat(this)

        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (isSystem24HourFormat) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .setHour(8).setMinute(0).setTitleText("Select Time").build()
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

                if (textFieldDay?.text.toString() == formattedDate) {
                    val currentTime = Calendar.getInstance()
                    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = currentTime.get(Calendar.MINUTE)

                    if (selectedHour > currentHour || (selectedHour == currentHour && selectedMinute > currentMinute)) {
                        textFieldHour?.setText(formattedTime)
                    } else {
                        showInvalidTimeDialog()
                        textFieldHour?.text?.clear()
                    }
                } else {
                    textFieldHour?.setText(formattedTime)
                }
            } else {
                showOutOfRangeDialog()
                textFieldHour?.text?.clear()
            }
        }

    }

    private fun showInvalidTimeDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.invalid_time))
            .setMessage(getString(R.string.select_time_late))
            .setPositiveButton(getString(R.string.ok), null)
            .create()

        dialog.show()
    }


    private fun isTimeInRange(hour: Int, minute: Int): Boolean {
        val selectedTime = hour * 100 + minute

        return (selectedTime in 1000..1600) || (selectedTime in 2000..100) || (selectedTime in 2000..2359) || (selectedTime in 0..100)
    }


    private fun showOutOfRangeDialog() {
        val dialog = AlertDialog.Builder(this).setTitle(getString(R.string.invalid_time))
            .setMessage(getString(R.string.select_time_range))
            .setPositiveButton(getString(R.string.ok), null).create()

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
            CalendarConstraints.Builder().setStart(today).setEnd(maxDate)
                .setValidator(DateValidatorPointForward.from(today)).build()
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

            val formattedDate =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            textFieldDay?.setText(formattedDate)
        }
    }


    private fun showReservationSuccessDialog(reservaGeneral: HashMap<String, String?>) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.reservation_complete))
            .setMessage(getString(R.string.reservation_complete_message))
            .setPositiveButton(getString(R.string.accept)) { _, _ ->
                db.collection("reservasGeneral").add(reservaGeneral).addOnSuccessListener { documentReference ->

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
                        Toast.makeText(this, "Error al crear la reserva General: ${e.message}", Toast.LENGTH_SHORT).show()
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