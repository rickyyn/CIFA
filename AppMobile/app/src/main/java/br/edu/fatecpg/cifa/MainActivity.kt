package br.edu.fatecpg.cifa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import br.edu.fatecpg.cifa.model.Aluno

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val ra = findViewById<EditText>(R.id.edt_ra)
        val senha = findViewById<EditText>(R.id.edt_senha)

        btnLogin.setOnClickListener {
            val raTexto = ra.text.toString()
            val senhaTexto = senha.text.toString()

            val aluno = Aluno(raTexto, senhaTexto)

            if (aluno.ra == "1312313" && aluno.senha == "12345") {
                val intent = Intent(this, QrcodeActivity::class.java)
                intent.putExtra("NOME_ALUNO", "Fábio") // Passando o nome do aluno
                startActivity(intent)
            }
            else if (raTexto.isEmpty() && senhaTexto.isEmpty()){
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()

            }
            else {
                Toast.makeText(this, "login incorreto", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}