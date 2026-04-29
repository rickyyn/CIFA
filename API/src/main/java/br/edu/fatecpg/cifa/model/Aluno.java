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
    private String id;
    private String nome;
    private Long ra;
    private String rfid_tag;
    private boolean status_ativo;
    private boolean esta_no_campus;
    @JsonIgnore
    private Timestamp createdat;
    @JsonIgnore
    private Timestamp updatedat;
}