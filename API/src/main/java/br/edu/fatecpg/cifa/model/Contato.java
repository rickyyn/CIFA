package br.edu.fatecpg.cifa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Contato {

    @DocumentId
    private String id;

    @JsonIgnore
    private Timestamp data;
    private String email;
    private String mensagem;
    private String nome;
    private String status;

}
