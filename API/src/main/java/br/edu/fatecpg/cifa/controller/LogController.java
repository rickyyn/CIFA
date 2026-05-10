package br.edu.fatecpg.cifa.controller;

import br.edu.fatecpg.cifa.dto.LogDto;
import br.edu.fatecpg.cifa.service.LogService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/relatorio")
public class LogController {

    @Autowired
    LogService logService;

    @GetMapping("/alunos/{id}")
    public Map<String, Object> getAluno(@PathVariable String id) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        DocumentSnapshot doc = db.collection("Alunos").document(id).get().get();
        return doc.getData();
    }

    @GetMapping("/exibirTodos")
    public ResponseEntity<List<LogDto>> getTodosOsLogs() {
        return ResponseEntity.ok(logService.exibirRelatorio());
    }
}
