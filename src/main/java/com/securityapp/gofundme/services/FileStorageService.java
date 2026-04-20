/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.services;

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

    private static final String BASE_UPLOAD_DIR = System.getProperty("user.dir");

    public String saveCampaignImage(MultipartFile file) throws IOException {
        return saveFile(file, "uploads/campaigns");
    }

    public String saveProfileImage(MultipartFile file) throws IOException {
        return saveFile(file, "uploads/profiles");
    }

    private String saveFile(MultipartFile file, String subPath) throws IOException {
        // Dossier target (runtime)
        Path targetDir = Paths.get(BASE_UPLOAD_DIR, "target", "classes", "static", subPath);
        // Dossier src (persistance)
        Path srcDir = Paths.get(BASE_UPLOAD_DIR, "src", "main", "resources", "static", subPath);

        createDirsIfNeeded(targetDir, srcDir);

        String original = file.getOriginalFilename();
        String extension = original.substring(original.lastIndexOf('.'));
        String fileName = UUID.randomUUID().toString() + extension;

        // Sauvegarde dans target
        Path targetFile = targetDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        // Copie dans src pour persistance
        try {
            Files.copy(targetFile, srcDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Impossible de persister dans src: " + e.getMessage());
        }

        return "/" + subPath.replace("\\", "/") + "/" + fileName;
    }

    private void createDirsIfNeeded(Path... paths) throws IOException {
        for (Path path : paths) {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        }
    }
}