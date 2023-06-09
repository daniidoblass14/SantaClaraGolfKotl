package com.example.santaclaragolfkotl

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class Movements : AppCompatActivity() {

    private var textViewName: TextView? = null
    private var textViewPhone: TextView? = null
    private var textViewType: TextView? = null
    private val db = FirebaseFirestore.getInstance();
    private lateinit var tableMovements: TableLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_movements_layout)

        tableMovements = findViewById(R.id.tableMovements)

        // Obtener la fecha actual en formato dd/MM/yyyy
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        // Realizar la consulta a Firebase
        db.collection("reservasGeneral")
            .whereEqualTo("fecha", currentDate)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Obtener los resultados de la consulta
                    val result = task.result

                    // Obtener los documentos de la consulta
                    val documents = result.documents

                    // Iterar sobre los documentos y crear las filas de la tabla
                    documents.forEachIndexed { index, document ->
                        val name = document.getString("nombre")
                        val phone = document.getString("telefono")
                        val bookingType = document.getString("tipo reserva")

                        // Crear una nueva fila
                        val tableRow = TableRow(this)

                        // Crear y configurar las celdas de la fila
                        val cell1 = TextView(this)
                        cell1.text = name
                        cell1.setPadding(100, 10, 10, 10)
                        cell1.textAlignment = View.TEXT_ALIGNMENT_CENTER

                        val cell2 = TextView(this)
                        cell2.text = phone
                        cell2.setPadding(150, 8, 8, 8)
                        cell2.textAlignment = View.TEXT_ALIGNMENT_CENTER

                        val cell3 = TextView(this)
                        cell3.text = bookingType
                        cell3.setPadding(120, 8, 8, 8)
                        cell3.textAlignment = View.TEXT_ALIGNMENT_CENTER

                        // Agregar las celdas a la fila
                        tableRow.addView(cell1)
                        tableRow.addView(cell2)
                        tableRow.addView(cell3)

                        // Agregar la fila a la tabla
                        tableMovements.addView(tableRow)
                    }
                } else {
                    // Ocurrió un error al obtener los datos
                    Toast.makeText(this, getString(R.string.error_data), Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onBackPressed() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(getString(R.string.menu_logout))
        dialogBuilder.setMessage(getString(R.string.logout_confirmation_message))
        dialogBuilder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
            val intent = Intent(this, AdminMenu::class.java)
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