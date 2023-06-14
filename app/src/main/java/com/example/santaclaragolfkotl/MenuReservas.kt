package com.example.santaclaragolfkotl

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
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


/**
 * Clase que representa el menú de reservas.
 */
class MenuReservas : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var imagenCampo: RoundedImageView
    private lateinit var imagenRestaurante: RoundedImageView
    private lateinit var imagenMaterial: RoundedImageView
    private lateinit var btnConfig: ImageButton
    private val db = FirebaseFirestore.getInstance();

    /**
     * Método de inicialización de la actividad.
     */
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
                            val dialogBuilder = AlertDialog.Builder(this)
                            dialogBuilder.setTitle(getString(R.string.change_password_confirmation_title))
                            dialogBuilder.setMessage(getString(R.string.change_password_confirmation_message))
                            dialogBuilder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                                dialog.dismiss()

                                // Enviar correo de restablecimiento de contraseña
                                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val confirmationDialogBuilder = AlertDialog.Builder(this)
                                            confirmationDialogBuilder.setTitle(getString(R.string.change_password_success_title))
                                            confirmationDialogBuilder.setMessage(getString(R.string.change_password_success_message))
                                            confirmationDialogBuilder.setPositiveButton(getString(R.string.accept)) { confirmationDialog, _ ->
                                                confirmationDialog.dismiss()
                                                // Reiniciar la aplicación
                                                val intent = Intent(applicationContext, MainActivity::class.java)
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                                startActivity(intent)
                                                finish()
                                            }

                                            val confirmationDialog = confirmationDialogBuilder.create()
                                            confirmationDialog.show()
                                        } else {
                                            Toast.makeText(this, getString(R.string.change_password_error_message), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            }
                            dialogBuilder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                                dialog.dismiss()
                            }

                            val dialog = dialogBuilder.create()
                            dialog.show()
                        } else {
                            Toast.makeText(this, getString(R.string.change_email_user_not_found_message), Toast.LENGTH_SHORT).show()
                        }

                        true
                    }

                    R.id.menu_cambiar_email -> {
                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder.setTitle(getString(R.string.change_email_title))
                        dialogBuilder.setMessage(getString(R.string.change_email_dialog_message))

                        val input = EditText(this)
                        input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        dialogBuilder.setView(input)

                        dialogBuilder.setPositiveButton(getString(R.string.accept)) { dialog, _ ->
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
                                                                                }
                                                                            }

                                                                            Toast.makeText(this, getString(R.string.change_email_success_message), Toast.LENGTH_SHORT).show()
                                                                            dialog.dismiss()
                                                                            restartApp()
                                                                        } else {
                                                                            Toast.makeText(this, getString(R.string.change_email_verification_error_message), Toast.LENGTH_SHORT).show()
                                                                            dialog.dismiss()
                                                                            restartApp()
                                                                        }
                                                                    }
                                                            } else {
                                                                Toast.makeText(this, getString(R.string.change_email_update_error_message), Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                }
                                                .addOnFailureListener { exception ->
                                                    Toast.makeText(this, getString(R.string.change_email_new_user_error_message), Toast.LENGTH_SHORT).show()
                                                    dialog.dismiss()
                                                }
                                        }
                                    } else {
                                        Toast.makeText(this,  getString(R.string.change_email_previous_user_error_message), Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                    }
                                }
                            } else {
                                Toast.makeText(this, getString(R.string.change_email_previous_user_error_message), Toast.LENGTH_SHORT).show()
                            }
                        }

                        dialogBuilder.setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                            dialog.cancel()
                        }

                        val dialog = dialogBuilder.create()
                        dialog.show()

                        true
                    }


                    R.id.menu_informacion -> {
                        // Acción cuando se selecciona "Información"
                        // Acción cuando se selecciona "Información"
                        val phoneNumber = getString(R.string.phone_creator) // Número de teléfono
                        val appVersion = getString(R.string.version)// Versión de la aplicación
                        val creator = getString(R.string.name_creator) // Creador de la aplicación

                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder.setTitle(getString(R.string.information_dialog_title))
                        dialogBuilder.setMessage("Phone: $phoneNumber\nVersion: $appVersion\nAuthor: $creator")
                        dialogBuilder.setPositiveButton(getString(R.string.accept)) { dialog, _ ->
                            dialog.dismiss()
                        }

                        val dialog = dialogBuilder.create()
                        dialog.show()
                        true
                    }

                    R.id.menu_logOut -> {
                        showLogoutConfirmationDialog()
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
        }
    }
    /**
     * Reinicia la aplicación.
     */
    private fun restartApp() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
    /**
     * Muestra el cuadro de diálogo de confirmación para cerrar sesión.
     */
    private fun showLogoutConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(getString(R.string.menu_logout))
        dialogBuilder.setMessage(getString(R.string.logout_confirmation_message))
        dialogBuilder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            // Cerrar sesión y redirigir a la pantalla de inicio de sesión
            FirebaseAuth.getInstance().signOut()
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

    override fun onBackPressed() {
       showLogoutConfirmationDialog()
    }


}
