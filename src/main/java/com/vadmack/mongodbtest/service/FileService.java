package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.entity.FileMetadata;
import com.vadmack.mongodbtest.exception.ServerSideException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataService metadataService;

    @Value("${upload.path}")
    private String rootDirectory;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(rootDirectory));
        } catch (IOException ex) {
            throw new ServerSideException("Could not create upload folder: " + ex.getMessage());
        }
    }

    public void saveFromLink(String url) {
        String[] urlParts = url.split("/");
        String filename = urlParts[urlParts.length - 1];
        metadataService.checkAlreadyExists(filename);

        File file = new File(String.valueOf(Paths.get(rootDirectory).resolve(filename)));
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, file.toPath());
            metadataService.create(file);
        } catch (IOException ex) {
            throw new ServerSideException("Could not save file: " + ex.getMessage());
        }
    }

    public Resource load(Long id) {
        FileMetadata metadata = metadataService.findById(id);
        return new FileSystemResource(Paths.get(rootDirectory).resolve(metadata.getDirectory()));
    }

    public void delete(Long id) {
        FileMetadata metadata = metadataService.findById(id);
        try {
            Files.deleteIfExists(Paths.get(rootDirectory).resolve(metadata.getDirectory()));
            metadataService.delete(id);
        } catch (IOException ex) {
            throw new ServerSideException("Could not delete file: " + ex.getMessage());
        }
    }

    public void copy(Long id, String toDirectory) {
        FileMetadata metadata = metadataService.findById(id);
        metadataService.checkAlreadyExists(toDirectory);

        File file = new File(String.valueOf(Paths.get(rootDirectory).resolve(toDirectory)));
        try {
            Files.copy(Paths.get(rootDirectory).resolve(metadata.getDirectory()),
                    file.toPath());
            metadataService.create(file);
        } catch (IOException ex) {
            throw new ServerSideException("Could not save file: " + ex.getMessage());
        }
    }

    public void move(Long id, String toDirectory) {
        FileMetadata metadata = metadataService.findById(id);
        metadataService.checkAlreadyExists(toDirectory);

        File file = new File(String.valueOf(Paths.get(rootDirectory).resolve(toDirectory)));
        try {
            Files.move(Paths.get(rootDirectory).resolve(metadata.getDirectory()),
                    file.toPath());
            FileMetadata updatedMetadata = metadataService.getMetadataFromFile(file);
            updatedMetadata.setId(metadata.getId());
            metadataService.update(updatedMetadata);
        } catch (IOException ex) {
            throw new ServerSideException("Could not save file: " + ex.getMessage());
        }
    }
}
