package br.edu.fatecpg.cifa.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Curso {
    @DocumentId
    @Exclude
    private String id;
    private String nome;
    private int total_ciclos;
}
