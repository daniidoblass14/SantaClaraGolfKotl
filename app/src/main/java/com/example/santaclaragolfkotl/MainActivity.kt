package com.example.santaclaragolfkotl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private var tvRegistrarse: TextView? = null
    private var btnIniciarSesion: Button? = null
    private var textEmail: TextInputLayout? = null
    private var textPassword: TextInputLayout? = null

    private val db = FirebaseFirestore.getInstance();
    private var user = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar el TextView y asignarle una referencia
        tvRegistrarse = findViewById(R.id.tvRegistrar)
        btnIniciarSesion = findViewById(R.id.IniciarSesion)
        textEmail = findViewById<View>(R.id.textFieldUsuario) as TextInputLayout?
        textPassword = findViewById<View>(R.id.textFieldPassword) as TextInputLayout?

        // Agregar un listener para el clic del TextView
        tvRegistrarse?.setOnClickListener {

            val intentRegistrar = Intent(this, Registrar::class.java)
            startActivity(intentRegistrar)
        }

        setUp()
    }

    private fun setUp() {
        FirebaseApp.initializeApp(this)

        tvRegistrarse?.setOnClickListener {

            val intentRegistrar = Intent(this, Registrar::class.java)
            startActivity(intentRegistrar)
        }

        btnIniciarSesion?.setOnClickListener {

            // Obtener valores de correo electrónico y contraseña ingresados por el usuario
            val email = textEmail?.editText?.text.toString()
            val password = textPassword?.editText?.text.toString()

            //Comprobamos si el email introducido está verificado o no.

            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {

                        val signInMethods = task.result?.signInMethods ?: emptyList()
                        if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                            // El correo electrónico ha sido verificado
                            Toast.makeText(this, "Correo Verificado", Toast.LENGTH_SHORT).show()
                            iniciarSesion(email, password)
                            insertarUsuario(email)
                        } else {
                            // El correo electrónico no ha sido verificado
                            Toast.makeText(this, "Correo No Verificado", Toast.LENGTH_SHORT).show()
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
                    //Ya existe el usuario.
                    Toast.makeText(this, "Bienvenido de nuevo," + email, Toast.LENGTH_SHORT).show()
                } else {
                    //No existe el usuario.
                    db.collection("temporal").document(email).get().addOnSuccessListener {
                        val nombre = it.get("nombre") as String?
                        val apellido = it.get("apellido") as String?
                        val telefono = it.get("telefono") as String?

                        db.collection("users").document(email).set(
                            hashMapOf(
                                "nombre" to nombre,
                                "apellido" to apellido,
                                "telefono" to telefono
                            )
                        )
                    }

                }
            }
        }
    }

    private fun iniciarSesion(email: String, password: String) {

        // Verificar la autenticación del usuario con Firebase
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // El usuario inició sesión correctamente
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                    val intentMenuResrvas = Intent(this, MenuReservas::class.java)
                    startActivity(intentMenuResrvas)

                } else {
                    // Error al iniciar sesión
                    val exception = task.exception
                    if (exception is FirebaseAuthInvalidUserException) {
                        // El usuario no existe
                        Toast.makeText(this, "El usuario no existe", Toast.LENGTH_SHORT).show()
                    } else if (exception is FirebaseAuthInvalidCredentialsException) {
                        // La contraseña es incorrecta
                        Toast.makeText(this, "La contraseña es incorrecta", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        // Otro tipo de error
                        Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }


}