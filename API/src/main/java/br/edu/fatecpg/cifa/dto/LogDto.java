package br.edu.fatecpg.cifa.dto;
import com.google.cloud.Timestamp;
public record LogDto(
        String nome,
        Long ra,
        String curso,
        String turma,
        String metodo,
        String tipo,
        boolean status_acesso,
        Timestamp timestamp
        )
{}
