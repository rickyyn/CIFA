package br.edu.fatecpg.cifa

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.fatecpg.cifa.R

class PerfilActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)


        val nome = intent.getStringExtra("NOME_ALUNO")


        val tvNome = findViewById<TextView>(R.id.tvNome)
        val btnHorarios = findViewById<LinearLayout>(R.id.btnHorarios)
        val btnQr = findViewById<LinearLayout>(R.id.btnQr)


        tvNome.text = nome ?: "Aluno"


        btnHorarios.setOnClickListener {
            Toast.makeText(this, "Abrir horários", Toast.LENGTH_SHORT).show()


        }


        btnQr.setOnClickListener {
            val intent = Intent(this, QrcodeActivity::class.java)
            startActivity(intent)
        }
    }
}