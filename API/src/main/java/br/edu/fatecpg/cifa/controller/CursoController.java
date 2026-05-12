package br.edu.fatecpg.cifa.controller;
import br.edu.fatecpg.cifa.model.Curso;
import br.edu.fatecpg.cifa.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    @PostMapping("/adicionarCurso")
    public ResponseEntity<String> adicionar(@RequestBody Curso curso) {
        String id = cursoService.cadastrarCurso(curso);
        return ResponseEntity.ok("Curso criado com sucesso, id:" + id);
    }

    @PutMapping("/editarCurso/{id}")
    public ResponseEntity<String> editar(@PathVariable String id, @RequestBody Curso curso){
        String resposta = cursoService.editarCurso(id, curso);
        return ResponseEntity.ok(resposta);
    }

    @DeleteMapping("/excluirCurso/{id}")
    public ResponseEntity<String> excluir(@PathVariable String id){
        String resposta = cursoService.excluirCurso(id);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/buscarPeloId/{id}")
    public ResponseEntity<Curso> buscarId(@PathVariable String id){
        Curso curso = cursoService.cursoPeloId(id);
        return ResponseEntity.ok(curso);
    }
}
