package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Actividad para registrar un nuevo usuario.
 */
class Registrar : AppCompatActivity() {

    private var btnConfirmar: Button? = null
    private var textNombre: TextInputLayout? = null
    private var textApellido: TextInputLayout? = null
    private var textTelefono: TextInputLayout? = null
    private var textEmail: TextInputLayout? = null
    private var textPassword: TextInputLayout? = null
    private var textRepeatPassword: TextInputLayout? = null

    private val db = FirebaseFirestore.getInstance()
    private var user = FirebaseAuth.getInstance().currentUser

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_layout)

        btnConfirmar = findViewById(R.id.btnInit)
        textNombre = findViewById<View>(R.id.textFieldName) as TextInputLayout
        textApellido = findViewById<View>(R.id.textFieldSurrname) as TextInputLayout
        textTelefono = findViewById<View>(R.id.textFieldPhone) as TextInputLayout
        textEmail = findViewById<View>(R.id.textFieldEmail) as TextInputLayout
        textPassword = findViewById<View>(R.id.textFieldPassword) as TextInputLayout
        textRepeatPassword = findViewById<View>(R.id.textFieldRepeatPassword) as TextInputLayout

        setUp()
    }

    /**
     * Configura la actividad y los botones.
     */
    private fun setUp() {
        FirebaseApp.initializeApp(this)

        btnConfirmar!!.setOnClickListener {

            val nombre = textNombre?.editText?.text.toString()
            val apellido = textApellido?.editText?.text.toString()
            val telefono = textTelefono?.editText?.text.toString()
            val email = textEmail?.editText?.text.toString()
            val password = textPassword?.editText?.text.toString()
            val repeatPassword = textRepeatPassword?.editText?.text.toString()

            if (password.length < 6) {
                Toast.makeText(this, getString(R.string.password_long), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (telefono.length != 9) {
                Toast.makeText(this, getString(R.string.phone_length), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password == repeatPassword) {

                // Verificar si el email ya está registrado
                db.collection("users").whereEqualTo("email", email).get().addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isEmpty) {
                        // El email ya está registrado
                        Toast.makeText(this, getString(R.string.email_register), Toast.LENGTH_SHORT).show()
                    } else {
                        // Verificar si el número de teléfono ya está registrado
                        db.collection("users").whereEqualTo("telefono", telefono).get().addOnCompleteListener { task ->
                            if (task.isSuccessful && !task.result.isEmpty) {
                                // El número de teléfono ya está registrado
                                Toast.makeText(this, getString(R.string.phone_regiter), Toast.LENGTH_SHORT).show()
                            } else {
                                // Registrar el nuevo usuario
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        user = FirebaseAuth.getInstance().currentUser
                                        user?.sendEmailVerification()

                                        db.collection("temporal").document(email).set(
                                            hashMapOf(
                                                "nombre" to nombre,
                                                "apellido" to apellido,
                                                "telefono" to telefono,
                                                "email" to email,
                                                "rol" to "user"
                                            )
                                        )

                                        finish()
                                        val intentLogin = Intent(this, MainActivity::class.java)
                                        startActivity(intentLogin)
                                    } else {
                                        // Error al crear el usuario
                                        Toast.makeText(this, getString(R.string.error_user), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.password_notEqual), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(getString(R.string.back))
        dialogBuilder.setMessage(getString(R.string.register_back))
        dialogBuilder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            val intent = Intent(this, MainActivity::class.java)
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
