package br.edu.fatecpg.cifa.controller;
import br.edu.fatecpg.cifa.model.Contato;

import br.edu.fatecpg.cifa.model.SolicitadorSenha;
import br.edu.fatecpg.cifa.service.EmailService;
import br.edu.fatecpg.cifa.service.MensagemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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


    @PostMapping("/responderContato")
    public ResponseEntity<String> responderAluno(@RequestBody Map<String, String> payload) {
        try {

            String emailAluno = payload.get("email");
            String assuntoOriginal = payload.get("assunto");
            String mensagemAdmin = payload.get("mensagem");

            if (emailAluno == null || emailAluno.isEmpty() || mensagemAdmin == null || mensagemAdmin.isEmpty()) {
                return ResponseEntity.badRequest().body("Erro: Os campos 'email' e 'mensagem' são obrigatórios.");
            }
            String assuntoEmail = "RE: " + (assuntoOriginal != null ? assuntoOriginal : "Contato CIFA");


            emailService.enviarEmailContato(emailAluno, assuntoEmail, mensagemAdmin);

            return ResponseEntity.ok("Resposta enviada com sucesso para o aluno!");
        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(500).body("Erro ao processar o envio do e-mail: " + e.getMessage());
        }
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
