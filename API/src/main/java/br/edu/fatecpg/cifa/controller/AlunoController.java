package br.edu.fatecpg.cifa.controller;

import br.edu.fatecpg.cifa.dto.AlunoDto;
import br.edu.fatecpg.cifa.model.Aluno;
import br.edu.fatecpg.cifa.service.AlunoService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/verificarAluno/{id}")
    public ResponseEntity<String> verificarAluno(@RequestBody Aluno aluno){
        return ResponseEntity.ok("Dado encontrado");
    }

    @GetMapping("/encontrarPorId/{id}")
    public ResponseEntity<Aluno> exibirAlunoPorId(@PathVariable String id){
        Aluno aluno = alunoService.encontrarPorId(id);
        return ResponseEntity.ok(aluno);
    }


}