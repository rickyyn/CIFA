package br.edu.fatecpg.cifa.service;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

@Service
public class AlunoService {

    public DocumentSnapshot buscar(String uid) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        return db.collection("alunos")
                .document(uid)
                .get()
                .get();
    }
}
