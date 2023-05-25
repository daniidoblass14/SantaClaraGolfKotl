package com.example.santaclaragolfkotl

import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

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

        // Realizar la consulta a Firebase
        db.collection("users").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Obtener los resultados de la consulta
                val result = task.result

                // Obtener los documentos de la consulta
                val documents = result.documents

                // Iterar sobre los documentos y crear las filas de la tabla
                documents.forEachIndexed { index, document ->
                    val name = document.getString("nombre")
                    val phone = document.getString("telefono")
                    val bookingType = document.getString("rol")

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
                Toast.makeText(this, "Error al obtener los datos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}