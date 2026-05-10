package br.edu.fatecpg.cifa.service;

import br.edu.fatecpg.cifa.model.Curso;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import org.springframework.stereotype.Service;

@Service
public class CursoService {


    private final Firestore db;

    public CursoService(Firestore db){
        this.db = db;
    }

    public String cadastrarCurso(Curso curso){
        try{
            String idCurso = curso.getId();
            if (idCurso == null || idCurso.isEmpty()) {
                throw new RuntimeException("O ID do curso não pode ser vazio");
            }
            DocumentReference docRef = db.collection("Cursos").document(idCurso);
            docRef.set(curso).get();
            return idCurso;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao processar cadastro: " + e.getMessage());
        }
    }
    public String excluirCurso(String id){
        try{
            DocumentReference docRef = db.collection("Cursos").document(id);
            ApiFuture<WriteResult> resposta = docRef.delete();
            resposta.get();
            return "Curso excluido com sucesso";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String editarCurso(String id, Curso curso){
        try{
            DocumentReference docRef = db.collection("Cursos").document(id);
            docRef.update(
                    "nome", curso.getNome(),
                    "total_ciclos", curso.getTotal_ciclos()
            );
            return "Editado com sucesso";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
