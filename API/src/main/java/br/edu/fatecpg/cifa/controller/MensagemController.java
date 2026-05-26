package br.edu.fatecpg.cifa.controller;
import br.edu.fatecpg.cifa.model.Contato;

import br.edu.fatecpg.cifa.model.SolicitadorSenha;
import br.edu.fatecpg.cifa.service.EmailService;
import br.edu.fatecpg.cifa.service.MensagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mensagem")
public class MensagemController {

    @Autowired
    private MensagemService mensagemService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/exibirContatos")
    public ResponseEntity<List<Contato>> exibirContatos(){
        return ResponseEntity.ok(mensagemService.exibirTodosContato());
    }

    @GetMapping("/exibirContatoPorId/{id}")
    public ResponseEntity <Contato> exibirContatosId(@PathVariable String id){
        return ResponseEntity.ok(mensagemService.exibirContatoPorId(id));
    }

    @PutMapping("/editarContato/{id}")
    public ResponseEntity<String> editarContato(@PathVariable String id, @RequestBody Contato contato){
        String resposta = mensagemService.editarContato(id, contato);
        return ResponseEntity.ok(resposta);
    }

//    ===============================================================================================================

    @GetMapping("/exibirsSolicitacoesSenha")
    public ResponseEntity<List<SolicitadorSenha>> exibirSolicitacoes(){
        return ResponseEntity.ok(mensagemService.exibirTodosSolicitacoesSenha());
    }

    @GetMapping("/exibirSolicitacaoPorId/{id}")
    public ResponseEntity <SolicitadorSenha> exibirSolicitacaoPorId(@PathVariable String id){
        return ResponseEntity.ok(mensagemService.exibirSolicitacaoPorId(id));
    }

    @PutMapping("/editarSolicitacao/{id}")
    public ResponseEntity<String> editarSolicitacao(@PathVariable String id, @RequestBody SolicitadorSenha ss){
        String resposta = mensagemService.editarSolicitacao(id, ss);
        return ResponseEntity.ok(resposta);
    }



}
