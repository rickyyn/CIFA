package br.edu.fatecpg.cifa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Aluno {

    @DocumentId
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String id;
    private String nome;
    private String email_pessoal;
    private String email_institucional;
    private Long ra;
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