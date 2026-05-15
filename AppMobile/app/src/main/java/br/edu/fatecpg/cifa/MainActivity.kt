package br.edu.fatecpg.cifa

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val btnLogin = findViewById<Button>(R.id.btn_login)
        val edtEmail = findViewById<EditText>(R.id.edt_email)
        val edtSenha = findViewById<EditText>(R.id.edt_senha)
        val tvEsqueceuSenha = findViewById<TextView>(R.id.tvEsqueceuSenha)
        val tvContato = findViewById<TextView>(R.id.tvContato)


        btnLogin.setOnClickListener {

            val email = edtEmail.text.toString()
            val senha = edtSenha.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(email, senha)
                .addOnSuccessListener {

                    val uid = auth.currentUser?.uid


                    db.collection("Alunos")
                        .document(uid!!)
                        .get()
                        .addOnSuccessListener { doc ->

                            if (doc.exists()) {


                                val primeiroacesso = doc.getBoolean("primeiro_acesso")
                                if (primeiroacesso == true) {
                                    val intent = Intent(this, PrimeiroAcessoActivity::class.java)

                                    intent.putExtra("EMAIL", email)
                                    intent.putExtra("SENHA", senha)
                                    startActivity(intent)
                                    finish()
                                    return@addOnSuccessListener
                                }
                                val nome = doc.getString("nome") ?: "Aluno"
                                val ra = doc.getLong("ra").toString()

                                val intent = Intent(this, PerfilActivity::class.java)
                                intent.putExtra("NOME_ALUNO", nome)
                                intent.putExtra("RA_ALUNO", ra)
                                startActivity(intent)

                                finish()

                            } else {
                                Toast.makeText(this, "Dados não encontrados", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Email ou senha inválidos", Toast.LENGTH_SHORT).show()
                }
        }

        tvEsqueceuSenha.setOnClickListener {
            startActivity(
                Intent(this, EsqueciSenhaActivity::class.java)
            )
        }

        tvContato.setOnClickListener{
            startActivity(
                Intent(this, ContatoActivity::class.java)
            )

        }

    }
}