package br.edu.fatecpg.cifa

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.Button
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

class QrcodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode)

        val handler = android.os.Handler(Looper.getMainLooper())
        var runnable: Runnable? = null
        val tempoIntervalo = 30000L

        val btnGerar = findViewById<Button>(R.id.btnGerar)
        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)
        val tvNome = findViewById<TextView>(R.id.textView2)

        val nomeAluno = intent.getStringExtra("NOME_ALUNO") ?: "Estudante"
        tvNome.text = "Bem-vindo, $nomeAluno"
        tvNome.setTextColor(getColor(R.color.white))


        fun gerarQrcode(){
            try {
                val expiraEm = System.currentTimeMillis() + 30000
                val dados = mapOf(
                    "nome" to nomeAluno,
                    "idade" to 20,
                    "expira_em" to expiraEm
                )

                val jsonParaQr = Gson().toJson(dados)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.encodeBitmap(jsonParaQr, BarcodeFormat.QR_CODE, 400, 400)
                ivQrCode.setImageBitmap(bitmap)
                ajustarBrilho(1.0f)
            } catch (e: Exception) { e.printStackTrace() }
        }

        btnGerar.setOnClickListener {
            runnable?.let { handler.removeCallbacks(it) }
            runnable = object : Runnable {
                override fun run() {
                    try {
                        gerarQrcode()
                        handler.postDelayed(this, 2000)
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
            handler.post(runnable!!)
        }

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