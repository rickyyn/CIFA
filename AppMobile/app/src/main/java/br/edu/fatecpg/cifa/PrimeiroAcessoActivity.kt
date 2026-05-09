package br.edu.fatecpg.cifa

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PrimeiroAcessoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primeiro_acesso)

        auth = FirebaseAuth.getInstance()

        val edtNovaSenha = findViewById<EditText>(R.id.edtNovaSenha)
        val etConfirmarSenha = findViewById<EditText>(R.id.etConfirmarSenha)
        val btnMudarSenha = findViewById<Button>(R.id.btnMudarSenha)

        val email = intent.getStringExtra("EMAIL") ?: ""
        val senhaAtual = intent.getStringExtra("SENHA") ?: ""

        btnMudarSenha.setOnClickListener {

            val novaSenha = edtNovaSenha.text.toString()
            val confirmar = etConfirmarSenha.text.toString()

            if (novaSenha.isEmpty() || confirmar.isEmpty()) {

                Toast.makeText(this, "Preencha tudo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (novaSenha != confirmar) {

                Toast.makeText(this, "Senhas diferentes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser

            if (user != null) {

                val credential =
                    EmailAuthProvider.getCredential(email, senhaAtual)

                user.reauthenticate(credential)
                    .addOnSuccessListener {

                        Toast.makeText(this, "Reautenticado", Toast.LENGTH_SHORT).show()

                        user.updatePassword(novaSenha)
                            .addOnSuccessListener {

                                Toast.makeText(
                                    this,
                                    "Senha alterada!",
                                    Toast.LENGTH_LONG
                                ).show()

                                db.collection("Alunos")
                                    .document(user.uid)
                                    .update("primeiro_acesso", false)

                                startActivity(
                                    Intent(this, PerfilActivity::class.java)
                                )

                                finish()
                            }
                            .addOnFailureListener { e ->

                                Toast.makeText(
                                    this,
                                    "ERRO UPDATE: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->

                        Toast.makeText(
                            this,
                            "ERRO REAUTH: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }
    }
}