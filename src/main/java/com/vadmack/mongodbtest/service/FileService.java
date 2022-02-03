package com.vadmack.mongodbtest.service;

import com.sun.nio.file.ExtendedCopyOption;
import com.vadmack.mongodbtest.entity.FileMetadata;
import com.vadmack.mongodbtest.entity.FileStatus;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataService metadataService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final Map<String, Future<?>> activeTasks = new HashMap<>();
    private final String SAVE_PREFIX = "SAVE_file-id:";
    private final String COPY_PREFIX = "COPY_file-id:";
    private final String MOVE_PREFIX = "MOVE_file-id:";


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
        Long metadataId = metadataService.create(file, FileStatus.NOT_READY_FOR_USE).getId();

        Runnable task = () -> {
            try (InputStream in = new URL(url).openStream()) {
                Files.copy(in, file.toPath());
                FileMetadata updatedMetadata = metadataService
                        .buildMetadataFromFile(file, FileStatus.READY_FOR_USE);
                updatedMetadata.setId(metadataId);
                metadataService.update(updatedMetadata);
                activeTasks.remove(SAVE_PREFIX + metadataId);
            } catch (IOException ex) {
                throw new ServerSideException("Could not save file: " + ex.getMessage(), ex);
            }
        };

        Future<?> submittedTask = executorService.submit(task);
        activeTasks.put(SAVE_PREFIX + metadataId, submittedTask);
    }

    public Resource load(Long id) {
        FileMetadata metadata = metadataService.findById(id);
        return new FileSystemResource(getAbsolutePathOfFile(metadata));
    }

    public void delete(Long id) {
        FileMetadata metadata = metadataService.findById(id);

        Optional<String> activeTaskKeyOptional = findActiveTaskKey(metadata.getId());
        if (activeTaskKeyOptional.isPresent()){
            Future<?> activeTask = activeTasks.get(activeTaskKeyOptional.get());
            activeTask.cancel(true);
            activeTasks.remove(activeTaskKeyOptional.get());
        }

        metadata.setStatus(FileStatus.NOT_READY_FOR_USE);
        metadataService.update(metadata);

        Runnable task = () -> {
            try {
                Files.deleteIfExists(getAbsolutePathOfFile(metadata));
                metadataService.delete(id);
            } catch (IOException ex) {
                throw new ServerSideException("Could not delete file: " + ex.getMessage(), ex);
            }
        };

        executorService.submit(task);
    }

    public void copy(Long id, String filePathSuffix) {
        checkPathSecurity(filePathSuffix);
        FileMetadata metadata = metadataService.findById(id);
        checkFileStatus(metadata);
        metadataService.checkAlreadyExists(filePathSuffix);

        File file = getAbsolutePathOfFile(filePathSuffix).toFile();
        Long copyId = metadataService.create(file, FileStatus.NOT_READY_FOR_USE).getId();

        Runnable task = () -> {
            try {
                Files.copy(getAbsolutePathOfFile(metadata), file.toPath(), ExtendedCopyOption.INTERRUPTIBLE);
                FileMetadata copyMetadata = metadataService
                        .buildMetadataFromFile(file, FileStatus.READY_FOR_USE);
                copyMetadata.setId(copyId);
                metadataService.update(copyMetadata);
                activeTasks.remove(COPY_PREFIX + id + "_file-id:" + copyId);
            } catch (IOException ex) {
                throw new ServerSideException("Could not save file: " + ex.getMessage(), ex);
            }
        };

        Future<?> submittedTask = executorService.submit(task);
        activeTasks.put(COPY_PREFIX + id + "_file-id:" + copyId, submittedTask);
    }

    public void move(Long id, String filePathSuffix) {
        checkPathSecurity(filePathSuffix);
        FileMetadata metadata = metadataService.findById(id);
        checkFileStatus(metadata);
        metadataService.checkAlreadyExists(filePathSuffix);

        File file = getAbsolutePathOfFile(filePathSuffix).toFile();
        metadata.setStatus(FileStatus.NOT_READY_FOR_USE);
        metadataService.update(metadata);

        Runnable task = () -> {
            try {
                Files.move(getAbsolutePathOfFile(metadata), file.toPath(), ExtendedCopyOption.INTERRUPTIBLE);
                FileMetadata updatedMetadata = metadataService
                        .buildMetadataFromFile(file, FileStatus.READY_FOR_USE);
                updatedMetadata.setId(id);
                metadataService.update(updatedMetadata);
                activeTasks.remove(MOVE_PREFIX + metadata.getId());
            } catch (IOException ex) {
                throw new ServerSideException("Could not save file: " + ex.getMessage(), ex);
            }
        };

        Future<?> submittedTask = executorService.submit(task);
        activeTasks.put(MOVE_PREFIX + metadata.getId(), submittedTask);
        executorService.execute(task);
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
        if (metadata.getStatus() == FileStatus.NOT_READY_FOR_USE) {
            throw new ValidationException("The file is not ready for use");
        }
    }

    private Optional<String> findActiveTaskKey(long id) {
        return activeTasks.keySet()
                .stream()
                .filter(s -> s.contains("file-id:" + id))
                .findFirst();
    }
}
