package br.edu.fatecpg.cifa.config;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import org.springframework.context.annotation.Bean;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {

            InputStream serviceAccount = getClass().getClassLoader()
                    .getResourceAsStream("firebase.json");

            if (serviceAccount == null) {
                throw new IOException("O arquivo firebase.json não foi encontrado em src/main/resources");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }
        return FirestoreClient.getFirestore();
    }
}