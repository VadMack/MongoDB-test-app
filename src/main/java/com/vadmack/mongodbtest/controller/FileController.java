package com.vadmack.mongodbtest.controller;

import com.vadmack.mongodbtest.dto.FileMetadataDto;
import com.vadmack.mongodbtest.entity.FileMetadata;
import com.vadmack.mongodbtest.service.FileMetadataService;
import com.vadmack.mongodbtest.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileMetadataService metadataService;

    @GetMapping()
    @ResponseBody
    public ResponseEntity<List<FileMetadataDto>> getList(
            @RequestParam(value = "filterFileame", required = false) String filename,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(value = "sort", defaultValue = "id:0") String[] sortBy
    ) {
        return ResponseEntity.ok(metadataService.findList(filename, pageNumber, pageSize, sortBy));
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {
        Resource file = fileService.load(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("link") String url) {
        fileService.saveFromLink(url);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/copy")
    public ResponseEntity<?> copyFile(@RequestParam Long id,
                                      @RequestParam("path") String relativePath) {
        fileService.copy(id, relativePath);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/move")
    public ResponseEntity<?> moveFile(@RequestParam Long id,
                                      @RequestParam("path") String relativePath) {
        fileService.move(id, relativePath);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable(value = "id") Long id) {
        fileService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
