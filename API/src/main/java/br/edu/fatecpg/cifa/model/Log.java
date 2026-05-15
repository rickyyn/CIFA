package br.edu.fatecpg.cifa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Log {
    private String nome;
    private Long ra;
    private String curso;
    private String turma;
    private String metodo;
    private String tipo;
    @PropertyName("status_acesso")
    private boolean status_acesso;

    private Timestamp timestamp;
}
