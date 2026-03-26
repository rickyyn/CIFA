package br.edu.fatecpg.cifa

import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.util.logging.Handler

class QrcodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrcode)
        
        val btnGerar = findViewById<Button>(R.id.btnGerar)
        val ivQrCode = findViewById<ImageView>(R.id.ivQrCode)
        val tvNome = findViewById<TextView>(R.id.textView2)

        val nomeAluno = intent.getStringExtra("NOME_ALUNO") ?: "Estudante"
        tvNome.text = "Bem-vindo, $nomeAluno"
        tvNome.setTextColor(getColor(R.color.white)) // Garantindo visibilidade no fundo escuro

        val dados = mapOf(
            "nome" to nomeAluno,
            "idade" to 20
        )
        val jsonDados = Gson().toJson(dados)

        btnGerar.setOnClickListener {
            try {

                val handler = Handler()
                ajustarBrilho(1.0f)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.encodeBitmap(jsonDados, BarcodeFormat.QR_CODE, 400, 400)
                ivQrCode.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
            }
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