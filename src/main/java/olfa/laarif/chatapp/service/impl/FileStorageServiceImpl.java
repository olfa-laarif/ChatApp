package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path rootLocation = Paths.get("uploads");

    public FileStorageServiceImpl() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'initialiser le dossier de stockage uploads", e);
        }
    }

    @Override
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

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            // Extrait le nom unique du fichier depuis l'URL (ex: /uploads/uuid_photo.png -> uuid_photo.png)
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = this.rootLocation.resolve(filename).normalize().toAbsolutePath();

            // Suppression physique du fichier s'il existe sur le disque
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log l'erreur sans planter la transaction métier principale
            System.err.println("Échec de la suppression physique du fichier : " + fileUrl + " -> " + e.getMessage());
        }
    }
}