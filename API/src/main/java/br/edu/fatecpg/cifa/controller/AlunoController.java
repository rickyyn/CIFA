package br.edu.fatecpg.cifa.controller;

import br.edu.fatecpg.cifa.dto.AlunoDto;
import br.edu.fatecpg.cifa.model.Aluno;
import br.edu.fatecpg.cifa.service.AlunoService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/alunos")
public class AlunoController {

    @Autowired
    private AlunoService alunoService;

    @PostMapping("/ler-qr")
    public String lerQr(@RequestParam("file") MultipartFile file) {
        return alunoService.verificarExpiracao(file);
    }

    @PostMapping(value = "/adicionarAluno", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> adicionar(@ModelAttribute Aluno aluno, @RequestParam("foto") MultipartFile foto) {
        String id = alunoService.cadastrarAluno(aluno, foto);
        return ResponseEntity.ok("Aluno criado com ID: " + id);
    }

    @GetMapping("/verAlunos")
    public ResponseEntity<List<Aluno>> exibirAlunos() {
        List<Aluno> lista = alunoService.exibirAlunos();
        return ResponseEntity.ok(lista);
    }

    @PutMapping(value = "editarAluno/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editar(@PathVariable String id, @RequestPart("aluno") Aluno aluno, @RequestPart(value = "imagem", required = false) MultipartFile imagem){
        alunoService.editarAluno(id, aluno, imagem);
        return ResponseEntity.ok("Aluno atualizado");
    }

    @DeleteMapping("/excluirAluno/{id}")
    public ResponseEntity<String> excluir(@PathVariable String id){
        alunoService.excluirAluno(id);
        return ResponseEntity.ok("Aluno excluido");
    }

    @PostMapping("/verificarAluno")
    public ResponseEntity<String> verificarAluno(@RequestBody Map<String, Object> payload) {
        String nome = (String) payload.get("nome");
        Long ra = ((Number) payload.get("ra")).longValue();
        String dataExpiracao = (String) payload.get("expira_em");
        boolean autorizado = alunoService.validarAcesso(ra);
        System.out.println("Resultado da autorizacao: " + autorizado);
        if (autorizado) {
            return ResponseEntity.ok("Liberado. Aluno: " + nome + " Expira em: " + dataExpiracao);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Negado");
    }

    @GetMapping("/encontrarPorId/{id}")
    public ResponseEntity<Aluno> exibirAlunoPorId(@PathVariable String id){
        Aluno aluno = alunoService.encontrarPorId(id);
        return ResponseEntity.ok(aluno);
    }

    @PostMapping("/importar")
    public ResponseEntity<String> importarCsv(@RequestParam("arquivo") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Arquivo vazio!");
        }
        try {
            alunoService.importarAlunos(file);
            return ResponseEntity.ok("Alunos importados com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro ao processar CSV: " + e.getMessage());
        }
    }

}