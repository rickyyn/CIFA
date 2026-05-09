package br.edu.fatecpg.cifa

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class EsqueciSenhaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_esqueci_senha)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnEnviar = findViewById<Button>(R.id.btnEnviarSolicitacao)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)


        btnEnviar.setOnClickListener {

            val email = edtEmail.text.toString()

            val dados = hashMapOf(

                "email" to email,
                "status" to "pendente",
                "data" to com.google.firebase.Timestamp.now()
            )

            FirebaseFirestore.getInstance()
                .collection("SolicitacoesSenha")
                .add(dados)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Solicitação enviada!",
                        Toast.LENGTH_LONG
                    ).show()

                    finish()
                }
        }
    }
}