package br.edu.fatecpg.cifa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Aluno {

    @DocumentId
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;

    @CsvBindByName(column = "nome")
    private String nome;

    @CsvBindByName(column = "email_pessoal")
    private String email_pessoal;

    @CsvBindByName(column = "email_institucional")
    private String email_institucional;

    @CsvBindByName(column = "ra")
    private Long ra;

    @CsvBindByName(column = "id_turma")
    private String id_turma;

    private String rfid_tag;

    private boolean status_ativo;

    private boolean esta_no_campus;

    private String imagem_url;
    private int ciclo_atual;
    @JsonIgnore
    private Timestamp createdat;
    @JsonIgnore
    private Timestamp updatedat;
}