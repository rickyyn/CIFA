package br.edu.fatecpg.cifa.controller;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/rfid")
public class RFIDController {
    @PostMapping("/vincular")
    public String vincularRfid(@RequestBody Map<String, String> dados) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        String uid = dados.get("uid");
        String alunoId = dados.get("alunoId");
        Map<String, Object> rfid = new HashMap<>();
        rfid.put("alunoId", alunoId);
        rfid.put("status", true);
        rfid.put("createdAt", Timestamp.now());
        db.collection("rfid").document(uid).set(rfid);
        return "RFID vinculado!";
    }
    @PostMapping("/validar")
    public Map<String, String> validar(@RequestBody Map<String, String> dados) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        String uid = dados.get("uid");
        DocumentSnapshot rfidDoc = db.collection("rfid").document(uid).get().get();
        if (!rfidDoc.exists()) {
            return Map.of("status", "erro");
        }
        Boolean status = rfidDoc.getBoolean("status");
        String alunoId = rfidDoc.getString("alunoId");

        if (status == null || !status) {
            return Map.of("status", "bloqueado");
        }

        Map<String, Object> log = new HashMap<>();
        log.put("alunoId", alunoId);
        log.put("uid", uid);
        log.put("timestamp", Timestamp.now());
        db.collection("Logs").add(log).get();
        return Map.of("status", "liberado");
    }
}
