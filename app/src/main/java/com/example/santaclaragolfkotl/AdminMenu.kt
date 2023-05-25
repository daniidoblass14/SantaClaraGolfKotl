package com.example.santaclaragolfkotl

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

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
    }
}