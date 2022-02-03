package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.dto.FileMetadataDto;
import com.vadmack.mongodbtest.entity.FileMetadata;
import com.vadmack.mongodbtest.entity.FileStatus;
import com.vadmack.mongodbtest.exception.NotFoundException;
import com.vadmack.mongodbtest.exception.ServerSideException;
import com.vadmack.mongodbtest.exception.ValidationException;
import com.vadmack.mongodbtest.repository.FileMetadataRepository;
import com.vadmack.mongodbtest.util.PageableService;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import com.vadmack.mongodbtest.util.SortService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileMetadataService {

    @Value("${upload.path}")
    private String rootDirectory;

    private final FileMetadataRepository repository;
    private final PageableService pageableService;
    private final SortService sortService;
    private final SequenceGeneratorService sequenceGeneratorService;

    private final ModelMapper modelMapper = new ModelMapper();

    public List<FileMetadataDto> findList(
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
            return repository.findAllByOriginalFilenameRegex(
                    filenamePart,
                    PageRequest.of(pageNumber, pageSize, sort))
                    .stream().map(this::entityToDto)
                    .collect(Collectors.toList());
        } else {
            return repository.findAllByOriginalFilenameRegex(filenamePart, sort)
                    .stream().map(this::entityToDto)
                    .collect(Collectors.toList());
        }
    }

    public FileMetadata findById(Long id) {
        return getById(id);
    }

    public void checkAlreadyExists(String directory, String originalFileName) {
        if (repository.findByDirectoryAndOriginalFilename(directory, originalFileName).isPresent()) {
            throw new ValidationException("File '" + directory + "/" + originalFileName +"' already exists");
        }
    }

    public void checkAlreadyExists(String filePathSuffix) {
        Path suffix = Paths.get(filePathSuffix);
        Path root = Paths.get(rootDirectory);
        Path base = Paths.get(rootDirectory).resolve(suffix).getParent();
        Path relativePath = root.relativize(base);

        if (repository.findByDirectoryAndOriginalFilename(relativePath.toString(),
                suffix.getFileName().toString())
                .isPresent()) {
            throw new ValidationException("File '" + filePathSuffix + "' already exists");
        }
    }

    public FileMetadata create(File file, FileStatus status) {
        FileMetadata metadata = buildMetadataFromFile(file, status);
        metadata.setId(sequenceGeneratorService.generateSequence(FileMetadata.SEQUENCE_NAME));
        return repository.save(metadata);
    }

    public FileMetadata buildMetadataFromFile(File file, FileStatus status) {
        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalFilename(file.getName());
        metadata.setFilename(file.getName());
        metadata.setStatus(status);

        if (!(status == FileStatus.NOT_READY_FOR_USE)) {
            try {
                metadata.setMimeType(Files.probeContentType(file.toPath()));
                metadata.setSize(Files.size(file.toPath()));
            } catch (IOException ex) {
                throw new ServerSideException("Could not save file: " + ex.getMessage(), ex);
            }
        }

        Path root = Paths.get(rootDirectory);
        Path base = Paths.get(file.getParent());
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

    private FileMetadataDto entityToDto(FileMetadata metadata) {
        return modelMapper.map(metadata, FileMetadataDto.class);
    }
}
