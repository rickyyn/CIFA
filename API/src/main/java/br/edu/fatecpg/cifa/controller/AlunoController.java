package br.edu.fatecpg.cifa.controller;

import br.edu.fatecpg.cifa.dto.AlunoDto;
import br.edu.fatecpg.cifa.model.Aluno;
import br.edu.fatecpg.cifa.service.AlunoService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/adicionarAluno")
    public ResponseEntity<String> adicionar(@RequestBody Aluno aluno) {
        String id = alunoService.cadastrarAluno(aluno);
        return ResponseEntity.ok("Aluno criado com ID: " + id);
    }

    @GetMapping("/verAlunos")
    public ResponseEntity<List<AlunoDto>> exibirAlunos() {
        List<AlunoDto> lista = alunoService.exibirAlunos();
        return ResponseEntity.ok(lista);
    }

    @PutMapping("/editarAluno/{id}")
    public ResponseEntity<String> editar(@PathVariable String id, @RequestBody Aluno aluno){
        alunoService.editarAluno(id, aluno);
        return ResponseEntity.ok("Aluno atualizado");
    }

    @DeleteMapping("/excluirAluno/{id}")
    public ResponseEntity<String> excluir(@PathVariable String id){
        alunoService.excluirAluno(id);
        return ResponseEntity.ok("Aluno excluido");
    }
}
