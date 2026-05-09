package br.edu.fatecpg.cifa

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.BarcodeCallback
import java.text.SimpleDateFormat
import java.util.*

class ScanActivity : AppCompatActivity() {

    private lateinit var scanner: DecoratedBarcodeView
    private lateinit var tvResultado: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        scanner = findViewById(R.id.barcodeScanner)
        tvResultado = findViewById(R.id.tvResultado)

        iniciarLeitura()
    }

    private fun iniciarLeitura() {
        scanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: com.journeyapps.barcodescanner.BarcodeResult?) {

                result?.text?.let { qrText ->

                    scanner.pause()

                    processarQr(qrText)
                }
            }
        })
    }

    private fun processarQr(qrText: String) {
        try {
            val dados = Gson().fromJson(qrText, Map::class.java)

            val ra = dados["ra"].toString()
            val raLong = ra.toLong()
            val expiraEm = dados["expira_em"].toString()

            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dataExpira = formato.parse(expiraEm)

            if (dataExpira == null || dataExpira.before(Date())) {
                mostrarResultado("QR EXPIRADO ❌", Color.RED)
                return
            }


            db.collection("Alunos")
                .whereEqualTo("ra", raLong)
                .get()
                .addOnSuccessListener { result ->

                    if (!result.isEmpty) {
                        val doc = result.documents[0]

                        val ativo = doc.getBoolean("status_ativo") ?: false
                        val nome = doc.getString("nome") ?: "Aluno"

                        if (ativo) {
                            mostrarResultado("LIBERADO: $nome", Color.GREEN)
                        } else {
                            mostrarResultado("BLOQUEADO", Color.RED)
                        }

                    } else {
                        mostrarResultado("ALUNO NÃO ENCONTRADO", Color.RED)
                    }
                }
                .addOnFailureListener {
                    mostrarResultado("ERRO NO FIREBASE", Color.RED)
                }

        } catch (e: Exception) {
            e.printStackTrace()
            mostrarResultado("QR INVÁLIDO", Color.RED)
        }
    }

    private fun mostrarResultado(msg: String, cor: Int) {
        tvResultado.text = msg
        tvResultado.setTextColor(cor)


        tvResultado.postDelayed({
            scanner.resume()
            tvResultado.text = "Aguardando leitura..."
            tvResultado.setTextColor(Color.WHITE)
        }, 3000)
    }

    override fun onResume() {
        super.onResume()
        scanner.resume()
    }

    override fun onPause() {
        super.onPause()
        scanner.pause()
    }
}