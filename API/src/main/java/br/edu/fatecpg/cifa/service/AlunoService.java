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

    public String cadastrarAluno(Aluno aluno, MultipartFile foto) {
        try {
            // 1. Upload da imagem para o Cloudinary
            // O ObjectUtils.asMap ajuda a definir opções como o nome da pasta (opcional)
            Map uploadResult = cloudinary.uploader().upload(foto.getBytes(), ObjectUtils.emptyMap());

            // 2. Extrair a URL pública gerada pelo Cloudinary
            String urlDaFoto = (String) uploadResult.get("url");

            // 3. Preencher os dados do objeto Aluno
            // Aqui usamos o campo que já existe no teu Firestore
            aluno.setImagem_url(urlDaFoto);

            // Definir timestamps de criação e atualização
            aluno.setCreatedat(Timestamp.now());
            aluno.setUpdatedat(Timestamp.now());

            // 4. Salvar o objeto completo na coleção "Alunos" do Firestore
            ApiFuture<DocumentReference> docRef = db.collection("Alunos").add(aluno);

            // Retorna o ID do documento criado para confirmação
            return docRef.get().getId();

        } catch (Exception e) {
            // Log do erro para depuração no console do IntelliJ
            e.printStackTrace();
            throw new RuntimeException("Erro ao processar cadastro: " + e.getMessage());
        }
    }

    @Cacheable(value = "listaAlunosCache")
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

    public void editarAluno(String id, Aluno aluno){
        try{
            DocumentReference docRef = db.collection("Alunos").document(id);
            docRef.update(
                    "nome", aluno.getNome(),
                    "email", aluno.getEmail(),
                    "ra", aluno.getRa(),
                    "id_curso", aluno.getId_curso(),
                    "rfid_tag", aluno.getRfid_tag(),
                    "imagemUrl", aluno.getImagem_url(),
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

//    public String verificarAlunoBd(Aluno aluno){
//        try{
//
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }


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

}