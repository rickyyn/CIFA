package br.edu.fatecpg.cifa.service;
import br.edu.fatecpg.cifa.dto.AlunoDto;
import br.edu.fatecpg.cifa.model.Aluno;
import com.cloudinary.Cloudinary;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.cloud.storage.Bucket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
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
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(aluno.getEmail_institucional())
                    .setPassword("123456")
                    .setDisplayName(aluno.getNome());
            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            String uid = userRecord.getUid();
            Map uploadResult = cloudinary.uploader().upload(foto.getBytes(), ObjectUtils.emptyMap());
            String urlDaFoto = (String) uploadResult.get("url");
            aluno.setImagem_url(urlDaFoto);
            aluno.setCreatedat(Timestamp.now());
            aluno.setUpdatedat(Timestamp.now());
            aluno.setStatus_ativo(true);

            db.collection("Alunos").document(uid).set(aluno).get();
            return uid;
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
    @Cacheable(value = "alunos", key = "#ra")
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

    private String extrairCurso(String turma) {
        if (turma == null || turma.isEmpty()) {
            return "N/A";
        }
        int underlinePos = turma.indexOf('_');
        if (underlinePos != -1) {
            return turma.substring(0, underlinePos);
        }
        return turma;
    }
    private void salvarLogAcessoQrCode(Aluno aluno) {
        try {
            Map<String, Object> fields = new HashMap<>();
            String alunoPath = "Alunos/" + aluno.getId();
            String isoTimestamp = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC-3"))
                    .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            if(aluno.isStatus_ativo()){
            String tipoLog = aluno.isEsta_no_campus() ? "saida" : "entrada";
            fields.put("nome", Map.of("stringValue", aluno.getNome()));
            fields.put("ra", Map.of("integerValue", aluno.getRa()));
            fields.put("turma", Map.of("stringValue", aluno.getId_turma()));
            fields.put("curso", Map.of("stringValue", extrairCurso(aluno.getId_turma())));
            fields.put("metodo", Map.of("stringValue", "QRCode"));
            fields.put("tipo", Map.of("stringValue", tipoLog));
            fields.put("status_acesso", Map.of("booleanValue", true));
            fields.put("timestamp", Map.of("timestampValue", isoTimestamp));
            db.collection("Alunos").document(aluno.getId())
                    .collection("Logs").add(fields);
            db.collection("Alunos").document(aluno.getId())
                    .update("esta_no_campus", !aluno.isEsta_no_campus());
            System.out.println("Log gravado");
            }else{
                fields.put("nome", Map.of("stringValue", aluno.getNome()));
                fields.put("ra", Map.of("integerValue", aluno.getRa()));
                fields.put("turma", Map.of("stringValue", aluno.getId_turma()));
                fields.put("curso", Map.of("stringValue", extrairCurso(aluno.getId_turma())));
                fields.put("metodo", Map.of("stringValue", "QRCode"));
                fields.put("status_acesso", Map.of("booleanValue", false));
                fields.put("timestamp", Map.of("timestampValue", isoTimestamp));
                fields.put("tipo", Map.of("stringValue", "Aluno inativo"));
                db.collection("Alunos").document(aluno.getId())
                        .collection("Logs").add(fields);
                System.out.println("Log gravado");
            }
        } catch (Exception e) {
            System.err.println("Erro do log " + e.getMessage());
        }
    }
    public boolean validarAcesso(Long ra) {
        Aluno aluno = buscarNoFirestore(ra);
        salvarLogAcessoQrCode(aluno);
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
            System.err.println("erro: Java nao acha o ESP32. " + e.getMessage());
        }
    }

    public void importarAlunos(MultipartFile file) throws Exception {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        content = content.replace("\uFEFF", "").trim();
        try (Reader reader = new StringReader(content)) {
            CsvToBean<Aluno> csvToBean = new CsvToBeanBuilder<Aluno>(reader)
                    .withType(Aluno.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(',')
                    .build();
            List<Aluno> alunos = csvToBean.parse();
            for (Aluno aluno : alunos) {
                if (aluno.getRa() != null) {
                    UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(aluno.getEmail_institucional())
                            .setPassword("123456")
                                    .setDisplayName(aluno.getNome())
                                            .setDisabled(false);
                    try {
                        UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
                        aluno.setCreatedat(Timestamp.now());
                        aluno.setUpdatedat(Timestamp.now());
                        aluno.setStatus_ativo(true);
                        aluno.setEsta_no_campus(false);
                        aluno.setCiclo_atual(1);
                        db.collection("Alunos").document(userRecord.getUid()).set(aluno).get();
                        System.out.println("Usuário e Documento criados para: " + aluno.getNome());
                    } catch (FirebaseAuthException e) {
                        System.err.println("Erro ao criar usuário no Auth: " + e.getMessage());
                    }
                }
            }
        }
    }


}