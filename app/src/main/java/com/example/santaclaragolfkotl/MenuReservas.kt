package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.makeramen.roundedimageview.RoundedImageView

class MenuReservas : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imagenCampo: RoundedImageView
    private lateinit var imagenRestaurante: RoundedImageView
    private lateinit var imagenMaterial: RoundedImageView
    private lateinit var btnConfig: ImageButton
    private val db = FirebaseFirestore.getInstance();

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_layout)

        firebaseAuth = Firebase.auth

        imagenCampo = findViewById(R.id.imageCampo)
        imagenRestaurante = findViewById(R.id.imageRestaurante)
        imagenMaterial = findViewById(R.id.imageMaterial)
        btnConfig = findViewById(R.id.btnConfiguracion)

        imagenCampo.setOnClickListener {
            val intentReservaCampo = Intent(this, ReservasCampo::class.java)
            startActivity(intentReservaCampo)
        }

        imagenRestaurante.setOnClickListener {
            val intentReservaRestaurante = Intent(this, ReservasRestaurante::class.java)
            startActivity(intentReservaRestaurante)
        }

        imagenMaterial.setOnClickListener {
            val intentMaterial = Intent(this, ReservasMaterial::class.java)
            startActivity(intentMaterial)
        }

        btnConfig.setOnClickListener {
            val popupMenu = PopupMenu(this, btnConfig)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_cambiar_contrasena -> {
                        val user = FirebaseAuth.getInstance().currentUser
                        val email = user?.email

                        if (email != null) {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val dialogBuilder = AlertDialog.Builder(this)
                                        dialogBuilder.setTitle("Atención")
                                        dialogBuilder.setMessage("Se ha enviado un correo para restablecer la contraseña.\nLa aplicación se reiniciará para aplicar los cambios.")
                                        dialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
                                            dialog.dismiss()
                                            // Reiniciar la aplicación
                                            val intent = Intent(applicationContext, MainActivity::class.java)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                            startActivity(intent)
                                            finish()
                                        }

                                        val dialog = dialogBuilder.create()
                                        dialog.show()
                                    } else {
                                        Toast.makeText(this, "Error al enviar el correo de restablecimiento de contraseña", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }

                        true
                    }
                    R.id.menu_cambiar_email -> {
                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder.setTitle("Cambiar email")
                        dialogBuilder.setMessage("Introduce el nuevo correo electrónico")

                        val input = EditText(this)
                        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        dialogBuilder.setView(input)

                        dialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
                            val newEmail = input.text.toString()

                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                val previousEmail = user.email
                                val usersCollection = FirebaseFirestore.getInstance().collection("users")
                                val temporalCollection = FirebaseFirestore.getInstance().collection("temporal")

                                val query = usersCollection.whereEqualTo("email", previousEmail)
                                query.get().addOnCompleteListener { queryTask ->
                                    if (queryTask.isSuccessful) {
                                        for (document in queryTask.result) {
                                            val userData = document.data

                                            val newUser = HashMap<String, Any>()
                                            newUser["apellido"] = userData["apellido"] as String
                                            newUser["email"] = newEmail
                                            newUser["nombre"] = userData["nombre"] as String
                                            newUser["rol"] = userData["rol"] as String
                                            newUser["telefono"] = userData["telefono"] as String

                                            usersCollection.document(newEmail).set(newUser)
                                                .addOnSuccessListener {
                                                    user.updateEmail(newEmail)
                                                        .addOnCompleteListener { updateEmailTask ->
                                                            if (updateEmailTask.isSuccessful) {
                                                                user.sendEmailVerification()
                                                                    .addOnCompleteListener { verificationTask ->
                                                                        if (verificationTask.isSuccessful) {
                                                                            document.reference.delete()

                                                                            // Eliminar usuario del documento "temporal"
                                                                            val temporalQuery = temporalCollection.whereEqualTo("email", previousEmail)
                                                                            temporalQuery.get().addOnCompleteListener { temporalQueryTask ->
                                                                                if (temporalQueryTask.isSuccessful) {
                                                                                    for (temporalDocument in temporalQueryTask.result) {
                                                                                        temporalDocument.reference.delete()
                                                                                    }
                                                                                } else {
                                                                                    Toast.makeText(this, "Error al eliminar el usuario del documento temporal", Toast.LENGTH_SHORT).show()
                                                                                }
                                                                            }

                                                                            Toast.makeText(this, "Se ha actualizado el correo electrónico. Se ha enviado un correo de verificación.", Toast.LENGTH_SHORT).show()
                                                                            dialog.dismiss()
                                                                            restartApp()
                                                                        } else {
                                                                            Toast.makeText(this, "Se ha actualizado el correo electrónico, pero no se pudo enviar el correo de verificación.", Toast.LENGTH_SHORT).show()
                                                                            dialog.dismiss()
                                                                            restartApp()
                                                                        }
                                                                    }
                                                            } else {
                                                                Toast.makeText(this, "Error al actualizar el correo electrónico", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(this, "Error al crear el nuevo usuario", Toast.LENGTH_SHORT).show()
                                                    dialog.dismiss()
                                                }
                                        }
                                    } else {
                                        Toast.makeText(this, "Error al buscar el usuario anterior", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Usuario no válido. Inicia sesión nuevamente.", Toast.LENGTH_SHORT).show()
                            }
                        }

                        dialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
                            dialog.cancel()
                        }

                        val dialog = dialogBuilder.create()
                        dialog.show()

                        true
                    }


                    R.id.menu_informacion -> {
                        // Acción cuando se selecciona "Información"
                        // Acción cuando se selecciona "Información"
                        val phoneNumber = "652 68 70 23" // Número de teléfono
                        val appVersion = "1.0.0" // Versión de la aplicación
                        val creator = "Daniel Jesús Doblas Florido" // Creador de la aplicación

                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder.setTitle("Información")
                        dialogBuilder.setMessage("Teléfono: $phoneNumber\nVersión: $appVersion\nCreador: $creator")
                        dialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
                            dialog.dismiss()
                        }

                        val dialog = dialogBuilder.create()
                        dialog.show()
                        true
                    }

                    R.id.menu_logOut -> {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }
    }

    private fun sendPasswordReset (email:String){
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null && currentUser.email != null) {
            val userEmail = currentUser.email

            userEmail?.let {
                firebaseAuth.sendPasswordResetEmail(it).addOnCompleteListener(){ task ->

                    if(task.isSuccessful){

                        Toast.makeText(this, "Correo de Cambio de contraseña, enviado correctamente.", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this, "Erro al cambiar la contraseña.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    }

    private fun restartApp() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

}
