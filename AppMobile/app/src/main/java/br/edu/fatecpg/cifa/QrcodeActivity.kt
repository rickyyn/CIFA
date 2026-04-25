package br.edu.fatecpg.cifa

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class QrcodeActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode)

        val handler = android.os.Handler(Looper.getMainLooper())
        val handlerTempo = android.os.Handler(Looper.getMainLooper())

        var tempoRestante = 30

        val imgFoto = findViewById<ImageView>(R.id.imgFoto)

        val uid = intent.getStringExtra("UID")

        if (uid.isNullOrEmpty()) {
            Toast.makeText(this, "UID inválido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("Alunos")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val imageUrl = doc.getString("imageUrl")

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .into(imgFoto)
                }
            }


        val tempo = findViewById<TextView>(R.id.edt_tempo)
        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)
        val tvNome = findViewById<TextView>(R.id.tvNomeUsuario)
        val btnVoltar = findViewById<TextView>(R.id.btnVoltar)



        btnVoltar.setOnClickListener {
            finish()
        }



        val nomeAluno = intent.getStringExtra("NOME_ALUNO") ?: "Estudante"
        val raAluno = intent.getStringExtra("RA_ALUNO") ?: "000000"

        println("RA RECEBIDO: $raAluno")

        tvNome.text = "$nomeAluno"



        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formato.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")

        fun gerarQrcode() {
            try {
                val expiraEmMillis = System.currentTimeMillis() + 30000
                val dataFormatada = formato.format(Date(expiraEmMillis))

                val dados = mapOf(
                    "ra" to raAluno,
                    "nome" to nomeAluno,
                    "expira_em" to dataFormatada
                )

                val json = Gson().toJson(dados)

                val bitmap = BarcodeEncoder().encodeBitmap(
                    json,
                    BarcodeFormat.QR_CODE,
                    400,
                    400
                )

                ivQrCode.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        val runnableTempo = object : Runnable {
            override fun run() {
                tempo.text = "${tempoRestante}s"
                tempoRestante--

                if (tempoRestante < 0) {
                    tempoRestante = 30
                }

                handlerTempo.postDelayed(this, 1000)
            }
        }

        handlerTempo.post(runnableTempo)


        val runnable = object : Runnable {
            override fun run() {
                gerarQrcode()
                handler.postDelayed(this, 30000)
            }
        }

        handler.post(runnable)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
//            insets
//        }
    }

    override fun onPause() {
        super.onPause()
        ajustarBrilho(-1.0f)
    }

    private fun ajustarBrilho(valor: Float) {
        val params = window.attributes
        params.screenBrightness = valor
        window.attributes = params
    }
}