package com.vadmack.mongodbtest.dto;

import com.vadmack.mongodbtest.entity.FileStatus;
import lombok.Data;

@Data
public class FileMetadataDto {
    private Long id;
    private String originalFilename;
    private Long size;
    private String mimeType;
    private String filename;
    private String directory;
    private FileStatus status;
}
