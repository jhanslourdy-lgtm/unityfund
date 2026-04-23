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
        return upload(file, "unityfund/campaigns");
    }

    public String saveProfileImage(MultipartFile file) throws IOException {
        return upload(file, "unityfund/profiles");
    }

    private String upload(MultipartFile file, String folder) throws IOException {
        Map<String, Object> options = new HashMap<>();
        options.put("folder", folder);
        options.put("resource_type", "image");
        options.put("overwrite", true);
        
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        return (String) uploadResult.get("secure_url");
    }
}