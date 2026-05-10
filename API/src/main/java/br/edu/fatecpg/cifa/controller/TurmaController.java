package br.edu.fatecpg.cifa.controller;

import br.edu.fatecpg.cifa.model.Turma;
import br.edu.fatecpg.cifa.service.TurmaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/turmas")
public class TurmaController {

    @Autowired
    TurmaService turmaService;

    @PostMapping("/adicionarTurma")
    public ResponseEntity<String> adicionar(@RequestBody Turma turma){
        String id = turmaService.adicionar(turma);
        return ResponseEntity.ok("Turma cadastrada, id: " + id);
    }

    @PutMapping("/editarTurma/{id}")
    public ResponseEntity<String> editar(@PathVariable String id, @RequestBody Turma turma){
        String resposta = turmaService.editar(id, turma);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/excluirTurma/{id}")
    public ResponseEntity<String> excluir(@PathVariable String id) {
        String resposta = turmaService.excluir(id);
        return ResponseEntity.ok(resposta);
    }
}
