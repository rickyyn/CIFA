package br.edu.fatecpg.cifa.service;

import br.edu.fatecpg.cifa.dto.LogDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    private final Firestore db;

    public LogService(Firestore db) {
        this.db = db;
    }

    public List<LogDto> exibirRelatorio() {
        try {
            ApiFuture<QuerySnapshot> future = db.collectionGroup("Logs").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            return documents.stream().map(doc -> {
                return new LogDto(
                        doc.getString("nome"),
                        doc.getLong("ra"),
                        doc.getString("curso"),
                        doc.getString("turma"),
                        doc.getString("metodo"),
                        doc.getString("tipo"),
                        doc.getBoolean("status_acesso"),
                        doc.getTimestamp("timestamp")
                );
            }).toList();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar logs", e);
        }
    }
}
