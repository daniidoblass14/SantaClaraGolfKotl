package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class Registrar : AppCompatActivity() {

    private var btnConfirmar: Button? = null
    private var textNombre: TextInputLayout? = null
    private var textApellido: TextInputLayout? = null
    private var textTelefono: TextInputLayout? = null
    private var textEmail: TextInputLayout? = null
    private var textPassword: TextInputLayout? = null
    private var textRepeatPassword: TextInputLayout? = null

    private val db = FirebaseFirestore.getInstance();
    private var user = FirebaseAuth.getInstance().currentUser

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

        if(!user?.isEmailVerified()!!){
            Toast.makeText(this, "Correo no verificado", Toast.LENGTH_SHORT).show()
        }
        else{
            println("------CORREO VERIFICATED------")

        }

        setUp()
    }

    private fun setUp() {
        title = "Registro de Usuario"
        FirebaseApp.initializeApp(this)

        btnConfirmar!!.setOnClickListener {

            val nombre = textNombre?.editText?.text.toString()
            val apellido = textApellido?.editText?.text.toString()
            val telefono = textTelefono?.editText?.text.toString()
            val email = textEmail?.editText?.text.toString()
            val password = textPassword?.editText?.text.toString()
            val repeatPassword = textRepeatPassword?.editText?.text.toString()

            if (password.equals(repeatPassword)) {

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        task ->
                    if (task.isSuccessful) {

                        user = FirebaseAuth.getInstance().currentUser
                        user?.sendEmailVerification()

                        db.collection("temporal").document(email).set(hashMapOf("nombre" to nombre,"apellido" to apellido,"telefono" to telefono))

                        val intentLogin = Intent(this, MainActivity::class.java)
                        startActivity(intentLogin)
                    }
                    else{

                    }
                }
            } else {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }

        }
    }
}