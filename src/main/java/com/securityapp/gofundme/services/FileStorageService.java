package com.securityapp.gofundme.services;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class FileStorageService {

    private final Cloudinary cloudinary;

    public FileStorageService(
            @Value("${CLOUDINARY_CLOUD_NAME:}") String cloudName,
            @Value("${CLOUDINARY_API_KEY:}") String apiKey,
            @Value("${CLOUDINARY_API_SECRET:}") String apiSecret) {

        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", true);

        this.cloudinary = new Cloudinary(config);
    }

    public String saveCampaignImage(MultipartFile file) throws IOException {
        validateImage(file);
        return upload(file, "unityfund/campaigns", "image");
    }

    public String saveCampaignMedia(MultipartFile file) throws IOException {
        validateMedia(file);
        // auto permet d'accepter image et vidéo. Avant, resource_type=image faisait échouer les vidéos.
        return upload(file, "unityfund/campaigns/media", "auto");
    }

    public String saveProfileImage(MultipartFile file) throws IOException {
        validateImage(file);
        return upload(file, "unityfund/profiles", "image");
    }

    private String upload(MultipartFile file, String folder, String resourceType) throws IOException {
        Map<String, Object> options = new HashMap<>();
        options.put("folder", folder);
        options.put("resource_type", resourceType);
        options.put("overwrite", true);

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier image reçu.");
        }
        String contentType = file.getContentType() != null ? file.getContentType() : "";
        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image.");
        }
    }

    private void validateMedia(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Aucun fichier média reçu.");
        }
        String contentType = file.getContentType() != null ? file.getContentType() : "";
        if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("Le fichier doit être une image ou une vidéo.");
        }
    }
}
