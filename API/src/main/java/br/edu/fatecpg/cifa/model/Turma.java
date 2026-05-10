package br.edu.fatecpg.cifa.model;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Turma {

    @DocumentId
    @Exclude
    private String id;
    private String ano_semestre;
    private String id_curso;
    private String periodo;

}
