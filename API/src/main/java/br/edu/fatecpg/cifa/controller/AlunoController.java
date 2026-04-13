package br.edu.fatecpg.cifa.controller;

import br.edu.fatecpg.cifa.service.AlunoService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/alunos")
public class AlunoController {

    @PostMapping("/criarAluno")
    public String criarAluno(@RequestBody Map<String, Object> dados) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> aluno = new HashMap<>();
        aluno.put("nome", dados.get("nome"));
        aluno.put("ra", dados.get("ra"));
        aluno.put("createdAt", Timestamp.now());
        DocumentReference docRef = db.collection("Alunos").document();
        docRef.set(aluno);
        return docRef.getId();
    }

    @Autowired
    private AlunoService alunoService;

    @PostMapping("/ler-qr")
    public String lerQr(@RequestParam("file") MultipartFile file) {
        return alunoService.verificarExpiracao(file);
    }

}
