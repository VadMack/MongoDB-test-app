package com.vadmack.mongodbtest.repository;

import com.vadmack.mongodbtest.entity.FileMetadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, Long> {
    List<FileMetadata> findAllByOriginalFilenameRegex(String regex, Pageable pageable);
    List<FileMetadata> findAllByOriginalFilenameRegex(String regex, Sort sort);
    Optional<FileMetadata> findByDirectoryAndOriginalFilename(String directory, String originalFilename);
}
