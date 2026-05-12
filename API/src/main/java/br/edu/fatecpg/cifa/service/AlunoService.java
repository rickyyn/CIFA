package br.edu.fatecpg.cifa.service;
import br.edu.fatecpg.cifa.dto.AlunoDto;
import br.edu.fatecpg.cifa.model.Aluno;
import com.cloudinary.Cloudinary;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import com.cloudinary.utils.ObjectUtils;
@Service
public class AlunoService {
    private final Firestore db;
    private final Cloudinary cloudinary;

    public AlunoService(Firestore db, Cloudinary cloudinary) {
        this.db = db;
        this.cloudinary = cloudinary;
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

    @CacheEvict(value = "alunos", allEntries = true)
    public String cadastrarAluno(Aluno aluno, MultipartFile foto) {
        try {
            Map uploadResult = cloudinary.uploader().upload(foto.getBytes(), ObjectUtils.emptyMap());
            String urlDaFoto = (String) uploadResult.get("url");
            aluno.setImagem_url(urlDaFoto);

            aluno.setCreatedat(Timestamp.now());
            aluno.setUpdatedat(Timestamp.now());

            ApiFuture<DocumentReference> docRef = db.collection("Alunos").add(aluno);
            return docRef.get().getId();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao processar cadastro: " + e.getMessage());
        }
    }

    @Cacheable(value = "alunos", key = "'all'")
    public List<Aluno> exibirAlunos() {
        try {
            QuerySnapshot querySnapshot = db.collection("Alunos").get().get();

            return querySnapshot.getDocuments().stream()
                    .map(doc -> doc.toObject(Aluno.class))
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("A consulta ao Firebase foi interrompida", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Falha ao recuperar dados do Firebase", e);
        }
    }

    public void editarAluno(String id, Aluno aluno, MultipartFile imagem){
        try{

            DocumentReference docRef = db.collection("Alunos").document(id);

            if (imagem != null && !imagem.isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(imagem.getBytes(), ObjectUtils.emptyMap());
                String urlDaFoto = (String) uploadResult.get("url");
                aluno.setImagem_url(urlDaFoto);
            }

            docRef.update(
                    "nome", aluno.getNome(),
                    "email_institucional", aluno.getEmail_institucional(),
                    "email_pessoal", aluno.getEmail_pessoal(),
                    "ra", aluno.getRa(),
                    "id_turma", aluno.getId_turma(),
                    "rfid_tag", aluno.getRfid_tag(),
                    "imagem_url", aluno.getImagem_url(),
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

    @Cacheable(value = "validacao_aluno", key = "#ra")
    public String verificarAlunoBd(Long ra, String nome, String dataExpiracao) {
        try {
            ApiFuture<QuerySnapshot> query = db.collection("Alunos")
                    .whereEqualTo("ra", ra)
                    .whereEqualTo("nome", nome)
                    .get();

            List<QueryDocumentSnapshot> docs = query.get().getDocuments();

            if (docs.isEmpty()) {
                return "Acesso negado: Dados não conferem.";
            }

            String idDocumento = docs.get(0).getId();

            return "Aluno: " + nome + " validado com sucesso! Expira em: " + dataExpiracao;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar verificação: " + e.getMessage());
        }
    }

    public Aluno encontrarPorId(String id){
        try{
            DocumentSnapshot doc = db.collection("Alunos").document(id).get().get();
            if (doc.exists()) {
                return doc.toObject(Aluno.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar aluno " + id, e);
        }
    }
    private Aluno buscarNoFirestore(Long ra) {
        try {
            ApiFuture<QuerySnapshot> query = db.collection("Alunos")
                    .whereEqualTo("ra", ra)
                    .get();
            List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            if (!documents.isEmpty()) {
                QueryDocumentSnapshot doc = documents.get(0);
                Aluno aluno = doc.toObject(Aluno.class);
                aluno.setId(doc.getId());
                return aluno;
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Erro ao buscar RA no Firestore: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
        return null;
    }
    public boolean validarAcesso(Long ra) {
        Aluno aluno = buscarNoFirestore(ra);
        if (aluno != null && aluno.isStatus_ativo()) {
            enviarSinalAbrirCatraca();
            return true;
        }
        return false;
    }

    private void enviarSinalAbrirCatraca() {
        String ipArduino = "192.168.15.150";
        System.out.println("Tentando conexao com: http://" + ipArduino + "/abrir");

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://" + ipArduino + "/abrir"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());

        } catch (Exception e) {
            System.err.println("ERRO DE REDE: O Java nao alcanca o ESP32. Detalhes: " + e.getMessage());
        }
    }

}