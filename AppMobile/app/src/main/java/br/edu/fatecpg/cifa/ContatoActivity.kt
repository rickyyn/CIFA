package br.edu.fatecpg.cifa

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class ContatoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_contato)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVoltar = findViewById<LinearLayout>(R.id.btn_voltar)
        val edtNome = findViewById<EditText>(R.id.edt_nome)
        val edtEmail = findViewById<EditText>(R.id.edt_email)
        val edtMensagem = findViewById<EditText>(R.id.edt_mensagem)
        val btnEnviar = findViewById<Button>(R.id.btn_enviar)


        btnEnviar.isEnabled = false
        btnEnviar.alpha = 0.5f


        fun validarCampos() {
            val nomeOk = edtNome.text.toString().trim().isNotEmpty()
            val emailOk = edtEmail.text.toString().trim().isNotEmpty()
            val msgOk = edtMensagem.text.toString().trim().isNotEmpty()

            val tudoPronto = nomeOk && emailOk && msgOk

            btnEnviar.isEnabled = tudoPronto
            btnEnviar.alpha = if (tudoPronto) 1f else 0.5f
        }


        edtNome.addTextChangedListener { validarCampos() }
        edtEmail.addTextChangedListener { validarCampos() }
        edtMensagem.addTextChangedListener { validarCampos() }

        btnVoltar.setOnClickListener {
            finish()
        }

        btnEnviar.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val mensagem = edtMensagem.text.toString().trim()
            val nome = edtNome.text.toString().trim()


            btnEnviar.isEnabled = false
            btnEnviar.text = "Enviando..."

            val dados = hashMapOf(
                "email" to email,
                "mensagem" to mensagem,
                "nome" to nome,
                "status" to "pendente",
                "data" to Timestamp.now()
            )

            FirebaseFirestore.getInstance()
                .collection("Mensagens")
                .add(dados)
                .addOnSuccessListener {
                    Toast.makeText(this, "Mensagem enviada com sucesso!", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnEnviar.isEnabled = true
                    btnEnviar.text = "Enviar"
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}