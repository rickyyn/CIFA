package br.edu.fatecpg.cifa

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.logging.Handler
import qrcode.QRCode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QrcodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode)

        val handler = android.os.Handler(Looper.getMainLooper())
        var runnable: Runnable? = null
        val tempoIntervalo = 30000L
        val handlerTempo = android.os.Handler(Looper.getMainLooper())
        var tempoRestante = 60
        var runnableTempo: Runnable? = null

        val tempo = findViewById<TextView>(R.id.edt_tempo)
        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)
        val tvNome = findViewById<TextView>(R.id.textView2)

        val nomeAluno = intent.getStringExtra("NOME_ALUNO") ?: "Estudante"
        tvNome.text = "Bem-vindo, $nomeAluno"
        tvNome.setTextColor(getColor(R.color.white))


        val expiraEmMillis = System.currentTimeMillis() + 60000

        val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formato.timeZone = java.util.TimeZone.getTimeZone("America/Sao_Paulo")

        val dataFormatada = formato.format(Date(expiraEmMillis))
        fun gerarQrcode(){
            try {
                val expiraEmMillis = System.currentTimeMillis() + 60000

                val dados = mapOf(
                    "nome" to nomeAluno,
                    "idade" to 20,
                    "expira_em" to dataFormatada
                )

                val jsonParaQr = Gson().toJson(dados)

                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.encodeBitmap(jsonParaQr, BarcodeFormat.QR_CODE, 400, 400)
                ivQrCode.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        runnableTempo = object : Runnable {
            override fun run() {
                tempo.setText("${tempoRestante}s")

                tempoRestante--

                if (tempoRestante < 0) {
                    tempoRestante = 60
                }

                handlerTempo.postDelayed(this, 1000)
            }
        }

        handlerTempo.post(runnableTempo!!)

        runnable?.let { handler.removeCallbacks(it) }
        runnable = object : Runnable {
            override fun run() {
                try {
                    gerarQrcode()
                    handler.postDelayed(this, 60000)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
        handler.post(runnable!!)





        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onPause() {
        super.onPause()
        ajustarBrilho(-1.0f)
    }
    private fun ajustarBrilho(valor: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = valor
        window.attributes = layoutParams
    }

}