package com.example.santaclaragolfkotl

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminMenu:AppCompatActivity() {

    private var btnMovements: Button? = null
    private var btnManageUser: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_menu_layout)

        btnMovements = findViewById(R.id.btnMovements)
        btnManageUser = findViewById(R.id.btnManageUser)

        btnMovements?.setOnClickListener {

            val intentMovements = Intent(this, Movements::class.java)
            startActivity(intentMovements)
        }

        btnManageUser?.setOnClickListener {

            val intentManageUsers = Intent(this, ManageUsers::class.java)
            startActivity(intentManageUsers)
        }
    }

    override fun onBackPressed() {
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
}