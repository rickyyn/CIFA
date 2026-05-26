package br.edu.fatecpg.cifa.service;
import br.edu.fatecpg.cifa.model.*;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MensagemService {

    private final Firestore db;

    public MensagemService(Firestore db){
        this.db = db;
    }

//    codigos para Contatos, n é ia isso nao eu que coloquei mesmo
    public List<Contato> exibirTodosContato(){
        try{
            QuerySnapshot querySnapshot = db.collection("Mensagens").get().get();
            return querySnapshot.getDocuments().stream()
                    .map(doc -> doc.toObject(Contato.class))
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Contato exibirContatoPorId(String id){
        try {
            DocumentSnapshot doc = db.collection("Mensagens").document(id).get().get();
            if (doc.exists()) {
                return doc.toObject(Contato.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public String editarContato(String id, Contato contato) {
        try {
            DocumentReference docRef = db.collection("Mensagens").document(id);
            docRef.update(
                "status", contato.getStatus()
            );
            return "Editado com sucesso";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //    codigos para Solicitações de senha, n é ia isso nao eu que coloquei mesmo
    public List<SolicitadorSenha> exibirTodosSolicitacoesSenha(){
        try{
            QuerySnapshot querySnapshot = db.collection("SolicitacoesSenha").get().get();
            return querySnapshot.getDocuments().stream()
                    .map(doc -> doc.toObject(SolicitadorSenha.class))
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public SolicitadorSenha exibirSolicitacaoPorId(String id){
        try {
            DocumentSnapshot doc = db.collection("SolicitacoesSenha").document(id).get().get();
            if (doc.exists()) {
                return doc.toObject(SolicitadorSenha.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public String editarSolicitacao(String id, SolicitadorSenha ss) {
        try {
            DocumentReference docRef = db.collection("SolicitacoesSenha").document(id);
            docRef.update(
                    "status", ss.getStatus()
            );
            return "Editado com sucesso";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
