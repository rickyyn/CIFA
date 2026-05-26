package br.edu.fatecpg.cifa.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class SolicitadorSenha {

    @DocumentId
    private String id;

    @JsonIgnore
    private Timestamp data;
    private String email;
    private String emailpessoal;
    private String status;
}
