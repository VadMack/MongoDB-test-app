package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.entity.FileMetadata;
import com.vadmack.mongodbtest.exception.ServerSideException;
import com.vadmack.mongodbtest.exception.ValidationException;
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
import java.nio.file.Path;
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
            throw new ServerSideException("Could not create upload folder: " + ex.getMessage(), ex);
        }
    }

    public void saveFromLink(String url) {
        String[] urlParts = url.split("/");
        String filename = urlParts[urlParts.length - 1];
        metadataService.checkAlreadyExists("", filename);

        File file = getAbsolutePathOfFile(filename).toFile();
        Long metadataId = metadataService.create(file, FileMetadata.Status.NOT_READY_FOR_USE).getId();

        Runnable task = () -> {
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, file.toPath());
                FileMetadata updatedMetadata = metadataService
                        .buildMetadataFromFile(file, FileMetadata.Status.READY_FOR_USE);
                updatedMetadata.setId(metadataId);
                metadataService.update(updatedMetadata);
            } catch (IOException ex) {
                throw new ServerSideException("Could not save file: " + ex.getMessage(), ex);
            }
        };

        Thread fileSaveThread = new Thread(task);
        fileSaveThread.start();
    }

    public Resource load(Long id) {
        FileMetadata metadata = metadataService.findById(id);
        return new FileSystemResource(getAbsolutePathOfFile(metadata));
    }

    public void delete(Long id) {
        FileMetadata metadata = metadataService.findById(id);
        checkFileStatus(metadata);
        metadata.setStatus(FileMetadata.Status.NOT_READY_FOR_USE);
        metadataService.update(metadata);

        Runnable task = () -> {
            try {
                Files.deleteIfExists(getAbsolutePathOfFile(metadata));
                metadataService.delete(id);
            } catch (IOException ex) {
                throw new ServerSideException("Could not delete file: " + ex.getMessage(), ex);
            }
        };

        Thread fileSaveThread = new Thread(task);
        fileSaveThread.start();
    }

    public void copy(Long id, String filePathSuffix) {
        checkPathSecurity(filePathSuffix);
        FileMetadata metadata = metadataService.findById(id);
        checkFileStatus(metadata);
        metadataService.checkAlreadyExists(filePathSuffix);

        File file = getAbsolutePathOfFile(filePathSuffix).toFile();
        Long copyId = metadataService.create(file, FileMetadata.Status.NOT_READY_FOR_USE).getId();

        Runnable task = () -> {
            try {
                Files.copy(getAbsolutePathOfFile(metadata), file.toPath());
                FileMetadata copyMetadata  = metadataService
                        .buildMetadataFromFile(file, FileMetadata.Status.READY_FOR_USE);
                copyMetadata.setId(copyId);
                metadataService.update(copyMetadata);
            } catch (IOException ex) {
                throw new ServerSideException("Could not save file: " + ex.getMessage(), ex);
            }
        };

        Thread fileCopyThread = new Thread(task);
        fileCopyThread.start();
    }

    public void move(Long id, String filePathSuffix) {
        checkPathSecurity(filePathSuffix);
        FileMetadata metadata = metadataService.findById(id);
        checkFileStatus(metadata);
        metadataService.checkAlreadyExists(filePathSuffix);

        File file = getAbsolutePathOfFile(filePathSuffix).toFile();
        metadata.setStatus(FileMetadata.Status.NOT_READY_FOR_USE);
        metadataService.update(metadata);

        Runnable task = () -> {
            try {
                Files.move(getAbsolutePathOfFile(metadata), file.toPath());
                FileMetadata updatedMetadata = metadataService
                        .buildMetadataFromFile(file, FileMetadata.Status.READY_FOR_USE);
                updatedMetadata.setId(id);
                metadataService.update(updatedMetadata);
            } catch (IOException ex) {
                throw new ServerSideException("Could not save file: " + ex.getMessage(), ex);
            }
        };

        Thread fileMoveThread = new Thread(task);
        fileMoveThread.start();
    }

    private Path getAbsolutePathOfFile(FileMetadata fileMetadata) {
        return Paths.get(rootDirectory)
                .resolve(fileMetadata.getDirectory()).resolve(fileMetadata.getOriginalFilename());
    }

    private Path getAbsolutePathOfFile(String filePathSuffix) {
        return Paths.get(rootDirectory)
                .resolve(filePathSuffix);
    }

    private void checkPathSecurity(String path) {
        if (path.contains("..")) {
            throw new ValidationException("'filePathSuffix' cannot contain '..'");
        }
    }

    private void checkFileStatus(FileMetadata metadata) {
        if (metadata.getStatus() == FileMetadata.Status.NOT_READY_FOR_USE) {
            throw new ValidationException("The file is not ready for use");
        }
    }
}
