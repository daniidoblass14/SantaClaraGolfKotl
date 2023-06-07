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

            dialogBuilder.setTitle("Ingrese su correo electrónico")
            dialogBuilder.setView(input)
            dialogBuilder.setPositiveButton("Enviar", DialogInterface.OnClickListener { dialog, which ->
                val email = input.text.toString().trim()

                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        val resultDialogBuilder = AlertDialog.Builder(this)

                        if (task.isSuccessful) {
                            resultDialogBuilder.setTitle("Correo electrónico enviado")
                            resultDialogBuilder.setMessage("Se ha enviado un correo electrónico a $email para restablecer la contraseña.")
                        } else {
                            resultDialogBuilder.setTitle("Error al enviar el correo electrónico")
                            resultDialogBuilder.setMessage("Hubo un error al enviar el correo electrónico. Verifique la dirección de correo electrónico e inténtelo nuevamente.")
                        }

                        resultDialogBuilder.setPositiveButton("Aceptar", null)
                        val resultDialog = resultDialogBuilder.create()
                        resultDialog.show()
                    }
            })
            dialogBuilder.setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
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
                        val verify = user?.isEmailVerified

                        if (verify == true) {
                            insertarUsuario(email)
                            isAdmin(email) { isAdmin ->
                                if (isAdmin) {
                                    iniciarSesionAdmin(email, password)
                                } else {
                                    iniciarSesionUser(email, password)
                                }
                            }
                            Toast.makeText(this, "Correo verificado", Toast.LENGTH_SHORT).show()

                        } else {
                            Toast.makeText(this, "El correo no está verificado", Toast.LENGTH_SHORT)
                                .show()
                        }

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
                        Toast.makeText(this, "Eres un administrador", Toast.LENGTH_SHORT).show()
                        callback(true)
                    } else {
                        Toast.makeText(this, "No eres un administrador", Toast.LENGTH_SHORT).show()
                        callback(false)
                    }
                }
            }
        }
    }

    private fun insertarUsuario(email: String) {
        db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val result = task.result
                if (result != null && !result.isEmpty) {
                    Toast.makeText(this, "Bienvenido de nuevo, $email", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intentMenuReservas = Intent(this, MenuReservas::class.java)
                    startActivity(intentMenuReservas)
                    finish()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "La contraseña es incorrecta", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun iniciarSesionAdmin(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intentAdminMenu = Intent(this, AdminMenu::class.java)
                    startActivity(intentAdminMenu)
                    finish()
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "La contraseña es incorrecta", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
