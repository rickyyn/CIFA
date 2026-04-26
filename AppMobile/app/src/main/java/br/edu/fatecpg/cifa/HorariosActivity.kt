package br.edu.fatecpg.cifa

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HorariosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_horarios)

        val curso = intent.getStringExtra("CURSO")
        val btnVoltar = findViewById<TextView>(R.id.btnVoltar)
        val myWebView: WebView = findViewById(R.id.webview)

        myWebView.webViewClient = WebViewClient()
        myWebView.settings.useWideViewPort = true
        myWebView.settings.loadWithOverviewMode = true
        myWebView.settings.databaseEnabled = true

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(myWebView, true)

        android.util.Log.d("HORARIOS", "CURSO RECEBIDO: '$curso'")

        when (curso) {
            "DSM" -> myWebView.loadUrl("https://www.fatecpg.edu.br/grade/atual.php?curso=DSM")
            "GE" -> myWebView.loadUrl("https://www.fatecpg.edu.br/grade/atual.php?curso=GE")
            "COMEX" -> myWebView.loadUrl("https://www.fatecpg.edu.br/grade/atual.php?curso=COMEX")
            "PQ" -> myWebView.loadUrl("https://www.fatecpg.edu.br/grade/atual.php?curso=PQ")
            "ADS" -> myWebView.loadUrl("https://www.fatecpg.edu.br/grade/atual.php?curso=ADS")
            else -> myWebView.loadUrl("https://www.fatecpg.edu.br")
        }

        btnVoltar.setOnClickListener {
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}