package br.edu.fatecpg.cifa.dto;

public record AlunoDto(
        String nome,
        String email,
        Long ra,
        String id_curso,
        String rfid_tag,
        boolean status_ativo,
        boolean esta_no_campus
){ }
