package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private var tvRegistrarse: TextView? = null
    private var tvChangePassword: TextView? = null
    private var btnIniciarSesion: Button? = null
    private var textEmail: TextInputLayout? = null
    private var textPassword: TextInputLayout? = null

    private val db = FirebaseFirestore.getInstance()
    private lateinit var firebaseAuth: FirebaseAuth


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvRegistrarse = findViewById(R.id.tvLogIn)
        tvChangePassword = findViewById(R.id.tvChangePassword)
        btnIniciarSesion = findViewById(R.id.btnInit)
        textEmail = findViewById<View>(R.id.textFieldUser) as TextInputLayout?
        textPassword = findViewById<View>(R.id.textFieldPassword) as TextInputLayout?

        firebaseAuth = FirebaseAuth.getInstance()


        tvRegistrarse?.setOnClickListener {
            val intentRegistrar = Intent(this, Registrar::class.java)
            startActivity(intentRegistrar)
        }

        tvChangePassword?.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            val input = EditText(this)

            dialogBuilder.setTitle(getString(R.string.introduce_email))
            dialogBuilder.setView(input)
            dialogBuilder.setPositiveButton(getString(R.string.send), DialogInterface.OnClickListener { dialog, which ->
                val email = input.text.toString().trim()
                val message = getString(R.string.email_sent_to) + " " + email + " " + getString(R.string.to_reset_password)
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        val resultDialogBuilder = AlertDialog.Builder(this)

                        if (task.isSuccessful) {
                            resultDialogBuilder.setTitle(getString(R.string.email_sent))
                            resultDialogBuilder.setMessage(message)
                        } else {
                            resultDialogBuilder.setTitle(getString(R.string.title_error_sent_email))
                            resultDialogBuilder.setMessage(getString(R.string.error_verify_email))
                        }

                        resultDialogBuilder.setPositiveButton(getString(R.string.accept), null)
                        val resultDialog = resultDialogBuilder.create()
                        resultDialog.show()
                    }
            })
            dialogBuilder.setNegativeButton(getString(R.string.btn_cancel), DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
            })

            val dialog = dialogBuilder.create()
            dialog.show()
        }

        setUp()
    }

    private fun setUp() {
        FirebaseApp.initializeApp(this)

        btnIniciarSesion?.setOnClickListener {
            val email = textEmail?.editText?.text.toString()
            val password = textPassword?.editText?.text.toString()

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser

                        if (user?.isEmailVerified == true) {
                            insertarUsuario(email)
                            isAdmin(email) { isAdmin ->
                                if (isAdmin) {
                                    iniciarSesionAdmin(email, password)
                                } else {
                                    iniciarSesionUser(email, password)
                                }
                            }
                        } else {
                            // El correo no está verificado
                            Toast.makeText(this, getString(R.string.email_no_verify), Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        // Error al iniciar sesión
                        Toast.makeText(this, getString(R.string.init_error), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun isAdmin(email: String, callback: (Boolean) -> Unit) {
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null && !result.isEmpty) {
                    val document = result.documents[0]
                    val rol = document.getString("rol")
                    if (rol == "admin") {
                        Toast.makeText(this, getString(R.string.be_admin), Toast.LENGTH_SHORT).show()
                        callback(true)
                    } else {
                        Toast.makeText(this, getString(R.string.not_be_admin), Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }
            }
        }
    }

    private fun insertarUsuario(email: String) {
        db.collection("users").document(email).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null && result.exists()) {
                    // El usuario ya existe en la colección "users"
                } else {
                    db.collection("temporal").document(email).get()
                        .addOnSuccessListener { document ->
                            val nombre = document.getString("nombre")
                            val apellido = document.getString("apellido")
                            val telefono = document.getString("telefono")
                            val rol = document.getString("rol")

                            db.collection("users").document(email).set(
                                hashMapOf(
                                    "nombre" to nombre,
                                    "apellido" to apellido,
                                    "telefono" to telefono,
                                    "email" to email,
                                    "rol" to rol
                                )
                            )
                        }
                }
            }
        }
    }

    private fun iniciarSesionUser(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intentMenuReservas = Intent(this, MenuReservas::class.java)
                    startActivity(intentMenuReservas)
                    finish()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, getString(R.string.invalid_user), Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, getString(R.string.invalid_password), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, getString(R.string.init_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun iniciarSesionAdmin(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intentAdminMenu = Intent(this, AdminMenu::class.java)
                    startActivity(intentAdminMenu)
                    finish()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, getString(R.string.invalid_user), Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, getString(R.string.invalid_password), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, getString(R.string.init_error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
