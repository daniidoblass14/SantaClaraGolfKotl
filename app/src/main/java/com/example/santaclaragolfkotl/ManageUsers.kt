package com.example.santaclaragolfkotl

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ManageUsers : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var textEmail: TextInputLayout? = null
    private var btnChangePassword: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_user_layout)

        btnChangePassword=findViewById<Button>(R.id.btnChangePassword)

        textEmail = findViewById<View>(R.id.textFieldEmail) as TextInputLayout?


        btnChangePassword?.setOnClickListener {

            sendPasswordReset(textEmail?.editText?.text.toString())
        }

        firebaseAuth = Firebase.auth

    }

    private fun sendPasswordReset (email:String){

        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(){task ->

            if(task.isSuccessful){

                Toast.makeText(this, "Correo de Cambio de contraseña, enviado correctamente.", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this, "Erro al cambiar la contraseña.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}