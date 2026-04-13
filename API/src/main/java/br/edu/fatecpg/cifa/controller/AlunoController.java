package br.edu.fatecpg.cifa.controller;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
