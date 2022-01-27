package com.vadmack.mongodbtest.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("fileMetadata")
public class FileMetadata {

    @Transient
    public static final String SEQUENCE_NAME = "file_sequence";

    @Id
    private Long id;
    private String originalFilename;
    private Long size;
    private String mimeType;
    private String filename;

    // relative path from root directory including filename
    @Indexed(unique = true)
    private String directory;
}
