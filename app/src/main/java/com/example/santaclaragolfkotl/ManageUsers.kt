package com.example.santaclaragolfkotl

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ManageUsers : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var textEmail: TextInputLayout? = null
    private var btnChangePassword: Button? = null
    private var btnChangeEmail: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manage_user_layout)

        btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        btnChangeEmail = findViewById<Button>(R.id.btnChangeEmail)

        textEmail = findViewById<View>(R.id.textFieldEmail) as TextInputLayout?

        btnChangePassword?.setOnClickListener {
            sendPasswordReset(textEmail?.editText?.text.toString())
        }

        btnChangeEmail?.setOnClickListener{
            changeEmail(textEmail?.editText?.text.toString())
        }

        firebaseAuth = Firebase.auth
    }

    private fun changeEmail(email: String) {

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
                val previousEmail = email
                val usersCollection = FirebaseFirestore.getInstance().collection("users")
                val temporalCollection = FirebaseFirestore.getInstance().collection("temporal")

                if (previousEmail == user.email) {
                    // El correo electrónico es el mismo, reiniciar la aplicación
                    showRestartAppDialog(dialog,usersCollection,temporalCollection,newEmail,previousEmail,user)
                } else {
                    queryChangeEmail(dialog,usersCollection,temporalCollection,newEmail,previousEmail,user)
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
    }

    private fun sendPasswordReset(email: String) {

        val user = FirebaseAuth.getInstance().currentUser

        if (user != null){
            val previousEmail = email

            if(user.email == email){
                showRestartAppDialogChangePassword(previousEmail)
            } else {
                queryChangePassword(previousEmail)
            }
        }

    }

    private fun showRestartAppDialogChangePassword(previousEmail: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Advertencia")
        dialogBuilder.setMessage("El correo electronico es el suyo, la app se reiniciará")
        dialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
            queryChangePassword(previousEmail)
            dialog.dismiss()
            finish() // Finalizar la actividad actual y volver al menú de administrador
            restartApp()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun showSuccessDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Éxito")
        dialogBuilder.setMessage("Cambios realizados correctamente.")
        dialogBuilder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
            finish() // Finalizar la actividad actual y volver al menú de administrador

        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun showRestartAppDialog(
        dialog1: DialogInterface,
        usersCollection: CollectionReference,
        temporalCollection: CollectionReference,
        newEmail: String,
        previousEmail: String,
        user: FirebaseUser
    ) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Alerta")
        dialogBuilder.setMessage("El correo electrónico es el mismo.")
        dialogBuilder.setPositiveButton("Sí") { dialog, _ ->
            dialog.dismiss()
            queryChangeEmail(dialog1,usersCollection,temporalCollection,newEmail,previousEmail,user)
            restartApp()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun restartApp() {
        // Reiniciar la aplicación
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun queryChangeEmail(
        dialog: DialogInterface,
        usersCollection: CollectionReference,
        temporalCollection: CollectionReference,
        newEmail: String,
        previousEmail: String,
        user: FirebaseUser
    ) {
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
                                                    //restartApp()
                                                } else {
                                                    Toast.makeText(this, "Se ha actualizado el correo electrónico, pero no se pudo enviar el correo de verificación.", Toast.LENGTH_SHORT).show()
                                                    dialog.dismiss()
                                                    //restartApp()
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
    }

    private fun queryChangePassword(previousEmail: String) {
        firebaseAuth.sendPasswordResetEmail(previousEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Correo de Cambio de contraseña, enviado correctamente.", Toast.LENGTH_SHORT).show()
                showSuccessDialog()
            } else {
                Toast.makeText(this, "Error al cambiar la contraseña.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
