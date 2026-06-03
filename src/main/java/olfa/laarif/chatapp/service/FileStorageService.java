package olfa.laarif.chatapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public FileStorageService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'initialiser le dossier de stockage uploads", e);
        }
    }

    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Le fichier est vide");
            }
            // Générer un nom unique pour éviter les collisions
            String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueFilename)).normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Retourne l'URL / le chemin d'accès relatif (ex: /uploads/nom-du-fichier.png)
            return "/uploads/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Échec du stockage du fichier", e);
        }
    }
}