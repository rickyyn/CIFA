package br.edu.fatecpg.cifa.service;

import br.edu.fatecpg.cifa.model.Turma;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class TurmaService {

    private Firestore db;

    public TurmaService(Firestore db){
        this.db = db;
    }

    public String adicionar(Turma turma){
        try{
        String idTurma = turma.getId();
            if (idTurma == null || idTurma.isEmpty()) {
                throw new RuntimeException("O ID da turma não pode ser vazio");
            }
            DocumentReference docRef = db.collection("Turmas").document(idTurma);
            docRef.set(turma).get();
            return idTurma;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String editar(String id, Turma turma){
        try{
            DocumentReference docRef = db.collection("Turmas").document(id);
            docRef.update(
                    "ano_semestre", turma.getAno_semestre(),
                    "id_curso", turma.getId_curso(),
                    "periodo", turma.getPeriodo()
            );
            return "Editado com sucesso";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String excluir(String id){
        try{
            DocumentReference docRef = db.collection("Turmas").document(id);
            ApiFuture<WriteResult> resposta = docRef.delete();
            resposta.get();
            return "Excluido com sucesso";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
