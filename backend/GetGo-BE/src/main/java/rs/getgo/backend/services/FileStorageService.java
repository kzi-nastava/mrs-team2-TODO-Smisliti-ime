package rs.getgo.backend.services;

import lombok.Getter;
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

    private final Path fileStorageLocation;
    @Getter
    private final String defaultProfilePicture;
    private final String baseUrl;

    public FileStorageService() {
        String uploadDir = "uploads";
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }

        baseUrl = "/uploads/";
        defaultProfilePicture = "/uploads/default-avatar.png";
    }

    public String storeFile(MultipartFile file, String prefix) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = null;
            if (originalFilename != null) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = prefix + "_" + UUID.randomUUID() + extension;

            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return baseUrl + newFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file", ex);
        }
    }

    public String renameFile(String originalUrl, String newUrl) {
        try {
            if (originalUrl == null || originalUrl.equals(defaultProfilePicture)) {
                return originalUrl;
            }

            // Extract filenames from URLs
            String originalFilename = originalUrl.replace(baseUrl, "");
            String newFilename = newUrl.replace(baseUrl, "");

            Path originalPath = this.fileStorageLocation.resolve(originalFilename).normalize();
            Path newPath = this.fileStorageLocation.resolve(newFilename).normalize();

            if (!Files.exists(originalPath)) {
                throw new RuntimeException("Original file not found: " + originalFilename);
            }

            // Move/rename the file
            Files.move(originalPath, newPath, StandardCopyOption.REPLACE_EXISTING);

            return newUrl;

        } catch (IOException ex) {
            throw new RuntimeException("Failed to rename file from " + originalUrl + " to " + newUrl, ex);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && !fileUrl.equals(defaultProfilePicture)) {
                // Extract filename from URL
                String filename = fileUrl.replace(baseUrl, "");
                Path filePath = this.fileStorageLocation.resolve(filename).normalize();
                Files.deleteIfExists(filePath);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file", ex);
        }
    }

    public String generateProfilePictureUrl(String userType, Long userId, String originalFileUrl) {
        String extension = extractExtension(originalFileUrl);
        String newFilename = userType + "_" + userId + "_" + UUID.randomUUID() + extension;
        return baseUrl + newFilename;
    }

    private String extractExtension(String fileUrl) {
        String extension = "";
        int dotIndex = fileUrl.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = fileUrl.substring(dotIndex);
        }
        return extension;
    }
}