package br.edu.fatecpg.cifa.service;
import br.edu.fatecpg.cifa.dto.AlunoDto;
import br.edu.fatecpg.cifa.model.Aluno;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;


@Service
public class AlunoService {
    private final Firestore db;

    public AlunoService(Firestore db) {
        this.db = db;
    }

    public DocumentSnapshot buscar(String uid) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        return db.collection("Alunos")
                .document(uid)
                .get()
                .get();
    }

    public String lerqrcode(MultipartFile arquivo) {
        try {

            BufferedImage imagem = ImageIO.read(arquivo.getInputStream());

            if (imagem == null) {
                return "Erro: Arquivo não encontrado no caminho especificado.";
            }

            LuminanceSource source = new BufferedImageLuminanceSource(imagem);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);

            return result.getText();

        } catch (com.google.zxing.NotFoundException e) {
            return "Erro: Nenhum QR Code encontrado na imagem.";
        } catch (Exception e) {
            return "Erro ao processar imagem: " + e.getMessage();
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    public String verificarExpiracao(MultipartFile qrcode){
        try {
            String conteudo = lerqrcode(qrcode);
            JsonNode jsonNode = objectMapper.readTree(conteudo);
            DateTimeFormatter formatarData = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            if(jsonNode.has("expira_em")){
                String dataStr = jsonNode.get("expira_em").asText();
                LocalDateTime dataExpiracao = LocalDateTime.parse(dataStr, formatarData);
                LocalDateTime horarioAtual = LocalDateTime.now();

                if (horarioAtual.isAfter(dataExpiracao)) {
                    return "Acesso Negado! O QR Code expirou em: " + dataStr;
                } else {
                    return "Acesso Liberado! Válido até: " + dataStr;
                }
            } else {
                return "O QR Code não contém um campo de expiração.";
            }
        } catch (Exception e) {
            return "Erro ao processar JSON: " + e.getMessage();
        }
    }

    public String cadastrarAluno(Aluno aluno) {
        try {
            aluno.setCreatedat(Timestamp.now());
            aluno.setUpdatedat(Timestamp.now());
            ApiFuture<DocumentReference> docRef = db.collection("Alunos").add(aluno);
            return docRef.get().getId();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar aluno", e);
        }
    }

    public List<AlunoDto> exibirAlunos() {
        try {
            ApiFuture<QuerySnapshot> future = db.collection("Alunos").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            return documents.stream().map(doc -> {
                Aluno aluno = doc.toObject(Aluno.class);
                return new AlunoDto(
                        aluno.getNome(),
                        aluno.getEmail(),
                        aluno.getRa(),
                        aluno.getId_curso(),
                        aluno.getRfid_tag(),
                        aluno.isStatus_ativo(),
                        aluno.isEsta_no_campus()
                );
            }).toList();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar alunos", e);
        }
    }

    public void editarAluno(String id, Aluno aluno){
        try{
            DocumentReference docRef = db.collection("Alunos").document(id);
            docRef.update(
                    "nome", aluno.getNome(),
                    "email", aluno.getEmail(),
                    "ra", aluno.getRa(),
                    "id_curso", aluno.getId_curso(),
                    "rfid_tag", aluno.getRfid_tag(),
                    "status_ativo", aluno.isStatus_ativo(),
                    "esta_no_campus", aluno.isEsta_no_campus(),
                    "updatedat", Timestamp.now()
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao editar aluno", e);
        }
    }

    public String excluirAluno(String id){
    try{
        DocumentReference docRef = db.collection("Alunos").document(id);
        ApiFuture<WriteResult> resposta = docRef.delete();
        resposta.get();
        return "Aluno excluido com sucesso";
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

}
