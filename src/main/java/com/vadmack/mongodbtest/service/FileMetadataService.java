package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.entity.FileMetadata;
import com.vadmack.mongodbtest.exception.NotFoundException;
import com.vadmack.mongodbtest.exception.ValidationException;
import com.vadmack.mongodbtest.repository.FileMetadataRepository;
import com.vadmack.mongodbtest.util.PageableService;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import com.vadmack.mongodbtest.util.SortService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    @Value("${upload.path}")
    private String rootDirectory;

    private final FileMetadataRepository repository;
    private final PageableService pageableService;
    private final SortService sortService;
    private final SequenceGeneratorService sequenceGeneratorService;

    public List<FileMetadata> findList(
            @Nullable String filenamePart,
            @Nullable Integer pageNumber,
            @Nullable Integer pageSize,
            @Nullable String[] sortBy) {
        if (filenamePart == null) {
            filenamePart = "";
        }
        pageableService.validateParams(pageNumber, pageSize);
        Sort sort = sortService.createSort(sortBy);
        if (pageNumber != null && pageSize != null) {
            if (sortBy == null) {
                sortBy = new String[1];
                sortBy[0] = "id:0";
            }
            return repository.findAllByDirectoryRegex(
                    filenamePart,
                    PageRequest.of(pageNumber, pageSize, sort));
        } else {
            return repository.findAllByDirectoryRegex(filenamePart, sort);
        }
    }

    public FileMetadata findById(Long id) {
        return getById(id);
    }

    public void checkAlreadyExists(String directory) {
        if (repository.findByDirectory(directory).isPresent()) {
            throw new ValidationException("File '" + directory + "' already exists");
        }
    }

    public void create(File file) throws IOException {
        FileMetadata metadata = getMetadataFromFile(file);
        metadata.setId(sequenceGeneratorService.generateSequence(FileMetadata.SEQUENCE_NAME));
        repository.save(metadata);
    }

    public FileMetadata getMetadataFromFile(File file) throws IOException{
        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalFilename(file.getName());
        metadata.setFilename(file.getName());
        metadata.setMimeType(Files.probeContentType(file.toPath()));
        metadata.setSize(Files.size(file.toPath()));

        Path root = Paths.get(rootDirectory);
        Path base = Paths.get(file.getPath());
        Path relativePath = root.relativize(base);
        metadata.setDirectory(relativePath.toString());
        return metadata;
    }

    public void update(FileMetadata metadata) {
        getById(metadata.getId());
        repository.save(metadata);
    }

    public void delete(Long id) {
        repository.delete(getById(id));
    }

    private FileMetadata getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("File with id=%d not found", id)));
    }
}
