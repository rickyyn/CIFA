package br.edu.fatecpg.cifa.service;

import br.edu.fatecpg.cifa.dto.LogDto;
import br.edu.fatecpg.cifa.model.Log;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class LogService {

    private final Firestore db;

    public LogService(Firestore db) {
        this.db = db;
    }

    public List<Log> exibirRelatorioGeral() {
        try {
            Query query = db.collectionGroup("Logs")
                    .orderBy("timestamp", Query.Direction.DESCENDING);

            QuerySnapshot querySnapshot = query.get().get();

            List<Log> logs = new ArrayList<>();

            for (QueryDocumentSnapshot doc : querySnapshot) {
                try {
                    Log log = new Log();
                    log.setNome(doc.getString("nome"));
                    log.setRa(doc.getLong("ra"));
                    log.setCurso(doc.getString("curso"));
                    log.setTurma(doc.getString("turma"));
                    log.setMetodo(doc.getString("metodo"));

                    Object tipoObj = doc.get("tipo");
                    if (tipoObj instanceof String) {
                        log.setTipo((String) tipoObj);
                    } else {
                        log.setTipo("Erro: Tipo Inválido no Banco");
                        System.err.println("Documento com erro de tipo: " + doc.getId());
                    }
                    log.setStatus_acesso(doc.getBoolean("status_acesso"));
                    log.setTimestamp(doc.getTimestamp("timestamp"));
                    logs.add(log);
                } catch (Exception e) {
                    System.err.println("Pulando documento inválido: " + doc.getId());
                }
            }
            return logs;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar logs: " + e.getMessage());
        }
    }

}
