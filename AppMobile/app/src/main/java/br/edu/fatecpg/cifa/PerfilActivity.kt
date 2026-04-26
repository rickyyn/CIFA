package br.edu.fatecpg.cifa

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.fatecpg.cifa.cloudinary.CloudinaryConfig.cloudinary
import com.bumptech.glide.Glide
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PerfilActivity : AppCompatActivity() {

    private lateinit var imgPerfil: ImageView
    private lateinit var uid: String
    private val db = FirebaseFirestore.getInstance()


    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                imgPerfil.setImageURI(uri)
                val tempFile = createTempFileFromUri(uri)
                tempFile?.let {
                    uploadImage(it.absolutePath)
                }
            }
        }
    }
    private var curso: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        val tvNome = findViewById<TextView>(R.id.tvNome)
        val tvRa = findViewById<TextView>(R.id.tvRa)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvCiclo = findViewById<TextView>(R.id.tvCiclo)
        imgPerfil = findViewById(R.id.imgPerfil)
        val btnHorarios = findViewById<LinearLayout>(R.id.btnHorarios)
        val btnQr = findViewById<LinearLayout>(R.id.btnQr)
        val btnVoltar = findViewById<TextView>(R.id.btnVoltar)
        val tvCurso = findViewById<TextView>(R.id.tvCurso)
        val tvTurno = findViewById<TextView>(R.id.tvTurno)





        uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            finish()
            return
        }



        btnVoltar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        db.collection("Alunos").document(uid).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nome = doc.getString("nome") ?: "Aluno"
                val ra = doc.get("ra")?.toString() ?: "000000"
                val ativo = doc.getBoolean("status_ativo") ?: false
                val ciclo = doc.get("ciclo_atual") ?: "N/A"
                val imagemUrl = doc.getString("imagemUrl")
                val auth = FirebaseAuth.getInstance()
                curso = doc.getString("id_curso")?.trim()?.uppercase()

                btnHorarios.isEnabled = true



                tvNome.text = "$nome"
                tvRa.text = "Registo do Aluno: $ra"
                tvCiclo.text = "Ciclo: $ciclo"
                tvStatus.text = if (ativo) "Matricula: Ativa" else "Matricula: Inativa"
                tvCurso.text = "Curso: $curso"

                when (curso) {
                    "DSM" -> tvTurno.text = "Turno: Vespertino"
                    "ADS" -> tvTurno.text ="Turno: Noturno"
                    "GE" -> tvTurno.text ="Turno: Matutino"
                    "COMEX" -> tvTurno.text ="Turno: Noturno"
                    "PQ" -> tvTurno.text = "Turno: Matutino"
                }


                if (!imagemUrl.isNullOrEmpty()) {
                    Glide.with(this).load(imagemUrl).into(imgPerfil)
                }

                btnQr.setOnClickListener {
                    val intent = Intent(this, QrcodeActivity::class.java)
                    intent.putExtra("NOME_ALUNO", nome)
                    intent.putExtra("RA_ALUNO", ra)
                    intent.putExtra("UID", auth.currentUser?.uid)
                    startActivity(intent)
                }
            }
        }

        imgPerfil.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }
        btnHorarios.setOnClickListener {
            val c = curso

            if (c.isNullOrEmpty()) {
                Toast.makeText(this, "Curso ainda não carregou", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, HorariosActivity::class.java)
            intent.putExtra("CURSO", c)
            startActivity(intent)
        }
    }


    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun uploadImage(filePath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = cloudinary.uploader().upload(filePath, ObjectUtils.emptyMap())
                val imagemUrl = result["secure_url"] as String

                db.collection("Alunos").document(uid).update("imagem_url", imagemUrl).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PerfilActivity, "Foto atualizada!", Toast.LENGTH_SHORT).show()
                }

                File(filePath).delete()
            } catch (e: Exception) {
                Log.e("UPLOAD_ERROR", e.message.toString())
            }
        }
    }
}
