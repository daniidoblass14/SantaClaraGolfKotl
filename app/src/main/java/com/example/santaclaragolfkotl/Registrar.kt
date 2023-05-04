package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class Registrar : AppCompatActivity()  {

    private var btnConfirmar: Button? = null
    private var textNombre: TextInputLayout? = null
    private var textApellido: TextInputLayout? = null
    private var textTelefono: TextInputLayout? = null
    private var textEmail: TextInputLayout? = null
    private var textPassword: TextInputLayout? = null
    private var textRepeatPassword: TextInputLayout? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        btnConfirmar = findViewById(R.id.IniciarSesion)
        textNombre = findViewById<View>(R.id.textFieldName) as TextInputLayout
        textApellido = findViewById<View>(R.id.textFieldSurrname) as TextInputLayout
        textTelefono = findViewById<View>(R.id.textFieldPhone) as TextInputLayout
        textEmail = findViewById<View>(R.id.textFieldEmail) as TextInputLayout
        textPassword = findViewById<View>(R.id.textFieldPassword) as TextInputLayout
        textRepeatPassword = findViewById<View>(R.id.textFieldRepeatPassword) as TextInputLayout
        FirebaseApp.initializeApp(this)


        setUp()
    }

    private fun setUp() {
        title = "Registro de Usuario"


        btnConfirmar!!.setOnClickListener {

            val email = textEmail?.editText?.text.toString()
            val password = textPassword?.editText?.text.toString()

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(this){
                    task ->
                if (task.isSuccessful) {
                    // El usuario se ha registrado correctamente
                } else {
                    // Se ha producido un error al registrar el usuario
                    val message = task.exception?.message
                    // Mostrar mensaje de error
                }
            }
        }

    }
}